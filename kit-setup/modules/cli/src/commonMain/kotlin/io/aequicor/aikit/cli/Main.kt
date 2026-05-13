package io.aequicor.aikit.cli

import io.aequicor.aikit.core.api.BundleGenerator
import io.aequicor.aikit.core.api.ManifestVerifier
import io.aequicor.aikit.core.api.SchemaProvider

/** Current CLI version; keep in sync with gradle.properties. */
internal const val VERSION = "0.0.3"

fun main(args: Array<String>) {
    AiKitCli(
        schemaProvider = SchemaProvider { TODO("SchemaProvider not yet implemented") },
        verifier = ManifestVerifier { TODO("ManifestVerifier not yet implemented") },
        generator = BundleGenerator { TODO("BundleGenerator not yet implemented") },
    ).main(args)
}
