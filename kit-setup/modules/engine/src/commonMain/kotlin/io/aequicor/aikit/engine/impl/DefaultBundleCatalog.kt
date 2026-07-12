package io.aequicor.aikit.engine.impl

import io.aequicor.aikit.core.domain.bundle.BundleCompatibility
import io.aequicor.aikit.core.domain.targets.ClaudeCode
import io.aequicor.aikit.core.domain.targets.Codex
import io.aequicor.aikit.core.domain.targets.OpenCode
import io.aequicor.aikit.core.domain.targets.QwenCode
import io.aequicor.aikit.engine.api.BundleCatalog
import io.aequicor.aikit.engine.api.BundleCatalogInfo
import io.aequicor.aikit.engine.api.BundleCatalogResult
import io.aequicor.aikit.format.AiKitFormat
import io.aequicor.aikit.io.fs.FsBundleSource
import io.aequicor.aikit.io.process.DefaultProcessRunner
import io.aequicor.aikit.io.process.ProcessRunner
import io.aequicor.aikit.io.remote.RemoteBundleRef
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString

internal class DefaultBundleCatalog(
    private val format: AiKitFormat,
    private val currentVersion: String,
    private val runner: ProcessRunner = DefaultProcessRunner(),
    private val localRepositoryRoot: Path? = null,
) : BundleCatalog {
    override fun list(baseDir: String, includeIncompatible: Boolean): Result<BundleCatalogResult> = runCatching {
        val cacheRoot = Path(Path(baseDir.ifBlank { "." }), CACHE_DIR)
        val materialized = materialize(cacheRoot)
        val bundlesRoot = Path(materialized.root, "bundles")
        check(SystemFileSystem.metadataOrNull(bundlesRoot)?.isDirectory == true) {
            "official bundle catalog has no bundles/ directory"
        }
        val infos = scan(bundlesRoot).filter { includeIncompatible || it.compatible }
            .sortedWith(compareBy<BundleCatalogInfo> { it.name }.thenByDescending { it.version })
        BundleCatalogResult(currentVersion, materialized.stale, infos)
    }

    private fun scan(root: Path): List<BundleCatalogInfo> = buildList {
        for (nameDir in SystemFileSystem.list(root).sortedBy { it.name }) {
            if (SystemFileSystem.metadataOrNull(nameDir)?.isDirectory != true) continue
            for (versionDir in SystemFileSystem.list(nameDir).sortedBy { it.name }) {
                if (SystemFileSystem.metadataOrNull(versionDir)?.isDirectory != true) continue
                val parsed = FsBundleSource(versionDir).use { format.parseBundleManifest(it).getOrThrow() }
                val manifest = parsed.manifest
                check(manifest.name == nameDir.name && manifest.version == versionDir.name) {
                    "catalog path '${nameDir.name}/${versionDir.name}' does not match " +
                        "bundle '${manifest.name}@${manifest.version}'"
                }
                val incompatibility = BundleCompatibility.incompatibility(manifest, currentVersion)
                add(
                    BundleCatalogInfo(
                        name = manifest.name,
                        version = manifest.version,
                        source = RemoteBundleRef.defaultFor("${manifest.name}@${manifest.version}")
                            .getOrThrow().toRefString(),
                        description = manifest.description,
                        targets = manifest.targets.map {
                            when (it) {
                                is ClaudeCode -> "claude-code"
                                is OpenCode -> "opencode"
                                is QwenCode -> "qwen-code"
                                is Codex -> "codex"
                            }
                        },
                        tags = manifest.tags,
                        bestFor = manifest.bestFor,
                        notFor = manifest.notFor,
                        kitSetup = manifest.kitSetup.orEmpty(),
                        compatible = incompatibility == null,
                        incompatibility = incompatibility,
                    )
                )
            }
        }
    }

    @Suppress("ReturnCount")
    private fun materialize(cacheRoot: Path): Materialized {
        localRepositoryRoot?.let { return Materialized(it, stale = false) }
        SystemFileSystem.createDirectories(cacheRoot)
        val remote = runner.run(listOf("git", "ls-remote", REPO_URL, "refs/heads/main")).getOrNull()
        val sha = remote?.takeIf { it.exitCode == 0 }?.output?.trim()?.split(Regex("""\s+"""))?.firstOrNull()
            ?.takeIf { COMMIT.matches(it) }
        if (sha != null) {
            val repoRoot = Path(cacheRoot, sha)
            if (SystemFileSystem.metadataOrNull(Path(repoRoot, "bundles"))?.isDirectory != true) {
                val staging = Path(cacheRoot, "$sha-tmp")
                deleteRecursively(staging)
                val clone = runner.run(
                    listOf("git", "clone", "--depth", "1", "--branch", "main", REPO_URL, staging.toString())
                ).getOrThrow()
                check(clone.exitCode == 0) { "cannot download official bundle catalog: ${clone.output.trim()}" }
                SystemFileSystem.atomicMove(staging, repoRoot)
            }
            SystemFileSystem.sink(Path(cacheRoot, POINTER)).buffered().use { it.writeString(sha) }
            return Materialized(repoRoot, stale = false)
        }

        val pointer = Path(cacheRoot, POINTER)
        val cachedSha = if (SystemFileSystem.metadataOrNull(pointer)?.isRegularFile == true) {
            SystemFileSystem.source(pointer).buffered().use { it.readString().trim() }
        } else null
        val cachedRoot = cachedSha?.let { Path(cacheRoot, it) }
        check(cachedRoot != null && SystemFileSystem.metadataOrNull(Path(cachedRoot, "bundles"))?.isDirectory == true) {
            "cannot reach the official bundle catalog and no cached catalog is available"
        }
        return Materialized(cachedRoot, stale = true)
    }

    private fun deleteRecursively(path: Path) {
        val metadata = SystemFileSystem.metadataOrNull(path) ?: return
        if (metadata.isDirectory) SystemFileSystem.list(path).forEach(::deleteRecursively)
        SystemFileSystem.delete(path, mustExist = false)
    }

    private data class Materialized(val root: Path, val stale: Boolean)

    private companion object {
        const val REPO_URL = "https://github.com/aequicor/ai-kit-v2.git"
        const val CACHE_DIR = ".aikit/cache/catalog"
        const val POINTER = "current"
        val COMMIT = Regex("""[0-9a-f]{40}""")
    }
}
