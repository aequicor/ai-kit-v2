package io.aequicor.aikit.engine

import io.aequicor.aikit.engine.api.BundleGenerator
import io.aequicor.aikit.engine.api.ManifestVerifier
import io.aequicor.aikit.engine.api.SchemaProvider
import io.aequicor.aikit.engine.impl.DefaultBundleGenerator
import io.aequicor.aikit.engine.impl.DefaultManifestVerifier
import io.aequicor.aikit.engine.impl.DefaultSchemaProvider
import io.aequicor.aikit.engine.write.FileWriter
import io.aequicor.aikit.engine.write.FsFileWriter
import io.aequicor.aikit.format.AiKitFormat

/**
 * Entry point for the AI-Kit generation pipeline.
 *
 * Wires together [AiKitFormat], I/O sources, template rendering, and [FileWriter] into the three
 * public operations consumed by the CLI.
 *
 * @property schemaProvider Returns the JSON Schema for `.aikit/manifest.json`.
 * @property verifier Validates a project manifest and its referenced bundles (no writes).
 * @property generator Full generate pipeline — reads, renders, and writes output files.
 */
class AiKitEngine private constructor(
    val schemaProvider: SchemaProvider,
    val verifier: ManifestVerifier,
    val generator: BundleGenerator,
) {
    companion object {
        /**
         * Creates a production-ready [AiKitEngine] that reads bundles from the filesystem and
         * writes output files to disk.
         */
        fun create(
            format: AiKitFormat = AiKitFormat.create(),
            fileWriter: FileWriter = FsFileWriter(),
        ): AiKitEngine = AiKitEngine(
            schemaProvider = DefaultSchemaProvider(format),
            verifier = DefaultManifestVerifier(format),
            generator = DefaultBundleGenerator(format, fileWriter),
        )
    }
}
