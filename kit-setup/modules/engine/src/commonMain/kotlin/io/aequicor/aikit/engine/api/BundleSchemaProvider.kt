package io.aequicor.aikit.engine.api

/**
 * Produces a JSON Schema for the `inputs` object of a specific bundle.
 *
 * Used by the `kit-setup schema bundle <ref>` command so users can validate and autocomplete the
 * `targets.<name>.inputs` block of their project manifest against the spec declared in the
 * bundle's `bundle.json`.
 */
fun interface BundleSchemaProvider {

    /**
     * Load the bundle referenced by [ref] and return its `inputs` JSON Schema as a string.
     *
     * @param ref bundle reference — directory path, `zip:<path>`, `<path>.zip`, or `embedded:<name>`.
     *   Same syntax as `targets.<name>.source` in a project manifest.
     * @param baseDir directory used to resolve relative paths in [ref]. Ignored for embedded
     *   bundles. Empty string is treated as the current working directory.
     */
    fun schemaFor(ref: String, baseDir: String): Result<String>
}
