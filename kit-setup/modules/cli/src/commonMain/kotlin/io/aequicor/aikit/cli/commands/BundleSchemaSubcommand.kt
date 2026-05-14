package io.aequicor.aikit.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import io.aequicor.aikit.engine.api.BundleSchemaProvider
import io.aequicor.aikit.engine.api.EmbeddedBundleCatalog

/**
 * `schema bundle <ref>` — prints the JSON Schema for the `inputs` of the given bundle.
 * `schema bundle --list` — lists embedded bundles compiled into the CLI binary.
 */
class BundleSchemaSubcommand(
    private val provider: BundleSchemaProvider,
    private val embeddedCatalog: EmbeddedBundleCatalog,
) : CliktCommand(
    name = "bundle",
    help = "Print the JSON Schema for a bundle's inputs, or list embedded bundles",
) {
    private val ref by argument(
        name = "REF",
        help = "Bundle reference: directory path, '<file>.zip', 'zip:<path>', or 'embedded:<name>[@<version>]'",
    ).optional()

    private val baseDir by option(
        "--base-dir",
        help = "Base directory used to resolve relative paths in REF (default: current directory)",
    ).default(".")

    private val listEmbedded by option(
        "--list",
        help = "List bundles compiled into the CLI binary instead of printing a schema",
    ).flag(default = false)

    override fun run() {
        when {
            listEmbedded && ref != null ->
                throw CliktError("--list cannot be combined with a bundle reference")
            listEmbedded -> printEmbeddedList()
            ref != null -> printSchema(ref!!)
            else -> throw CliktError("Specify a bundle REF or use --list to see embedded bundles")
        }
    }

    private fun printSchema(reference: String) {
        provider.schemaFor(reference, baseDir)
            .onSuccess { echo(it) }
            .onFailure { throw CliktError(it.message ?: "Failed to load bundle schema", statusCode = 1) }
    }

    private fun printEmbeddedList() {
        val infos = embeddedCatalog.list()
            .getOrElse { throw CliktError(it.message ?: "Failed to list embedded bundles", statusCode = 1) }

        if (infos.isEmpty()) {
            echo("no embedded bundles available")
            return
        }
        val sorted = infos.sortedBy { it.name }
        for (info in sorted) {
            val ref = if (info.version != null) "embedded:${info.name}@${info.version}"
                      else "embedded:${info.name}"
            val parts = listOfNotNull(ref, info.description)
            echo(parts.joinToString("  "))
        }
    }
}
