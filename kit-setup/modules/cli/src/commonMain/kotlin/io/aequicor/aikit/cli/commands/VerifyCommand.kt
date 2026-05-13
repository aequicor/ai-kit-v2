package io.aequicor.aikit.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.arguments.argument
import io.aequicor.aikit.engine.api.ManifestVerifier

/** Validates the manifest at [manifest] and exits non-zero on any error. */
class VerifyCommand(private val verifier: ManifestVerifier) : CliktCommand(
    name = "verify",
    help = "Validate a manifest file",
) {
    private val manifest by argument(
        name = "MANIFEST",
        help = "Path to the manifest file (e.g. .aikit/manifest.yaml)",
    )

    override fun run() {
        verifier.verify(manifest).onFailure { error ->
            throw CliktError(error.message ?: "Verification failed", statusCode = 1)
        }
        echo("OK")
    }
}
