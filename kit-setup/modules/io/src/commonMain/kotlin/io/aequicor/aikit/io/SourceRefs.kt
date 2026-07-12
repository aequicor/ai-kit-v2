package io.aequicor.aikit.io

import io.aequicor.aikit.io.remote.RemoteBundleRef

/**
 * Maps the `source` field of a manifest target to a concrete bundle reference understood by
 * [BundleSourceFactory]. Single home for the sentinel values `"internal"` and `"remote"` so the
 * generator, verifier and schema provider all resolve them identically.
 */
object SourceRefs {

    /** Removed sentinel retained only to produce an actionable migration error. */
    const val INTERNAL = "internal"

    /**
     * Resolve [source] into a factory-ready reference.
     *
     * - `"internal"` and `embedded:` → actionable migration error.
     * - `"remote"` → default-registry `remote:` reference for the bundle's name
     *   (version stripped — remote bundles track a branch, the version is verified
     *   against the downloaded `bundle.json`).
     * - anything else (paths, `zip:`, explicit `remote:`) → unchanged.
     *
     * @param source raw `source` value from `.aikit/manifest.json`.
     * @param bundle `name@version` reference from the same target.
     */
    fun effective(source: String, bundle: String): Result<String> = when (source) {
        INTERNAL -> Result.failure(IllegalArgumentException(LEGACY_SOURCE_MESSAGE))
        RemoteBundleRef.SOURCE_SENTINEL ->
            RemoteBundleRef.defaultFor(bundle).map { it.toRefString() }
        else -> if (source.startsWith("embedded:")) {
            Result.failure(IllegalArgumentException(LEGACY_SOURCE_MESSAGE))
        } else {
            Result.success(source)
        }
    }

    const val LEGACY_SOURCE_MESSAGE =
        "embedded bundles were removed in kit-setup 1.0.0; use source 'remote', " +
            "or download the bundle into the project and set source to its local directory or ZIP path"
}
