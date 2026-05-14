package io.aequicor.aikit.engine.lock

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private val json = Json { ignoreUnknownKeys = true }

class LockFileMigrationTest {

    @Test
    fun legacyLockWithoutManifestRefDeserializesWithNull() {
        val legacyJson = """
            {
              "lockVersion": "1",
              "aikitVersion": "0.0.12",
              "generatedAt": "2024-01-01T00:00:00Z",
              "applications": []
            }
        """.trimIndent()
        val lock = json.decodeFromString(LockFile.serializer(), legacyJson)
        assertNull(lock.manifestRef)
        assertEquals("1", lock.lockVersion)
        assertEquals("0.0.12", lock.aikitVersion)
    }

    @Test
    fun lockWithManifestRefRoundTrips() {
        val original = LockFile(
            lockVersion = "1",
            aikitVersion = "0.1.0",
            generatedAt = "2025-01-01T00:00:00Z",
            applications = emptyList(),
            manifestRef = ".aikit/manifest.autonomous.json",
        )
        val encoded = json.encodeToString(LockFile.serializer(), original)
        val decoded = json.decodeFromString(LockFile.serializer(), encoded)
        assertEquals(original, decoded)
        assertEquals(".aikit/manifest.autonomous.json", decoded.manifestRef)
    }

    @Test
    fun lockWithNullManifestRefRoundTrips() {
        val original = LockFile(
            lockVersion = "1",
            aikitVersion = "0.1.0",
            generatedAt = "2025-01-01T00:00:00Z",
            applications = emptyList(),
            manifestRef = null,
        )
        val decoded = json.decodeFromString(
            LockFile.serializer(),
            json.encodeToString(LockFile.serializer(), original),
        )
        assertNull(decoded.manifestRef)
    }

    @Test
    fun legacyLockWithExtraFieldsDeserializesGracefully() {
        val withExtra = """
            {
              "lockVersion": "1",
              "aikitVersion": "0.0.9",
              "generatedAt": "2023-06-01T10:00:00Z",
              "applications": [],
              "futureField": "some value"
            }
        """.trimIndent()
        val lock = json.decodeFromString(LockFile.serializer(), withExtra)
        assertNull(lock.manifestRef)
        assertEquals("0.0.9", lock.aikitVersion)
    }
}
