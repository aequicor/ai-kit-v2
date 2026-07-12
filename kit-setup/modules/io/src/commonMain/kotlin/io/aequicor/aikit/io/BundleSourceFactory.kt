package io.aequicor.aikit.io

import io.aequicor.aikit.io.fs.FsBundleSource
import io.aequicor.aikit.io.process.DefaultProcessRunner
import io.aequicor.aikit.io.process.ProcessRunner
import io.aequicor.aikit.io.remote.RemoteBundleRef
import io.aequicor.aikit.io.remote.RemoteBundleSource
import io.aequicor.aikit.io.zip.ZipBundleSource
import kotlinx.io.files.Path

/**
 * Chooses the right [BundleSource] implementation for a bundle reference string.
 *
 * Reference forms recognised:
 * - `embedded:<name>` — rejected with migration instructions.
 * - `remote:<owner>/<repo>/<path>[@<branch>]` — bundle in a GitHub repository, downloaded
 *   into `<baseDir>/.aikit/cache/bundles/` on first read (see [RemoteBundleSource]).
 * - `zip:<path>` or `<path>.zip` — packed bundle on disk.
 * - anything else — directory path on disk (resolved against [baseDir] if relative).
 *
 * @property processRunner launcher for the git commands behind `remote:`; injectable for tests.
 */
class BundleSourceFactory(
    private val processRunner: ProcessRunner = DefaultProcessRunner(),
) {

    /**
     * Resolve [ref] to a concrete source.
     *
     * @param ref reference string as it appears in `.aikit/manifest.json` or on the CLI.
     * @param baseDir directory used to resolve relative paths; also the project root that
     *   hosts the `.aikit/cache/` directory for remote bundles.
     */
    fun create(ref: String, baseDir: Path): Result<BundleSource> = runCatching {
        when {
            ref.startsWith(EMBEDDED_SCHEME) -> throw IllegalArgumentException(SourceRefs.LEGACY_SOURCE_MESSAGE)
            RemoteBundleRef.matches(ref) -> RemoteBundleSource(
                ref = RemoteBundleRef.parse(ref).getOrThrow(),
                cacheRoot = Path(baseDir, CACHE_RELATIVE_DIR),
                runner = processRunner,
            )
            ref.startsWith(ZIP_SCHEME) -> ZipBundleSource(resolvePath(ref.removePrefix(ZIP_SCHEME), baseDir))
            ref.endsWith(ZIP_SUFFIX) -> ZipBundleSource(resolvePath(ref, baseDir))
            else -> FsBundleSource(resolvePath(ref, baseDir))
        }
    }

    private fun resolvePath(raw: String, baseDir: Path): Path {
        val candidate = Path(raw)
        if (isAbsolute(candidate)) return candidate
        return Path(baseDir, raw)
    }

    private fun isAbsolute(path: Path): Boolean {
        val raw = path.toString()
        return raw.startsWith('/') || (raw.length >= WINDOWS_DRIVE_PREFIX_LENGTH && raw[1] == ':')
    }

    private companion object {
        const val EMBEDDED_SCHEME = "embedded:"
        const val ZIP_SCHEME = "zip:"
        const val ZIP_SUFFIX = ".zip"
        const val CACHE_RELATIVE_DIR = ".aikit/cache/bundles"
        const val WINDOWS_DRIVE_PREFIX_LENGTH = 2
    }
}
