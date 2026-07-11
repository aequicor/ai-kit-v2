package io.aequicor.aikit.e2e

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * End-to-end coverage of the `codex` target: a minimal bundle written to disk (path source)
 * is verified and generated, producing `AGENTS.md`, `.codex/config.toml` (TOML),
 * `.codex/agents/<name>.toml` and `.codex/prompts/<name>.md`; `remove` cleans everything up.
 */
class CodexTargetTest {

    private fun writeCodexBundle(sandbox: java.nio.file.Path): java.nio.file.Path {
        val bundle = sandbox.resolve(".aikit/bundles/codex-kit")
        bundle.resolve("codex/agents").createDirectories()
        bundle.resolve("codex/prompts").createDirectories()

        bundle.resolve("bundle.json").writeText(
            """
            {
              "schemaVersion": 1,
              "name": "codex-kit",
              "version": "0.0.1",
              "description": "e2e codex bundle",
              "targets": ["codex"],
              "inputs": [
                { "id": "projectName", "type": "string", "title": "Name", "required": true },
                { "id": "githubMcp", "type": "boolean", "title": "GitHub MCP", "default": false }
              ]
            }
            """.trimIndent(),
        )
        bundle.resolve("codex/config.json").writeText(
            """
            {
              "schemaVersion": 1,
              "agent": "codex",
              "settings": {
                "model": "gpt-5.6",
                "approvalPolicy": "on-request",
                "sandboxMode": "workspace-write",
                "features": { "multi_agent": true }
              },
              "memory": [ { "name": "AGENTS.md", "source": "AGENTS.md" } ],
              "mcpServers": [
                {
                  "when": "${'$'}{bundle.input.githubMcp}",
                  "name": "github",
                  "command": "npx",
                  "args": ["-y", "@modelcontextprotocol/server-github"],
                  "timeout": 30
                }
              ],
              "agents": [ { "name": "reviewer", "source": "agents/reviewer.toml" } ],
              "commands": [ { "name": "review", "source": "prompts/review.md" } ]
            }
            """.trimIndent(),
        )
        bundle.resolve("codex/AGENTS.md").writeText("# AGENTS.md — \${bundle.input.projectName}\n")
        bundle.resolve("codex/agents/reviewer.toml").writeText(
            "name = \"reviewer\"\ndescription = \"Review \${bundle.input.projectName}\"\n" +
                "developer_instructions = \"\"\"\nReview the diff.\n\"\"\"\n",
        )
        bundle.resolve("codex/prompts/review.md").writeText("# /review for \${bundle.input.projectName}\n")
        return bundle
    }

    private fun codexManifest(githubMcp: Boolean): String = """
        {
          "aikitVersion": "0.0.0",
          "applications": [
            {
              "id": "root",
              "path": ".",
              "targets": {
                "codex": {
                  "bundle": "codex-kit@0.0.1",
                  "source": "./.aikit/bundles/codex-kit",
                  "inputs": { "projectName": "codex-e2e", "githubMcp": $githubMcp }
                }
              }
            }
          ]
        }
    """.trimIndent()

    @Test
    fun `codex target generates AGENTS_md, config_toml, subagent and prompt`() {
        val sandbox = Fixtures.newSandbox()
        writeCodexBundle(sandbox)
        Fixtures.writeManifest(sandbox, codexManifest(githubMcp = true))

        val verify = KitRunner.run("verify", ".aikit/manifest.json", cwd = sandbox)
        assertEquals(0, verify.exitCode, verify.combined)

        val generate = KitRunner.run("generate", ".aikit/manifest.json", cwd = sandbox)
        assertEquals(0, generate.exitCode, generate.combined)

        assertTrue(sandbox.resolve("AGENTS.md").exists())
        assertEquals("# AGENTS.md — codex-e2e\n", sandbox.resolve("AGENTS.md").readText())

        val toml = sandbox.resolve(".codex/config.toml")
        assertTrue(toml.exists())
        val tomlText = toml.readText()
        assertTrue(tomlText.contains("model = \"gpt-5.6\""), tomlText)
        assertTrue(tomlText.contains("approval_policy = \"on-request\""), tomlText)
        assertTrue(tomlText.contains("sandbox_mode = \"workspace-write\""), tomlText)
        assertTrue(tomlText.contains("[features]"), tomlText)
        assertTrue(tomlText.contains("[mcp_servers.github]"), tomlText)
        assertTrue(tomlText.contains("timeout_secs = 30"), tomlText)

        val agent = sandbox.resolve(".codex/agents/reviewer.toml")
        assertTrue(agent.exists())
        assertTrue(agent.readText().contains("Review codex-e2e"))

        assertTrue(sandbox.resolve(".codex/prompts/review.md").exists())
    }

    @Test
    fun `mcp server with false condition is excluded from config_toml`() {
        val sandbox = Fixtures.newSandbox()
        writeCodexBundle(sandbox)
        Fixtures.writeManifest(sandbox, codexManifest(githubMcp = false))

        val generate = KitRunner.run("generate", ".aikit/manifest.json", cwd = sandbox)
        assertEquals(0, generate.exitCode, generate.combined)

        val tomlText = sandbox.resolve(".codex/config.toml").readText()
        assertFalse(tomlText.contains("mcp_servers"), tomlText)
    }

    @Test
    fun `remove deletes every generated codex file`() {
        val sandbox = Fixtures.newSandbox()
        writeCodexBundle(sandbox)
        Fixtures.writeManifest(sandbox, codexManifest(githubMcp = true))
        KitRunner.run("generate", ".aikit/manifest.json", cwd = sandbox)

        val remove = KitRunner.run("remove", cwd = sandbox)
        assertEquals(0, remove.exitCode, remove.combined)

        assertFalse(sandbox.resolve("AGENTS.md").exists())
        assertFalse(sandbox.resolve(".codex/config.toml").exists())
        assertFalse(sandbox.resolve(".codex/agents/reviewer.toml").exists())
        assertFalse(sandbox.resolve(".codex").exists())
    }
}
