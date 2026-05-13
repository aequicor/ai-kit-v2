package io.aequicor.aikit.akel

import kotlin.test.Test
import kotlin.test.assertEquals

class WhenExpressionResourceTest {

    @Test
    fun whenExpressionsFromConfigJsonExample() {
        loadWhenCases("when-expressions.json").forEachIndexed { i, case ->
            val actual = Akel.evaluate(case.expr, case.context).getOrElse { ex ->
                throw AssertionError("case $i [${case.expr}] threw: ${ex.message}", ex)
            }
            assertEquals(case.expected, actual, "case $i: ${case.expr}")
        }
    }

    @Test
    fun edgeCases() {
        loadWhenCases("edge-cases.json").forEachIndexed { i, case ->
            val actual = Akel.evaluate(case.expr, case.context).getOrElse { ex ->
                throw AssertionError("case $i [${case.expr}] threw: ${ex.message}", ex)
            }
            assertEquals(case.expected, actual, "case $i: ${case.expr}")
        }
    }
}
