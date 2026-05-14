package io.aequicor.aikit.format.bundle

import io.aequicor.aikit.core.domain.bundle.BundleManifest
import io.aequicor.aikit.core.domain.bundle.InputSpec
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Renders a JSON Schema (draft 2020-12) describing the `inputs` object accepted by a bundle.
 *
 * The schema is intended to be attached to the `targets.<name>.inputs` block of a project
 * manifest, so IDEs can autocomplete and validate user values against the spec declared in
 * the bundle's `bundle.json`.
 *
 * The function is pure — it takes a fully parsed [BundleManifest] and produces a string,
 * with no I/O of its own.
 */
internal fun renderBundleInputsSchema(manifest: BundleManifest, json: Json): String {
    val schema = buildJsonObject {
        put("\$schema", "https://json-schema.org/draft/2020-12/schema")
        put("title", "${manifest.name}@${manifest.version} inputs")
        put("type", "object")
        put("additionalProperties", false)
        put("properties", buildPropertiesObject(manifest.inputs))
        val required = manifest.inputs.filter { it.isRequired() }.map { it.id }
        if (required.isNotEmpty()) {
            put("required", JsonArray(required.map { JsonPrimitive(it) }))
        }
    }
    return json.encodeToString(JsonObject.serializer(), schema)
}

private fun buildPropertiesObject(inputs: List<InputSpec>): JsonObject = buildJsonObject {
    for (input in inputs) {
        put(input.id, renderInput(input))
    }
}

private fun renderInput(input: InputSpec): JsonObject = buildJsonObject {
    put("title", input.title)
    input.description?.let { put("description", it) }
    when (input) {
        is InputSpec.BoolInput -> renderBool(input)
        is InputSpec.StringInput -> renderString(input)
        is InputSpec.IntInput -> renderInt(input)
        is InputSpec.DoubleInput -> renderDouble(input)
        is InputSpec.SelectInput -> renderSelect(input)
        is InputSpec.MultiSelectInput -> renderMultiSelect(input)
    }
}

private fun kotlinx.serialization.json.JsonObjectBuilder.renderBool(input: InputSpec.BoolInput) {
    put("type", "boolean")
    input.default?.let { put("default", it) }
}

private fun kotlinx.serialization.json.JsonObjectBuilder.renderString(input: InputSpec.StringInput) {
    put("type", "string")
    input.default?.let { put("default", it) }
}

private fun kotlinx.serialization.json.JsonObjectBuilder.renderInt(input: InputSpec.IntInput) {
    put("type", "integer")
    input.min?.let { put("minimum", it) }
    input.max?.let { put("maximum", it) }
    input.default?.let { put("default", it) }
}

private fun kotlinx.serialization.json.JsonObjectBuilder.renderDouble(input: InputSpec.DoubleInput) {
    put("type", "number")
    input.min?.let { put("minimum", it) }
    input.max?.let { put("maximum", it) }
    input.default?.let { put("default", it) }
}

private fun kotlinx.serialization.json.JsonObjectBuilder.renderSelect(input: InputSpec.SelectInput) {
    put("type", "string")
    put("enum", JsonArray(input.options.map { JsonPrimitive(it) }))
    input.default?.let { put("default", it) }
}

private fun kotlinx.serialization.json.JsonObjectBuilder.renderMultiSelect(input: InputSpec.MultiSelectInput) {
    put("type", "array")
    put("uniqueItems", true)
    put("items", buildJsonObject {
        put("type", "string")
        put("enum", JsonArray(input.options.map { JsonPrimitive(it) }))
    })
    input.default?.let { values ->
        put("default", JsonArray(values.map { JsonPrimitive(it) }))
    }
}

private fun InputSpec.isRequired(): Boolean = when (this) {
    is InputSpec.BoolInput -> false
    is InputSpec.MultiSelectInput -> false
    is InputSpec.StringInput -> required == true && default == null
    is InputSpec.IntInput -> required == true && default == null
    is InputSpec.DoubleInput -> required == true && default == null
    is InputSpec.SelectInput -> default == null
}

