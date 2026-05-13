package io.aequicor.aikit.cli

import io.aequicor.aikit.engine.AiKitEngine

/** Current CLI version; keep in sync with gradle.properties. */
internal const val VERSION = "0.0.6"

fun main(args: Array<String>) {
    val engine = AiKitEngine.create()
    AiKitCli(
        schemaProvider = engine.schemaProvider,
        verifier = engine.verifier,
        generator = engine.generator,
    ).main(args)
}
