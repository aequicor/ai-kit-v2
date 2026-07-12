package io.aequicor.aikit.engine.api

import kotlinx.serialization.Serializable

fun interface BundleCatalog {
    fun list(baseDir: String, includeIncompatible: Boolean): Result<BundleCatalogResult>
}

@Serializable
data class BundleCatalogResult(
    val kitSetupVersion: String,
    val stale: Boolean,
    val bundles: List<BundleCatalogInfo>,
)

@Serializable
data class BundleCatalogInfo(
    val name: String,
    val version: String,
    val source: String,
    val description: String,
    val targets: List<String>,
    val tags: List<String>,
    val bestFor: List<String>,
    val notFor: List<String>,
    val kitSetup: String,
    val compatible: Boolean,
    val incompatibility: String? = null,
)
