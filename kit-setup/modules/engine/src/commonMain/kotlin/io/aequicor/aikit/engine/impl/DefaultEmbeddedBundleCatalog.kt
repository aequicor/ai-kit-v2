package io.aequicor.aikit.engine.impl

import io.aequicor.aikit.engine.api.EmbeddedBundleCatalog
import io.aequicor.aikit.engine.api.EmbeddedBundleInfo

/**
 * Stub catalog returning an empty list.
 *
 * Embedded-bundle support is not yet wired through codegen (`EmbeddedBundleSource` is a
 * placeholder). Once that lands, replace this implementation with one that reads the generated
 * resource table; CLI callers do not need to change.
 */
internal class DefaultEmbeddedBundleCatalog : EmbeddedBundleCatalog {
    override fun list(): Result<List<EmbeddedBundleInfo>> = Result.success(emptyList())
}
