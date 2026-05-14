package io.aequicor.aikit.engine.impl

import io.aequicor.aikit.engine.api.EmbeddedBundleCatalog
import io.aequicor.aikit.engine.api.EmbeddedBundleInfo
import io.aequicor.aikit.io.embedded.embeddedBundleCatalog

/**
 * Catalog populated from [embeddedBundleCatalog] — a generated list produced by the
 * `:modules:io:generateEmbeddedBundles` Gradle task. One entry per bundle baked into the binary.
 */
internal class DefaultEmbeddedBundleCatalog : EmbeddedBundleCatalog {
    override fun list(): Result<List<EmbeddedBundleInfo>> = Result.success(
        embeddedBundleCatalog.map { EmbeddedBundleInfo(it.name, it.version, it.description) }
    )
}
