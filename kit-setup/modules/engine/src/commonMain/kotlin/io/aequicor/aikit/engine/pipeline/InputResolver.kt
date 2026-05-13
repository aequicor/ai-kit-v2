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
    ): Result<Map<String, AkelValue>> = runCatching {
        buildMap {
            for (spec in specs) {
                val value = coerce(spec, rawInputs[spec.id])
                if (value != null) put(spec.id, value)
            }
        }
    }

    private fun coerce(spec: InputSpec, raw: JsonElement?): AkelValue? = when (spec) {
        is InputSpec.BoolInput -> coerceBool(spec, raw)
        is InputSpec.StringInput -> coerceString(spec, raw)
        is InputSpec.SelectInput -> coerceSelect(spec, raw)
        is InputSpec.MultiSelectInput -> coerceMultiSelect(spec, raw)
        is InputSpec.IntInput -> coerceInt(spec, raw)
        is InputSpec.DoubleInput -> coerceDouble(spec, raw)
    }

    private fun coerceBool(spec: InputSpec.BoolInput, raw: JsonElement?): AkelValue? {
        val b = (raw as? JsonPrimitive)?.booleanOrNull ?: spec.default
        return b?.let { AkelValue.Bool(it) }
    }

    private fun coerceString(spec: InputSpec.StringInput, raw: JsonElement?): AkelValue? {
        val s = (raw as? JsonPrimitive)?.content ?: spec.default
        if (s == null && spec.required == true) {
            throw EngineError.InputValidationError(spec.id, "required input '${spec.id}' is missing")
        }
        return s?.let { AkelValue.Str(it) }
    }

    private fun coerceSelect(spec: InputSpec.SelectInput, raw: JsonElement?): AkelValue? {
        val s = (raw as? JsonPrimitive)?.content ?: spec.default
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
