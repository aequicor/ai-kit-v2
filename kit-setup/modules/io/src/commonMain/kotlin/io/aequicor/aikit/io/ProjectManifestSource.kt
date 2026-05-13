package io.aequicor.aikit.io

import kotlinx.io.Source

/**
 * Abstract source of a user-project manifest (`.aikit/manifest.json`).
 *
 * Splits two concerns: opening the manifest file itself, and resolving the bundle references it
 * contains. Bundle references may point to directories on disk, zip archives, or built-in bundles;
 * the concrete [BundleSource] for each reference is produced by [resolveBundleSource].
 */
interface ProjectManifestSource {

    /**
     * Open `.aikit/manifest.json` for reading.
     *
     * @return [Source] over UTF-8 bytes of the manifest, or failure if the file is missing.
     */
    fun openManifest(): Result<Source>

    /**
     * Resolve a bundle reference (as it appears in the project manifest) into a [BundleSource].
     *
     * The reference syntax is implementation-defined: a path relative to the project root, an
     * absolute path, a `zip:` URL, or an `embedded:` identifier are all reasonable.
     *
     * @return open [BundleSource] (caller closes), or failure if the reference cannot be resolved.
     */
    fun resolveBundleSource(bundleRef: String): Result<BundleSource>
}
