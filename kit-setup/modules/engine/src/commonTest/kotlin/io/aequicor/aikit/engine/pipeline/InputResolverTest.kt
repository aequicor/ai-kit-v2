package io.aequicor.aikit.engine.pipeline

import io.aequicor.aikit.akel.AkelValue
import io.aequicor.aikit.core.domain.bundle.InputSpec
import io.aequicor.aikit.engine.error.EngineError
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class InputResolverTest {

    private fun fakeReader(env: Map<String, String> = emptyMap()): ValueSourceReader =
        object : ValueSourceReader {
            override fun readEnv(name: String): String? = env[name]
            override fun readFile(relativePath: String): Result<String> =
                Result.failure(Exception("no files in fake reader"))
        }

    // ─── coercion from string after env expansion ───────────────────────

    @Test fun envRefExpandedToInt() {
        val specs = listOf(InputSpec.IntInput("count", "Count"))
        val raw = mapOf("count" to JsonPrimitive("\${env:COUNT}"))
        val result = InputResolver.resolve(specs, raw, reader = fakeReader(env = mapOf("COUNT" to "42")))
            .getOrThrow()
        assertEquals(42, assertIs<AkelValue.Int>(result["count"]).value)
    }

    @Test fun envRefExpandedToDouble() {
        val specs = listOf(InputSpec.DoubleInput("rate", "Rate"))
        val raw = mapOf("rate" to JsonPrimitive("\${env:RATE}"))
        val result = InputResolver.resolve(specs, raw, reader = fakeReader(env = mapOf("RATE" to "3.14")))
            .getOrThrow()
        assertEquals(3.14, assertIs<AkelValue.Dbl>(result["rate"]).value)
    }

    @Test fun envRefExpandedToBoolTrue() {
        val specs = listOf(InputSpec.BoolInput("flag", "Flag"))
        val raw = mapOf("flag" to JsonPrimitive("\${env:FLAG}"))
        val result = InputResolver.resolve(specs, raw, reader = fakeReader(env = mapOf("FLAG" to "true")))
            .getOrThrow()
        assertEquals(true, assertIs<AkelValue.Bool>(result["flag"]).value)
    }

    @Test fun envRefExpandedToBoolFalse() {
        val specs = listOf(InputSpec.BoolInput("flag", "Flag"))
        val raw = mapOf("flag" to JsonPrimitive("\${env:FLAG}"))
        val result = InputResolver.resolve(specs, raw, reader = fakeReader(env = mapOf("FLAG" to "false")))
            .getOrThrow()
        assertEquals(false, assertIs<AkelValue.Bool>(result["flag"]).value)
    }

    @Test fun envRefExpandedToString() {
        val specs = listOf(InputSpec.StringInput("name", "Name"))
        val raw = mapOf("name" to JsonPrimitive("\${env:NAME}"))
        val result = InputResolver.resolve(specs, raw, reader = fakeReader(env = mapOf("NAME" to "billing-service")))
            .getOrThrow()
        assertEquals("billing-service", assertIs<AkelValue.Str>(result["name"]).value)
    }

    @Test fun partialInterpolationInString() {
        val specs = listOf(InputSpec.StringInput("name", "Name"))
        val raw = mapOf("name" to JsonPrimitive("prefix-\${env:APP}-suffix"))
        val result = InputResolver.resolve(specs, raw, reader = fakeReader(env = mapOf("APP" to "billing")))
            .getOrThrow()
        assertEquals("prefix-billing-suffix", assertIs<AkelValue.Str>(result["name"]).value)
    }

    // ─── string coercion without expansion ──────────────────────────────

    @Test fun stringValueForIntInputCoerced() {
        val specs = listOf(InputSpec.IntInput("count", "Count"))
        val raw = mapOf("count" to JsonPrimitive("99"))
        val result = InputResolver.resolve(specs, raw, reader = fakeReader()).getOrThrow()
        assertEquals(99, assertIs<AkelValue.Int>(result["count"]).value)
    }

    @Test fun stringValueForDoubleInputCoerced() {
        val specs = listOf(InputSpec.DoubleInput("val", "Val"))
        val raw = mapOf("val" to JsonPrimitive("1.5"))
        val result = InputResolver.resolve(specs, raw, reader = fakeReader()).getOrThrow()
        assertEquals(1.5, assertIs<AkelValue.Dbl>(result["val"]).value)
    }

    @Test fun stringValueForBoolInputCoerced() {
        val specs = listOf(InputSpec.BoolInput("flag", "Flag"))
        val raw = mapOf("flag" to JsonPrimitive("true"))
        val result = InputResolver.resolve(specs, raw, reader = fakeReader()).getOrThrow()
        assertEquals(true, assertIs<AkelValue.Bool>(result["flag"]).value)
    }

    // ─── error cases ────────────────────────────────────────────────────

    @Test fun missingEnvVarProducesInputValidationError() {
        val specs = listOf(InputSpec.StringInput("name", "Name"))
        val raw = mapOf("name" to JsonPrimitive("\${env:MISSING}"))
        val ex = assertFailsWith<EngineError.InputValidationError> {
            InputResolver.resolve(specs, raw, reader = fakeReader()).getOrThrow()
        }
        assertEquals("name", ex.inputId)
        assertTrue("MISSING" in (ex.message ?: ""))
    }

    @Test fun badStringForIntProducesError() {
        val specs = listOf(InputSpec.IntInput("count", "Count", required = true))
        val raw = mapOf("count" to JsonPrimitive("notanumber"))
        val ex = assertFailsWith<EngineError.InputValidationError> {
            InputResolver.resolve(specs, raw, reader = fakeReader()).getOrThrow()
        }
        assertEquals("count", ex.inputId)
    }

    // ─── no-reader path still works ─────────────────────────────────────

    @Test fun resolveWithoutReaderWorks() {
        val specs = listOf(InputSpec.StringInput("name", "Name", default = "my-app"))
        val result = InputResolver.resolve(specs, emptyMap()).getOrThrow()
        assertEquals("my-app", assertIs<AkelValue.Str>(result["name"]).value)
    }
}
