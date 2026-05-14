package io.aequicor.aikit.engine.write

import kotlinx.io.buffered
import kotlinx.io.files.FileMetadata
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray

/** Default [FileWriter] backed by [SystemFileSystem]. */
class FsFileWriter : FileWriter {

    override fun write(path: String, content: ByteArray): Result<Unit> = runCatching {
        val file = Path(path)
        val parent = file.parent
        if (parent != null) SystemFileSystem.createDirectories(parent)
        SystemFileSystem.sink(file).buffered().use { it.write(content) }
    }

    override fun read(path: String): Result<ByteArray> = runCatching {
        SystemFileSystem.source(Path(path)).buffered().use { it.readByteArray() }
    }

    override fun exists(path: String): Boolean {
        val p = Path(path)
        if (!SystemFileSystem.exists(p)) return false
        val meta: FileMetadata? = SystemFileSystem.metadataOrNull(p)
        return meta?.isRegularFile == true
    }

    override fun delete(path: String): Result<Unit> = runCatching {
        val p = Path(path)
        if (SystemFileSystem.exists(p)) SystemFileSystem.delete(p)
    }

    override fun deleteDirectoryIfEmpty(path: String): Result<Unit> = runCatching {
        val p = Path(path)
        if (!SystemFileSystem.exists(p)) return@runCatching
        val meta = SystemFileSystem.metadataOrNull(p) ?: return@runCatching
        if (!meta.isDirectory) return@runCatching
        val entries = runCatching { SystemFileSystem.list(p) }.getOrDefault(emptyList())
        if (entries.isEmpty()) SystemFileSystem.delete(p)
    }
}
