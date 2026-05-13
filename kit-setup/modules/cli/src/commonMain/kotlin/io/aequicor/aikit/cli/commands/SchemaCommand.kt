package io.aequicor.aikit.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import io.aequicor.aikit.core.api.SchemaProvider

/** Prints the JSON schema for `.aikit/manifest.yaml` to stdout. */
class SchemaCommand(private val provider: SchemaProvider) : CliktCommand(
    name = "schema",
    help = "Print the JSON schema for .aikit/manifest.yaml",
) {
    override fun run() {
        echo(provider.schema())
    }
}
