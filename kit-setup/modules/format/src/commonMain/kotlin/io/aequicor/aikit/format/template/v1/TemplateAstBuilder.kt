package io.aequicor.aikit.format.template.v1

import io.aequicor.aikit.format.template.TemplatePart
import io.aequicor.aikit.format.error.FormatError

/**
 * Converts the flat token stream from [TemplateLexer] into a [TemplatePart] tree.
 *
 * Nesting is allowed: a [TemplateToken.WhenOpen] inside a conditional block's body opens a new
 * level; it must be closed by a matching [TemplateToken.WhenClose] before the outer block closes.
 *
 * Errors on unmatched delimiters:
 * - `<!-- end -->` without a preceding `<!-- when -->` → [FormatError.MalformedTemplate].
 * - `<!-- when -->` that is never closed → [FormatError.MalformedTemplate].
 */
internal object TemplateAstBuilder {

    fun build(tokens: List<TemplateToken>, path: String): Result<List<TemplatePart>> = runCatching {
        val stack = ArrayDeque<Pair<String, MutableList<TemplatePart>>>()
        val root = mutableListOf<TemplatePart>()
        var current = root

        for (token in tokens) {
            when (token) {
                is TemplateToken.Text -> {
                    current += TemplatePart.Literal(token.value)
                }

                is TemplateToken.Substitution -> {
                    current += TemplatePart.Substitution(token.inputId)
                }

                is TemplateToken.WhenOpen -> {
                    val body = mutableListOf<TemplatePart>()
                    stack.addLast(token.expr to body)
                    current = body
                }

                TemplateToken.WhenClose -> {
                    if (stack.isEmpty()) {
                        throw FormatError.MalformedTemplate(path, "unexpected <!-- end --> without matching <!-- when -->")
                    }
                    val (expr, body) = stack.removeLast()
                    current = if (stack.isEmpty()) root else stack.last().second
                    current += TemplatePart.Conditional(expr, body)
                }
            }
        }

        if (stack.isNotEmpty()) {
            val unclosed = stack.map { it.first }
            throw FormatError.MalformedTemplate(path, "unclosed <!-- when --> block(s): $unclosed")
        }

        root
    }
}
