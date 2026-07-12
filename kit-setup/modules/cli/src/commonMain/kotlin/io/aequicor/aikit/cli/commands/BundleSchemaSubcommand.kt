package io.aequicor.aikit.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import io.aequicor.aikit.engine.api.BundleSchemaProvider
import io.aequicor.aikit.engine.api.BundleCatalog
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * `schema bundle <ref>` — prints the JSON Schema for the `inputs` of the given bundle.
 * `schema bundle --list` — lists compatible versions from the official catalog.
 */
class BundleSchemaSubcommand(
    private val provider: BundleSchemaProvider,
    private val bundleCatalog: BundleCatalog,
) : CliktCommand(
    name = "bundle",
    help = "Print the JSON Schema for a bundle's inputs, or list the official catalog",
) {
    private val ref by argument(
        name = "REF",
        help = "Bundle reference: directory path, ZIP, or remote:<owner>/<repo>/<path>[@<branch>]",
    ).optional()

    private val baseDir by option(
        "--base-dir",
        help = "Base directory used to resolve relative paths in REF (default: current directory)",
    ).default(".")

    private val listEmbedded by option(
        "--list",
        help = "List compatible bundles from the official catalog",
    ).flag(default = false)

    private val includeAll by option(
        "--all",
        help = "Include bundles incompatible with this kit-setup version",
    ).flag(default = false)

    private val jsonOutput by option(
        "--json",
        help = "Print the catalog as machine-readable JSON",
    ).flag(default = false)

    override fun run() {
        when {
            listEmbedded && ref != null ->
                throw CliktError("--list cannot be combined with a bundle reference")
            (includeAll || jsonOutput) && !listEmbedded ->
                throw CliktError("--all and --json require --list")
            listEmbedded -> printCatalog()
            ref != null -> printSchema(ref!!)
            else -> throw CliktError("Specify a bundle REF or use --list to see the official catalog")
        }
    }

    private fun printSchema(reference: String) {
        provider.schemaFor(reference, baseDir)
            .onSuccess { echo(it) }
            .onFailure { throw CliktError(it.message ?: "Failed to load bundle schema", statusCode = 1) }
    }

    private fun printCatalog() {
        val result = bundleCatalog.list(baseDir, includeAll)
            .getOrElse { throw CliktError(it.message ?: "Failed to list bundle catalog", statusCode = 1) }

        if (jsonOutput) {
            echo(Json { prettyPrint = true }.encodeToString(result))
            return
        }

        if (result.bundles.isEmpty()) {
            echo("no compatible bundles available")
            return
        }
        if (result.stale) echo("warning: using cached bundle catalog (network unavailable)", err = true)
        for (info in result.bundles) {
            val status = if (info.compatible) "compatible" else "incompatible: ${info.incompatibility}"
            echo("${info.name}@${info.version}  $status  ${info.description}  ${info.source}")
        }
    }
}
