package io.aequicor.aikit.e2e

import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LifecycleTest {

    private lateinit var sandbox: Path

    @BeforeEach
    fun setUp() {
        sandbox = Fixtures.newSandbox("aikit-e2e-lifecycle-")
    }

    @AfterEach
    fun tearDown() {
        sandbox.toFile().deleteRecursively()
    }

    @Test
    fun `generate is idempotent and remove cleans up`() {
        val manifest = Fixtures.writeManifest(
            sandbox,
            Fixtures.simpleKitManifest(
                aikitVersion = Discovery.aikitVersion,
                bundleVersion = Discovery.simpleKitVersion,
            ),
        )

        // First generate
        assertSuccess(KitRunner.run("generate", manifest.toString(), cwd = sandbox), "generate #1")
        val firstSnapshot = snapshotTree(sandbox)

        // Second generate must succeed and produce the same tree (idempotent).
        assertSuccess(KitRunner.run("generate", manifest.toString(), cwd = sandbox), "generate #2")
        val secondSnapshot = snapshotTree(sandbox)
        assertEquals(firstSnapshot, secondSnapshot, "generate is not idempotent")

        // Remove must succeed and wipe `.claude/` and the root memory file.
        assertSuccess(KitRunner.run("remove", manifest.toString(), cwd = sandbox), "remove")
        assertFileAbsent(sandbox.resolve(".claude/commands/review.md"))
        assertFileAbsent(sandbox.resolve(".claude/agents/code-reviewer.md"))
        assertFileAbsent(sandbox.resolve("CLAUDE.md"))
    }

    private fun snapshotTree(root: Path): Set<String> =
        Files.walk(root).use { stream ->
            stream
                .filter { Files.isRegularFile(it) }
                .map { root.relativize(it).toString().replace('\\', '/') }
                .filter { !it.startsWith(".aikit/") || it == ".aikit/manifest.json" }
                .toList()
                .toSet()
        }
}
