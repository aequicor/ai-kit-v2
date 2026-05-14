package io.aequicor.aikit.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import io.aequicor.aikit.cli.util.resolveManifestOrFail
import io.aequicor.aikit.engine.api.ManifestResolver
import io.aequicor.aikit.engine.api.ManifestVerifier
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

/** Validates one or all manifest files and exits non-zero on any error. */
class VerifyCommand(
    private val verifier: ManifestVerifier,
    private val manifestResolver: ManifestResolver,
) : CliktCommand(
    name = "verify",
    help = "Validate a manifest file (or all manifests in .aikit/ with --all)",
) {
    private val manifestArg by argument(
        name = "MANIFEST",
        help = "Path to the manifest file (default: resolved from .aikit/local.properties or .aikit/manifest.json)",
    ).optional()

    private val manifestOpt by option(
        "--manifest",
        help = "Explicit path to the manifest file",
    )
    private val modeOpt by option(
        "--mode",
        help = "Shorthand for --manifest .aikit/manifest.<mode>.json",
    )
    private val all by option(
        "--all",
        help = "Validate every manifest.*.json file found in .aikit/ (and .aikit/manifest.json if present)",
    ).flag()

    override fun run() {
        if (all) {
            if (manifestArg != null || manifestOpt != null || modeOpt != null) {
                throw CliktError("--all cannot be combined with an explicit manifest path or --mode", statusCode = 1)
            }
            verifyAll()
        } else {
            val resolved = resolveManifestOrFail(
                resolver = manifestResolver,
                positionalArg = manifestArg,
                manifestFlag = manifestOpt,
                modeFlag = modeOpt,
                echo = ::echo,
            )
            verifySingle(resolved.manifestPath)
        }
    }

    private fun verifySingle(path: String) {
        verifier.verify(path).onFailure { error ->
            throw CliktError(error.message ?: "Verification failed", statusCode = 1)
        }
        echo("OK")
    }

    private fun verifyAll() {
        val aiKitDir = Path(".aikit")
        if (!SystemFileSystem.exists(aiKitDir)) {
            throw CliktError("Directory '.aikit/' not found — run kit-setup from the project root", statusCode = 1)
        }
        val manifests = SystemFileSystem.list(aiKitDir)
            .filter { path ->
                val name = path.name
                name == "manifest.json" || (name.startsWith("manifest.") && name.endsWith(".json") && name != "manifest.lock.json")
            }
            .sortedBy { it.name }

        if (manifests.isEmpty()) {
            echo("No manifest files found in .aikit/")
            return
        }

        var anyFailed = false
        for (manifestPath in manifests) {
            val result = verifier.verify(manifestPath.toString())
            if (result.isSuccess) {
                echo("${manifestPath.name}: OK")
            } else {
                echo("${manifestPath.name}: ERROR — ${result.exceptionOrNull()?.message ?: "unknown error"}")
                anyFailed = true
            }
        }
        if (anyFailed) throw CliktError("One or more manifests failed validation", statusCode = 1)
    }
}
