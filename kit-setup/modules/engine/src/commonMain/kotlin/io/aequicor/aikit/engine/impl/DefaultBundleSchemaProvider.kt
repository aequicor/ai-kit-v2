package io.aequicor.aikit.engine.impl

import io.aequicor.aikit.engine.api.BundleSchemaProvider
import io.aequicor.aikit.engine.error.EngineError
import io.aequicor.aikit.format.AiKitFormat
import io.aequicor.aikit.io.BundleSourceFactory
import kotlinx.io.files.Path

internal class DefaultBundleSchemaProvider(
    private val format: AiKitFormat,
    private val currentVersion: String,
    private val sourceFactory: BundleSourceFactory = BundleSourceFactory(),
) : BundleSchemaProvider {

    override fun schemaFor(ref: String, baseDir: String): Result<String> = runCatching {
        val basePath = Path(baseDir.ifBlank { "." })
        val source = sourceFactory.create(ref, basePath)
            .getOrElse { throw EngineError.BundleLoadError(ref, "cannot open bundle '$ref': ${it.message}", it) }

        source.use { src ->
            val parsed = format.parseBundleManifest(src)
                .getOrElse { throw EngineError.BundleLoadError(ref, "cannot parse bundle '$ref': ${it.message}", it) }
            requireCompatible(parsed.manifest, currentVersion, ref)
            format.bundleInputsJsonSchema(parsed.manifest)
        }
    }
}
