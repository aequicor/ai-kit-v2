package io.aequicor.aikit.format.template

/**
 * A node in the parsed template AST produced by [TemplateBodyParser].
 *
 * The engine walks this tree at render-time, substituting input values and evaluating
 * `when`-conditions via AKEL. This type is internal to the `format`/`engine` layer;
 * it is NOT a core domain concept — [io.aequicor.aikit.core.domain.template.Template.bytes]
 * holds the final rendered content.
 */
sealed interface TemplatePart {
    /** A verbatim text segment with no AI-Kit syntax. */
    data class Literal(val text: String) : TemplatePart

    /** `${bundle.input.<inputId>}` placeholder; resolved by the engine. */
    data class Substitution(val inputId: String) : TemplatePart

    /**
     * `<!-- when: <whenExpr> --> … <!-- end -->` block.
     *
     * [whenExpr] is the raw AKEL expression string, evaluated by the engine.
     * [body] is the list of child nodes rendered only when [whenExpr] is truthy.
     */
    data class Conditional(val whenExpr: String, val body: List<TemplatePart>) : TemplatePart
}
