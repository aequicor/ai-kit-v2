package io.aequicor.aikit.io

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SourceRefsTest {

    @Test
    fun internal_isRejectedWithMigrationAdvice() {
        val result = SourceRefs.effective("internal", "simple-kit@0.0.1")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("local directory or ZIP"))
    }

    @Test
    fun remoteSentinel_mapsToDefaultRegistry() {
        assertEquals(
            "remote:aequicor/ai-kit-v2/bundles/my-bundle/0.1.0@main",
            SourceRefs.effective("remote", "my-bundle@0.1.0").getOrThrow(),
        )
    }

    @Test
    fun explicitReferences_passThroughUnchanged() {
        val refs = listOf(
            "./.aikit/bundles/x",
            "zip:./.aikit/bundles/x.zip",
            "remote:acme/presets/bundle@dev",
        )

        refs.forEach { raw ->
            assertEquals(raw, SourceRefs.effective(raw, "x@1.0.0").getOrThrow())
        }
        assertTrue(SourceRefs.effective("embedded:simple-kit", "x@1.0.0").isFailure)
    }
}
