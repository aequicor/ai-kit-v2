package io.aequicor.aikit.format.bundle

import io.aequicor.aikit.format.AiKitFormat
import io.aequicor.aikit.io.BundleSource
import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.io.writeString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BundleInputsJsonSchemaTest {

    private val format = AiKitFormat.create()
    private val json = Json { ignoreUnknownKeys = true }

    private fun schemaOf(bundleJson: String): JsonObject {
        val parsed = format.parseBundleManifest(SchemaMemoryBundleSource(bundleJson)).getOrThrow()
        val raw = format.bundleInputsJsonSchema(parsed.manifest)
        return json.parseToJsonElement(raw).jsonObject
    }

    @Test
    fun schema_has_root_metadata_and_no_extra_props() {
        val schema = schemaOf(
            """
            {"schemaVersion":1,"name":"my-bundle","version":"1.0.0","description":"d",
             "targets":["claude-code"]}
            """.trimIndent()
        )
        assertEquals("https://json-schema.org/draft/2020-12/schema", schema["\$schema"]!!.jsonPrimitive.content)
        assertEquals("object", schema["type"]!!.jsonPrimitive.content)
        assertEquals(false, schema["additionalProperties"]!!.jsonPrimitive.boolean)
        assertEquals("my-bundle@1.0.0 inputs", schema["title"]!!.jsonPrimitive.content)
        assertNull(schema["required"])
        assertTrue(schema["properties"]!!.jsonObject.isEmpty())
    }

    @Test
    fun bool_input_maps_to_boolean_with_default() {
        val props = propsOf(
            """{"id":"flag","type":"boolean","title":"Flag?","default":true}"""
        )
        val flag = props["flag"]!!.jsonObject
        assertEquals("boolean", flag["type"]!!.jsonPrimitive.content)
        assertEquals(true, flag["default"]!!.jsonPrimitive.boolean)
        assertEquals("Flag?", flag["title"]!!.jsonPrimitive.content)
    }

    @Test
    fun string_input_with_required_no_default_is_required() {
        val schema = schemaOf(
            """
            {"schemaVersion":1,"name":"x","version":"1.0","description":"d","targets":["claude-code"],
             "inputs":[{"id":"name","type":"string","title":"Name","required":true}]}
            """.trimIndent()
        )
        val name = schema["properties"]!!.jsonObject["name"]!!.jsonObject
        assertEquals("string", name["type"]!!.jsonPrimitive.content)
        assertNull(name["default"])
        val required = schema["required"]!!.jsonArray
        assertEquals(listOf("name"), required.map { it.jsonPrimitive.content })
    }

    @Test
    fun string_input_with_default_is_optional() {
        val schema = schemaOf(
            """
            {"schemaVersion":1,"name":"x","version":"1.0","description":"d","targets":["claude-code"],
             "inputs":[{"id":"name","type":"string","title":"Name","default":"hi","required":true}]}
            """.trimIndent()
        )
        assertNull(schema["required"])
        assertEquals("hi", schema["properties"]!!.jsonObject["name"]!!.jsonObject["default"]!!.jsonPrimitive.content)
    }

    @Test
    fun int_input_carries_min_max() {
        val props = propsOf(
            """{"id":"n","type":"int","title":"N","default":4096,"min":1,"max":200000}"""
        )
        val n = props["n"]!!.jsonObject
        assertEquals("integer", n["type"]!!.jsonPrimitive.content)
        assertEquals(1, n["minimum"]!!.jsonPrimitive.intOrNull)
        assertEquals(200000, n["maximum"]!!.jsonPrimitive.intOrNull)
        assertEquals(4096, n["default"]!!.jsonPrimitive.intOrNull)
    }

    @Test
    fun double_input_carries_min_max() {
        val props = propsOf(
            """{"id":"t","type":"double","title":"Temp","default":0.7,"min":0.0,"max":2.0}"""
        )
        val t = props["t"]!!.jsonObject
        assertEquals("number", t["type"]!!.jsonPrimitive.content)
        assertEquals(0.0, t["minimum"]!!.jsonPrimitive.doubleOrNull)
        assertEquals(2.0, t["maximum"]!!.jsonPrimitive.doubleOrNull)
        assertEquals(0.7, t["default"]!!.jsonPrimitive.doubleOrNull)
    }

    @Test
    fun select_input_maps_to_enum_string() {
        val schema = schemaOf(
            """
            {"schemaVersion":1,"name":"x","version":"1.0","description":"d","targets":["claude-code"],
             "inputs":[{"id":"env","type":"select","title":"Env","options":["dev","prod"]}]}
            """.trimIndent()
        )
        val env = schema["properties"]!!.jsonObject["env"]!!.jsonObject
        assertEquals("string", env["type"]!!.jsonPrimitive.content)
        assertEquals(listOf("dev", "prod"), env["enum"]!!.jsonArray.map { it.jsonPrimitive.content })
        assertEquals(listOf("env"), schema["required"]!!.jsonArray.map { it.jsonPrimitive.content })
    }

    @Test
    fun select_input_with_default_is_optional() {
        val schema = schemaOf(
            """
            {"schemaVersion":1,"name":"x","version":"1.0","description":"d","targets":["claude-code"],
             "inputs":[{"id":"env","type":"select","title":"Env","options":["dev","prod"],"default":"dev"}]}
            """.trimIndent()
        )
        assertNull(schema["required"])
        val env = schema["properties"]!!.jsonObject["env"]!!.jsonObject
        assertEquals("dev", env["default"]!!.jsonPrimitive.content)
    }

    @Test
    fun multiselect_maps_to_array_with_enum_items() {
        val props = propsOf(
            """{"id":"feats","type":"multiselect","title":"Feats","options":["a","b"],"default":["a"]}"""
        )
        val feats = props["feats"]!!.jsonObject
        assertEquals("array", feats["type"]!!.jsonPrimitive.content)
        assertEquals(true, feats["uniqueItems"]!!.jsonPrimitive.boolean)
        val items = feats["items"]!!.jsonObject
        assertEquals("string", items["type"]!!.jsonPrimitive.content)
        assertEquals(listOf("a", "b"), items["enum"]!!.jsonArray.map { it.jsonPrimitive.content })
        assertEquals(listOf("a"), feats["default"]!!.jsonArray.map { it.jsonPrimitive.content })
    }

    @Test
    fun description_propagates_when_present() {
        val props = propsOf(
            """{"id":"name","type":"string","title":"Name","description":"Full user name"}"""
        )
        assertEquals("Full user name", props["name"]!!.jsonObject["description"]!!.jsonPrimitive.content)
    }

    private fun propsOf(inputJson: String): JsonObject {
        val schema = schemaOf(
            """
            {"schemaVersion":1,"name":"x","version":"1.0","description":"d","targets":["claude-code"],
             "inputs":[$inputJson]}
            """.trimIndent()
        )
        return schema["properties"]!!.jsonObject
    }

}

private class SchemaMemoryBundleSource(private val manifestJson: String) : BundleSource {
    override fun openManifest(): Result<Source> =
        Result.success(Buffer().also { it.writeString(manifestJson) })
    override fun openTargetConfig(targetFolder: String): Result<Source?> = Result.success(null)
    override fun listTargetFiles(targetFolder: String): Result<List<String>> = Result.success(emptyList())
    override fun openTargetFile(targetFolder: String, relativePath: String): Result<Source> =
        Result.failure(UnsupportedOperationException("not used"))
    override fun close() = Unit
}
