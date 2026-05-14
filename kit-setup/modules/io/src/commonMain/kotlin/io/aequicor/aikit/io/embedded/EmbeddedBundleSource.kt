package io.aequicor.aikit.io.embedded

import io.aequicor.aikit.io.BundleSource
import kotlinx.io.Buffer
import kotlinx.io.Source

/**
 * Reads a bundle baked into the CLI binary at build time.
 *
 * File contents are served from [embeddedBundleFiles] — a generated map produced by the
 * `:modules:io:generateEmbeddedBundles` Gradle task. If no bundle with the given [name] is found
 * in the map, every method fails with [NoSuchElementException].
 *
 * @property name identifier of the embedded bundle (matches the key in `embeddedBundleFiles`).
 */
class EmbeddedBundleSource(private val name: String) : BundleSource {

    private val files: Map<String, ByteArray>? = embeddedBundleFiles[name]

    override fun openManifest(): Result<Source> = open("bundle.json")

    override fun openTargetConfig(targetFolder: String): Result<Source?> {
        files ?: return missing()
        val key = "$targetFolder/config.json"
        return if (files.containsKey(key)) open(key).map { it } else Result.success(null)
    }

    override fun listTargetFiles(targetFolder: String): Result<List<String>> {
        files ?: return missing()
        val prefix = "$targetFolder/"
        return Result.success(
            files.keys.filter { it.startsWith(prefix) }.map { it.removePrefix(prefix) }.sorted()
        )
    }

    override fun openTargetFile(targetFolder: String, relativePath: String): Result<Source> =
        open("$targetFolder/$relativePath")

    override fun close() = Unit

    private fun open(path: String): Result<Source> {
        val bytes = files?.get(path)
            ?: return Result.failure(NoSuchElementException("Embedded file not found: $name/$path"))
        return Result.success(Buffer().apply { write(bytes) })
    }

    private fun <T> missing(): Result<T> =
        Result.failure(NoSuchElementException("No embedded bundle with name: $name"))
}
