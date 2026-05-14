package io.aequicor.aikit.engine

import io.aequicor.aikit.engine.api.BundleGenerator
import io.aequicor.aikit.engine.api.BundleSchemaProvider
import io.aequicor.aikit.engine.api.EmbeddedBundleCatalog
import io.aequicor.aikit.engine.api.ManifestResolver
import io.aequicor.aikit.engine.api.ManifestVerifier
import io.aequicor.aikit.engine.api.SchemaProvider
import io.aequicor.aikit.engine.manifest.createFsManifestResolver // expect fun from nativeMain
import io.aequicor.aikit.engine.impl.DefaultBundleGenerator
import io.aequicor.aikit.engine.impl.DefaultBundleSchemaProvider
import io.aequicor.aikit.engine.impl.DefaultEmbeddedBundleCatalog
import io.aequicor.aikit.engine.impl.DefaultManifestVerifier
import io.aequicor.aikit.engine.impl.DefaultSchemaProvider
import io.aequicor.aikit.engine.lock.DefaultLockStore
import io.aequicor.aikit.engine.lock.HashProvider
import io.aequicor.aikit.engine.lock.LockStore
import io.aequicor.aikit.engine.lock.Sha256HashProvider
import io.aequicor.aikit.engine.remove.DefaultProjectRemover
import io.aequicor.aikit.engine.remove.ProjectRemover
import io.aequicor.aikit.engine.write.FileWriter
import io.aequicor.aikit.engine.write.FsFileWriter
import io.aequicor.aikit.format.AiKitFormat

/**
 * Entry point for the AI-Kit generation pipeline.
 *
 * Wires together [AiKitFormat], I/O sources, template rendering, [LockStore] and [FileWriter]
 * into the operations exposed to the CLI.
 *
 * @property schemaProvider Returns the JSON Schema for `.aikit/manifest.json`.
 * @property bundleSchemaProvider Returns the JSON Schema for the `inputs` of a specific bundle.
 * @property embeddedBundleCatalog Lists bundles compiled into the CLI binary.
 * @property verifier Validates a project manifest and its referenced bundles (no writes).
 * @property generator Generate pipeline — plans, diffs against the lock, writes output and lock.
 * @property remover Removes AI-Kit artifacts from a project using the lock file as the registry.
 * @property manifestResolver Resolves the active manifest from CLI flags, env vars, or local.properties.
 */
@Suppress("LongParameterList")
class AiKitEngine private constructor(
    val schemaProvider: SchemaProvider,
    val bundleSchemaProvider: BundleSchemaProvider,
    val embeddedBundleCatalog: EmbeddedBundleCatalog,
    val verifier: ManifestVerifier,
    val generator: BundleGenerator,
    val remover: ProjectRemover,
    val manifestResolver: ManifestResolver,
) {
    companion object {
        /**
         * Creates a production-ready [AiKitEngine] backed by the real filesystem.
         *
         * @param aikitVersion CLI version string recorded in `manifest.lock.json` (`aikitVersion`).
         */
        fun create(
            aikitVersion: String,
            format: AiKitFormat = AiKitFormat.create(),
            fileWriter: FileWriter = FsFileWriter(),
            lockStore: LockStore = DefaultLockStore(),
            hashProvider: HashProvider = Sha256HashProvider(),
        ): AiKitEngine {
            val remover = DefaultProjectRemover(
                fileWriter = fileWriter,
                lockStore = lockStore,
                hashProvider = hashProvider,
            )
            return AiKitEngine(
                schemaProvider = DefaultSchemaProvider(format),
                bundleSchemaProvider = DefaultBundleSchemaProvider(format),
                embeddedBundleCatalog = DefaultEmbeddedBundleCatalog(),
                verifier = DefaultManifestVerifier(format),
                generator = DefaultBundleGenerator(
                    format = format,
                    fileWriter = fileWriter,
                    lockStore = lockStore,
                    hashProvider = hashProvider,
                    aikitVersion = aikitVersion,
                    remover = remover,
                ),
                remover = remover,
                manifestResolver = createFsManifestResolver(),
            )
        }
    }
}
