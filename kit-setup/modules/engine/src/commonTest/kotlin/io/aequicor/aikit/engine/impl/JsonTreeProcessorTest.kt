package io.aequicor.aikit.engine.impl

import io.aequicor.aikit.akel.AkelValue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JsonTreeProcessorTest {

    private val json = Json

    @Test fun dropsObjectWhenFalse() {
        val tree = json.parseToJsonElement(
            """{ "a": { "when": "false", "x": 1 }, "b": { "when": "true", "y": 2 } }""",
        )
        val out = JsonTreeProcessor.process(tree, emptyMap()) as JsonObject
        assertTrue("a" !in out)
        assertEquals(2, out["b"]!!.jsonObject["y"]!!.jsonPrimitive.content.toInt())
    }

    @Test fun stripsWhenKeyWhenTrue() {
        val tree = json.parseToJsonElement("""{ "when": "true", "x": "hello" }""")
        val out = JsonTreeProcessor.process(tree, emptyMap()) as JsonObject
        assertTrue("when" !in out)
        assertEquals("hello", out["x"]!!.jsonPrimitive.content)
    }

    @Test fun dropsArrayElementsWhereWhenFalse() {
        val tree = json.parseToJsonElement(
            """{ "items": [ { "v": 1 }, { "when": "false", "v": 2 }, { "v": 3 } ] }""",
        )
        val out = JsonTreeProcessor.process(tree, emptyMap()) as JsonObject
        val items = out["items"]!!.jsonArray
        assertEquals(2, items.size)
        assertEquals(1, items[0].jsonObject["v"]!!.jsonPrimitive.content.toInt())
        assertEquals(3, items[1].jsonObject["v"]!!.jsonPrimitive.content.toInt())
    }

    @Test fun substitutesStringInputs() {
        val tree = json.parseToJsonElement(
            """{ "model": "${"$"}{bundle.input.model}", "title": "name=${"$"}{bundle.input.name}" }""",
        )
        val out = JsonTreeProcessor.process(
            tree,
            mapOf("model" to AkelValue.Str("opus"), "name" to AkelValue.Str("kit")),
        ) as JsonObject
        assertEquals("opus", out["model"]!!.jsonPrimitive.content)
        assertEquals("name=kit", out["title"]!!.jsonPrimitive.content)
    }

    @Test fun whenWithInputExpression() {
        val tree = json.parseToJsonElement(
            """{ "github": { "when": "${"$"}{bundle.input.flag}", "x": 1 }, "other": { "x": 2 } }""",
        )
        val onTrue = JsonTreeProcessor.process(tree, mapOf("flag" to AkelValue.Bool(true))) as JsonObject
        assertTrue("github" in onTrue)
        val onFalse = JsonTreeProcessor.process(tree, mapOf("flag" to AkelValue.Bool(false))) as JsonObject
        assertTrue("github" !in onFalse)
    }

    @Test fun rootObjectFalseReturnsNull() {
        val tree = json.parseToJsonElement("""{ "when": "false", "x": 1 }""")
        assertNull(JsonTreeProcessor.process(tree, emptyMap()))
    }
}
