package io.aequicor.aikit.engine.api

/**
 * Resolves the active project manifest path from multiple sources in priority order:
 * explicit arg → --mode flag → AIKIT_MANIFEST env → AIKIT_MODE env →
 * .aikit/local.properties → legacy .aikit/manifest.json.
 */
interface ManifestResolver {

    /** Resolve the active manifest for the given [request]. */
    fun resolve(request: ResolveRequest): Result<ResolvedManifest>
}

/**
 * @property projectRoot Directory containing `.aikit/` (typically cwd).
 * @property explicitManifestArg Positional `MANIFEST` argument or `--manifest` flag; null if absent.
 * @property explicitModeFlag `--mode <id>` value; null if absent.
 */
data class ResolveRequest(
    val projectRoot: String,
    val explicitManifestArg: String? = null,
    val explicitModeFlag: String? = null,
)

/**
 * @property manifestPath Absolute path to the resolved manifest file.
 * @property manifestRef Path stored in the lock for change detection (usually the raw value from the source).
 * @property modeHint Mode name when resolved via `--mode` or `AIKIT_MODE`; null otherwise.
 * @property source Which source determined the manifest.
 */
data class ResolvedManifest(
    val manifestPath: String,
    val manifestRef: String,
    val modeHint: String? = null,
    val source: ManifestSource,
)

/** Identifies which source provided the active manifest. Used for audit-trail output in the CLI. */
enum class ManifestSource {
    /** Positional `MANIFEST` argument or `--manifest` flag. */
    EXPLICIT_ARG,

    /** `--mode <id>` CLI flag. */
    MODE_FLAG,

    /** `AIKIT_MANIFEST` environment variable. */
    ENV_MANIFEST,

    /** `AIKIT_MODE` environment variable expanded to a path by convention. */
    ENV_MODE,

    /** `manifest=` field in `.aikit/local.properties`. */
    LOCAL_PROPERTIES,

    /** Legacy `.aikit/manifest.json` fallback (no explicit source configured). */
    LEGACY_DEFAULT,
}

/** Errors emitted by [ManifestResolver.resolve]. */
sealed class ManifestResolveError(message: String) : Exception(message) {

    /** The manifest path from the chosen source does not exist on disk. */
    class ManifestNotFound(val path: String) :
        ManifestResolveError("manifest not found: '$path'")

    /** No source provided a manifest and the legacy `.aikit/manifest.json` is also absent. */
    class NoManifestConfigured(val projectRoot: String) :
        ManifestResolveError(
            "no manifest configured in '$projectRoot' — " +
                "create '.aikit/manifest.json', set AIKIT_MANIFEST, " +
                "or add 'manifest=<path>' to '.aikit/local.properties'",
        )

    /** Explicit manifest path and `--mode` / `AIKIT_MODE` were supplied at the same time. */
    class ConflictingArgs(detail: String) : ManifestResolveError(detail)
}
