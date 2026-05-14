package io.aequicor.aikit.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option

private const val RELEASES_URL = "https://github.com/aequicor/ai-kit-v2/releases"
private const val LATEST_URL = "https://github.com/aequicor/ai-kit-v2/releases/latest"

/**
 * Self-update entry point for the CLI binary.
 *
 * The native CLI cannot atomically replace its own executable across all three target platforms
 * without bundling an HTTP client and platform-specific dance (especially on Windows where the
 * running .exe is locked). Until that lands as a dedicated module, this subcommand prints the
 * appropriate manual upgrade command for the host platform.
 */
class UpdateSelfSubcommand(private val currentVersion: String) : CliktCommand(
    name = "self",
    help = "Update the kit-setup CLI itself",
) {
    private val check by option(
        "--check",
        help = "Only print the current version and the link to the latest release",
    ).flag()

    override fun run() {
        echo("kit-setup current version: $currentVersion")
        if (check) {
            echo("Latest release: $LATEST_URL")
            return
        }
        echo("")
        echo("Automatic binary replacement is not yet available — please run one of the following:")
        echo("")
        echo("  Linux / macOS:")
        echo("    curl -sSL $LATEST_URL/download/kit-setup-\$(uname -s)-\$(uname -m) -o kit-setup && chmod +x kit-setup")
        echo("")
        echo("  Windows (PowerShell):")
        echo("    iwr -Uri $LATEST_URL/download/kit-setup-Windows-x64.exe -OutFile kit-setup.exe")
        echo("")
        echo("Full list of artifacts: $RELEASES_URL")
    }
}
