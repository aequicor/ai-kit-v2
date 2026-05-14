package io.aequicor.aikit.engine.lock

/**
 * Content hash for lock-file entries.
 *
 * The hash is stored in `manifest.lock.json` as a hex string under the `sha256` field; an
 * implementation MUST produce the same value for identical input bytes on every platform.
 * Drift detection compares the recorded hash to a fresh hash of the file currently on disk.
 */
interface HashProvider {
    /** Returns the lowercase hex digest of [bytes]. */
    fun hash(bytes: ByteArray): String
}
