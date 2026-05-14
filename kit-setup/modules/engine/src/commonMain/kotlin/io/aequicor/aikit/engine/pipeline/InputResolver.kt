package io.aequicor.aikit.engine.pipeline

import io.aequicor.aikit.akel.AkelValue
import io.aequicor.aikit.core.domain.bundle.InputSpec
import io.aequicor.aikit.engine.error.EngineError
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull

/**
 * Resolves raw JSON input values from the project manifest into typed [AkelValue]s,
 * applying defaults from the bundle's [InputSpec] when a value is absent.
 */
internal object InputResolver {

    fun resolve(
        specs: List<InputSpec>,
        rawInputs: Map<String, JsonElement>,
        cwdBasename: String = "",
    ): Result<Map<String, AkelValue>> = runCatching {
        buildMap {
            for (spec in specs) {
                val value = coerce(spec, rawInputs[spec.id], cwdBasename)
                if (value != null) put(spec.id, value)
            }
        }
    }

    private fun coerce(spec: InputSpec, raw: JsonElement?, cwdBasename: String): AkelValue? = when (spec) {
        is InputSpec.BoolInput -> coerceBool(spec, raw)
        is InputSpec.StringInput -> coerceString(spec, raw, cwdBasename)
        is InputSpec.SelectInput -> coerceSelect(spec, raw, cwdBasename)
        is InputSpec.MultiSelectInput -> coerceMultiSelect(spec, raw)
        is InputSpec.IntInput -> coerceInt(spec, raw)
        is InputSpec.DoubleInput -> coerceDouble(spec, raw)
    }

    private fun resolveDefault(default: String?, cwdBasename: String): String? =
        default?.replace("\${cwd.basename}", cwdBasename)

    private fun coerceBool(spec: InputSpec.BoolInput, raw: JsonElement?): AkelValue? {
        if (raw is JsonPrimitive && raw.booleanOrNull == null) {
            throw EngineError.InputValidationError(
                spec.id,
                "input '${spec.id}' must be a boolean (true/false), got: ${raw.content}",
            )
        }
        val b = (raw as? JsonPrimitive)?.booleanOrNull ?: spec.default
        return b?.let { AkelValue.Bool(it) }
    }

    private fun coerceString(spec: InputSpec.StringInput, raw: JsonElement?, cwdBasename: String): AkelValue? {
        val s = (raw as? JsonPrimitive)?.content ?: resolveDefault(spec.default, cwdBasename)
        if (s == null && spec.required == true) {
            throw EngineError.InputValidationError(spec.id, "required input '${spec.id}' is missing")
        }
        return s?.let { AkelValue.Str(it) }
    }

    private fun coerceSelect(spec: InputSpec.SelectInput, raw: JsonElement?, cwdBasename: String): AkelValue? {
        val s = (raw as? JsonPrimitive)?.content ?: resolveDefault(spec.default, cwdBasename)
        if (s != null && s !in spec.options) {
            throw EngineError.InputValidationError(
                spec.id,
                "input '${spec.id}' value '$s' is not one of ${spec.options}",
            )
        }
        return s?.let { AkelValue.Str(it) }
    }

    private fun coerceMultiSelect(spec: InputSpec.MultiSelectInput, raw: JsonElement?): AkelValue {
        val list = when {
            raw is JsonArray -> raw.map { (it as JsonPrimitive).content }
            raw == null || raw is JsonNull -> spec.default ?: emptyList()
            else -> throw EngineError.InputValidationError(spec.id, "input '${spec.id}' must be an array")
        }
        val invalid = list.filter { it !in spec.options }
        if (invalid.isNotEmpty()) {
            throw EngineError.InputValidationError(
                spec.id,
                "input '${spec.id}' contains invalid values $invalid; allowed: ${spec.options}",
            )
        }
        return AkelValue.Lst(list.map { AkelValue.Str(it) })
    }

    private fun coerceInt(spec: InputSpec.IntInput, raw: JsonElement?): AkelValue? {
        val n = (raw as? JsonPrimitive)?.intOrNull ?: spec.default
        if (n == null && spec.required == true) {
            throw EngineError.InputValidationError(spec.id, "required input '${spec.id}' is missing")
        }
        return n?.let { AkelValue.Int(it) }
    }

    private fun coerceDouble(spec: InputSpec.DoubleInput, raw: JsonElement?): AkelValue? {
        val d = (raw as? JsonPrimitive)?.doubleOrNull ?: spec.default
        if (d == null && spec.required == true) {
            throw EngineError.InputValidationError(spec.id, "required input '${spec.id}' is missing")
        }
        return d?.let { AkelValue.Dbl(it) }
    }
}
