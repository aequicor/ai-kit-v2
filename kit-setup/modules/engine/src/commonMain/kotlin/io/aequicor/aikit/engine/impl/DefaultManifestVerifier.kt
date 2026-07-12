package io.aequicor.aikit.engine.impl

import io.aequicor.aikit.core.domain.targets.ClaudeCode
import io.aequicor.aikit.core.domain.targets.Codex
import io.aequicor.aikit.core.domain.targets.OpenCode
import io.aequicor.aikit.core.domain.targets.QwenCode
import io.aequicor.aikit.core.domain.targets.Target
import io.aequicor.aikit.engine.api.ManifestVerifier
import io.aequicor.aikit.engine.error.EngineError
import io.aequicor.aikit.engine.pipeline.InputResolver
import io.aequicor.aikit.format.AiKitFormat
import io.aequicor.aikit.io.SourceRefs
import io.aequicor.aikit.io.fs.FsProjectManifestSource
import kotlinx.io.files.Path

/**
 * Validates a project manifest and all referenced bundles without writing any files.
 *
 * Runs the load + resolve + validate stages of the pipeline but skips render and write.
 */
internal class DefaultManifestVerifier(
    private val format: AiKitFormat,
    private val currentVersion: String,
) : ManifestVerifier {

    override fun verify(manifestPath: String): Result<Unit> = runCatching {
        val manifestSource = FsProjectManifestSource(Path(manifestPath))

        val rawManifest = manifestSource.openManifest()
            .mapCatching { format.parseProjectManifest(it).getOrThrow() }
            .getOrElse { throw EngineError.ManifestLoadError("cannot load manifest: ${it.message}", it) }

        for (app in rawManifest.applications) {
            for ((targetName, rawTarget) in app.targets) {
                val effectiveSource = SourceRefs.effective(rawTarget.source, rawTarget.bundle)
                    .getOrElse {
                        throw EngineError.BundleLoadError(
                            rawTarget.source,
                            "invalid bundle source '${rawTarget.source}': ${it.message}",
                            it,
                        )
                    }
                val bundleSource = manifestSource.resolveBundleSource(effectiveSource)
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
                    requireCompatible(parsedBundle.manifest, currentVersion, actualRef)
                    if (actualRef != rawTarget.bundle) {
                        throw EngineError.BundleLoadError(
                            rawTarget.bundle,
                            "bundle version mismatch: expected '${rawTarget.bundle}', found '$actualRef'",
                        )
                    }

                    val targetFound = parsedBundle.manifest.targets.any { it.matchesFolder(targetName) }
                    if (!targetFound) {
                        throw EngineError.BundleLoadError(
                            rawTarget.bundle,
                            "bundle '${rawTarget.bundle}' does not contain a '$targetName' target",
                        )
                    }

                    InputResolver.resolve(parsedBundle.manifest.inputs, rawTarget.inputs).getOrThrow()
                }
            }
        }
    }
}

internal fun Target.matchesFolder(folder: String): Boolean = when (this) {
    is ClaudeCode -> folder == "claude"
    is OpenCode -> folder == "opencode"
    is QwenCode -> folder == "qwen"
    is Codex -> folder == "codex"
}
