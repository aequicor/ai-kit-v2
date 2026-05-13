package io.aequicor.aikit.akel

/**
 * An error produced by parsing or evaluating an AKEL expression.
 *
 * All public AKEL operations return [Result] of these; the runtime never throws.
 */
sealed class AkelError(message: String) : RuntimeException(message) {

    /**
     * Syntactic error: lexing or parsing failure.
     *
     * @property position 0-based character offset in the source expression where the error was detected.
     */
    class Syntax(val position: Int, message: String) :
        AkelError("syntax error at $position: $message")

    /**
     * Type error: an operator was applied to incompatible operand types.
     */
    class Type(message: String) : AkelError("type error: $message")

    /**
     * Reference to an unresolved path (`${<path>}` where `<path>` is not bound in the context).
     *
     * @property path the full dotted reference path that could not be resolved.
     */
    class UnknownRef(val path: String) :
        AkelError("unknown reference: \${$path}")
}
