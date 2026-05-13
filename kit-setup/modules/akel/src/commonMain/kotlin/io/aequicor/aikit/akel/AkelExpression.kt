package io.aequicor.aikit.akel

import io.aequicor.aikit.akel.internal.Evaluator
import io.aequicor.aikit.akel.internal.Node

/**
 * A parsed, ready-to-evaluate AKEL expression.
 *
 * Instances are immutable and safe to reuse across evaluations.
 */
class AkelExpression internal constructor(internal val root: Node) {

    /**
     * Evaluates the expression against [context].
     *
     * Returns:
     * - [AkelError.UnknownRef] if a reachable `${<path>}` resolves to `null`.
     * - [AkelError.Type] if operands have incompatible types.
     * - The evaluated [AkelValue] otherwise.
     *
     * Short-circuiting in `&&` / `||` is respected — references inside an unevaluated branch
     * are never resolved and never produce errors.
     */
    fun evaluate(context: AkelContext): Result<AkelValue> =
        runCatching { Evaluator.eval(root, context) }

    /**
     * Convenience overload that evaluates and casts the result to [Boolean].
     *
     * Returns [AkelError.Type] if the expression resolves to a non-boolean value.
     */
    fun evaluateAsBoolean(context: AkelContext): Result<Boolean> =
        evaluate(context).mapCatching { value ->
            (value as? AkelValue.Bool)?.value
                ?: throw AkelError.Type("expected boolean result, got ${value.type}")
        }
}
