package io.aequicor.aikit.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.versionOption
import io.aequicor.aikit.cli.commands.GenerateCommand
import io.aequicor.aikit.cli.commands.RemoveCommand
import io.aequicor.aikit.cli.commands.SchemaCommand
import io.aequicor.aikit.cli.commands.UpdateCommand
import io.aequicor.aikit.cli.commands.UpdateSelfSubcommand
import io.aequicor.aikit.cli.commands.VerifyCommand
import io.aequicor.aikit.engine.api.BundleGenerator
import io.aequicor.aikit.engine.api.BundleSchemaProvider
import io.aequicor.aikit.engine.api.EmbeddedBundleCatalog
import io.aequicor.aikit.engine.api.ManifestVerifier
import io.aequicor.aikit.engine.api.SchemaProvider
import io.aequicor.aikit.engine.remove.ProjectRemover

/** Root CLI command; wires subcommands. */
@Suppress("LongParameterList")
class AiKitCli(
    schemaProvider: SchemaProvider,
    bundleSchemaProvider: BundleSchemaProvider,
    embeddedBundleCatalog: EmbeddedBundleCatalog,
    verifier: ManifestVerifier,
    generator: BundleGenerator,
    remover: ProjectRemover,
    currentVersion: String,
) : CliktCommand(name = "kit-setup") {
    init {
        versionOption(currentVersion)
        subcommands(
            SchemaCommand(schemaProvider, bundleSchemaProvider, embeddedBundleCatalog),
            VerifyCommand(verifier),
            GenerateCommand(generator),
            RemoveCommand(remover),
            UpdateCommand(generator).subcommands(UpdateSelfSubcommand(currentVersion)),
        )
    }

    override fun run() = Unit
}
