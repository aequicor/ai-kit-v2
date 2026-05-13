package io.aequicor.aikit.engine.api

/**
 * Validates a project manifest and all referenced bundles without writing any files.
 *
 * Returns [Result.success] on success, or [Result.failure] wrapping a descriptive error.
 */
fun interface ManifestVerifier {
    fun verify(manifestPath: String): Result<Unit>
}
