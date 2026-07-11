package io.aequicor.aikit.io

import kotlin.test.Test
import kotlin.test.assertEquals

class SourceRefsTest {

    @Test
    fun internal_mapsToEmbedded() {
        assertEquals(
            "embedded:simple-kit@0.0.1",
            SourceRefs.effective("internal", "simple-kit@0.0.1").getOrThrow(),
        )
    }

    @Test
    fun remoteSentinel_mapsToDefaultRegistry() {
        assertEquals(
            "remote:aequicor/ai-kit-v2/my-bundle@main",
            SourceRefs.effective("remote", "my-bundle@0.1.0").getOrThrow(),
        )
    }

    @Test
    fun explicitReferences_passThroughUnchanged() {
        val refs = listOf(
            "./.aikit/bundles/x",
            "zip:./.aikit/bundles/x.zip",
            "embedded:simple-kit",
            "remote:acme/presets/bundle@dev",
        )

        refs.forEach { raw ->
            assertEquals(raw, SourceRefs.effective(raw, "x@1.0.0").getOrThrow())
        }
    }
}
