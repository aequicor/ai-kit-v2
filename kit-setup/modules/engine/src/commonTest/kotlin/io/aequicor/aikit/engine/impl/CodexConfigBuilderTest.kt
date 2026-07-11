package io.aequicor.aikit.engine.impl

import io.aequicor.aikit.akel.AkelValue
import io.aequicor.aikit.core.domain.targets.Codex
import io.aequicor.aikit.layout.CodexLayout
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CodexConfigBuilderTest {

    private val json = Json { prettyPrint = false }
    private val builder = NativeConfigBuilder(json)

    private val codexStub = Codex(
        schemaVersion = 1, minVersion = null, mcpServers = emptyList(),
        commands = emptyList(), skills = emptyList(), subagents = emptyList(),
        model = null, modelReasoningEffort = null, approvalPolicy = null,
        sandboxMode = null, webSearch = null, features = null,
    )

    @Test
    fun emitsCodexConfigToml_withSnakeCaseKeysAndMcpTables() {
        val configText = """
            {
              "schemaVersion": 1,
              "settings": {
                "model": "gpt-5.6",
                "modelReasoningEffort": "high",
                "approvalPolicy": "on-request",
                "sandboxMode": "workspace-write",
                "features": { "multi_agent": true }
              },
              "mcpServers": [
                {
                  "when": "${"$"}{bundle.input.githubMcp}",
                  "name": "github",
                  "command": "npx",
                  "args": ["-y", "@modelcontextprotocol/server-github"],
                  "env": { "GITHUB_TOKEN": "x" },
                  "timeout": 30
                }
              ]
            }
        """.trimIndent()
        val tree = json.parseToJsonElement(configText)
        val inputs = mapOf("githubMcp" to AkelValue.Bool(true))

        val files = builder.build(codexStub, tree, CodexLayout, inputs)

        assertEquals(1, files.size)
        assertEquals(".codex/config.toml", files[0].relPath)
        val toml = files[0].bytes.decodeToString()
        assertTrue(toml.contains("model = \"gpt-5.6\""), toml)
        assertTrue(toml.contains("model_reasoning_effort = \"high\""), toml)
        assertTrue(toml.contains("approval_policy = \"on-request\""), toml)
        assertTrue(toml.contains("sandbox_mode = \"workspace-write\""), toml)
        assertTrue(toml.contains("[features]"), toml)
        assertTrue(toml.contains("multi_agent = true"), toml)
        assertTrue(toml.contains("[mcp_servers.github]"), toml)
        assertTrue(toml.contains("command = \"npx\""), toml)
        assertTrue(toml.contains("args = [\"-y\", \"@modelcontextprotocol/server-github\"]"), toml)
        assertTrue(toml.contains("env = { GITHUB_TOKEN = \"x\" }"), toml)
        assertTrue(toml.contains("timeout_secs = 30"), toml)
        // bundle-internal keys must not leak into the native config
        assertFalse(toml.contains("when"), toml)
        assertFalse(toml.contains("name ="), toml)
    }

    @Test
    fun stripsMcpServer_whenConditionFalse() {
        val configText = """
            {
              "settings": { "model": "gpt-5.6" },
              "mcpServers": [
                { "when": "${"$"}{bundle.input.githubMcp}", "name": "github", "command": "npx" }
              ]
            }
        """.trimIndent()
        val tree = json.parseToJsonElement(configText)
        val inputs = mapOf("githubMcp" to AkelValue.Bool(false))

        val files = builder.build(codexStub, tree, CodexLayout, inputs)

        val toml = files.single().bytes.decodeToString()
        assertNotNull(toml)
        assertFalse(toml.contains("mcp_servers"), toml)
        assertTrue(toml.contains("model = \"gpt-5.6\""), toml)
    }

    @Test
    fun emitsNothing_whenConfigHasNoDeclarativeSections() {
        val tree = json.parseToJsonElement("""{"schemaVersion":1,"memory":[{"name":"AGENTS.md","source":"AGENTS.md"}]}""")

        val files = builder.build(codexStub, tree, CodexLayout, emptyMap())

        assertTrue(files.isEmpty())
    }
}
