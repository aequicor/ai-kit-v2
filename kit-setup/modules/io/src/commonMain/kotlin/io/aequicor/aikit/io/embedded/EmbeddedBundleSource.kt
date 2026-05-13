package io.aequicor.aikit.io.embedded

import io.aequicor.aikit.io.BundleSource
import kotlinx.io.Source

/**
 * Reads a bundle that ships baked into the CLI binary.
 *
 * **Not yet implemented.** Embedding requires either a Gradle codegen step that produces a Kotlin
 * resource table or a future kotlinx resource API on Native. Tracked as a follow-up. The class is
 * present so [io.aequicor.aikit.io.BundleSourceFactory] retains its `embedded:` routing.
 *
 * @property name identifier of the embedded bundle (as referenced from the project manifest).
 */
class EmbeddedBundleSource(@Suppress("unused") private val name: String) : BundleSource {

    override fun openManifest(): Result<Source> = notImplemented()
    override fun openTargetConfig(targetFolder: String): Result<Source?> = notImplemented()
    override fun listTargetFiles(targetFolder: String): Result<List<String>> = notImplemented()
    override fun openTargetFile(targetFolder: String, relativePath: String): Result<Source> = notImplemented()
    override fun close() = Unit

    private fun <T> notImplemented(): Result<T> =
        Result.failure(NotImplementedError("EmbeddedBundleSource is not implemented yet"))
}
