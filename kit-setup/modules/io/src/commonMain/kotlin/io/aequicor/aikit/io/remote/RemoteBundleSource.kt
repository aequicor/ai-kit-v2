package io.aequicor.aikit.io.remote

import io.aequicor.aikit.io.BundleSource
import io.aequicor.aikit.io.fs.FsBundleSource
import io.aequicor.aikit.io.process.DefaultProcessRunner
import io.aequicor.aikit.io.process.ProcessRunner
import io.aequicor.aikit.io.process.processPath
import kotlinx.io.Source
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

/**
 * [BundleSource] for a bundle hosted in a GitHub repository.
 *
 * Lazy download-on-first-read:
 * 1. Resolve the tip commit of the tracked branch via `git ls-remote` — the bundle follows
 *    the branch, and the resolved sha is recorded in `manifest.lock.json` for reproducibility.
 * 2. If `<cacheRoot>/<owner>__<repo>__<sha>/` is absent, `git clone --depth 1` the branch
 *    into it (via a `-tmp` staging directory so an interrupted clone never looks valid).
 * 3. Serve all reads from the cached working tree through [FsBundleSource].
 *
 * Requires a `git` executable on `PATH`. All coordinates are validated by [RemoteBundleRef],
 * which is the safety barrier for the process invocations below.
 *
 * @property ref validated bundle coordinates.
 * @property cacheRoot directory for cached downloads, typically `<project>/.aikit/cache/bundles`.
 * @property runner process launcher; injectable for tests.
 */
class RemoteBundleSource(
    private val ref: RemoteBundleRef,
    private val cacheRoot: Path,
    private val runner: ProcessRunner = DefaultProcessRunner(),
) : BundleSource {

    private var delegate: FsBundleSource? = null
    private var sha: String? = null
    private var closed = false

    /**
     * Commit sha the bundle was materialised from. Non-null only after the first successful
     * read operation ([openManifest] etc.); recorded in the lock file by the generator.
     */
    val resolvedSha: String? get() = sha

    override fun openManifest(): Result<Source> = withDelegate { it.openManifest() }

    override fun openTargetConfig(targetFolder: String): Result<Source?> =
        withDelegate { it.openTargetConfig(targetFolder) }

    override fun listTargetFiles(targetFolder: String): Result<List<String>> =
        withDelegate { it.listTargetFiles(targetFolder) }

    override fun openTargetFile(targetFolder: String, relativePath: String): Result<Source> =
        withDelegate { it.openTargetFile(targetFolder, relativePath) }

    override fun close() {
        closed = true
        delegate = null
    }

    private fun <T> withDelegate(block: (FsBundleSource) -> Result<T>): Result<T> = when {
        closed -> Result.failure(IllegalStateException("bundle source is closed"))
        delegate != null -> block(delegate!!)
        else -> materialise().mapCatching { block(it).getOrThrow() }
    }

    private fun materialise(): Result<FsBundleSource> = runCatching {
        val commit = resolveBranchTip().getOrThrow()
        val repoDir = Path(cacheRoot, "${ref.owner}__${ref.repo}__$commit")
        val bundleDir = ref.path.split('/').fold(repoDir) { acc, segment -> Path(acc, segment) }

        if (!isValidCacheEntry(bundleDir)) {
            download(repoDir)
            check(isValidCacheEntry(bundleDir)) {
                "bundle path '${ref.path}' not found in ${ref.repoUrl} (branch '${ref.branch}')"
            }
        }

        sha = commit
        FsBundleSource(bundleDir).also { delegate = it }
    }

    private fun resolveBranchTip(): Result<String> = runCatching {
        val result = runner
            .run(listOf("git", "ls-remote", ref.repoUrl, "refs/heads/${ref.branch}"))
            .getOrThrow()
        check(result.exitCode == 0) {
            "git ls-remote failed for ${ref.repoUrl} (is git installed and the network up?): " +
                result.output.trim()
        }
        val commit = result.output.trim().split(WHITESPACE).firstOrNull().orEmpty()
        check(COMMIT_SHA_PATTERN.matches(commit)) {
            "branch '${ref.branch}' not found in ${ref.repoUrl}"
        }
        commit
    }

    private fun download(repoDir: Path) {
        val staging = Path(repoDir.parent ?: cacheRoot, "${repoDir.name}-tmp")
        deleteRecursively(staging)
        SystemFileSystem.createDirectories(staging.parent ?: cacheRoot)

        val result = runner
            .run(
                listOf(
                    "git", "clone",
                    "--depth", "1",
                    "--filter=blob:none",
                    "--sparse",
                    "--branch", ref.branch,
                    ref.repoUrl,
                    processPath(staging.toString()),
                ),
            )
            .getOrThrow()
        check(result.exitCode == 0) {
            "git clone failed for ${ref.repoUrl} (branch '${ref.branch}'): ${result.output.trim()}"
        }
        runGit(staging, listOf("config", "core.longpaths", "true"))
        runGit(staging, listOf("sparse-checkout", "set", ref.path))

        deleteRecursively(repoDir)
        SystemFileSystem.atomicMove(staging, repoDir)
    }

    private fun runGit(repository: Path, arguments: List<String>) {
        val result = runner.run(listOf("git", "-C", processPath(repository.toString())) + arguments)
            .getOrThrow()
        check(result.exitCode == 0) { "git ${arguments.first()} failed: ${result.output.trim()}" }
    }

    private fun isValidCacheEntry(bundleDir: Path): Boolean {
        val manifest = Path(bundleDir, BUNDLE_MANIFEST_FILENAME)
        return SystemFileSystem.metadataOrNull(manifest)?.isRegularFile == true
    }

    private fun deleteRecursively(path: Path) {
        val meta = SystemFileSystem.metadataOrNull(path) ?: return
        if (meta.isDirectory) {
            SystemFileSystem.list(path).forEach(::deleteRecursively)
        }
        SystemFileSystem.delete(path, mustExist = false)
    }

    private companion object {
        const val BUNDLE_MANIFEST_FILENAME = "bundle.json"
        val COMMIT_SHA_PATTERN = Regex("""[0-9a-f]{40}""")
        val WHITESPACE = Regex("""\s+""")
    }
}
