package io.aequicor.aikit.format.template.v1

/**
 * Token types produced by [TemplateLexer].
 */
internal sealed interface TemplateToken {
    /** Plain text segment with no AI-Kit control syntax. */
    data class Text(val value: String) : TemplateToken

    /** `${bundle.input.<inputId>}` placeholder. */
    data class Substitution(val inputId: String) : TemplateToken

    /** `<!-- when: <expr> -->` opening delimiter. [expr] is the raw AKEL expression string. */
    data class WhenOpen(val expr: String) : TemplateToken

    /** `<!-- end -->` closing delimiter matching a previous [WhenOpen]. */
    data object WhenClose : TemplateToken
}

/**
 * Splits raw template text into a flat token stream.
 *
 * Recognised syntax:
 * - `${bundle.input.<id>}` — substitution placeholder
 * - `<!-- when: <expr> -->` — start of a conditional block (stripped from output)
 * - `<!-- end -->` — end of the nearest enclosing conditional block
 *
 * All other text (including other HTML comments) becomes [TemplateToken.Text].
 */
internal object TemplateLexer {

    private val SUBSTITUTION_PATTERN = Regex("""\$\{bundle\.input\.([a-zA-Z_][a-zA-Z0-9_\-]*)}""")
    private val WHEN_OPEN_PATTERN = Regex("""<!--\s*when:\s*(.*?)\s*-->""", RegexOption.DOT_MATCHES_ALL)
    private val WHEN_CLOSE_PATTERN = Regex("""<!--\s*end\s*-->""")

    fun tokenize(text: String): List<TemplateToken> {
        val tokens = mutableListOf<TemplateToken>()
        var pos = 0

        while (pos < text.length) {
            val remaining = text.substring(pos)

            val subMatch = SUBSTITUTION_PATTERN.find(remaining)
            val whenOpenMatch = WHEN_OPEN_PATTERN.find(remaining)
            val whenCloseMatch = WHEN_CLOSE_PATTERN.find(remaining)

            val earliest = listOfNotNull(subMatch, whenOpenMatch, whenCloseMatch)
                .minByOrNull { it.range.first }

            if (earliest == null) {
                if (pos < text.length) tokens += TemplateToken.Text(text.substring(pos))
                break
            }

            val matchStart = pos + earliest.range.first

            if (matchStart > pos) {
                tokens += TemplateToken.Text(text.substring(pos, matchStart))
            }

            when (earliest) {
                subMatch -> tokens += TemplateToken.Substitution(earliest.groupValues[1])
                whenOpenMatch -> tokens += TemplateToken.WhenOpen(earliest.groupValues[1].trim())
                whenCloseMatch -> tokens += TemplateToken.WhenClose
                else -> Unit
            }

            pos = matchStart + earliest.value.length
        }

        return tokens
    }
}
