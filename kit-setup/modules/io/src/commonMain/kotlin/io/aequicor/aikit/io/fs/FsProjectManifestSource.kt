package io.aequicor.aikit.io.fs

import io.aequicor.aikit.io.BundleSource
import io.aequicor.aikit.io.BundleSourceFactory
import io.aequicor.aikit.io.ProjectManifestSource
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

/**
 * Reads `.aikit/manifest.json` from a project directory and resolves bundle references relative
 * to that project root.
 *
 * Reference syntax (relaxed for the first iteration):
 * - relative or absolute path to a bundle directory → [FsBundleSource]
 * - other forms (`zip:…`, `embedded:…`) fail until ZipBundleSource / EmbeddedBundleSource land.
 *
 * @property manifestPath path to `.aikit/manifest.json` (typically `<project>/.aikit/manifest.json`).
 * @property projectRoot directory used to resolve relative bundle references.
 *   Defaults to the parent of [manifestPath]'s parent (i.e. the project root, not the `.aikit`
 *   folder), since manifests live one level below their owning project.
 */
class FsProjectManifestSource(
    private val manifestPath: Path,
    override val projectRoot: Path = defaultProjectRoot(manifestPath),
    private val factory: BundleSourceFactory = BundleSourceFactory(),
) : ProjectManifestSource {

    override fun openManifest(): Result<Source> = runCatching {
        SystemFileSystem.source(manifestPath).buffered()
    }

    override fun resolveBundleSource(bundleRef: String): Result<BundleSource> =
        factory.create(bundleRef, projectRoot)

    private companion object {
        fun defaultProjectRoot(manifestPath: Path): Path =
            manifestPath.parent?.parent ?: error("manifest path has no parent: $manifestPath")
    }
}
