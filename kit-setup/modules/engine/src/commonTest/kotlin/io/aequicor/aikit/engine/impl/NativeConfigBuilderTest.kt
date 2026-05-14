package io.aequicor.aikit.engine.impl

import io.aequicor.aikit.akel.AkelValue
import io.aequicor.aikit.core.domain.targets.ClaudeCode
import io.aequicor.aikit.layout.ClaudeCodeLayout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NativeConfigBuilderTest {

    private val json = Json { prettyPrint = false }
    private val builder = NativeConfigBuilder(json)

    private val claudeStub = ClaudeCode(
        schemaVersion = 1, minVersion = null, mcpServers = emptyList(),
        commands = emptyList(), skills = emptyList(), subagents = emptyList(),
        model = null, includeCoAuthoredBy = null, env = null, permissions = null, hooks = emptyMap(),
    )

    @Test fun emitsClaudeSettingsAndMcpFromConfig() {
        val configText = """
            {
              "schemaVersion": 1,
              "settings": {
                "model": "opus",
                "permissions": {
                  "deny": [ { "value": "Bash(rm -rf:*)", "when": "${"$"}{bundle.input.strict}" } ]
                }
              },
              "mcpServers": [
                {
                  "when": "${"$"}{bundle.input.githubMcp}",
                  "name": "github",
                  "command": "npx",
                  "args": ["-y", "@modelcontextprotocol/server-github"]
                }
              ],
              "hooks": {
                "SessionStart": [ { "matcher": "*", "command": ".claude/hooks/session-start.sh" } ]
              }
            }
        """.trimIndent()
        val tree = json.parseToJsonElement(configText)
        val inputs = mapOf(
            "strict" to AkelValue.Bool(true),
            "githubMcp" to AkelValue.Bool(true),
        )

        val files = builder.build(claudeStub, tree, ClaudeCodeLayout, inputs)
        val byPath = files.associateBy { it.relPath }

        val settings = byPath[".claude/settings.json"]
        assertNotNull(settings)
        val parsedSettings = json.parseToJsonElement(settings.bytes.decodeToString()).jsonObject
        assertEquals("opus", parsedSettings["model"]!!.jsonPrimitive.content)
        val deny = parsedSettings["permissions"]!!.jsonObject["deny"]!!.jsonArray
        assertEquals(1, deny.size)
        assertTrue("hooks" in parsedSettings)

        val mcp = byPath[".mcp.json"]
        assertNotNull(mcp)
        val parsedMcp = json.parseToJsonElement(mcp.bytes.decodeToString()).jsonObject
        val github = parsedMcp["mcpServers"]!!.jsonObject["github"]!!.jsonObject
        assertEquals("npx", github["command"]!!.jsonPrimitive.content)
    }

    @Test fun stripsMcpServerWhenConditionFalse() {
        val configText = """
            {
              "mcpServers": [
                {
                  "when": "${"$"}{bundle.input.githubMcp}",
                  "name": "github",
                  "command": "npx"
                }
              ]
            }
        """.trimIndent()
        val tree = json.parseToJsonElement(configText)
        val files = builder.build(
            claudeStub, tree, ClaudeCodeLayout,
            mapOf("githubMcp" to AkelValue.Bool(false)),
        )
        assertTrue(files.none { it.relPath == ".mcp.json" })
    }
}
