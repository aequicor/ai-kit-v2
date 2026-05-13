package io.aequicor.aikit.core

sealed interface AiKitError {
    data class ParseError(val message: String, val cause: Throwable? = null) : AiKitError
    data class ValidationError(val message: String) : AiKitError
    data class ResolverError(val message: String, val cause: Throwable? = null) : AiKitError
    data class GenerationError(val message: String, val cause: Throwable? = null) : AiKitError
    data class UnknownAgent(val id: AgentId) : AiKitError
}
