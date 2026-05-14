package io.aequicor.aikit.engine.impl

import io.aequicor.aikit.akel.Akel
import io.aequicor.aikit.akel.AkelContext
import io.aequicor.aikit.akel.AkelValue
import io.aequicor.aikit.engine.error.EngineError
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull

/**
 * Applies the two manifest-language passes defined in `CONFIG_JSON.md` to a parsed JSON tree:
 *
 * 1. Top-down `when` evaluation. For every [JsonObject] with a `when` field, the AKEL expression
 *    is evaluated; if `false`, the object is removed entirely (in arrays, the slot is dropped).
 *    If `true` or absent, the `when` key is stripped and recursion continues into children.
 * 2. `${bundle.input.<id>}` substitution. After filtering, every string in the tree is scanned
 *    for `${bundle.input.<id>}` placeholders. Each placeholder is replaced with the looked-up
 *    [AkelValue] rendered to its JSON-string form.
 */
internal object JsonTreeProcessor {

    private val PLACEHOLDER = Regex("""\$\{bundle\.input\.([A-Za-z_][A-Za-z0-9_-]*)\}""")

    /** Returns the processed tree, or `null` if the entire root object was filtered out. */
    fun process(root: JsonElement, inputs: Map<String, AkelValue>): JsonElement? {
        val context = AkelContext.of(inputs.mapKeys { "bundle.input.${it.key}" })
        val filtered = filterWhen(root, context) ?: return null
        return interpolate(filtered, context)
    }

    private fun filterWhen(element: JsonElement, context: AkelContext): JsonElement? = when (element) {
        is JsonObject -> filterObject(element, context)
        is JsonArray -> JsonArray(element.mapNotNull { filterWhen(it, context) })
        else -> element
    }

    private fun filterObject(obj: JsonObject, context: AkelContext): JsonObject? {
        val whenExpr = (obj["when"] as? JsonPrimitive)?.contentOrNull
        if (whenExpr != null) {
            val include = Akel.evaluate(whenExpr, context).getOrElse {
                throw EngineError.RenderError("config.json", "invalid AKEL expression in 'when': ${it.message}", it)
            }
            if (!include) return null
        }
        val newEntries = obj.entries
            .filter { it.key != "when" }
            .mapNotNull { (k, v) -> filterWhen(v, context)?.let { k to it } }
        return JsonObject(newEntries.toMap())
    }

    private fun interpolate(element: JsonElement, context: AkelContext): JsonElement = when (element) {
        is JsonObject -> JsonObject(element.mapValues { interpolate(it.value, context) })
        is JsonArray -> JsonArray(element.map { interpolate(it, context) })
        is JsonPrimitive -> if (element.isString) interpolateString(element.content, context) else element
    }

    private fun interpolateString(text: String, context: AkelContext): JsonPrimitive {
        if (!text.contains("\${bundle.input.")) return JsonPrimitive(text)
        val replaced = PLACEHOLDER.replace(text) { match ->
            val id = match.groupValues[1]
            val value = context.lookup("bundle.input.$id")
                ?: throw EngineError.RenderError(
                    "config.json",
                    "unresolved input reference '\${bundle.input.$id}' in config.json",
                )
            renderAkelValue(value)
        }
        return JsonPrimitive(replaced)
    }

    private fun renderAkelValue(value: AkelValue): String = when (value) {
        is AkelValue.Bool -> value.value.toString()
        is AkelValue.Int -> value.value.toString()
        is AkelValue.Dbl -> value.value.toString()
        is AkelValue.Str -> value.value
        is AkelValue.Lst -> value.elements.joinToString(",") { renderAkelValue(it) }
    }

    @Suppress("unused")
    private fun JsonPrimitive.coerceForLog(): String = booleanOrNull?.toString()
        ?: intOrNull?.toString()
        ?: doubleOrNull?.toString()
        ?: contentOrNull
        ?: ""
}
