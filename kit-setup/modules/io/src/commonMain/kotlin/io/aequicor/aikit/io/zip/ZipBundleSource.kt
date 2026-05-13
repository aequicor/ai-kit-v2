package io.aequicor.aikit.io.zip

import io.aequicor.aikit.io.BundleSource
import kotlinx.io.Source
import kotlinx.io.files.Path

/**
 * Reads a bundle from a `.zip` archive.
 *
 * **Not yet implemented.** kotlinx-io 0.6 does not ship a zip codec; integrating one (custom
 * inflate, okio-zip, or korlibs-zip) is tracked as a follow-up. The class exists as a
 * placeholder so [io.aequicor.aikit.io.BundleSourceFactory] can return a coherent error today
 * without losing its routing logic.
 *
 * @property archive path to the `.zip` file.
 */
class ZipBundleSource(@Suppress("unused") private val archive: Path) : BundleSource {

    override fun openManifest(): Result<Source> = notImplemented()
    override fun openTargetConfig(targetFolder: String): Result<Source?> = notImplemented()
    override fun listTargetFiles(targetFolder: String): Result<List<String>> = notImplemented()
    override fun openTargetFile(targetFolder: String, relativePath: String): Result<Source> = notImplemented()
    override fun close() = Unit

    private fun <T> notImplemented(): Result<T> =
        Result.failure(NotImplementedError("ZipBundleSource is not implemented yet"))
}
