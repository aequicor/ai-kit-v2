package io.aequicor.aikit.akel

import io.aequicor.aikit.akel.internal.Lexer
import io.aequicor.aikit.akel.internal.Parser

/**
 * Public entry point for the AKEL (AI-Kit Expression Language) parser/evaluator.
 *
 * AKEL is a strictly-typed boolean expression language used in the `when` field of bundle
 * `config.json` manifests. See `kit-setup/CONFIG_JSON.md` for the language specification.
 *
 * References inside an expression have the form `${<dotted.path>}`. AKEL does not interpret the
 * path — the integrating layer resolves it through an [AkelContext] (e.g. `bundle.input.*`,
 * `bundle.meta.*`, `project.*` are all valid namespaces depending on the host application).
 *
 * Usage:
 * ```
 * val expr = Akel.parse("\${bundle.input.profile} == 'full'").getOrThrow()
 * val ctx = AkelContext.of(mapOf("bundle.input.profile" to AkelValue.Str("full")))
 * val result: Boolean = expr.evaluateAsBoolean(ctx).getOrThrow()
 * ```
 */
object Akel {

    /**
     * Parses [source] into an [AkelExpression].
     *
     * Returns [AkelError.Syntax] on any lexing or parsing failure. The expression is not
     * evaluated; references are not resolved at parse time.
     */
    fun parse(source: String): Result<AkelExpression> = runCatching {
        val tokens = Lexer(source).tokenize()
        val root = Parser(source, tokens).parseExpression()
        AkelExpression(root)
    }

    /**
     * One-shot helper: parses [source] and evaluates it as a boolean against [context].
     *
     * Equivalent to `parse(source).flatMap { it.evaluateAsBoolean(context) }`.
     */
    fun evaluate(source: String, context: AkelContext): Result<Boolean> =
        parse(source).mapCatching { it.evaluateAsBoolean(context).getOrThrow() }
}
