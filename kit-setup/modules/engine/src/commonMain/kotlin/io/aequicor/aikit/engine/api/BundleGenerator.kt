package io.aequicor.aikit.engine.api

/**
 * Runs the full generate pipeline: loads the project manifest, resolves bundles, renders
 * templates, writes output files, and persists `.aikit/manifest.lock.json`.
 */
interface BundleGenerator {

    /** Generate output files according to the manifest at [manifestPath]. */
    fun generate(
        manifestPath: String,
        options: GenerateOptions = GenerateOptions(),
    ): Result<GenerateReport>

    /**
     * Generate output files using a pre-resolved manifest.
     *
     * When [resolved] references a different manifest than the previous lock, the generator
     * performs a clean wipe of previously generated files **after** a successful plan phase,
     * ensuring a broken new manifest never destroys a working installation.
     */
    fun generate(
        resolved: ResolvedManifest,
        options: GenerateOptions = GenerateOptions(),
    ): Result<GenerateReport>
}

/**
 * Toggles for the generate pipeline.
 *
 * @property dryRun render and diff against the lock-file but write nothing to disk.
 * @property force overwrite drifted files (files the user changed by hand since the last generate).
 * @property allowMissingLock skip the "expected lock not found" warning. Used by initial install.
 * @property clean force a full wipe of previously generated files before applying, even when the
 *   manifest has not changed. Useful after major bundle updates.
 */
data class GenerateOptions(
    val dryRun: Boolean = false,
    val force: Boolean = false,
    val allowMissingLock: Boolean = true,
    val clean: Boolean = false,
)

/** What the generator did (or would do in dry-run). All paths are project-root-relative POSIX. */
data class GenerateReport(
    /** Brand-new files written for the first time. */
    val created: List<String>,
    /** Existing tracked files whose contents changed and were rewritten. */
    val updated: List<String>,
    /** Tracked files whose rendered content matched what was already on disk — left untouched. */
    val unchanged: List<String>,
    /**
     * Files that were tracked but the user has edited since the last generate. Without `force`
     * they are NOT rewritten so user edits are preserved; the CLI surfaces this as a warning.
     */
    val skippedDrift: List<String>,
    /**
     * Files that were tracked by a previous generate but are no longer produced by the current
     * bundle/inputs. Their on-disk hash still matches the lock, so they were safely deleted.
     */
    val deletedOrphans: List<String>,
    /**
     * Same as [deletedOrphans] but the user had edited the file after generation — kept as-is and
     * surfaced as a warning so the user can decide what to do.
     */
    val keptDriftedOrphans: List<String>,
    /** True when [GenerateOptions.dryRun] was set; in that case no on-disk changes occurred. */
    val dryRun: Boolean,
    /** Project-root-relative POSIX path of the lock file (`.aikit/manifest.lock.json`). */
    val lockPath: String,
    /** Manifest ref recorded in the previous lock; null if there was no previous lock. */
    val previousManifestRef: String? = null,
    /** Manifest ref written to the new lock. */
    val currentManifestRef: String? = null,
    /** True when a clean wipe of the previous installation was performed before applying. */
    val wipedBefore: Boolean = false,
)
