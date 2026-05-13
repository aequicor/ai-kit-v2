package io.aequicor.aikit.akel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.fail

class AkelTest {

    private fun ctx(vararg entries: Pair<String, AkelValue>): AkelContext =
        AkelContext.of(entries.toMap())

    private fun eval(source: String, ctx: AkelContext = AkelContext.EMPTY): Boolean =
        Akel.evaluate(source, ctx).getOrThrow()

    @Test
    fun literals() {
        assertTrue(eval("true"))
        assertEquals(false, eval("false"))
    }

    @Test
    fun logicalOperators() {
        assertTrue(eval("true && true"))
        assertEquals(false, eval("true && false"))
        assertTrue(eval("false || true"))
        assertTrue(eval("!false"))
        assertTrue(eval("!(false || false)"))
    }

    @Test
    fun shortCircuitDoesNotResolveUnknownRef() {
        assertEquals(false, eval("false && \${bundle.input.missing}"))
        assertTrue(eval("true || \${bundle.input.missing}"))
    }

    @Test
    fun refLookupResolvesBoolean() {
        val c = ctx("bundle.input.flag" to AkelValue.Bool(true))
        assertTrue(eval("\${bundle.input.flag}", c))
        assertEquals(false, eval("!\${bundle.input.flag}", c))
    }

    @Test
    fun refWithArbitraryNamespace() {
        val c = ctx(
            "project.name" to AkelValue.Str("ai-kit"),
            "bundle.meta.version" to AkelValue.Str("1.0.0"),
        )
        assertTrue(eval("\${project.name} == 'ai-kit'", c))
        assertTrue(eval("\${bundle.meta.version} == '1.0.0'", c))
    }

    @Test
    fun refSingleSegment() {
        val c = ctx("flag" to AkelValue.Bool(true))
        assertTrue(eval("\${flag}", c))
    }

    @Test
    fun stringEquality() {
        val c = ctx("bundle.input.profile" to AkelValue.Str("full"))
        assertTrue(eval("\${bundle.input.profile} == 'full'", c))
        assertEquals(false, eval("\${bundle.input.profile} != 'full'", c))
    }

    @Test
    fun comparisonOperators() {
        val c = ctx("bundle.input.n" to AkelValue.Int(42))
        assertTrue(eval("\${bundle.input.n} > 10", c))
        assertTrue(eval("\${bundle.input.n} >= 42", c))
        assertEquals(false, eval("\${bundle.input.n} < 0", c))
    }

    @Test
    fun inWithLiteralList() {
        val c = ctx("bundle.input.p" to AkelValue.Str("ci"))
        assertTrue(eval("\${bundle.input.p} in ['ci', 'full']", c))
        assertEquals(false, eval("\${bundle.input.p} in ['dev']", c))
    }

    @Test
    fun inWithMultiselectRef() {
        val c = ctx(
            "bundle.input.skills" to AkelValue.Lst(
                listOf(AkelValue.Str("review"), AkelValue.Str("security-review")),
            ),
        )
        assertTrue(eval("'review' in \${bundle.input.skills}", c))
        assertEquals(false, eval("'unknown' in \${bundle.input.skills}", c))
    }

    @Test
    fun typeMismatchOnEqualityFails() {
        val c = ctx("bundle.input.n" to AkelValue.Int(5))
        val result = Akel.evaluate("\${bundle.input.n} == '5'", c)
        val ex = result.exceptionOrNull() ?: fail("expected failure")
        assertIs<AkelError.Type>(ex)
    }

    @Test
    fun unknownRefInActiveBranchFails() {
        val result = Akel.evaluate("\${bundle.input.absent}", AkelContext.EMPTY)
        val ex = result.exceptionOrNull() ?: fail("expected failure")
        assertIs<AkelError.UnknownRef>(ex)
        assertEquals("bundle.input.absent", ex.path)
    }

    @Test
    fun syntaxErrorReportsPosition() {
        val result = Akel.parse("true && (false")
        val ex = result.exceptionOrNull() ?: fail("expected failure")
        assertIs<AkelError.Syntax>(ex)
    }

    @Test
    fun precedenceMatchesSpec() {
        val c = ctx(
            "bundle.input.a" to AkelValue.Bool(true),
            "bundle.input.b" to AkelValue.Bool(false),
            "bundle.input.n" to AkelValue.Int(5),
        )
        assertTrue(eval("\${bundle.input.a} || \${bundle.input.b} && false", c))
        assertTrue(eval("!\${bundle.input.b} && \${bundle.input.n} > 0", c))
    }

    @Test
    fun parsedExpressionReusable() {
        val expr = Akel.parse("\${bundle.input.flag}").getOrThrow()
        val truthy = expr.evaluateAsBoolean(ctx("bundle.input.flag" to AkelValue.Bool(true))).getOrThrow()
        val falsy = expr.evaluateAsBoolean(ctx("bundle.input.flag" to AkelValue.Bool(false))).getOrThrow()
        assertTrue(truthy)
        assertEquals(false, falsy)
    }

    @Test
    fun doubleComparisons() {
        val c = ctx("bundle.input.x" to AkelValue.Dbl(0.7))
        assertTrue(eval("\${bundle.input.x} > 0.5", c))
        assertEquals(false, eval("\${bundle.input.x} < 0.5", c))
    }

    @Test
    fun negativeIntLiteral() {
        assertTrue(eval("-7 < 0"))
    }
}
