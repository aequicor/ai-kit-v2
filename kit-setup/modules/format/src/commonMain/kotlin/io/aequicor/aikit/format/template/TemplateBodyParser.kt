package io.aequicor.aikit.format.template

import io.aequicor.aikit.format.template.TemplatePart
import io.aequicor.aikit.format.template.v1.TemplateAstBuilder
import io.aequicor.aikit.format.template.v1.TemplateLexer

/**
 * Parses raw template file text into a [TemplatePart] AST.
 *
 * Non-template files (binary assets, `.js` plugins, etc.) are passed through as a single
 * [TemplatePart.Literal] containing the entire file content.
 */
internal class TemplateBodyParser {

    fun parse(text: String, path: String): Result<List<TemplatePart>> {
        if (!isTextTemplate(path)) {
            return Result.success(listOf(TemplatePart.Literal(text)))
        }
        val tokens = TemplateLexer.tokenize(text)
        return TemplateAstBuilder.build(tokens, path)
    }

    private fun isTextTemplate(path: String): Boolean =
        TEXT_EXTENSIONS.any { path.endsWith(it, ignoreCase = true) }

    private companion object {
        val TEXT_EXTENSIONS = setOf(".md", ".txt", ".json", ".yaml", ".yml", ".toml", ".sh", ".bash")
    }
}
