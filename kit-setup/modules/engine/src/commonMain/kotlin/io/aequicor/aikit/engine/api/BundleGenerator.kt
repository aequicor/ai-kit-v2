package io.aequicor.aikit.engine.api

/**
 * Runs the full generate pipeline: loads the project manifest, resolves bundles, renders
 * templates, and writes output files for each declared AI-agent target.
 *
 * Returns [Result.success] on success, or [Result.failure] wrapping a descriptive error.
 */
fun interface BundleGenerator {
    fun generate(manifestPath: String): Result<Unit>
}
