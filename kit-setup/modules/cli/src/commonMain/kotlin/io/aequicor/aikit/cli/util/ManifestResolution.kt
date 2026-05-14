package io.aequicor.aikit.cli.util

import com.github.ajalt.clikt.core.CliktError
import io.aequicor.aikit.engine.api.ManifestResolveError
import io.aequicor.aikit.engine.api.ManifestResolver
import io.aequicor.aikit.engine.api.ManifestSource
import io.aequicor.aikit.engine.api.ResolveRequest
import io.aequicor.aikit.engine.api.ResolvedManifest
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString

/**
 * Resolves the active manifest from CLI inputs and prints an audit-trail line.
 *
 * Priority: [positionalArg] / [manifestFlag] (explicit) → [modeFlag] → env vars → local.properties → legacy default.
 *
 * @throws CliktError on any resolution failure.
 */
@Suppress("CyclomaticComplexMethod")
internal fun resolveManifestOrFail(
    resolver: ManifestResolver,
    positionalArg: String?,
    manifestFlag: String?,
    modeFlag: String?,
    echo: (String) -> Unit,
): ResolvedManifest {
    val projectRoot = runCatching {
        SystemFileSystem.resolve(Path(".")).toString()
    }.getOrDefault(".")

    val explicit = positionalArg ?: manifestFlag

    val resolved = resolver.resolve(
        ResolveRequest(
            projectRoot = projectRoot,
            explicitManifestArg = explicit,
            explicitModeFlag = modeFlag,
        ),
    ).getOrElse { error ->
        val msg = when (error) {
            is ManifestResolveError.ManifestNotFound ->
                "Manifest not found: '${error.path}'"
            is ManifestResolveError.NoManifestConfigured ->
                "No manifest configured. Create '.aikit/manifest.json', set AIKIT_MANIFEST, " +
                    "or add 'manifest=<path>' to '.aikit/local.properties'."
            is ManifestResolveError.ConflictingArgs ->
                error.message ?: "Conflicting manifest arguments"
            else -> error.message ?: "Cannot resolve manifest"
        }
        throw CliktError(msg, statusCode = 1)
    }

    if (resolved.source != ManifestSource.LEGACY_DEFAULT) {
        val sourceLabel = when (resolved.source) {
            ManifestSource.EXPLICIT_ARG -> "explicit arg"
            ManifestSource.MODE_FLAG -> "--mode ${resolved.modeHint}"
            ManifestSource.ENV_MANIFEST -> "env AIKIT_MANIFEST"
            ManifestSource.ENV_MODE -> "env AIKIT_MODE=${resolved.modeHint}"
            ManifestSource.LOCAL_PROPERTIES -> ".aikit/local.properties"
            ManifestSource.LEGACY_DEFAULT -> ""
        }
        echo("Manifest: ${resolved.manifestRef} (from $sourceLabel)")
    }

    warnIfLocalPropertiesNotIgnored(projectRoot, echo)

    return resolved
}

@Suppress("ReturnCount")
private fun warnIfLocalPropertiesNotIgnored(projectRoot: String, echo: (String) -> Unit) {
    val localProps = Path(projectRoot, ".aikit/local.properties")
    if (!SystemFileSystem.exists(localProps)) return

    val gitignore = Path(projectRoot, ".gitignore")
    if (!SystemFileSystem.exists(gitignore)) {
        echo("Tip: add \".aikit/local.properties\" to .gitignore — it's a per-developer file.")
        return
    }

    val content = runCatching {
        SystemFileSystem.source(gitignore).buffered().use { it.readString() }
    }.getOrNull() ?: return

    val covered = content.lines().any { line ->
        val trimmed = line.trim()
        trimmed == "local.properties" ||
            trimmed == ".aikit/local.properties" ||
            trimmed == ".aikit/*.properties" ||
            trimmed == "*.properties"
    }
    if (!covered) {
        echo("Tip: add \".aikit/local.properties\" to .gitignore — it's a per-developer file.")
    }
}
