package io.aequicor.aikit.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import io.aequicor.aikit.engine.api.BundleSchemaProvider
import io.aequicor.aikit.engine.api.EmbeddedBundleCatalog
import io.aequicor.aikit.engine.api.SchemaProvider

/**
 * Parent command for JSON Schema output.
 *
 * Subcommands:
 * - `manifest` — prints the JSON Schema for `.aikit/manifest.json`
 * - `bundle` — prints the JSON Schema for a specific bundle's `inputs` block,
 *   or lists embedded bundles when invoked with `--list`
 */
class SchemaCommand(
    provider: SchemaProvider,
    bundleSchemaProvider: BundleSchemaProvider,
    embeddedCatalog: EmbeddedBundleCatalog,
) : CliktCommand(
    name = "schema",
    help = "Print JSON Schema for .aikit/manifest.json or for a bundle's inputs",
) {
    init {
        subcommands(
            ManifestSchemaSubcommand(provider),
            BundleSchemaSubcommand(bundleSchemaProvider, embeddedCatalog),
        )
    }

    override fun run() = Unit
}
