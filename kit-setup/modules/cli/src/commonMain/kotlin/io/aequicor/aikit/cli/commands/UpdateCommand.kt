package io.aequicor.aikit.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import io.aequicor.aikit.cli.util.resolveManifestOrFail
import io.aequicor.aikit.engine.api.BundleGenerator
import io.aequicor.aikit.engine.api.GenerateOptions
import io.aequicor.aikit.engine.api.GenerateReport
import io.aequicor.aikit.engine.api.ManifestResolver
import io.aequicor.aikit.engine.error.EngineError

/**
 * Apply changes to a previously installed AI-Kit project.
 *
 * Without a subcommand: regenerate from the active manifest while reporting the diff against the
 * existing lock — used after bumping a bundle version, changing inputs, or adding/removing a target.
 * With `kit-setup update self`: print the current CLI version and instructions for upgrading.
 */
class UpdateCommand(
    private val generator: BundleGenerator,
    private val manifestResolver: ManifestResolver,
) : CliktCommand(
    name = "update",
    help = "Apply manifest changes (bundle version, inputs, targets) or update the CLI itself",
    invokeWithoutSubcommand = true,
) {
    private val manifestArg by argument(
        name = "MANIFEST",
        help = "Path to the manifest file (default: resolved from .aikit/local.properties or .aikit/manifest.json)",
    ).optional()

    private val manifestOpt by option(
        "--manifest",
        help = "Explicit path to the manifest file",
    )
    private val modeOpt by option(
        "--mode",
        help = "Shorthand for --manifest .aikit/manifest.<mode>.json",
    )
    private val dryRun by option("--dry-run", help = "Show the planned diff without writing").flag()
    private val force by option(
        "--force",
        help = "Overwrite files that you have changed after generation",
    ).flag()

    override fun run() {
        if (currentContext.invokedSubcommand != null) return

        val resolved = resolveManifestOrFail(
            resolver = manifestResolver,
            positionalArg = manifestArg,
            manifestFlag = manifestOpt,
            modeFlag = modeOpt,
            echo = ::echo,
        )
        val report = generator.generate(resolved, GenerateOptions(dryRun = dryRun, force = force))
            .getOrElse { error -> throw CliktError(formatError(error, resolved.manifestRef), statusCode = 1) }
        printDiff(report)
    }

    @Suppress("CyclomaticComplexMethod")
    private fun printDiff(report: GenerateReport) {
        val prefix = if (report.dryRun) "[dry-run] " else ""
        val touched = report.created.size + report.updated.size + report.deletedOrphans.size
        if (touched == 0 && report.skippedDrift.isEmpty()) {
            echo("${prefix}No changes — generated tree already matches the manifest.")
        } else {
            if (report.created.isNotEmpty()) {
                echo("${prefix}New files (${report.created.size}):")
                report.created.forEach { echo("  + $it") }
            }
            if (report.updated.isNotEmpty()) {
                echo("${prefix}Changed (${report.updated.size}):")
                report.updated.forEach { echo("  ~ $it") }
            }
            if (report.deletedOrphans.isNotEmpty()) {
                echo("${prefix}Removed (${report.deletedOrphans.size}):")
                report.deletedOrphans.forEach { echo("  - $it") }
            }
            if (report.skippedDrift.isNotEmpty()) {
                echo("Skipped — modified by you (use --force to overwrite):")
                report.skippedDrift.forEach { echo("  ! $it") }
            }
        }
        if (report.keptDriftedOrphans.isNotEmpty()) {
            echo("Kept — no longer produced but edited by you:")
            report.keptDriftedOrphans.forEach { echo("  ! $it") }
        }
        echo(if (report.dryRun) "Dry run complete." else "Lock: ${report.lockPath}")
    }

    private fun formatError(error: Throwable, manifestPath: String): String = when (error) {
        is EngineError.ManifestLoadError ->
            "Cannot load manifest '$manifestPath': ${error.cause?.message ?: error.message}"
        is EngineError.BundleLoadError ->
            "Bundle '${error.bundleRef}': ${error.message}"
        is EngineError.InputValidationError ->
            "Invalid input '${error.inputId}': ${error.message}"
        is EngineError.RenderError ->
            "Render error in '${error.templatePath}': ${error.message}"
        is EngineError.WriteError ->
            "Cannot write '${error.outputPath}': ${error.cause?.message ?: error.message}"
        else -> error.message ?: "Update failed"
    }
}
