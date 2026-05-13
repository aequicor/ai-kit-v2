package io.aequicor.aikit.engine.impl

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
import io.aequicor.aikit.io.fs.FsProjectManifestSource
import kotlinx.io.files.Path

/**
 * Full generate pipeline: manifest → bundle → render → write.
 *
 * For each application/target pair in the manifest, the generator:
 * 1. Loads and parses the bundle.
 * 2. Resolves and validates input values.
 * 3. Renders each template file.
 * 4. Writes the result to the correct output path inside the application directory.
 */
internal class DefaultBundleGenerator(
    private val format: AiKitFormat,
    private val fileWriter: FileWriter,
) : BundleGenerator {

    override fun generate(manifestPath: String): Result<Unit> = runCatching {
        val manifestSource = FsProjectManifestSource(Path(manifestPath))

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
                    val bundleManifest = format.parseBundleManifest(src)
                        .getOrElse {
                            throw EngineError.BundleLoadError(
                                rawTarget.source,
                                "cannot parse bundle '${rawTarget.source}': ${it.message}",
                                it,
                            )
                        }

                    val target = bundleManifest.targets.firstOrNull { it.matchesFolder(targetName) }
                        ?: throw EngineError.BundleLoadError(
                            rawTarget.bundle,
                            "bundle '${rawTarget.bundle}' does not contain a '$targetName' target",
                        )

                    val inputs = InputResolver.resolve(bundleManifest.inputs, rawTarget.inputs)
                        .getOrThrow()

                    renderAndWrite(target, targetName, app.path, inputs)
                }
            }
        }
    }

    private fun renderAndWrite(
        target: Target,
        targetFolder: String,
        appPath: String,
        inputs: Map<String, AkelValue>,
    ) {
        val outputRoot = outputRoot(target, appPath)
        val allTemplates = collectTemplates(target)

        for (template in allTemplates) {
            val relativePath = template.path.removePrefix("$targetFolder/")
            val outputPath = "$outputRoot/$relativePath"
            val renderedBytes = renderTemplate(template, inputs)

            fileWriter.write(outputPath, renderedBytes)
                .getOrElse { throw EngineError.WriteError(outputPath, "cannot write '$outputPath': ${it.message}", it) }
        }
    }

    private fun renderTemplate(
        template: Template,
        inputs: Map<String, AkelValue>,
    ): ByteArray {
        val text = template.bytes.decodeToString()
        val parts = format.parseTemplateBody(text, template.path)
            .getOrElse { throw EngineError.RenderError(template.path, "cannot parse template '${template.path}': ${it.message}", it) }

        return TemplateRenderer.render(parts, inputs, template.path)
            .getOrElse { throw it as? EngineError ?: EngineError.RenderError(template.path, it.message ?: "render error", it) }
            .encodeToByteArray()
    }

    private fun collectTemplates(target: Target): List<Template> = when (target) {
        is ClaudeCode -> target.commands + target.skills + target.subagents
        is OpenCode -> target.commands + target.skills + target.plugins
        is QwenCode -> target.commands + target.skills + target.subagents
    }

    private fun outputRoot(target: Target, appPath: String): String = when (target) {
        is ClaudeCode -> "$appPath/.claude"
        is OpenCode -> "$appPath/.opencode"
        is QwenCode -> "$appPath/.qwen"
    }
}
