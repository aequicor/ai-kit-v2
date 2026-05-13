package io.aequicor.aikit.io.zip

/**
 * Builds a minimal ZIP archive using STORED compression (method = 0) so tests work
 * without a compression library. The format follows PKWARE's .ZIP Application Note.
 */
internal fun buildStoredZip(entries: Map<String, ByteArray>): ByteArray {
    data class Written(val name: ByteArray, val data: ByteArray, val crc: UInt, val offset: Int)

    val buf = ArrayList<Byte>()
    val written = ArrayList<Written>()

    for ((name, data) in entries) {
        val nameBytes = name.encodeToByteArray()
        val crc = crc32(data)
        val offset = buf.size
        // Local file header
        buf.putI32(0x04034b50)   // signature
        buf.putI16(20)            // version needed
        buf.putI16(0)             // flags
        buf.putI16(0)             // method: STORED
        buf.putI16(0)             // mod time
        buf.putI16(0)             // mod date
        buf.putI32(crc.toInt())   // CRC-32
        buf.putI32(data.size)     // compressed size
        buf.putI32(data.size)     // uncompressed size
        buf.putI16(nameBytes.size)
        buf.putI16(0)             // extra length
        buf.addAll(nameBytes.toList())
        buf.addAll(data.toList())
        written += Written(nameBytes, data, crc, offset)
    }

    val cdOffset = buf.size
    for (w in written) {
        buf.putI32(0x02014b50)       // central directory signature
        buf.putI16(20)                // version made by
        buf.putI16(20)                // version needed
        buf.putI16(0)                 // flags
        buf.putI16(0)                 // method: STORED
        buf.putI16(0)                 // mod time
        buf.putI16(0)                 // mod date
        buf.putI32(w.crc.toInt())     // CRC-32
        buf.putI32(w.data.size)       // compressed size
        buf.putI32(w.data.size)       // uncompressed size
        buf.putI16(w.name.size)       // file name length
        buf.putI16(0)                 // extra length
        buf.putI16(0)                 // comment length
        buf.putI16(0)                 // disk start
        buf.putI16(0)                 // internal attributes
        buf.putI32(0)                 // external attributes
        buf.putI32(w.offset)          // local header offset
        buf.addAll(w.name.toList())
    }
    val cdSize = buf.size - cdOffset

    // End of central directory
    buf.putI32(0x06054b50)            // EOCD signature
    buf.putI16(0)                      // disk number
    buf.putI16(0)                      // disk with CD start
    buf.putI16(written.size)           // entries on disk
    buf.putI16(written.size)           // total entries
    buf.putI32(cdSize)                 // CD size
    buf.putI32(cdOffset)               // CD offset
    buf.putI16(0)                      // comment length

    return buf.toByteArray()
}

private fun ArrayList<Byte>.putI16(v: Int) {
    add((v and 0xFF).toByte())
    add(((v shr 8) and 0xFF).toByte())
}

private fun ArrayList<Byte>.putI32(v: Int) {
    add((v and 0xFF).toByte())
    add(((v shr 8) and 0xFF).toByte())
    add(((v shr 16) and 0xFF).toByte())
    add(((v shr 24) and 0xFF).toByte())
}

internal fun crc32(data: ByteArray): UInt {
    var crc = 0xFFFFFFFFu
    for (byte in data) {
        var b = (byte.toInt() and 0xFF).toUInt()
        repeat(8) {
            val mix = (crc xor b) and 1u
            crc = crc shr 1
            if (mix != 0u) crc = crc xor 0xEDB88320u
            b = b shr 1
        }
    }
    return crc.inv()
}
