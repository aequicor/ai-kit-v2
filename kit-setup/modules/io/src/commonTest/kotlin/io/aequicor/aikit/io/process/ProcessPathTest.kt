package io.aequicor.aikit.io.process

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProcessPathTest {
    @Test
    fun `windows path becomes a safe git argument`() {
        val normalized = processPath("C:\\Users\\dev\\project/.aikit/cache")
        assertEquals("C:/Users/dev/project/.aikit/cache", normalized)
        assertTrue(isSafeProcessToken(normalized))
    }
}
