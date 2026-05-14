package io.aequicor.aikit.engine.template

internal object TemplatePreformatter {
    private val MULTIPLE_NEWLINES = Regex("\n{3,}")

    fun preformat(text: String): String =
        MULTIPLE_NEWLINES.replace(text, "\n\n")
}
