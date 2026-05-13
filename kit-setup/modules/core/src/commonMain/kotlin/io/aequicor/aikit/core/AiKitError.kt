package io.aequicor.aikit.core

/** Errors produced by AI-Kit operations; safe to use as Result failure values. */
sealed class AiKitError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    /** The manifest could not be parsed. */
    class ParseError(message: String, cause: Throwable? = null) : AiKitError(message, cause)

    /** The manifest is structurally valid but violates business rules. */
    class ValidationError(message: String) : AiKitError(message)

    /** A referenced bundle could not be located or read. */
    class ResolverError(message: String, cause: Throwable? = null) : AiKitError(message, cause)

    /** Writing agent configuration files failed. */
    class GenerationError(message: String, cause: Throwable? = null) : AiKitError(message, cause)
}
