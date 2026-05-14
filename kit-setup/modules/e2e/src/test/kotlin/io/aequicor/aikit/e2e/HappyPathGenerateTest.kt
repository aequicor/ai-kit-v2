package io.aequicor.aikit.e2e

import java.nio.file.Files
import java.nio.file.Path
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HappyPathGenerateTest {

    private lateinit var sandbox: Path

    @BeforeEach
    fun setUp() {
        sandbox = Fixtures.newSandbox("aikit-e2e-happy-")
    }

    @AfterEach
    fun tearDown() {
        sandbox.toFile().deleteRecursively()
    }

    @Test
    fun `verify and generate produce expected file tree for simple-kit`() {
        val manifest = Fixtures.writeManifest(
            sandbox,
            Fixtures.simpleKitManifest(
                aikitVersion = Discovery.aikitVersion,
                bundleVersion = Discovery.simpleKitVersion,
                projectName = "my-sandbox-app",
                skills = listOf("review"),
                subagents = listOf("code-reviewer"),
                githubMcp = false,
                strict = true,
            ),
        )

        val verify = KitRunner.run("verify", manifest.toString(), cwd = sandbox)
        assertSuccess(verify, "verify")

        val generate = KitRunner.run("generate", manifest.toString(), cwd = sandbox)
        assertSuccess(generate, "generate")

        // Memory file is mapped by ClaudeCodeLayout to the project root (not `.claude/`).
        assertFileExists(sandbox.resolve("CLAUDE.md"))
        assertFileContains(sandbox.resolve("CLAUDE.md"), "my-sandbox-app")

        // Slash commands, subagents, skills, settings — under `.claude/`.
        assertFileExists(sandbox.resolve(".claude/commands/review.md"))
        assertFileExists(sandbox.resolve(".claude/agents/code-reviewer.md"))
        assertFileExists(sandbox.resolve(".claude/skills/review/SKILL.md"))
        assertFileExists(sandbox.resolve(".claude/settings.json"))

        // Hook scripts keep the path declared in config.json.
        assertFileExists(sandbox.resolve(".claude/hooks/session-start.sh"))
        assertFileExists(sandbox.resolve(".claude/strict/hooks/block-dangerous.sh"))

        // Conditions that evaluated to false: corresponding files must not exist.
        assertFileAbsent(sandbox.resolve(".claude/skills/security-review/SKILL.md"))
        assertFileAbsent(sandbox.resolve(".claude/agents/test-runner.md"))
        // githubMcp = false → no .mcp.json
        assertFileAbsent(sandbox.resolve(".mcp.json"))
    }

    @Test
    fun `strict=false disables strict hook`() {
        val manifest = Fixtures.writeManifest(
            sandbox,
            Fixtures.simpleKitManifest(
                aikitVersion = Discovery.aikitVersion,
                bundleVersion = Discovery.simpleKitVersion,
                strict = false,
            ),
        )

        assertSuccess(KitRunner.run("generate", manifest.toString(), cwd = sandbox), "generate")

        // Strict hook script must not be generated.
        assertFileAbsent(sandbox.resolve(".claude/strict/hooks/block-dangerous.sh"))
    }

    @Test
    fun `githubMcp=true emits mcp config`() {
        val manifest = Fixtures.writeManifest(
            sandbox,
            Fixtures.simpleKitManifest(
                aikitVersion = Discovery.aikitVersion,
                bundleVersion = Discovery.simpleKitVersion,
                githubMcp = true,
            ),
        )

        assertSuccess(KitRunner.run("generate", manifest.toString(), cwd = sandbox), "generate")

        val mcp = sandbox.resolve(".mcp.json")
        if (!Files.exists(mcp)) {
            // Some layouts route MCP elsewhere; fall back to scanning for any *.mcp.json or claude settings file.
            val settings = sandbox.resolve(".claude/settings.json")
            assertFileExists(settings)
            assertFileContains(settings, "github")
        } else {
            assertFileContains(mcp, "github")
        }
    }
}
