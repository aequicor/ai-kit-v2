package io.aequicor.aikit.format.projectManifest

import kotlinx.serialization.json.JsonElement

/**
 * Parsed representation of `.aikit/manifest.json` **before** bundle resolution.
 *
 * This is a `format`-module type, not a core domain type. Bundle references and input values are
 * not yet resolved — the `engine` combines this with loaded [BundleManifest]s (via `BundleSource`)
 * to produce the fully-hydrated core [ProjectManifest].
 */
data class RawProjectManifest(
    val aikitVersion: String,
    val applications: List<RawApplicationEntry>,
)

data class RawApplicationEntry(
    val id: String,
    val path: String,
    /** Keyed by agent name (e.g. `"claude"`, `"opencode"`). */
    val targets: Map<String, RawBundleTarget>,
)

/**
 * A single target within a [RawApplicationEntry], before bundle loading.
 *
 * @property bundle Bundle reference in `name@version` format.
 * @property source `"internal"` or a path to a zip / directory on disk.
 * @property inputs Raw JSON values for the bundle inputs. Types are not yet validated against
 *   the bundle's [InputSpec] — that happens in the engine's resolve stage.
 */
data class RawBundleTarget(
    val bundle: String,
    val source: String,
    val inputs: Map<String, JsonElement>,
)
