package io.aequicor.aikit.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import io.aequicor.aikit.engine.api.BundleSchemaProvider
import io.aequicor.aikit.engine.api.BundleCatalog
import io.aequicor.aikit.engine.api.SchemaProvider

/**
 * Parent command for JSON Schema output.
 *
 * Subcommands:
 * - `manifest` — prints the JSON Schema for `.aikit/manifest.json`
 * - `bundle` — prints the JSON Schema for a specific bundle's `inputs` block,
 *   or lists the official catalog when invoked with `--list`
 */
class SchemaCommand(
    provider: SchemaProvider,
    bundleSchemaProvider: BundleSchemaProvider,
    bundleCatalog: BundleCatalog,
) : CliktCommand(
    name = "schema",
    help = "Print JSON Schema for .aikit/manifest.json or for a bundle's inputs",
) {
    init {
        subcommands(
            ManifestSchemaSubcommand(provider),
            BundleSchemaSubcommand(bundleSchemaProvider, bundleCatalog),
        )
    }

    override fun run() = Unit
}
