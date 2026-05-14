@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package io.aequicor.aikit.format.projectManifest

import io.aequicor.aikit.format.projectManifest.v1.ProjectManifestDtoV1
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.serializer

/**
 * Generates JSON Schema (draft 2020-12) for `.aikit/manifest.json` by introspecting
 * the [ProjectManifestDtoV1] serialization descriptor. Fields with a default value are
 * excluded from `required`; nested classes and collections are traversed recursively.
 */
internal fun renderManifestJsonSchema(json: Json): String {
    val rootDescriptor = serializer<ProjectManifestDtoV1>().descriptor
    val classSchema = descriptorToSchema(rootDescriptor)
    val schema = buildJsonObject {
        put("\$schema", "https://json-schema.org/draft/2020-12/schema")
        classSchema.entries.forEach { (k, v) -> put(k, v) }
    }
    return json.encodeToString(JsonObject.serializer(), schema)
}

private fun descriptorToSchema(descriptor: SerialDescriptor): JsonObject = buildJsonObject {
    when (descriptor.kind) {
        StructureKind.CLASS -> buildClassSchema(descriptor)
        StructureKind.LIST -> {
            put("type", "array")
            put("items", descriptorToSchema(descriptor.getElementDescriptor(0)))
        }
        StructureKind.MAP -> {
            put("type", "object")
            // element 0 = key (always string in JSON), element 1 = value
            put("additionalProperties", descriptorToSchema(descriptor.getElementDescriptor(1)))
        }
        PrimitiveKind.STRING -> put("type", "string")
        PrimitiveKind.INT, PrimitiveKind.LONG, PrimitiveKind.SHORT, PrimitiveKind.BYTE ->
            put("type", "integer")
        PrimitiveKind.DOUBLE, PrimitiveKind.FLOAT -> put("type", "number")
        PrimitiveKind.BOOLEAN -> put("type", "boolean")
        else -> {} // JsonElement (PolymorphicKind.SEALED) and unknowns → {} = any
    }
}

private fun JsonObjectBuilder.buildClassSchema(descriptor: SerialDescriptor) {
    put("type", "object")
    val requiredFields = (0 until descriptor.elementsCount)
        .filter { !descriptor.isElementOptional(it) }
        .map { descriptor.getElementName(it) }
    if (requiredFields.isNotEmpty()) {
        put("required", JsonArray(requiredFields.map { JsonPrimitive(it) }))
    }
    val properties = buildJsonObject {
        for (i in 0 until descriptor.elementsCount) {
            put(descriptor.getElementName(i), descriptorToSchema(descriptor.getElementDescriptor(i)))
        }
    }
    put("properties", properties)
}
