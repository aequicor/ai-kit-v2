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
import io.aequicor.aikit.engine.error.EngineError
import io.aequicor.aikit.engine.pipeline.InputResolver
import io.aequicor.aikit.engine.template.TemplateRenderer
import io.aequicor.aikit.engine.write.FileWriter
import io.aequicor.aikit.format.AiKitFormat
import io.aequicor.aikit.format.ParsedBundle
import io.aequicor.aikit.format.template.TemplateSource
import io.aequicor.aikit.io.fs.FsProjectManifestSource
import kotlinx.io.files.Path

/**
 * Full generate pipeline: manifest → bundle → render → write.
 *
 * For each application/target pair in the manifest, the generator:
 * 1. Loads and parses the bundle.
 * 2. Resolves and validates input values.
 * 3. Renders each template file (evaluates conditions, substitutes inputs).
 * 4. Writes the result to the correct output path inside the application directory.
 */
internal class DefaultBundleGenerator(
    private val format: AiKitFormat,
    private val fileWriter: FileWriter,
) : BundleGenerator {

    override fun generate(manifestPath: String): Result<Unit> = runCatching {
        val manifestSource = FsProjectManifestSource(Path(manifestPath))
        val projectRoot = manifestSource.projectRoot.toString()
        val cwdBasename = manifestSource.projectRoot.name

        val rawManifest = manifestSource.openManifest()
            .mapCatching { format.parseProjectManifest(it).getOrThrow() }
            .getOrElse { throw EngineError.ManifestLoadError("cannot load manifest: ${it.message}", it) }

        for (app in rawManifest.applications) {
            for ((targetName, rawTarget) in app.targets) {
                val bundleSource = manifestSource.resolveBundleSource(rawTarget.source)
                    .getOrElse {
                        throw EngineError.BundleLoadError(
                            rawTarget.source,
                            "cannot open bundle '${rawTarget.source}': ${it.message}",
                            it,
                        )
                    }

                bundleSource.use { src ->
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

                    renderAndWrite(target, parsedBundle, app.path, projectRoot, inputs)
                }
            }
        }
    }

    private fun renderAndWrite(
        target: Target,
        parsedBundle: ParsedBundle,
        appPath: String,
        projectRoot: String,
        inputs: Map<String, AkelValue>,
    ) {
        val outputRoot = outputRoot(target, appPath, projectRoot)
        val bundleFolder = bundleFolder(target)
        val sources = parsedBundle.targetSources[bundleFolder] ?: emptyList()
        val context = AkelContext.of(inputs.mapKeys { "bundle.input.${it.key}" })

        for (template in render(sources, inputs, context)) {
            val relativePath = template.path.removePrefix("$bundleFolder/")
            val outputPath = "$outputRoot/$relativePath"
            fileWriter.write(outputPath, template.bytes)
                .getOrElse { throw EngineError.WriteError(outputPath, "cannot write '$outputPath': ${it.message}", it) }
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

    private fun outputRoot(target: Target, appPath: String, projectRoot: String): String {
        val agentFolder = when (target) {
            is ClaudeCode -> ".claude"
            is OpenCode -> ".opencode"
            is QwenCode -> ".qwen"
        }
        return Path(projectRoot, appPath, agentFolder).toString()
    }

    private fun bundleFolder(target: Target): String = when (target) {
        is ClaudeCode -> "claude-code"
        is OpenCode -> "opencode"
        is QwenCode -> "qwen-code"
    }
}
