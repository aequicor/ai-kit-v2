package io.aequicor.aikit.engine.lock

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

private const val LOCK_RELATIVE_PATH = ".aikit/manifest.lock.json"

/** Filesystem-backed [LockStore] using kotlinx-io and kotlinx-serialization. */
class DefaultLockStore : LockStore {

    @OptIn(ExperimentalSerializationApi::class)
    private val json: Json = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
        ignoreUnknownKeys = true
    }

    override fun read(projectRoot: String): Result<LockFile?> = runCatching {
        val path = lockPath(projectRoot)
        if (!SystemFileSystem.exists(path)) return@runCatching null
        val text = SystemFileSystem.source(path).buffered().use { it.readString() }
        json.decodeFromString(LockFile.serializer(), text)
    }

    override fun write(projectRoot: String, lock: LockFile): Result<Unit> = runCatching {
        val path = lockPath(projectRoot)
        val parent = path.parent
        if (parent != null) SystemFileSystem.createDirectories(parent)
        val tmp = Path("$path.tmp")
        SystemFileSystem.sink(tmp).buffered().use { sink ->
            sink.write(json.encodeToString(LockFile.serializer(), lock).encodeToByteArray())
        }
        if (SystemFileSystem.exists(path)) SystemFileSystem.delete(path)
        SystemFileSystem.atomicMove(tmp, path)
    }

    override fun delete(projectRoot: String): Result<Unit> = runCatching {
        val path = lockPath(projectRoot)
        if (SystemFileSystem.exists(path)) SystemFileSystem.delete(path)
    }

    override fun pathOf(projectRoot: String): String = lockPath(projectRoot).toString()

    private fun lockPath(projectRoot: String): Path = Path(projectRoot, LOCK_RELATIVE_PATH)
}
