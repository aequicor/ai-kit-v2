package io.aequicor.aikit.engine.template

import io.aequicor.aikit.akel.Akel
import io.aequicor.aikit.akel.AkelContext
import io.aequicor.aikit.akel.AkelValue
import io.aequicor.aikit.engine.error.EngineError
import io.aequicor.aikit.format.template.TemplatePart

/**
 * Walks a [TemplatePart] tree and produces a final UTF-8 string.
 *
 * - [TemplatePart.Literal]: copied verbatim.
 * - [TemplatePart.Substitution]: resolved from [inputs] using the `bundle.input.<id>` key.
 * - [TemplatePart.Conditional]: the AKEL `whenExpr` is evaluated against [inputs]; the body is
 *   rendered only when the expression is truthy.
 */
internal object TemplateRenderer {

    fun render(
        parts: List<TemplatePart>,
        inputs: Map<String, AkelValue>,
        templatePath: String,
    ): Result<String> = runCatching {
        val context = AkelContext.of(inputs.mapKeys { "bundle.input.${it.key}" })
        buildString { appendParts(parts, context, templatePath) }
    }

    private fun StringBuilder.appendParts(
        parts: List<TemplatePart>,
        context: AkelContext,
        templatePath: String,
    ) {
        for (part in parts) {
            when (part) {
                is TemplatePart.Literal -> append(part.text)

                is TemplatePart.Substitution -> {
                    val value = context.lookup("bundle.input.${part.inputId}")
                        ?: throw EngineError.RenderError(
                            templatePath,
                            "unresolved input reference '\${bundle.input.${part.inputId}}' in $templatePath",
                        )
                    append(akelValueToString(value))
                }

                is TemplatePart.Conditional -> {
                    val include = Akel.evaluate(part.whenExpr, context).getOrElse { cause ->
                        throw EngineError.RenderError(
                            templatePath,
                            "failed to evaluate 'when: ${part.whenExpr}' in $templatePath: ${cause.message}",
                            cause,
                        )
                    }
                    if (include) appendParts(part.body, context, templatePath)
                }
            }
        }
    }

    private fun akelValueToString(value: AkelValue): String = when (value) {
        is AkelValue.Bool -> value.value.toString()
        is AkelValue.Str -> value.value
        is AkelValue.Int -> value.value.toString()
        is AkelValue.Dbl -> value.value.toString()
        is AkelValue.Lst -> value.elements.joinToString(", ") { akelValueToString(it) }
    }
}
