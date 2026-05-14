@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.aequicor.aikit.engine.pipeline

import kotlinx.cinterop.toKString
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import platform.posix.getenv

private class FsValueSourceReader(private val projectRoot: String) : ValueSourceReader {
    override fun readEnv(name: String): String? = getenv(name)?.toKString()

    override fun readFile(relativePath: String): Result<String> = runCatching {
        val file = Path(projectRoot, relativePath)
        SystemFileSystem.source(file).buffered().use { it.readByteArray() }.decodeToString().trimEnd()
    }
}

internal actual fun createDefaultValueSourceReader(projectRoot: String): ValueSourceReader =
    FsValueSourceReader(projectRoot)
