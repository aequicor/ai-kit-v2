package io.aequicor.aikit.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.arguments.argument
import io.aequicor.aikit.engine.api.BundleGenerator

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
            throw CliktError(error.message ?: "Generation failed", statusCode = 1)
        }
        echo("Done")
    }
}
