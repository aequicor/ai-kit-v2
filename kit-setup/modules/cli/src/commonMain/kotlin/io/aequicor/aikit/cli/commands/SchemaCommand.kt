package io.aequicor.aikit.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import io.aequicor.aikit.engine.api.BundleSchemaProvider
import io.aequicor.aikit.engine.api.EmbeddedBundleCatalog
import io.aequicor.aikit.engine.api.SchemaProvider

/**
 * Prints the JSON Schema for `.aikit/manifest.json` to stdout when invoked without a subcommand.
 *
 * The nested `bundle` subcommand prints the schema for a specific bundle's `inputs` block, or
 * lists embedded bundles when invoked with `--list`.
 */
class SchemaCommand(
    private val provider: SchemaProvider,
    bundleSchemaProvider: BundleSchemaProvider,
    embeddedCatalog: EmbeddedBundleCatalog,
) : CliktCommand(
    name = "schema",
    help = "Print the JSON schema for .aikit/manifest.yaml",
    invokeWithoutSubcommand = true,
) {
    init {
        subcommands(BundleSchemaSubcommand(bundleSchemaProvider, embeddedCatalog))
    }

    override fun run() {
        if (currentContext.invokedSubcommand == null) {
            echo(provider.schema())
        }
    }
}
