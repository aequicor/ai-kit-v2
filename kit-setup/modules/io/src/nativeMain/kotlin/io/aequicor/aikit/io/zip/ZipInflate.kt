@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.aequicor.aikit.io.zip

import kotlinx.cinterop.alloc
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.zlib.Z_FINISH
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_END
import platform.zlib.inflate
import platform.zlib.inflateEnd
import platform.zlib.inflateInit2
import platform.zlib.z_stream

internal actual fun deflateInflate(compressed: ByteArray, uncompressedSize: Int): ByteArray {
    if (uncompressedSize == 0) return ByteArray(0)
    val output = ByteArray(uncompressedSize)
    compressed.usePinned { compPin ->
        output.usePinned { outPin ->
            memScoped {
                val zs = alloc<z_stream>()
                zs.next_in = compPin.addressOf(0).reinterpret()
                zs.avail_in = compressed.size.toUInt()
                zs.next_out = outPin.addressOf(0).reinterpret()
                zs.avail_out = uncompressedSize.toUInt()
                // windowBits = -15: raw DEFLATE (no zlib/gzip wrapper)
                val initRet = inflateInit2(zs.ptr, -15)
                check(initRet == Z_OK) { "inflateInit2 failed: $initRet" }
                val inflateRet = inflate(zs.ptr, Z_FINISH)
                inflateEnd(zs.ptr)
                check(inflateRet == Z_STREAM_END) { "inflate failed: ret=$inflateRet" }
            }
        }
    }
    return output
}
