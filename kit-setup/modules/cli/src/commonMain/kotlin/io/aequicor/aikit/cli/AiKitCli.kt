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
import io.aequicor.aikit.engine.api.BundleCatalog
import io.aequicor.aikit.engine.api.ManifestResolver
import io.aequicor.aikit.engine.api.ManifestVerifier
import io.aequicor.aikit.engine.api.SchemaProvider
import io.aequicor.aikit.engine.remove.ProjectRemover

/** Root CLI command; wires subcommands. */
@Suppress("LongParameterList")
class AiKitCli(
    schemaProvider: SchemaProvider,
    bundleSchemaProvider: BundleSchemaProvider,
    bundleCatalog: BundleCatalog,
    verifier: ManifestVerifier,
    generator: BundleGenerator,
    remover: ProjectRemover,
    manifestResolver: ManifestResolver,
    currentVersion: String,
) : CliktCommand(name = "kit-setup") {
    init {
        versionOption(currentVersion)
        subcommands(
            SchemaCommand(schemaProvider, bundleSchemaProvider, bundleCatalog),
            VerifyCommand(verifier, manifestResolver),
            GenerateCommand(generator, manifestResolver),
            RemoveCommand(remover, manifestResolver),
            UpdateCommand(generator, manifestResolver).subcommands(UpdateSelfSubcommand(currentVersion)),
        )
    }

    override fun run() = Unit
}
