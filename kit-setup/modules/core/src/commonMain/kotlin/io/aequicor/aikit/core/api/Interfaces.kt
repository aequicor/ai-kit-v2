package io.aequicor.aikit.core.api

/** Returns the JSON schema for the `.aikit/manifest.yaml` format. */
fun interface SchemaProvider {
    fun schema(): String
}

/** Parses and validates the manifest at [manifestPath], returning errors on failure. */
fun interface ManifestVerifier {
    fun verify(manifestPath: String): Result<Unit>
}

/** Resolves bundles referenced by the manifest and writes agent configuration files. */
fun interface BundleGenerator {
    fun generate(manifestPath: String): Result<Unit>
}
