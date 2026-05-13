package io.aequicor.aikit.format.template

import io.aequicor.aikit.format.template.TemplatePart
import io.aequicor.aikit.format.AiKitFormat
import io.aequicor.aikit.format.error.FormatError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class TemplateBodyParserTest {

    private val format = AiKitFormat.create()

    @Test
    fun plain_text_becomes_single_literal() {
        val parts = format.parseTemplateBody("Hello world", "foo.md").getOrThrow()
        assertEquals(listOf(TemplatePart.Literal("Hello world")), parts)
    }

    @Test
    fun substitution_is_split_out() {
        val parts = format.parseTemplateBody("Hi \${bundle.input.name}!", "t.md").getOrThrow()
        assertEquals(
            listOf(
                TemplatePart.Literal("Hi "),
                TemplatePart.Substitution("name"),
                TemplatePart.Literal("!"),
            ),
            parts,
        )
    }

    @Test
    fun conditional_block_is_parsed() {
        val text = "before\n<!-- when: flag -->\nbody\n<!-- end -->\nafter"
        val parts = format.parseTemplateBody(text, "c.md").getOrThrow()

        assertEquals(3, parts.size)
        assertEquals(TemplatePart.Literal("before\n"), parts[0])
        val cond = assertIs<TemplatePart.Conditional>(parts[1])
        assertEquals("flag", cond.whenExpr)
        assertEquals(listOf(TemplatePart.Literal("\nbody\n")), cond.body)
        assertEquals(TemplatePart.Literal("\nafter"), parts[2])
    }

    @Test
    fun nested_conditionals() {
        val text = "<!-- when: a --><!-- when: b -->\ninner\n<!-- end -->\nouter\n<!-- end -->"
        val parts = format.parseTemplateBody(text, "n.md").getOrThrow()
        val outer = assertIs<TemplatePart.Conditional>(parts[0])
        assertEquals("a", outer.whenExpr)
        // outer.body = [Conditional("b",...), Literal("\nouter\n")]
        val inner = assertIs<TemplatePart.Conditional>(outer.body[0])
        assertEquals("b", inner.whenExpr)
        assertEquals(listOf(TemplatePart.Literal("\ninner\n")), inner.body)
        assertEquals(TemplatePart.Literal("\nouter\n"), outer.body[1])
    }

    @Test
    fun unclosed_when_block_fails() {
        val err = format.parseTemplateBody("<!-- when: x -->\nno end", "bad.md").exceptionOrNull()
        assertNotNull(err)
        assertIs<FormatError.MalformedTemplate>(err)
    }

    @Test
    fun unexpected_end_fails() {
        val err = format.parseTemplateBody("<!-- end -->", "bad.md").exceptionOrNull()
        assertNotNull(err)
        assertIs<FormatError.MalformedTemplate>(err)
    }

    @Test
    fun non_md_file_is_literal_passthrough() {
        val text = "\${bundle.input.x} <!-- when: foo -->bar<!-- end -->"
        val parts = format.parseTemplateBody(text, "plugin.js").getOrThrow()
        assertEquals(listOf(TemplatePart.Literal(text)), parts)
    }

    @Test
    fun empty_template_produces_empty_list() {
        val parts = format.parseTemplateBody("", "empty.md").getOrThrow()
        assertEquals(emptyList(), parts)
    }
}
