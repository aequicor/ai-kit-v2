package io.aequicor.aikit.engine.impl

import io.aequicor.aikit.core.domain.bundle.BundleCompatibility
import io.aequicor.aikit.core.domain.bundle.BundleManifest
import io.aequicor.aikit.engine.error.EngineError

internal fun requireCompatible(manifest: BundleManifest, currentVersion: String, bundleRef: String) {
    val reason = BundleCompatibility.incompatibility(manifest, currentVersion) ?: return
    throw EngineError.BundleLoadError(bundleRef, "incompatible bundle '$bundleRef': $reason")
}
