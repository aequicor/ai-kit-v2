package io.aequicor.aikit.io

import kotlinx.io.Source

/**
 * Abstract source of a single AI-Kit bundle.
 *
 * Hides the difference between bundles laid out as a directory on disk, packed into a zip archive,
 * or downloaded from a remote repository. All read methods return a freshly opened [Source]; the caller is
 * responsible for closing it (use `.use { … }`). Implementations may keep underlying resources
 * (file handles, zip entries) open until [close] is invoked.
 *
 * Layout assumption (matches `BUNDLE_JSON.md`):
 * ```
 * <bundle-root>/
 *   bundle.json
 *   <target.folder>/
 *     config.json        (optional)
 *     ...templates (*.md, recursive)
 *     ...other assets
 * ```
 */
interface BundleSource : AutoCloseable {

    /**
     * Open `bundle.json` for reading.
     *
     * @return [Source] over UTF-8 bytes of the manifest, or failure if the file is missing.
     */
    fun openManifest(): Result<Source>

    /**
     * Open `<targetFolder>/config.json` if present.
     *
     * @return [Result] holding a [Source], `null` if the file does not exist for this target,
     *   or failure on read error.
     */
    fun openTargetConfig(targetFolder: String): Result<Source?>

    /**
     * Recursively list every file under `<targetFolder>/`, returning paths relative to that folder
     * using POSIX `/` separators. The list is stable across invocations on the same source.
     *
     * @return list of relative paths, or failure if the folder is missing or unreadable.
     */
    fun listTargetFiles(targetFolder: String): Result<List<String>>

    /**
     * Open a file inside a target by its relative path (one of the entries returned from
     * [listTargetFiles]).
     */
    fun openTargetFile(targetFolder: String, relativePath: String): Result<Source>

    /**
     * Release any underlying handles. Safe to call multiple times. After [close], every other
     * method must fail.
     */
    override fun close()
}
