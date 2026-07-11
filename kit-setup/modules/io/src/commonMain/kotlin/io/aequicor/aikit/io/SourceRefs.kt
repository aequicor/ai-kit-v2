package io.aequicor.aikit.io

import io.aequicor.aikit.io.remote.RemoteBundleRef

/**
 * Maps the `source` field of a manifest target to a concrete bundle reference understood by
 * [BundleSourceFactory]. Single home for the sentinel values `"internal"` and `"remote"` so the
 * generator, verifier and schema provider all resolve them identically.
 */
object SourceRefs {

    /** Sentinel: bundle baked into the CLI binary. */
    const val INTERNAL = "internal"

    /**
     * Resolve [source] into a factory-ready reference.
     *
     * - `"internal"` → `embedded:<bundle>`.
     * - `"remote"` → default-registry `remote:` reference for the bundle's name
     *   (version stripped — remote bundles track a branch, the version is verified
     *   against the downloaded `bundle.json`).
     * - anything else (paths, `zip:`, `embedded:`, explicit `remote:`) → unchanged.
     *
     * @param source raw `source` value from `.aikit/manifest.json`.
     * @param bundle `name@version` reference from the same target.
     */
    fun effective(source: String, bundle: String): Result<String> = when (source) {
        INTERNAL -> Result.success("embedded:$bundle")
        RemoteBundleRef.SOURCE_SENTINEL ->
            RemoteBundleRef.defaultFor(bundle.substringBefore('@')).map { it.toRefString() }
        else -> Result.success(source)
    }
}
