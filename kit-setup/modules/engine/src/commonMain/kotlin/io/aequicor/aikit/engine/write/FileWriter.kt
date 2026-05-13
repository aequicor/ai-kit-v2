package io.aequicor.aikit.engine.write

/**
 * Writes rendered template content to the installation destination.
 *
 * Implementations decide how to handle existing files (overwrite, skip-if-identical, etc.).
 * The [path] is the absolute destination path on disk.
 */
interface FileWriter {
    fun write(path: String, content: ByteArray): Result<Unit>
}
