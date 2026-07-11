package io.aequicor.aikit.engine.lock

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Registry of files produced by the last successful `kit-setup generate` run.
 *
 * Persisted as `.aikit/manifest.lock.json` next to the project manifest. Used by:
 * - `generate` to detect user edits (drift) and prune orphaned files when the manifest changes.
 * - `remove` to know exactly which files belong to AI-Kit and can be safely deleted.
 *
 * The schema is versioned via [lockVersion] independently of the project manifest schema.
 */
@Serializable
data class LockFile(
    /** Lock-file schema version. Currently `"1"`. */
    val lockVersion: String,
    /** CLI version that produced this lock; diagnostic only. */
    val aikitVersion: String,
    /** ISO-8601 UTC timestamp of the generation. */
    val generatedAt: String,
    /** One entry per application from the project manifest. */
    val applications: List<LockApplication>,
    /**
     * Path of the manifest that produced this lock, relative to the project root.
     * Used to detect manifest switches and trigger a clean wipe before the next generate.
     * Null for locks produced before multi-manifest support was added.
     */
    val manifestRef: String? = null,
)

/** Per-application section of the lock — mirrors `applications[]` in `.aikit/manifest.json`. */
@Serializable
data class LockApplication(
    val id: String,
    val path: String,
    val targets: Map<String, LockTarget>,
)

/** Per-target section: bundle reference plus the list of files this target produced. */
@Serializable
data class LockTarget(
    val bundle: String,
    val source: String,
    /**
     * Commit sha the bundle was downloaded at — recorded for `remote` sources only, so the
     * exact content behind a branch-tracking installation stays auditable. Null for embedded,
     * path and zip sources.
     */
    val resolvedSha: String? = null,
    val files: List<LockFileEntry>,
)

/**
 * Single file produced by generate.
 *
 * @property path POSIX-style path relative to the project root (directory containing `.aikit/`).
 * @property hash content hash of the rendered bytes; used for drift detection on subsequent runs.
 */
@Serializable
data class LockFileEntry(
    val path: String,
    @SerialName("sha256") val hash: String,
)
