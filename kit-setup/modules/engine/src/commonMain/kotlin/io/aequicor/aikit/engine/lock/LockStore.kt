package io.aequicor.aikit.engine.lock

/**
 * Read/write access to `.aikit/manifest.lock.json`.
 *
 * Implementations decide where the file lives and how to write atomically. The store does NOT
 * mutate the contents of [LockFile] — callers build the new lock and hand it over for persistence.
 */
interface LockStore {

    /**
     * Returns the existing lock at [projectRoot] or `null` if no `manifest.lock.json` is present.
     * A malformed file produces a failed [Result] — callers must decide whether to ignore it.
     */
    fun read(projectRoot: String): Result<LockFile?>

    /**
     * Atomically replace the lock file at [projectRoot] with [lock]. Creates the parent
     * directory if needed.
     */
    fun write(projectRoot: String, lock: LockFile): Result<Unit>

    /** Delete `manifest.lock.json` at [projectRoot] if it exists. */
    fun delete(projectRoot: String): Result<Unit>

    /** POSIX path of the lock file under [projectRoot], used in CLI messages. */
    fun pathOf(projectRoot: String): String
}
