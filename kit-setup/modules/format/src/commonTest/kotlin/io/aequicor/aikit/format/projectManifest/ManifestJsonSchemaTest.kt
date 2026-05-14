package io.aequicor.aikit.format.projectManifest

import io.aequicor.aikit.format.AiKitFormat
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ManifestJsonSchemaTest {

    private val format = AiKitFormat.create()
    private val json = Json { ignoreUnknownKeys = true }

    private fun schema(): JsonObject =
        json.parseToJsonElement(format.manifestJsonSchema()).jsonObject

    @Test
    fun root_has_schema_directive_and_type_object() {
        val root = schema()
        assertEquals("https://json-schema.org/draft/2020-12/schema", root["\$schema"]!!.jsonPrimitive.content)
        assertEquals("object", root["type"]!!.jsonPrimitive.content)
    }

    @Test
    fun aikitVersion_is_required_string() {
        val root = schema()
        val required = root["required"]!!.jsonArray.map { it.jsonPrimitive.content }
        assertTrue("aikitVersion" in required)
        val prop = root["properties"]!!.jsonObject["aikitVersion"]!!.jsonObject
        assertEquals("string", prop["type"]!!.jsonPrimitive.content)
    }

    @Test
    fun applications_is_optional_array() {
        val root = schema()
        val required = root["required"]!!.jsonArray.map { it.jsonPrimitive.content }
        assertTrue("applications" !in required)
        val prop = root["properties"]!!.jsonObject["applications"]!!.jsonObject
        assertEquals("array", prop["type"]!!.jsonPrimitive.content)
    }

    @Test
    fun application_item_has_required_id_and_path() {
        val items = schema()["properties"]!!.jsonObject["applications"]!!
            .jsonObject["items"]!!.jsonObject
        assertEquals("object", items["type"]!!.jsonPrimitive.content)
        val required = items["required"]!!.jsonArray.map { it.jsonPrimitive.content }
        assertTrue("id" in required)
        assertTrue("path" in required)
        assertTrue("targets" !in required)
    }

    @Test
    fun targets_is_object_with_additional_properties() {
        val itemProps = schema()["properties"]!!.jsonObject["applications"]!!
            .jsonObject["items"]!!.jsonObject["properties"]!!.jsonObject
        val targets = itemProps["targets"]!!.jsonObject
        assertEquals("object", targets["type"]!!.jsonPrimitive.content)
        val additionalProps = targets["additionalProperties"]!!.jsonObject
        assertEquals("object", additionalProps["type"]!!.jsonPrimitive.content)
    }

    @Test
    fun bundle_target_has_required_bundle_and_source() {
        val targetSchema = schema()["properties"]!!.jsonObject["applications"]!!
            .jsonObject["items"]!!.jsonObject["properties"]!!.jsonObject["targets"]!!
            .jsonObject["additionalProperties"]!!.jsonObject
        val required = targetSchema["required"]!!.jsonArray.map { it.jsonPrimitive.content }
        assertTrue("bundle" in required)
        assertTrue("source" in required)
        assertTrue("inputs" !in required)
    }

    @Test
    fun inputs_accepts_any_value() {
        val targetProps = schema()["properties"]!!.jsonObject["applications"]!!
            .jsonObject["items"]!!.jsonObject["properties"]!!.jsonObject["targets"]!!
            .jsonObject["additionalProperties"]!!.jsonObject["properties"]!!.jsonObject
        val inputs = targetProps["inputs"]!!.jsonObject
        assertEquals("object", inputs["type"]!!.jsonPrimitive.content)
        // additionalProperties: {} means any value is allowed
        assertTrue(inputs["additionalProperties"]!!.jsonObject.isEmpty())
    }
}
