package io.aequicor.aikit.engine.write

/**
 * Filesystem access used by the engine: write/read/delete + existence check.
 *
 * Kept narrow on purpose — the engine has no business with arbitrary path operations. All methods
 * accept absolute or normalized paths produced by the engine itself; results are wrapped in
 * [Result] so callers can branch on failure without exceptions.
 */
interface FileWriter {
    /** Write [content] to [path], creating parent directories as needed. */
    fun write(path: String, content: ByteArray): Result<Unit>

    /** Read all bytes at [path]. Fails if the file does not exist. */
    fun read(path: String): Result<ByteArray>

    /** True iff a file (not directory) exists at [path]. */
    fun exists(path: String): Boolean

    /** Delete [path] if it exists. Succeeds (no-op) when the file is already gone. */
    fun delete(path: String): Result<Unit>

    /**
     * Delete the empty directory at [path]. No-op if the directory does not exist or is not empty.
     * Used by `remove` to clean up parent folders after their tracked files are gone.
     */
    fun deleteDirectoryIfEmpty(path: String): Result<Unit>
}
