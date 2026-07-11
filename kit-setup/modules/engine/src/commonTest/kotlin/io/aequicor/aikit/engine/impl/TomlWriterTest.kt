package io.aequicor.aikit.engine.impl

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

class TomlWriterTest {

    private fun toml(json: String): String = TomlWriter.write(Json.parseToJsonElement(json) as JsonObject)

    @Test
    fun scalars_allTypes() {
        val out = toml(
            """{"model":"gpt-5.6","approval_policy":"on-request","timeout_secs":30,"threshold":0.75,"web_search":true}""",
        )

        assertEquals(
            """
            model = "gpt-5.6"
            approval_policy = "on-request"
            timeout_secs = 30
            threshold = 0.75
            web_search = true
            """.trimIndent() + "\n",
            out,
        )
    }

    @Test
    fun strings_areEscaped() {
        val out = toml("""{"a":"say \"hi\"","b":"line1\nline2","c":"back\\slash","d":"tab\there"}""")

        assertEquals(
            """
            a = "say \"hi\""
            b = "line1\nline2"
            c = "back\\slash"
            d = "tab\there"
            """.trimIndent() + "\n",
            out,
        )
    }

    @Test
    fun arrays_ofPrimitives() {
        val out = toml("""{"args":["-y","@modelcontextprotocol/server-github"],"ports":[1,2,3],"empty":[]}""")

        assertEquals(
            """
            args = ["-y", "@modelcontextprotocol/server-github"]
            ports = [1, 2, 3]
            empty = []
            """.trimIndent() + "\n",
            out,
        )
    }

    @Test
    fun features_becomesTable() {
        val out = toml("""{"model":"gpt-5.6","features":{"multi_agent":true,"memories":false}}""")

        assertEquals(
            """
            model = "gpt-5.6"

            [features]
            multi_agent = true
            memories = false
            """.trimIndent() + "\n",
            out,
        )
    }

    @Test
    fun mcpServers_becomeDottedTables_withInlineEnv() {
        val out = toml(
            """
            {
              "model": "gpt-5.6",
              "mcp_servers": {
                "github": {
                  "command": "npx",
                  "args": ["-y", "@modelcontextprotocol/server-github"],
                  "env": {"GITHUB_TOKEN": "x"}
                },
                "db": {"command": "/usr/local/bin/db-mcp", "timeout_secs": 30}
              }
            }
            """.trimIndent(),
        )

        assertEquals(
            """
            model = "gpt-5.6"

            [mcp_servers.github]
            command = "npx"
            args = ["-y", "@modelcontextprotocol/server-github"]
            env = { GITHUB_TOKEN = "x" }

            [mcp_servers.db]
            command = "/usr/local/bin/db-mcp"
            timeout_secs = 30
            """.trimIndent() + "\n",
            out,
        )
    }

    @Test
    fun nulls_areSkipped() {
        val out = toml("""{"model":"gpt-5.6","missing":null,"features":{"x":true,"y":null}}""")

        assertEquals(
            """
            model = "gpt-5.6"

            [features]
            x = true
            """.trimIndent() + "\n",
            out,
        )
    }

    @Test
    fun nonBareKeys_areQuoted() {
        val out = toml("""{"mcp_servers":{"my.server":{"command":"run"}}}""")

        assertEquals(
            """
            [mcp_servers."my.server"]
            command = "run"
            """.trimIndent() + "\n",
            out,
        )
    }

    @Test
    fun emptyTable_emitsBareHeader() {
        val out = toml("""{"features":{}}""")

        assertEquals("[features]\n", out)
    }
}
