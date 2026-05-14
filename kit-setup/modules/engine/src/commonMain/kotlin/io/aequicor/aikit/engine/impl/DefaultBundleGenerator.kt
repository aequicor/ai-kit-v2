package io.aequicor.aikit.engine.impl

import io.aequicor.aikit.akel.Akel
import io.aequicor.aikit.akel.AkelContext
import io.aequicor.aikit.akel.AkelValue
import io.aequicor.aikit.core.domain.targets.ClaudeCode
import io.aequicor.aikit.core.domain.targets.OpenCode
import io.aequicor.aikit.core.domain.targets.QwenCode
import io.aequicor.aikit.core.domain.targets.Target
import io.aequicor.aikit.core.domain.template.Template
import io.aequicor.aikit.engine.api.BundleGenerator
import io.aequicor.aikit.engine.api.GenerateOptions
import io.aequicor.aikit.engine.api.GenerateReport
import io.aequicor.aikit.engine.error.EngineError
import io.aequicor.aikit.engine.lock.HashProvider
import io.aequicor.aikit.engine.lock.LockApplication
import io.aequicor.aikit.engine.lock.LockFile
import io.aequicor.aikit.engine.lock.LockFileEntry
import io.aequicor.aikit.engine.lock.LockStore
import io.aequicor.aikit.engine.lock.LockTarget
import io.aequicor.aikit.engine.pipeline.InputResolver
import io.aequicor.aikit.engine.template.TemplateRenderer
import io.aequicor.aikit.engine.write.FileWriter
import io.aequicor.aikit.format.AiKitFormat
import io.aequicor.aikit.format.ParsedBundle
import io.aequicor.aikit.format.template.TemplateSource
import io.aequicor.aikit.io.fs.FsProjectManifestSource
import kotlinx.io.files.Path

private const val LOCK_VERSION = "1"
private const val LOCK_RELATIVE_PATH = ".aikit/manifest.lock.json"

/**
 * Plan-then-apply generator.
 *
 * Step 1 (plan) — render every template into memory and hash the result.
 * Step 2 (diff) — load the previous `manifest.lock.json` (if any) and classify each path:
 *   created / updated / unchanged / drifted-skipped / orphan / drifted-orphan.
 * Step 3 (apply) — perform writes and deletes (skipped in dry-run); persist a fresh lock.
 */
internal class DefaultBundleGenerator(
    private val format: AiKitFormat,
    private val fileWriter: FileWriter,
    private val lockStore: LockStore,
    private val hashProvider: HashProvider,
    private val aikitVersion: String,
    private val now: () -> String = { "" },
) : BundleGenerator {

    @Suppress("LongMethod", "CyclomaticComplexMethod")
    override fun generate(manifestPath: String, options: GenerateOptions): Result<GenerateReport> =
        runCatching {
            val manifestSource = FsProjectManifestSource(Path(manifestPath))
            val projectRoot = manifestSource.projectRoot.toString()
            val cwdBasename = manifestSource.projectRoot.name

            val rawManifest = manifestSource.openManifest()
                .mapCatching { format.parseProjectManifest(it).getOrThrow() }
                .getOrElse {
                    throw EngineError.ManifestLoadError("cannot load manifest: ${it.message}", it)
                }

            val plan = mutableListOf<PlannedTarget>()
            for (app in rawManifest.applications) {
                for ((targetName, rawTarget) in app.targets) {
                    plan += planTarget(
                        appId = app.id,
                        appPath = app.path,
                        targetName = targetName,
                        rawTarget = rawTarget,
                        manifestSource = manifestSource,
                        cwdBasename = cwdBasename,
                    )
                }
            }

            val oldLock = lockStore.read(projectRoot).getOrNull()
            val plannedRelPaths = plan.flatMap { it.files.map(PlannedFile::relPath) }.toSet()

            val created = mutableListOf<String>()
            val updated = mutableListOf<String>()
            val unchanged = mutableListOf<String>()
            val skippedDrift = mutableListOf<String>()
            val deletedOrphans = mutableListOf<String>()
            val keptDriftedOrphans = mutableListOf<String>()

            val oldByRelPath = oldLock?.applications
                ?.flatMap { it.targets.values.flatMap(LockTarget::files) }
                ?.associateBy(LockFileEntry::path)
                .orEmpty()

            for (planned in plan) {
                for (file in planned.files) {
                    val absPath = absolute(projectRoot, file.relPath)
                    val onDiskHash = onDiskHashOrNull(absPath)
                    val recordedHash = oldByRelPath[file.relPath]?.hash

                    when {
                        onDiskHash == null -> {
                            if (!options.dryRun) writeFile(absPath, file.bytes)
                            created += file.relPath
                        }
                        recordedHash != null && onDiskHash != recordedHash && !options.force -> {
                            skippedDrift += file.relPath
                        }
                        onDiskHash == file.hash -> {
                            unchanged += file.relPath
                        }
                        else -> {
                            if (!options.dryRun) writeFile(absPath, file.bytes)
                            updated += file.relPath
                        }
                    }
                }
            }

            val orphanCandidates = oldByRelPath.filterKeys { it !in plannedRelPaths }
            for ((relPath, oldEntry) in orphanCandidates) {
                val absPath = absolute(projectRoot, relPath)
                if (fileWriter.exists(absPath)) {
                    val onDisk = onDiskHashOrNull(absPath)
                    if (onDisk == oldEntry.hash || options.force) {
                        if (!options.dryRun) {
                            fileWriter.delete(absPath).getOrElse {
                                throw EngineError.WriteError(absPath, "cannot delete '$absPath': ${it.message}", it)
                            }
                            pruneEmptyParents(projectRoot, relPath)
                        }
                        deletedOrphans += relPath
                    } else {
                        keptDriftedOrphans += relPath
                    }
                }
            }

            val newLock = buildLock(rawManifest, plan)
            if (!options.dryRun) {
                lockStore.write(projectRoot, newLock).getOrElse {
                    throw EngineError.WriteError(
                        lockStore.pathOf(projectRoot),
                        "cannot write lock file: ${it.message}",
                        it,
                    )
                }
            }

            GenerateReport(
                created = created,
                updated = updated,
                unchanged = unchanged,
                skippedDrift = skippedDrift,
                deletedOrphans = deletedOrphans,
                keptDriftedOrphans = keptDriftedOrphans,
                dryRun = options.dryRun,
                lockPath = LOCK_RELATIVE_PATH,
            )
        }

    @Suppress("LongParameterList", "ThrowsCount")
    private fun planTarget(
        appId: String,
        appPath: String,
        targetName: String,
        rawTarget: io.aequicor.aikit.format.projectManifest.RawBundleTarget,
        manifestSource: FsProjectManifestSource,
        cwdBasename: String,
    ): PlannedTarget {
        val effectiveSource = resolveSource(rawTarget.source, rawTarget.bundle)
        val bundleSource = manifestSource.resolveBundleSource(effectiveSource)
            .getOrElse {
                throw EngineError.BundleLoadError(
                    rawTarget.source,
                    "cannot open bundle '${rawTarget.source}': ${it.message}",
                    it,
                )
            }

        return bundleSource.use { src ->
            val parsedBundle = format.parseBundleManifest(src)
                .getOrElse {
                    throw EngineError.BundleLoadError(
                        rawTarget.source,
                        "cannot parse bundle '${rawTarget.source}': ${it.message}",
                        it,
                    )
                }

            val actualRef = "${parsedBundle.manifest.name}@${parsedBundle.manifest.version}"
            if (actualRef != rawTarget.bundle) {
                throw EngineError.BundleLoadError(
                    rawTarget.bundle,
                    "bundle version mismatch: expected '${rawTarget.bundle}', found '$actualRef'",
                )
            }

            val target = parsedBundle.manifest.targets.firstOrNull { it.matchesFolder(targetName) }
                ?: throw EngineError.BundleLoadError(
                    rawTarget.bundle,
                    "bundle '${rawTarget.bundle}' does not contain a '$targetName' target",
                )

            val inputs = InputResolver.resolve(parsedBundle.manifest.inputs, rawTarget.inputs, cwdBasename)
                .getOrThrow()

            val bundleFolder = bundleFolder(target)
            val sources = parsedBundle.targetSources[bundleFolder] ?: emptyList()
            val context = AkelContext.of(inputs.mapKeys { "bundle.input.${it.key}" })

            val rendered = render(sources, inputs, context)
            val outputRel = outputRootRelative(target, appPath)
            val files = rendered.map { template ->
                val templateRel = template.path.removePrefix("$bundleFolder/")
                val relPath = "$outputRel/$templateRel"
                PlannedFile(
                    relPath = relPath,
                    bytes = template.bytes,
                    hash = hashProvider.hash(template.bytes),
                )
            }

            PlannedTarget(
                appId = appId,
                appPath = appPath,
                targetName = targetName,
                bundle = actualRef,
                source = rawTarget.source,
                files = files,
            )
        }
    }

    private fun render(
        sources: List<TemplateSource>,
        inputs: Map<String, AkelValue>,
        context: AkelContext,
    ): List<Template> = sources.mapNotNull { source ->
        val condition = source.condition
        if (condition != null) {
            val include = Akel.evaluate(condition, context).getOrElse { false }
            if (!include) return@mapNotNull null
        }
        val text = source.bytes.decodeToString()
        val parts = format.parseTemplateBody(text, source.path)
            .getOrElse { throw EngineError.RenderError(source.path, "cannot parse template '${source.path}': ${it.message}", it) }
        val renderedBytes = TemplateRenderer.render(parts, inputs, source.path)
            .getOrElse { throw it as? EngineError ?: EngineError.RenderError(source.path, it.message ?: "render error", it) }
            .encodeToByteArray()
        Template(path = source.path, bytes = renderedBytes)
    }

    private fun writeFile(absPath: String, bytes: ByteArray) {
        fileWriter.write(absPath, bytes).getOrElse {
            throw EngineError.WriteError(absPath, "cannot write '$absPath': ${it.message}", it)
        }
    }

    private fun onDiskHashOrNull(absPath: String): String? =
        if (!fileWriter.exists(absPath)) null
        else fileWriter.read(absPath).map(hashProvider::hash).getOrNull()

    private fun pruneEmptyParents(projectRoot: String, relPath: String) {
        var current = relPath.substringBeforeLast('/', missingDelimiterValue = "")
        while (current.isNotEmpty()) {
            val abs = absolute(projectRoot, current)
            fileWriter.deleteDirectoryIfEmpty(abs)
            current = current.substringBeforeLast('/', missingDelimiterValue = "")
        }
    }

    private fun buildLock(
        rawManifest: io.aequicor.aikit.format.projectManifest.RawProjectManifest,
        plan: List<PlannedTarget>,
    ): LockFile {
        val byApp = plan.groupBy { it.appId }
        val applications = rawManifest.applications.map { app ->
            val targets = byApp[app.id].orEmpty().associate { pt ->
                pt.targetName to LockTarget(
                    bundle = pt.bundle,
                    source = pt.source,
                    files = pt.files.map { LockFileEntry(path = it.relPath, hash = it.hash) },
                )
            }
            LockApplication(id = app.id, path = app.path, targets = targets)
        }
        return LockFile(
            lockVersion = LOCK_VERSION,
            aikitVersion = aikitVersion,
            generatedAt = now(),
            applications = applications,
        )
    }

    private fun outputRootRelative(target: Target, appPath: String): String {
        val agentFolder = agentFolder(target)
        val appNorm = appPath.trim().removePrefix("./").trim('/').replace('\\', '/')
        return if (appNorm.isEmpty() || appNorm == ".") agentFolder else "$appNorm/$agentFolder"
    }

    private fun absolute(projectRoot: String, relPath: String): String =
        Path(projectRoot, relPath).toString()

    private fun agentFolder(target: Target): String = when (target) {
        is ClaudeCode -> ".claude"
        is OpenCode -> ".opencode"
        is QwenCode -> ".qwen"
    }

    private fun bundleFolder(target: Target): String = when (target) {
        is ClaudeCode -> "claude-code"
        is OpenCode -> "opencode"
        is QwenCode -> "qwen-code"
    }

    private fun resolveSource(source: String, bundle: String): String =
        if (source == "internal") "embedded:$bundle" else source

    private data class PlannedFile(val relPath: String, val bytes: ByteArray, val hash: String)

    private data class PlannedTarget(
        val appId: String,
        val appPath: String,
        val targetName: String,
        val bundle: String,
        val source: String,
        val files: List<PlannedFile>,
    )
}
