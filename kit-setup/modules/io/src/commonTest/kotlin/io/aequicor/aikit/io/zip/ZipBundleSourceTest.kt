package io.aequicor.aikit.io.zip

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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ZipBundleSourceTest {

    private lateinit var tmp: Path
    private lateinit var archivePath: Path

    @BeforeTest
    fun setUp() {
        tmp = Path("build/zip-test-${Random.nextInt(1_000_000)}")
        SystemFileSystem.createDirectories(tmp)
        archivePath = Path(tmp, "bundle.zip")
    }

    @AfterTest
    fun tearDown() {
        deleteRecursively(tmp)
    }

    @Test
    fun openManifest_returnsContent() {
        writeZip(
            archivePath, mapOf(
                "bundle.json" to """{"name":"demo"}""",
            )
        )
        val src = ZipBundleSource(archivePath)
        val text = src.openManifest().getOrThrow().use { it.readString() }
        assertEquals("""{"name":"demo"}""", text)
    }

    @Test
    fun openManifest_fails_whenMissing() {
        writeZip(archivePath, mapOf("other.txt" to "x"))
        val result = ZipBundleSource(archivePath).openManifest()
        assertTrue(result.isFailure)
    }

    @Test
    fun openTargetConfig_returnsNull_whenAbsent() {
        writeZip(
            archivePath, mapOf(
                "bundle.json" to "{}",
                "claude/CLAUDE.md" to "# hi",
            )
        )
        val src = ZipBundleSource(archivePath)
        assertNull(src.openTargetConfig("claude").getOrThrow())
    }

    @Test
    fun openTargetConfig_returnsContent_whenPresent() {
        writeZip(
            archivePath, mapOf(
                "bundle.json" to "{}",
                "claude/config.json" to """{"v":1}""",
            )
        )
        val src = ZipBundleSource(archivePath)
        val text = src.openTargetConfig("claude").getOrThrow()?.use { it.readString() }
        assertEquals("""{"v":1}""", text)
    }

    @Test
    fun listTargetFiles_returnsRelativePaths() {
        writeZip(
            archivePath, mapOf(
                "bundle.json" to "{}",
                "claude/CLAUDE.md" to "root",
                "claude/commands/review.md" to "cmd",
            )
        )
        val files = ZipBundleSource(archivePath).listTargetFiles("claude").getOrThrow()
        assertEquals(listOf("CLAUDE.md", "commands/review.md"), files)
    }

    @Test
    fun listTargetFiles_excludesConfigJson() {
        writeZip(
            archivePath, mapOf(
                "bundle.json" to "{}",
                "claude/config.json" to "{}",
                "claude/CLAUDE.md" to "hi",
            )
        )
        val files = ZipBundleSource(archivePath).listTargetFiles("claude").getOrThrow()
        assertEquals(listOf("CLAUDE.md", "config.json"), files)
    }

    @Test
    fun openTargetFile_readsContent() {
        writeZip(
            archivePath, mapOf(
                "bundle.json" to "{}",
                "claude/commands/review.md" to "hello",
            )
        )
        val src = ZipBundleSource(archivePath)
        val text = src.openTargetFile("claude", "commands/review.md").getOrThrow().use { it.readString() }
        assertEquals("hello", text)
    }

    @Test
    fun openTargetFile_fails_whenMissing() {
        writeZip(archivePath, mapOf("bundle.json" to "{}"))
        val result = ZipBundleSource(archivePath).openTargetFile("claude", "missing.md")
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun close_preventsSubsequentUse() {
        writeZip(archivePath, mapOf("bundle.json" to "{}"))
        val src = ZipBundleSource(archivePath)
        src.close()
        assertTrue(src.openManifest().isFailure)
    }

    @Test
    fun multipleTargets_separatedCorrectly() {
        writeZip(
            archivePath, mapOf(
                "bundle.json" to "{}",
                "claude/CLAUDE.md" to "claude",
                "opencode/AGENTS.md" to "opencode",
            )
        )
        val src = ZipBundleSource(archivePath)
        assertEquals(listOf("CLAUDE.md"), src.listTargetFiles("claude").getOrThrow())
        assertEquals(listOf("AGENTS.md"), src.listTargetFiles("opencode").getOrThrow())
    }

    // --- helpers ---

    private fun writeZip(path: Path, entries: Map<String, String>) {
        val bytes = buildStoredZip(entries.mapValues { it.value.encodeToByteArray() })
        SystemFileSystem.sink(path).buffered().use { sink ->
            sink.write(bytes)
        }
    }

    private fun deleteRecursively(path: Path) {
        val meta = SystemFileSystem.metadataOrNull(path) ?: return
        if (meta.isDirectory) {
            for (child in SystemFileSystem.list(path)) deleteRecursively(child)
        }
        SystemFileSystem.delete(path, mustExist = false)
    }
}
