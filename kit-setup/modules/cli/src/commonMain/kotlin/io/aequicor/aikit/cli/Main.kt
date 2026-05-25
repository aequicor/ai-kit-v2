package io.aequicor.aikit.cli

import io.aequicor.aikit.engine.AiKitEngine

/** Current CLI version; keep in sync with gradle.properties. */
internal const val VERSION = "0.2.0"

fun main(args: Array<String>) {
    val engine = AiKitEngine.create(aikitVersion = VERSION)
    AiKitCli(
        schemaProvider = engine.schemaProvider,
        bundleSchemaProvider = engine.bundleSchemaProvider,
        embeddedBundleCatalog = engine.embeddedBundleCatalog,
        verifier = engine.verifier,
        generator = engine.generator,
        remover = engine.remover,
        manifestResolver = engine.manifestResolver,
        currentVersion = VERSION,
    ).main(args)
}
