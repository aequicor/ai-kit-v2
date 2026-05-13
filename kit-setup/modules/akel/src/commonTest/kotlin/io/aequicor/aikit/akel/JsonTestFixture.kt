package io.aequicor.aikit.akel

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val RESOURCES_DIR = "src/commonTest/resources"

/**
 * A single AKEL evaluation test case loaded from a JSON file.
 *
 * @property expr the AKEL expression string (value of a `when` field).
 * @property context resolved inputs as an [AkelContext].
 * @property expected expected boolean result.
 */
data class WhenCase(
    val expr: String,
    val context: AkelContext,
    val expected: Boolean,
)

/** Reads and parses all [WhenCase] entries from a JSON test-resource file. */
fun loadWhenCases(fileName: String): List<WhenCase> {
    val text = SystemFileSystem.source(Path("$RESOURCES_DIR/$fileName")).buffered().readString()
    return Json.parseToJsonElement(text).jsonArray.map { element ->
        val obj = element.jsonObject
        WhenCase(
            expr = obj.getValue("expr").jsonPrimitive.content,
            context = parseContext(obj.getValue("ctx").jsonObject),
            expected = obj.getValue("expected").jsonPrimitive.boolean,
        )
    }
}

private fun parseContext(obj: JsonObject): AkelContext =
    AkelContext.of(obj.mapValues { (_, v) -> v.toAkelValue() })

private fun JsonPrimitive.toAkelValue(): AkelValue = when {
    booleanOrNull != null && (content == "true" || content == "false") -> AkelValue.Bool(boolean)
    intOrNull != null -> AkelValue.Int(int)
    doubleOrNull != null -> AkelValue.Dbl(double)
    else -> AkelValue.Str(content)
}

private fun kotlinx.serialization.json.JsonElement.toAkelValue(): AkelValue = when (this) {
    is JsonPrimitive -> toAkelValue()
    is JsonArray -> AkelValue.Lst(map { it.jsonPrimitive.toAkelValue() })
    else -> error("unsupported JSON element type in test context: $this")
}
