package io.aequicor.aikit.io.fs

import io.aequicor.aikit.io.BundleSource
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.FileMetadata
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

/**
 * Reads a bundle from a directory on the local file system.
 *
 * The [root] directory must exist and contain a `bundle.json` at the top level. Target folders
 * are sub-directories of [root]; their names come from `BundleManifest.targets[].folder`.
 *
 * Stateless: each `open*` call opens a fresh stream. [close] is a no-op for the FS implementation.
 *
 * @property root absolute or relative path to the bundle root directory.
 */
class FsBundleSource(private val root: Path) : BundleSource {

    override fun openManifest(): Result<Source> = openFile(Path(root, BUNDLE_MANIFEST_FILENAME))

    override fun openTargetConfig(targetFolder: String): Result<Source?> {
        val path = Path(Path(root, targetFolder), TARGET_CONFIG_FILENAME)
        val meta = runCatching { SystemFileSystem.metadataOrNull(path) }.getOrNull()
        if (meta == null || !meta.isRegularFile) return Result.success(null)
        return openFile(path).map { it as Source? }
    }

    override fun listTargetFiles(targetFolder: String): Result<List<String>> = runCatching {
        val targetRoot = Path(root, targetFolder)
        val meta = SystemFileSystem.metadataOrNull(targetRoot)
            ?: error("target folder not found: $targetFolder")
        require(meta.isDirectory) { "target path is not a directory: $targetFolder" }

        val collected = mutableListOf<String>()
        walk(targetRoot, prefix = "", out = collected)
        collected.sorted()
    }

    override fun openTargetFile(targetFolder: String, relativePath: String): Result<Source> {
        val path = relativePath.split('/').fold(Path(root, targetFolder)) { acc, segment ->
            Path(acc, segment)
        }
        return openFile(path)
    }

    override fun close() = Unit

    private fun openFile(path: Path): Result<Source> = runCatching {
        SystemFileSystem.source(path).buffered()
    }

    private fun walk(dir: Path, prefix: String, out: MutableList<String>) {
        for (entry in SystemFileSystem.list(dir)) {
            val name = entry.name
            val relative = if (prefix.isEmpty()) name else "$prefix/$name"
            val meta: FileMetadata = SystemFileSystem.metadataOrNull(entry) ?: continue
            when {
                meta.isDirectory -> walk(entry, relative, out)
                meta.isRegularFile -> out.add(relative)
            }
        }
    }

    private companion object {
        const val BUNDLE_MANIFEST_FILENAME = "bundle.json"
        const val TARGET_CONFIG_FILENAME = "config.json"
    }
}
