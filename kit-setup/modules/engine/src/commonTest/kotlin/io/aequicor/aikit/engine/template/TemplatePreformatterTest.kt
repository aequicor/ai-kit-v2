package io.aequicor.aikit.engine.template

import kotlin.test.Test
import kotlin.test.assertEquals

class TemplatePreformatterTest {

    @Test
    fun doubleNewlineCollapsed() {
        assertEquals("a\nb", TemplatePreformatter.preformat("a\n\nb"))
    }

    @Test
    fun tripleNewlineCollapsed() {
        assertEquals("a\nb", TemplatePreformatter.preformat("a\n\n\nb"))
    }

    @Test
    fun manyNewlinesCollapsed() {
        assertEquals("a\nb", TemplatePreformatter.preformat("a\n\n\n\n\nb"))
    }

    @Test
    fun singleNewlineUnchanged() {
        assertEquals("a\nb", TemplatePreformatter.preformat("a\nb"))
    }

    @Test
    fun noNewlineUnchanged() {
        assertEquals("hello", TemplatePreformatter.preformat("hello"))
    }

    @Test
    fun emptyStringUnchanged() {
        assertEquals("", TemplatePreformatter.preformat(""))
    }

    @Test
    fun multipleGroupsCollapsed() {
        assertEquals("a\nb\nc", TemplatePreformatter.preformat("a\n\nb\n\n\nc"))
    }
}
