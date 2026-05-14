package io.aequicor.aikit.engine.api

/**
 * Discoverable catalog of bundles compiled into the CLI binary.
 *
 * Lets users see which `embedded:<name>` references are available without unpacking the
 * executable. Implementations will be populated from a generated resource table once the
 * embedded-bundle codegen lands; the default implementation currently returns an empty list.
 */
fun interface EmbeddedBundleCatalog {

    /** All bundles known to the CLI, suitable for human-readable listing. */
    fun list(): Result<List<EmbeddedBundleInfo>>
}

/**
 * Public-facing metadata for an embedded bundle.
 *
 * @property name identifier used in `embedded:<name>` references.
 * @property version SemVer string of the bundle, or `null` if the catalog does not parse manifests.
 * @property description one-line human-readable description, or `null` if unknown.
 */
data class EmbeddedBundleInfo(
    val name: String,
    val version: String?,
    val description: String?,
)
