package io.aequicor.aikit.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.versionOption
import io.aequicor.aikit.cli.commands.GenerateCommand
import io.aequicor.aikit.cli.commands.SchemaCommand
import io.aequicor.aikit.cli.commands.VerifyCommand
import io.aequicor.aikit.engine.api.BundleGenerator
import io.aequicor.aikit.engine.api.ManifestVerifier
import io.aequicor.aikit.engine.api.SchemaProvider

/** Root CLI command; delegates to [SchemaCommand], [VerifyCommand], [GenerateCommand]. */
class AiKitCli(
    schemaProvider: SchemaProvider,
    verifier: ManifestVerifier,
    generator: BundleGenerator,
) : CliktCommand(name = "kit-setup") {
    init {
        versionOption(VERSION)
        subcommands(
            SchemaCommand(schemaProvider),
            VerifyCommand(verifier),
            GenerateCommand(generator),
        )
    }

    override fun run() = Unit
}
