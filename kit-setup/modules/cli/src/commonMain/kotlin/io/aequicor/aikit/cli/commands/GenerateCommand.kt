package io.aequicor.aikit.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import io.aequicor.aikit.engine.api.BundleGenerator
import io.aequicor.aikit.engine.api.GenerateOptions
import io.aequicor.aikit.engine.api.GenerateReport
import io.aequicor.aikit.engine.error.EngineError

/** Resolves bundles from [manifest], renders templates and writes agent configuration files. */
class GenerateCommand(private val generator: BundleGenerator) : CliktCommand(
    name = "generate",
    help = "Generate agent configuration from a manifest file",
) {
    private val manifest by argument(
        name = "MANIFEST",
        help = "Path to the manifest file (e.g. .aikit/manifest.json)",
    )

    private val dryRun by option("--dry-run", help = "Show the plan without writing or deleting files").flag()
    private val force by option(
        "--force",
        help = "Overwrite files that the user has changed since the last generation",
    ).flag()

    override fun run() {
        val report = generator.generate(manifest, GenerateOptions(dryRun = dryRun, force = force))
            .getOrElse { error -> throw CliktError(formatError(error), statusCode = 1) }
        printReport(report)
    }

    private fun printReport(report: GenerateReport) {
        val prefix = if (report.dryRun) "[dry-run] " else ""
        if (report.created.isNotEmpty()) {
            echo("${prefix}Created (${report.created.size}):")
            report.created.forEach { echo("  + $it") }
        }
        if (report.updated.isNotEmpty()) {
            echo("${prefix}Updated (${report.updated.size}):")
            report.updated.forEach { echo("  ~ $it") }
        }
        if (report.deletedOrphans.isNotEmpty()) {
            echo("${prefix}Removed (${report.deletedOrphans.size}):")
            report.deletedOrphans.forEach { echo("  - $it") }
        }
        if (report.unchanged.isNotEmpty()) {
            echo("Unchanged: ${report.unchanged.size} file(s)")
        }
        if (report.skippedDrift.isNotEmpty()) {
            echo("Skipped — modified by user (re-run with --force to overwrite):")
            report.skippedDrift.forEach { echo("  ! $it") }
        }
        if (report.keptDriftedOrphans.isNotEmpty()) {
            echo("Kept — would be removed but you edited them after generation:")
            report.keptDriftedOrphans.forEach { echo("  ! $it") }
        }
        echo(if (report.dryRun) "Dry run complete. Lock file would be written to ${report.lockPath}." else "Done. Lock: ${report.lockPath}")
    }

    private fun formatError(error: Throwable): String = when (error) {
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
}
