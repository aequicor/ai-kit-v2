package io.aequicor.aikit.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import io.aequicor.aikit.engine.api.SchemaProvider

/** `schema manifest` — prints the JSON Schema for `.aikit/manifest.json`. */
class ManifestSchemaSubcommand(
    private val provider: SchemaProvider,
) : CliktCommand(
    name = "manifest",
    help = "Print the JSON Schema for .aikit/manifest.json",
) {
    override fun run() {
        echo(provider.schema())
    }
}
