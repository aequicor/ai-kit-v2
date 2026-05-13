package io.aequicor.aikit.engine.write

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

/** Writes files to the real filesystem, creating parent directories as needed. */
class FsFileWriter : FileWriter {

    override fun write(path: String, content: ByteArray): Result<Unit> = runCatching {
        val file = Path(path)
        val parent = file.parent
        if (parent != null) {
            SystemFileSystem.createDirectories(parent)
        }
        SystemFileSystem.sink(file).buffered().use { it.write(content) }
    }
}
