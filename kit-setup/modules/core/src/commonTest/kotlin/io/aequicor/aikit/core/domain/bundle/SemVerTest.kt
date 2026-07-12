package io.aequicor.aikit.core.domain.bundle

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SemVerTest {
    @Test
    fun `range includes lower bound and excludes upper bound`() {
        val range = SemVerRange.parse(">=1.0.0 <2.0.0").getOrThrow()
        assertTrue(range.contains(SemVer.parse("1.0.0").getOrThrow()))
        assertTrue(range.contains(SemVer.parse("1.9.9").getOrThrow()))
        assertFalse(range.contains(SemVer.parse("2.0.0").getOrThrow()))
    }

    @Test
    fun `prerelease follows SemVer precedence`() {
        val release = SemVer.parse("1.0.0").getOrThrow()
        val prerelease = SemVer.parse("1.0.0-rc.1").getOrThrow()
        assertTrue(prerelease < release)
    }

    @Test
    fun `invalid ranges fail`() {
        assertTrue(SemVerRange.parse("^1.0.0").isFailure)
        assertTrue(SemVerRange.parse("").isFailure)
        assertTrue(SemVerRange.parse(">=1.0").isFailure)
    }
}
