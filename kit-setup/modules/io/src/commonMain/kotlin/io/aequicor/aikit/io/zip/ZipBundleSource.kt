package io.aequicor.aikit.io.zip

import io.aequicor.aikit.io.BundleSource
import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray

/**
 * Reads a bundle from a `.zip` archive on the local file system.
 *
 * The entire archive is loaded into memory on first access. Entries are parsed from
 * the ZIP central directory and decompressed on demand. Both STORED (method 0) and
 * DEFLATE (method 8) compression are supported. ZIP64 archives are not supported.
 *
 * Expected ZIP layout (mirrors [io.aequicor.aikit.io.fs.FsBundleSource]):
 * ```
 * bundle.json
 * <target>/config.json
 * <target>/<relative-paths…>
 * ```
 *
 * @property archive path to the `.zip` file.
 */
class ZipBundleSource(private val archive: Path) : BundleSource {

    private var closed = false

    private val reader: ZipReader by lazy {
        val bytes = SystemFileSystem.source(archive).buffered().use { it.readByteArray() }
        ZipReader(bytes)
    }

    override fun openManifest(): Result<Source> = runCatching {
        checkOpen()
        val entry = reader.entries[BUNDLE_MANIFEST]
            ?: error("$BUNDLE_MANIFEST not found in $archive")
        reader.readEntryData(entry).toSource()
    }

    override fun openTargetConfig(targetFolder: String): Result<Source?> = runCatching {
        checkOpen()
        val key = "$targetFolder/$TARGET_CONFIG"
        val entry = reader.entries[key] ?: return Result.success(null)
        reader.readEntryData(entry).toSource()
    }

    override fun listTargetFiles(targetFolder: String): Result<List<String>> = runCatching {
        checkOpen()
        val prefix = "$targetFolder/"
        val files = reader.entries.keys
            .filter { it.startsWith(prefix) }
            .map { it.removePrefix(prefix) }
            .sorted()
        check(files.isNotEmpty() || reader.entries.keys.any { it.startsWith(prefix) }) {
            "target folder not found in archive: $targetFolder"
        }
        files
    }

    override fun openTargetFile(targetFolder: String, relativePath: String): Result<Source> = runCatching {
        checkOpen()
        val key = "$targetFolder/$relativePath"
        val entry = reader.entries[key] ?: error("file not found in archive: $key")
        reader.readEntryData(entry).toSource()
    }

    override fun close() {
        closed = true
    }

    private fun checkOpen() = check(!closed) { "ZipBundleSource is closed" }

    private fun ByteArray.toSource(): Source = Buffer().also { it.write(this) }

    private companion object {
        const val BUNDLE_MANIFEST = "bundle.json"
        const val TARGET_CONFIG = "config.json"
    }
}
