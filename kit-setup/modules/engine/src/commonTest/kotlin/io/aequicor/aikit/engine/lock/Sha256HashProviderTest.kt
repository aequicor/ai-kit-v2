package io.aequicor.aikit.engine.lock

import kotlin.test.Test
import kotlin.test.assertEquals

class Sha256HashProviderTest {

    private val sha = Sha256HashProvider()

    @Test
    fun hashEmpty() {
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            sha.hash(ByteArray(0)),
        )
    }

    @Test
    fun hashAbc() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            sha.hash("abc".encodeToByteArray()),
        )
    }

    @Test
    fun hashLongMessage() {
        val text = "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq"
        assertEquals(
            "248d6a61d20638b8e5c026930c3e6039a33ce45964ff2167f6ecedd419db06c1",
            sha.hash(text.encodeToByteArray()),
        )
    }

    @Test
    fun hashDeterministicAcrossCalls() {
        val payload = "the quick brown fox jumps over the lazy dog".encodeToByteArray()
        assertEquals(sha.hash(payload), sha.hash(payload))
    }
}
