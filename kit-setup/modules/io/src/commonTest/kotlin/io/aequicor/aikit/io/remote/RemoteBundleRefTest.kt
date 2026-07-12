package io.aequicor.aikit.io.remote

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RemoteBundleRefTest {

    @Test
    fun parse_fullForm() {
        val ref = RemoteBundleRef.parse("remote:acme/presets/bundles/kotlin@stable").getOrThrow()

        assertEquals("acme", ref.owner)
        assertEquals("presets", ref.repo)
        assertEquals("bundles/kotlin", ref.path)
        assertEquals("stable", ref.branch)
        assertEquals("https://github.com/acme/presets.git", ref.repoUrl)
    }

    @Test
    fun parse_defaultsBranchToMain() {
        val ref = RemoteBundleRef.parse("remote:acme/presets/my-bundle").getOrThrow()

        assertEquals("main", ref.branch)
    }

    @Test
    fun defaultFor_pointsAtCanonicalRegistry() {
        val ref = RemoteBundleRef.defaultFor("my-bundle@0.3.0").getOrThrow()

        assertEquals("remote:aequicor/ai-kit-v2/bundles/my-bundle/0.3.0@main", ref.toRefString())
    }

    @Test
    fun toRefString_roundTrips() {
        val raw = "remote:acme/presets/bundles/kotlin@stable"

        assertEquals(raw, RemoteBundleRef.parse(raw).getOrThrow().toRefString())
    }

    @Test
    fun parse_rejectsMalformedReferences() {
        val bad = listOf(
            "remote:",                             // empty
            "remote:acme",                         // missing repo + path
            "remote:acme/presets",                 // missing path
            "remote:acme/presets/../secrets",      // path traversal
            "remote:acme/presets/.",               // dot segment
            "remote:acme/pre sets/bundle",         // whitespace
            "remote:acme/presets/bundle@br..anch", // traversal in branch
            "remote:acme/presets/bundle@br;rm",    // shell metacharacter in branch
            "remote:acme/presets/bu;ndle",         // shell metacharacter in path
            "remote:ac\$me/presets/bundle",        // shell metacharacter in owner
            "zip:./x.zip",                         // wrong scheme
        )

        bad.forEach { raw ->
            assertTrue(RemoteBundleRef.parse(raw).isFailure, "expected failure for '$raw'")
        }
    }
}
