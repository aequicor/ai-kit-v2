@file:Suppress("MagicNumber")

package io.aequicor.aikit.engine.lock

private const val BLOCK_SIZE = 64
private const val SCHEDULE_SIZE = 64
private const val DIGEST_BYTES = 32

private val K = intArrayOf(
    0x428a2f98.toInt(), 0x71374491.toInt(), 0xb5c0fbcf.toInt(), 0xe9b5dba5.toInt(),
    0x3956c25b.toInt(), 0x59f111f1.toInt(), 0x923f82a4.toInt(), 0xab1c5ed5.toInt(),
    0xd807aa98.toInt(), 0x12835b01.toInt(), 0x243185be.toInt(), 0x550c7dc3.toInt(),
    0x72be5d74.toInt(), 0x80deb1fe.toInt(), 0x9bdc06a7.toInt(), 0xc19bf174.toInt(),
    0xe49b69c1.toInt(), 0xefbe4786.toInt(), 0x0fc19dc6.toInt(), 0x240ca1cc.toInt(),
    0x2de92c6f.toInt(), 0x4a7484aa.toInt(), 0x5cb0a9dc.toInt(), 0x76f988da.toInt(),
    0x983e5152.toInt(), 0xa831c66d.toInt(), 0xb00327c8.toInt(), 0xbf597fc7.toInt(),
    0xc6e00bf3.toInt(), 0xd5a79147.toInt(), 0x06ca6351.toInt(), 0x14292967.toInt(),
    0x27b70a85.toInt(), 0x2e1b2138.toInt(), 0x4d2c6dfc.toInt(), 0x53380d13.toInt(),
    0x650a7354.toInt(), 0x766a0abb.toInt(), 0x81c2c92e.toInt(), 0x92722c85.toInt(),
    0xa2bfe8a1.toInt(), 0xa81a664b.toInt(), 0xc24b8b70.toInt(), 0xc76c51a3.toInt(),
    0xd192e819.toInt(), 0xd6990624.toInt(), 0xf40e3585.toInt(), 0x106aa070.toInt(),
    0x19a4c116.toInt(), 0x1e376c08.toInt(), 0x2748774c.toInt(), 0x34b0bcb5.toInt(),
    0x391c0cb3.toInt(), 0x4ed8aa4a.toInt(), 0x5b9cca4f.toInt(), 0x682e6ff3.toInt(),
    0x748f82ee.toInt(), 0x78a5636f.toInt(), 0x84c87814.toInt(), 0x8cc70208.toInt(),
    0x90befffa.toInt(), 0xa4506ceb.toInt(), 0xbef9a3f7.toInt(), 0xc67178f2.toInt(),
)

/**
 * Pure-Kotlin SHA-256 implementation used to hash generated file contents for the lock file.
 *
 * Multiplatform (no JVM/native primitives), deterministic across all CLI targets, allocates one
 * digest buffer per call. Performance is not critical — generated files are KB-sized and few.
 */
class Sha256HashProvider : HashProvider {

    override fun hash(bytes: ByteArray): String {
        val padded = pad(bytes)
        val state = intArrayOf(
            0x6a09e667.toInt(), 0xbb67ae85.toInt(), 0x3c6ef372.toInt(), 0xa54ff53a.toInt(),
            0x510e527f.toInt(), 0x9b05688c.toInt(), 0x1f83d9ab.toInt(), 0x5be0cd19.toInt(),
        )
        val schedule = IntArray(SCHEDULE_SIZE)
        var i = 0
        while (i < padded.size) {
            processBlock(padded, i, state, schedule)
            i += BLOCK_SIZE
        }
        return toHex(state)
    }

    private fun pad(bytes: ByteArray): ByteArray {
        val bitLen = bytes.size.toLong() * 8
        val withOne = bytes.size + 1
        val padLen = (BLOCK_SIZE - (withOne + 8) % BLOCK_SIZE) % BLOCK_SIZE
        val total = withOne + padLen + 8
        val out = ByteArray(total)
        bytes.copyInto(out)
        out[bytes.size] = 0x80.toByte()
        for (j in 0 until 8) {
            out[total - 1 - j] = (bitLen ushr (j * 8)).toByte()
        }
        return out
    }

    @Suppress("LongMethod", "MagicNumber")
    private fun processBlock(buf: ByteArray, offset: Int, state: IntArray, w: IntArray) {
        var idx = 0
        while (idx < 16) {
            val base = offset + idx * 4
            w[idx] = ((buf[base].toInt() and 0xff) shl 24) or
                ((buf[base + 1].toInt() and 0xff) shl 16) or
                ((buf[base + 2].toInt() and 0xff) shl 8) or
                (buf[base + 3].toInt() and 0xff)
            idx++
        }
        while (idx < SCHEDULE_SIZE) {
            val s0 = w[idx - 15].rotateRight(7) xor w[idx - 15].rotateRight(18) xor (w[idx - 15] ushr 3)
            val s1 = w[idx - 2].rotateRight(17) xor w[idx - 2].rotateRight(19) xor (w[idx - 2] ushr 10)
            w[idx] = w[idx - 16] + s0 + w[idx - 7] + s1
            idx++
        }

        var a = state[0]
        var b = state[1]
        var c = state[2]
        var d = state[3]
        var e = state[4]
        var f = state[5]
        var g = state[6]
        var h = state[7]

        for (t in 0 until SCHEDULE_SIZE) {
            val s1 = e.rotateRight(6) xor e.rotateRight(11) xor e.rotateRight(25)
            val ch = (e and f) xor (e.inv() and g)
            val temp1 = h + s1 + ch + K[t] + w[t]
            val s0 = a.rotateRight(2) xor a.rotateRight(13) xor a.rotateRight(22)
            val maj = (a and b) xor (a and c) xor (b and c)
            val temp2 = s0 + maj
            h = g
            g = f
            f = e
            e = d + temp1
            d = c
            c = b
            b = a
            a = temp1 + temp2
        }

        state[0] += a
        state[1] += b
        state[2] += c
        state[3] += d
        state[4] += e
        state[5] += f
        state[6] += g
        state[7] += h
    }

    private fun toHex(state: IntArray): String {
        val sb = StringBuilder(DIGEST_BYTES * 2)
        for (word in state) {
            for (shift in 28 downTo 0 step 4) {
                sb.append(HEX[(word ushr shift) and 0xf])
            }
        }
        return sb.toString()
    }

    private companion object {
        private val HEX = "0123456789abcdef".toCharArray()
    }
}
