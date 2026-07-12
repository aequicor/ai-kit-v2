package io.aequicor.aikit.io.remote

import io.aequicor.aikit.io.process.ProcessResult
import io.aequicor.aikit.io.process.ProcessRunner
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val FAKE_SHA = "0123456789abcdef0123456789abcdef01234567"

class RemoteBundleSourceTest {

    private lateinit var tmp: Path
    private lateinit var cacheRoot: Path

    @BeforeTest
    fun setUp() {
        tmp = Path("build/remote-test-${Random.nextInt(1_000_000)}")
        cacheRoot = Path(tmp, "cache")
        SystemFileSystem.createDirectories(tmp)
    }

    @AfterTest
    fun tearDown() {
        deleteRecursively(tmp)
    }

    /**
     * Fake git: `ls-remote` prints a fixed sha, `clone` materialises a fake working tree
     * (with `my-bundle/bundle.json`) at the destination path.
     */
    private class FakeGit : ProcessRunner {
        var lsRemoteCalls = 0
        var cloneCalls = 0

        override fun run(command: List<String>): Result<ProcessResult> = runCatching {
            when (command.getOrNull(1)) {
                "ls-remote" -> {
                    lsRemoteCalls++
                    ProcessResult(0, "$FAKE_SHA\trefs/heads/main\n")
                }
                "clone" -> {
                    cloneCalls++
                    val dest = Path(command.last())
                    val bundleDir = Path(dest, "my-bundle")
                    SystemFileSystem.createDirectories(bundleDir)
                    writeFile(
                        Path(bundleDir, "bundle.json"),
                        """{"name":"my-bundle","version":"0.1.0"}""",
                    )
                    ProcessResult(0, "")
                }
                "-C" -> ProcessResult(0, "")
                else -> error("unexpected command: $command")
            }
        }
    }

    private fun ref(): RemoteBundleRef =
        RemoteBundleRef.parse("remote:acme/kits/my-bundle@main").getOrThrow()

    @Test
    fun downloadsOnFirstRead_andExposesSha() {
        val git = FakeGit()
        val source = RemoteBundleSource(ref(), cacheRoot, git)

        val manifest = source.openManifest().getOrThrow().use { it.readString() }

        assertEquals("""{"name":"my-bundle","version":"0.1.0"}""", manifest)
        assertEquals(FAKE_SHA, source.resolvedSha)
        assertEquals(1, git.cloneCalls)
    }

    @Test
    fun reusesCache_whenShaAlreadyMaterialised() {
        val git = FakeGit()
        RemoteBundleSource(ref(), cacheRoot, git).use { it.openManifest().getOrThrow().close() }

        val second = FakeGit()
        RemoteBundleSource(ref(), cacheRoot, second).use { source ->
            source.openManifest().getOrThrow().close()
            assertEquals(FAKE_SHA, source.resolvedSha)
        }

        assertEquals(1, second.lsRemoteCalls)
        assertEquals(0, second.cloneCalls, "cache hit must not clone again")
    }

    @Test
    fun failsClearly_whenBundlePathMissingInRepo() {
        val git = FakeGit()
        val badRef = RemoteBundleRef.parse("remote:acme/kits/no-such-dir@main").getOrThrow()
        val source = RemoteBundleSource(badRef, cacheRoot, git)

        val result = source.openManifest()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("no-such-dir"))
    }

    @Test
    fun failsClearly_whenGitUnavailable() {
        val noGit = ProcessRunner { Result.success(ProcessResult(127, "git: command not found")) }
        val source = RemoteBundleSource(ref(), cacheRoot, noGit)

        val result = source.openManifest()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("git"))
    }
}

private fun writeFile(path: Path, content: String) {
    SystemFileSystem.sink(path).buffered().use { it.writeString(content) }
}

private fun deleteRecursively(path: Path) {
    val meta = SystemFileSystem.metadataOrNull(path) ?: return
    if (meta.isDirectory) {
        SystemFileSystem.list(path).forEach(::deleteRecursively)
    }
    SystemFileSystem.delete(path, mustExist = false)
}
