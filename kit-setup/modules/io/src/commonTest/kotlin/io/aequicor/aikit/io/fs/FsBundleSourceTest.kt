package io.aequicor.aikit.io.fs

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FsBundleSourceTest {

    private lateinit var tmp: Path

    @BeforeTest
    fun setUp() {
        tmp = Path("build/io-test-${kotlin.random.Random.nextInt(1_000_000)}")
        SystemFileSystem.createDirectories(tmp)
    }

    @AfterTest
    fun tearDown() {
        deleteRecursively(tmp)
    }

    @Test
    fun openManifest_returnsContent() {
        writeFile(Path(tmp, "bundle.json"), """{"name":"demo"}""")

        val src = FsBundleSource(tmp)
        val text = src.openManifest().getOrThrow().use { it.readString() }

        assertEquals("""{"name":"demo"}""", text)
    }

    @Test
    fun openTargetConfig_returnsNull_whenMissing() {
        writeFile(Path(tmp, "bundle.json"), "{}")
        SystemFileSystem.createDirectories(Path(tmp, "claude"))

        val src = FsBundleSource(tmp)

        assertNull(src.openTargetConfig("claude").getOrThrow())
    }

    @Test
    fun openTargetConfig_returnsContent_whenPresent() {
        writeFile(Path(tmp, "bundle.json"), "{}")
        SystemFileSystem.createDirectories(Path(tmp, "claude"))
        writeFile(Path(Path(tmp, "claude"), "config.json"), """{"v":1}""")

        val src = FsBundleSource(tmp)
        val text = src.openTargetConfig("claude").getOrThrow()?.use { it.readString() }

        assertEquals("""{"v":1}""", text)
    }

    @Test
    fun listTargetFiles_returnsRelativePathsRecursively() {
        writeFile(Path(tmp, "bundle.json"), "{}")
        val target = Path(tmp, "claude")
        SystemFileSystem.createDirectories(target)
        SystemFileSystem.createDirectories(Path(target, "commands"))
        writeFile(Path(target, "CLAUDE.md"), "root")
        writeFile(Path(Path(target, "commands"), "review.md"), "cmd")

        val files = FsBundleSource(tmp).listTargetFiles("claude").getOrThrow()

        assertEquals(listOf("CLAUDE.md", "commands/review.md"), files)
    }

    @Test
    fun openTargetFile_resolvesRelativePath() {
        writeFile(Path(tmp, "bundle.json"), "{}")
        SystemFileSystem.createDirectories(Path(Path(tmp, "claude"), "commands"))
        writeFile(Path(Path(Path(tmp, "claude"), "commands"), "review.md"), "hello")

        val src = FsBundleSource(tmp)
        val text = src.openTargetFile("claude", "commands/review.md").getOrThrow().use { it.readString() }

        assertEquals("hello", text)
    }

    @Test
    fun listTargetFiles_fails_whenTargetMissing() {
        writeFile(Path(tmp, "bundle.json"), "{}")
        val result = FsBundleSource(tmp).listTargetFiles("missing")
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    private fun writeFile(path: Path, content: String) {
        path.parent?.let { SystemFileSystem.createDirectories(it) }
        SystemFileSystem.sink(path).buffered().use { it.writeString(content) }
    }

    private fun deleteRecursively(path: Path) {
        val meta = SystemFileSystem.metadataOrNull(path) ?: return
        if (meta.isDirectory) {
            for (child in SystemFileSystem.list(path)) deleteRecursively(child)
        }
        SystemFileSystem.delete(path, mustExist = false)
    }
}
