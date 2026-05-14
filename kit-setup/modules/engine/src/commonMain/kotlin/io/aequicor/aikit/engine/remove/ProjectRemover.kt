package io.aequicor.aikit.engine.remove

/**
 * Removes AI-Kit-generated files from a project using `.aikit/manifest.lock.json` as the registry.
 *
 * The remover never touches files outside the lock — files the user created themselves stay put.
 * Files whose on-disk hash no longer matches the lock are treated as user-modified (drift) and
 * are skipped unless [RemoveOptions.force] is set.
 */
interface ProjectRemover {

    /** Run the removal at [projectRoot] (directory containing `.aikit/`). */
    fun remove(projectRoot: String, options: RemoveOptions = RemoveOptions()): Result<RemoveReport>
}

/**
 * @property dryRun compute the report without deleting anything.
 * @property force delete tracked files even if their content has drifted from the lock.
 * @property keepManifest leave `.aikit/manifest.json` and `manifest.lock.json` in place.
 */
data class RemoveOptions(
    val dryRun: Boolean = false,
    val force: Boolean = false,
    val keepManifest: Boolean = false,
)

/** Outcome of a [ProjectRemover.remove] call. All paths are project-root-relative POSIX. */
data class RemoveReport(
    /** Files deleted (or that would be deleted in dry-run). */
    val deleted: List<String>,
    /** Tracked files left alone because the user modified them after generation. */
    val skippedDrift: List<String>,
    /** Tracked files listed in the lock but already missing on disk. */
    val missing: List<String>,
    /** True if `.aikit/manifest.json` / `.lock.json` were removed. */
    val manifestRemoved: Boolean,
    val dryRun: Boolean,
)

/** Error indicating that no `manifest.lock.json` was found at the project root. */
class LockNotFoundError(val lockPath: String) :
    Exception("lock file not found at '$lockPath' — run `kit-setup generate` first or remove files manually")
