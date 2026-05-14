package io.aequicor.aikit.engine.manifest

import io.aequicor.aikit.engine.api.ManifestResolveError
import io.aequicor.aikit.engine.api.ManifestSource
import io.aequicor.aikit.engine.api.ResolveRequest
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class FsManifestResolverTest {

    private lateinit var root: Path
    private val resolver = createFsManifestResolver()

    @BeforeTest
    fun setUp() {
        root = Path(SystemTemporaryDirectory.toString(), "aikit-test-${Random.nextLong().toString(16)}")
        SystemFileSystem.createDirectories(Path(root.toString(), ".aikit"))
    }

    @AfterTest
    fun tearDown() {
        deleteRecursively(root)
    }

    private fun request(explicit: String? = null, mode: String? = null) = ResolveRequest(
        projectRoot = root.toString(),
        explicitManifestArg = explicit,
        explicitModeFlag = mode,
    )

    private fun writeFile(relative: String, content: String = "{}") {
        val path = Path(root.toString(), relative)
        val parent = path.parent
        if (parent != null && !SystemFileSystem.exists(parent)) {
            SystemFileSystem.createDirectories(parent)
        }
        SystemFileSystem.sink(path).buffered().use { it.write(content.encodeToByteArray()) }
    }

    // ── Explicit arg ──────────────────────────────────────────────────────

    @Test
    fun explicitArgResolvesExistingFile() {
        writeFile(".aikit/manifest.json")
        val r = resolver.resolve(request(explicit = ".aikit/manifest.json")).getOrThrow()
        assertEquals(ManifestSource.EXPLICIT_ARG, r.source)
        assertEquals(".aikit/manifest.json", r.manifestRef)
    }

    @Test
    fun explicitArgFailsWhenFileMissing() {
        val err = resolver.resolve(request(explicit = ".aikit/missing.json")).exceptionOrNull()
        assertIs<ManifestResolveError.ManifestNotFound>(err)
        assertEquals(".aikit/missing.json", err.path)
    }

    // ── Mode flag ─────────────────────────────────────────────────────────

    @Test
    fun modeFlagResolvesExistingFile() {
        writeFile(".aikit/manifest.autonomous.json")
        val r = resolver.resolve(request(mode = "autonomous")).getOrThrow()
        assertEquals(ManifestSource.MODE_FLAG, r.source)
        assertEquals("autonomous", r.modeHint)
        assertEquals(".aikit/manifest.autonomous.json", r.manifestRef)
    }

    @Test
    fun modeFlagFailsWhenFileDoesNotExist() {
        val err = resolver.resolve(request(mode = "nonexistent")).exceptionOrNull()
        assertIs<ManifestResolveError.ManifestNotFound>(err)
    }

    // ── Conflicting args ──────────────────────────────────────────────────

    @Test
    fun explicitArgAndModeFlagTogetherFail() {
        writeFile(".aikit/manifest.json")
        val err = resolver.resolve(
            request(explicit = ".aikit/manifest.json", mode = "autonomous"),
        ).exceptionOrNull()
        assertIs<ManifestResolveError.ConflictingArgs>(err)
    }

    // ── local.properties ─────────────────────────────────────────────────

    @Test
    fun localPropertiesManifestKeyResolvesFile() {
        writeFile(".aikit/manifest.interactive.json")
        writeFile(".aikit/local.properties", "manifest=.aikit/manifest.interactive.json\n")
        val r = resolver.resolve(request()).getOrThrow()
        assertEquals(ManifestSource.LOCAL_PROPERTIES, r.source)
        assertEquals(".aikit/manifest.interactive.json", r.manifestRef)
    }

    @Test
    fun localPropertiesMissingTargetFileFails() {
        writeFile(".aikit/local.properties", "manifest=.aikit/manifest.nonexistent.json\n")
        val err = resolver.resolve(request()).exceptionOrNull()
        assertIs<ManifestResolveError.ManifestNotFound>(err)
    }

    @Test
    fun localPropertiesIgnoresCommentLines() {
        writeFile(".aikit/manifest.interactive.json")
        writeFile(
            ".aikit/local.properties",
            "# per-developer config — do not commit\nmanifest=.aikit/manifest.interactive.json\n",
        )
        val r = resolver.resolve(request()).getOrThrow()
        assertEquals(ManifestSource.LOCAL_PROPERTIES, r.source)
    }

    @Test
    fun localPropertiesIgnoresLinesWithoutEquals() {
        writeFile(".aikit/manifest.json")
        writeFile(".aikit/local.properties", "malformed-no-equals\nother=value\n")
        // no manifest key → falls through to legacy .aikit/manifest.json
        val r = resolver.resolve(request()).getOrThrow()
        assertEquals(ManifestSource.LEGACY_DEFAULT, r.source)
    }

    @Test
    fun localPropertiesDuplicateKeysLastWins() {
        writeFile(".aikit/manifest.interactive.json")
        writeFile(
            ".aikit/local.properties",
            "manifest=.aikit/manifest.nonexistent.json\nmanifest=.aikit/manifest.interactive.json\n",
        )
        val r = resolver.resolve(request()).getOrThrow()
        assertEquals(ManifestSource.LOCAL_PROPERTIES, r.source)
        assertEquals(".aikit/manifest.interactive.json", r.manifestRef)
    }

    @Test
    fun localPropertiesNoManifestKeyFallsThrough() {
        writeFile(".aikit/manifest.json")
        writeFile(".aikit/local.properties", "verbose=true\nlog_level=debug\n")
        val r = resolver.resolve(request()).getOrThrow()
        assertEquals(ManifestSource.LEGACY_DEFAULT, r.source)
    }

    // ── Priority: explicit > local.properties ─────────────────────────────

    @Test
    fun explicitArgTakesPriorityOverLocalProperties() {
        writeFile(".aikit/manifest.interactive.json")
        writeFile(".aikit/manifest.autonomous.json")
        writeFile(".aikit/local.properties", "manifest=.aikit/manifest.autonomous.json\n")
        val r = resolver.resolve(request(explicit = ".aikit/manifest.interactive.json")).getOrThrow()
        assertEquals(ManifestSource.EXPLICIT_ARG, r.source)
    }

    // ── Legacy fallback ───────────────────────────────────────────────────

    @Test
    fun legacyManifestJsonFallback() {
        writeFile(".aikit/manifest.json")
        val r = resolver.resolve(request()).getOrThrow()
        assertEquals(ManifestSource.LEGACY_DEFAULT, r.source)
        assertNull(r.modeHint)
    }

    // ── Nothing configured ────────────────────────────────────────────────

    @Test
    fun noManifestConfiguredError() {
        val err = resolver.resolve(request()).exceptionOrNull()
        assertIs<ManifestResolveError.NoManifestConfigured>(err)
        assertEquals(root.toString(), err.projectRoot)
    }

    // ── helper ────────────────────────────────────────────────────────────

    private fun deleteRecursively(path: Path) {
        if (!SystemFileSystem.exists(path)) return
        val meta = SystemFileSystem.metadataOrNull(path) ?: return
        if (meta.isDirectory) {
            for (child in SystemFileSystem.list(path)) deleteRecursively(child)
        }
        SystemFileSystem.delete(path)
    }
}
