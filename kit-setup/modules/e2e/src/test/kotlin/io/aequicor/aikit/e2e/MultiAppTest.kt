package io.aequicor.aikit.e2e

import java.nio.file.Path
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MultiAppTest {

    private lateinit var sandbox: Path

    @BeforeEach
    fun setUp() {
        sandbox = Fixtures.newSandbox("aikit-e2e-multiapp-")
    }

    @AfterEach
    fun tearDown() {
        sandbox.toFile().deleteRecursively()
    }

    @Test
    fun `manifest with two applications generates into both subpaths`() {
        val manifest = Fixtures.writeManifest(
            sandbox,
            Fixtures.multiAppManifest(
                aikitVersion = Discovery.aikitVersion,
                bundleVersion = Discovery.simpleKitVersion,
            ),
        )

        assertSuccess(KitRunner.run("verify", manifest.toString(), cwd = sandbox), "verify multi-app")
        assertSuccess(KitRunner.run("generate", manifest.toString(), cwd = sandbox), "generate multi-app")

        // Root application (.)
        assertFileExists(sandbox.resolve("CLAUDE.md"))
        assertFileContains(sandbox.resolve("CLAUDE.md"), "root-app")
        assertFileExists(sandbox.resolve(".claude/agents/code-reviewer.md"))
        assertFileExists(sandbox.resolve(".claude/strict/hooks/block-dangerous.sh"))

        // Nested application (./api)
        assertFileExists(sandbox.resolve("api/CLAUDE.md"))
        assertFileContains(sandbox.resolve("api/CLAUDE.md"), "api-app")
        assertFileExists(sandbox.resolve("api/.claude/agents/test-runner.md"))
        // strict=false → no block-dangerous.sh in nested app
        assertFileAbsent(sandbox.resolve("api/.claude/strict/hooks/block-dangerous.sh"))

        // Different subagent sets per app — must not bleed across.
        assertFileAbsent(sandbox.resolve(".claude/agents/test-runner.md"))
        assertFileAbsent(sandbox.resolve("api/.claude/agents/code-reviewer.md"))
    }
}
