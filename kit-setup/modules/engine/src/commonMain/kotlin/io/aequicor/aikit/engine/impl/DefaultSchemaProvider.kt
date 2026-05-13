package io.aequicor.aikit.engine.impl

import io.aequicor.aikit.engine.api.SchemaProvider
import io.aequicor.aikit.format.AiKitFormat

internal class DefaultSchemaProvider(private val format: AiKitFormat) : SchemaProvider {
    override fun schema(): String = format.manifestJsonSchema()
}
