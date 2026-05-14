package io.aequicor.aikit.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.arguments.argument
import io.aequicor.aikit.engine.api.BundleGenerator
import io.aequicor.aikit.engine.error.EngineError

/** Resolves bundles from [manifest] and writes agent configuration files. */
class GenerateCommand(private val generator: BundleGenerator) : CliktCommand(
    name = "generate",
    help = "Generate agent configuration from a manifest file",
) {
    private val manifest by argument(
        name = "MANIFEST",
        help = "Path to the manifest file (e.g. .aikit/manifest.yaml)",
    )

    override fun run() {
        generator.generate(manifest).onFailure { error ->
            val msg = when (error) {
                is EngineError.ManifestLoadError ->
                    "Cannot load manifest '$manifest': ${error.cause?.message ?: error.message}"
                is EngineError.BundleLoadError ->
                    "Bundle '${error.bundleRef}': ${error.message}"
                is EngineError.InputValidationError ->
                    "Invalid input '${error.inputId}': ${error.message}"
                is EngineError.RenderError ->
                    "Render error in '${error.templatePath}': ${error.message}"
                is EngineError.WriteError ->
                    "Cannot write '${error.outputPath}': ${error.cause?.message ?: error.message}"
                else -> error.message ?: "Generation failed"
            }
            throw CliktError(msg, statusCode = 1)
        }
        echo("Done")
    }
}
