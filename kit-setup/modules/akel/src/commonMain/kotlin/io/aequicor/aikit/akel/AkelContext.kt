package io.aequicor.aikit.akel

/**
 * Resolver for `${<path>}` references during evaluation.
 *
 * AKEL itself does not assign meaning to reference paths — they are arbitrary dotted strings
 * (e.g. `bundle.input.profile`, `bundle.meta.version`, `project.name`). The integrating layer
 * decides which namespaces it supports and how to resolve them.
 *
 * Implementations are pure: the same path must always resolve to the same value within a single
 * evaluation. A simple map-backed implementation is provided by [AkelContext.of].
 */
fun interface AkelContext {

    /**
     * Returns the value bound to [path], or `null` if no binding exists.
     *
     * The [path] is the full dotted string between `${` and `}` — e.g. for the reference
     * `${bundle.input.flag}` the lookup receives `"bundle.input.flag"`.
     *
     * Returning `null` causes the evaluator to fail with [AkelError.UnknownRef] — unless the
     * reference sits in a branch that was already short-circuited away.
     */
    fun lookup(path: String): AkelValue?

    companion object {

        /** An empty context — every lookup returns `null`. */
        val EMPTY: AkelContext = AkelContext { null }

        /** Builds a context from a plain map of full reference path → value. */
        fun of(bindings: Map<String, AkelValue>): AkelContext =
            AkelContext { path -> bindings[path] }
    }
}
