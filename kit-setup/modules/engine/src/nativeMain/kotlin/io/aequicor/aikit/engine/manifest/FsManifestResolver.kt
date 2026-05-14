@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.aequicor.aikit.engine.manifest

import io.aequicor.aikit.engine.api.ManifestResolveError
import io.aequicor.aikit.engine.api.ManifestResolver
import io.aequicor.aikit.engine.api.ManifestSource
import io.aequicor.aikit.engine.api.ResolveRequest
import io.aequicor.aikit.engine.api.ResolvedManifest
import kotlinx.cinterop.toKString
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import platform.posix.getenv

private const val ENV_MANIFEST = "AIKIT_MANIFEST"
private const val ENV_MODE = "AIKIT_MODE"
private const val LOCAL_PROPS_FILE = ".aikit/local.properties"
private const val LOCAL_PROPS_KEY = "manifest"
private fun manifestPath(mode: String) = ".aikit/manifest.$mode.json"
private const val LEGACY_MANIFEST = ".aikit/manifest.json"

/**
 * Filesystem-backed [ManifestResolver].
 *
 * Reads env vars via `platform.posix.getenv` (works on all four native targets:
 * linuxX64, macosX64, macosArm64, mingwX64) and reads `.aikit/local.properties`
 * via [SystemFileSystem] — same approach used by [FsValueSourceReader][io.aequicor.aikit.engine.pipeline.FsValueSourceReader].
 */
internal class FsManifestResolver : ManifestResolver {

    override fun resolve(request: ResolveRequest): Result<ResolvedManifest> = runCatching {
        val root = request.projectRoot

        if (request.explicitManifestArg != null && request.explicitModeFlag != null) {
            throw ManifestResolveError.ConflictingArgs(
                "cannot combine an explicit manifest path ('${request.explicitManifestArg}') " +
                    "with --mode ('${request.explicitModeFlag}')",
            )
        }

        // 1. Explicit --manifest / positional MANIFEST arg
        request.explicitManifestArg?.let { arg ->
            return@runCatching toResolved(root, arg, source = ManifestSource.EXPLICIT_ARG)
        }

        // 2. --mode flag
        request.explicitModeFlag?.let { mode ->
            val path = manifestPath(mode)
            return@runCatching toResolved(root, path, modeHint = mode, source = ManifestSource.MODE_FLAG)
        }

        // 3. AIKIT_MANIFEST env var
        getenv(ENV_MANIFEST)?.toKString()?.takeIf { it.isNotBlank() }?.let { envPath ->
            return@runCatching toResolved(root, envPath, source = ManifestSource.ENV_MANIFEST)
        }

        // 4. AIKIT_MODE env var
        getenv(ENV_MODE)?.toKString()?.takeIf { it.isNotBlank() }?.let { mode ->
            val path = manifestPath(mode)
            return@runCatching toResolved(root, path, modeHint = mode, source = ManifestSource.ENV_MODE)
        }

        // 5. .aikit/local.properties — manifest= field
        readLocalProperties(root)[LOCAL_PROPS_KEY]?.takeIf { it.isNotBlank() }?.let { localPath ->
            return@runCatching toResolved(root, localPath, source = ManifestSource.LOCAL_PROPERTIES)
        }

        // 6. Legacy .aikit/manifest.json
        val legacyPath = Path(root, LEGACY_MANIFEST)
        if (SystemFileSystem.exists(legacyPath)) {
            return@runCatching ResolvedManifest(
                manifestPath = SystemFileSystem.resolve(legacyPath).toString(),
                manifestRef = LEGACY_MANIFEST,
                modeHint = null,
                source = ManifestSource.LEGACY_DEFAULT,
            )
        }

        // 7. Nothing found
        throw ManifestResolveError.NoManifestConfigured(root)
    }

    private fun toResolved(
        root: String,
        rawPath: String,
        modeHint: String? = null,
        source: ManifestSource,
    ): ResolvedManifest {
        val isAbsolute = rawPath.startsWith('/') || (rawPath.length >= 3 && rawPath[1] == ':')
        val fsPath = if (isAbsolute) Path(rawPath) else Path(root, rawPath)
        if (!SystemFileSystem.exists(fsPath)) {
            throw ManifestResolveError.ManifestNotFound(rawPath)
        }
        return ResolvedManifest(
            manifestPath = SystemFileSystem.resolve(fsPath).toString(),
            manifestRef = rawPath.replace('\\', '/'),
            modeHint = modeHint,
            source = source,
        )
    }

    private fun readLocalProperties(root: String): Map<String, String> {
        val file = Path(root, LOCAL_PROPS_FILE)
        if (!SystemFileSystem.exists(file)) return emptyMap()
        val text = SystemFileSystem.source(file).buffered().use { it.readString() }
        return text.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith('#') }
            .mapNotNull { line ->
                val idx = line.indexOf('=')
                if (idx < 0) return@mapNotNull null
                line.substring(0, idx).trim() to line.substring(idx + 1).trim()
            }
            .toMap()
    }
}

/** Creates a production [ManifestResolver] backed by the real filesystem and process environment. */
internal actual fun createFsManifestResolver(): ManifestResolver = FsManifestResolver()
