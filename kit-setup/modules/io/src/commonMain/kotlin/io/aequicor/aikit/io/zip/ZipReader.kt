package io.aequicor.aikit.io.zip

/**
 * Minimal ZIP reader. Loads the entire archive into memory, parses the central directory,
 * and decompresses entries on demand. Supports STORED (method 0) and DEFLATE (method 8).
 * ZIP64 is not supported.
 */
internal class ZipReader(private val bytes: ByteArray) {

    /** All file entries keyed by their full name (forward-slash path, no leading slash). */
    val entries: Map<String, ZipEntry> by lazy { readCentralDirectory() }

    fun readEntryData(entry: ZipEntry): ByteArray {
        check(i32(entry.localHeaderOffset) == LOCAL_SIG) { "Bad local file header signature" }
        val nameLen = i16(entry.localHeaderOffset + LH_NAME_LEN_OFF)
        val extraLen = i16(entry.localHeaderOffset + LH_EXTRA_LEN_OFF)
        val dataStart = entry.localHeaderOffset + LH_BASE_SIZE + nameLen + extraLen
        val compressed = bytes.copyOfRange(dataStart, dataStart + entry.compressedSize)
        return when (entry.compressionMethod) {
            METHOD_STORED -> compressed
            METHOD_DEFLATE -> deflateInflate(compressed, entry.uncompressedSize)
            else -> error("Unsupported ZIP compression method: ${entry.compressionMethod}")
        }
    }

    private fun readCentralDirectory(): Map<String, ZipEntry> {
        val eocd = findEocd()
        val count = i16(eocd + EOCD_COUNT_OFF)
        val cdOffset = i32(eocd + EOCD_CD_OFFSET_OFF)
        check(cdOffset >= 0) { "ZIP64 central directory not supported" }

        val result = LinkedHashMap<String, ZipEntry>(count)
        var pos = cdOffset
        repeat(count) {
            check(i32(pos) == CD_SIG) { "Invalid central directory signature at $pos" }
            val method = i16(pos + CD_METHOD_OFF)
            val compSize = i32(pos + CD_COMP_SIZE_OFF)
            val uncompSize = i32(pos + CD_UNCOMP_SIZE_OFF)
            val nameLen = i16(pos + CD_NAME_LEN_OFF)
            val extraLen = i16(pos + CD_EXTRA_LEN_OFF)
            val commentLen = i16(pos + CD_COMMENT_LEN_OFF)
            val localOffset = i32(pos + CD_LOCAL_OFFSET_OFF)
            val name = bytes.decodeToString(pos + CD_BASE_SIZE, pos + CD_BASE_SIZE + nameLen)
            if (!name.endsWith('/')) {
                result[name] = ZipEntry(name, method, compSize, uncompSize, localOffset)
            }
            pos += CD_BASE_SIZE + nameLen + extraLen + commentLen
        }
        return result
    }

    private fun findEocd(): Int {
        val minPos = maxOf(0, bytes.size - EOCD_BASE_SIZE - MAX_COMMENT)
        for (i in bytes.size - EOCD_BASE_SIZE downTo minPos) {
            if (i32(i) == EOCD_SIG) return i
        }
        error("EOCD signature not found — not a valid ZIP archive")
    }

    private fun i16(off: Int): Int =
        (bytes[off].toInt() and BYTE_MASK) or ((bytes[off + 1].toInt() and BYTE_MASK) shl BITS_8)

    @Suppress("MagicNumber")
    private fun i32(off: Int): Int =
        (bytes[off].toInt() and BYTE_MASK) or
        ((bytes[off + 1].toInt() and BYTE_MASK) shl BITS_8) or
        ((bytes[off + 2].toInt() and BYTE_MASK) shl BITS_16) or
        ((bytes[off + 3].toInt() and BYTE_MASK) shl BITS_24)

    internal companion object {
        // Signatures
        const val EOCD_SIG = 0x06054b50
        const val CD_SIG = 0x02014b50
        const val LOCAL_SIG = 0x04034b50

        // Compression methods
        const val METHOD_STORED = 0
        const val METHOD_DEFLATE = 8

        // EOCD offsets and limits
        const val EOCD_BASE_SIZE = 22
        const val EOCD_COUNT_OFF = 8
        const val EOCD_CD_OFFSET_OFF = 16
        const val MAX_COMMENT = 65535

        // Central directory offsets
        const val CD_BASE_SIZE = 46
        const val CD_METHOD_OFF = 10
        const val CD_COMP_SIZE_OFF = 20
        const val CD_UNCOMP_SIZE_OFF = 24
        const val CD_NAME_LEN_OFF = 28
        const val CD_EXTRA_LEN_OFF = 30
        const val CD_COMMENT_LEN_OFF = 32
        const val CD_LOCAL_OFFSET_OFF = 42

        // Local file header offsets
        const val LH_BASE_SIZE = 30
        const val LH_NAME_LEN_OFF = 26
        const val LH_EXTRA_LEN_OFF = 28

        // Byte manipulation
        const val BYTE_MASK = 0xFF
        const val BITS_8 = 8
        const val BITS_16 = 16
        const val BITS_24 = 24
    }
}

internal data class ZipEntry(
    val name: String,
    val compressionMethod: Int,
    val compressedSize: Int,
    val uncompressedSize: Int,
    val localHeaderOffset: Int,
)
