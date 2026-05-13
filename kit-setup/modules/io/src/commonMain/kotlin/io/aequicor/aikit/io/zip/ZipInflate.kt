package io.aequicor.aikit.io.zip

/**
 * Decompresses raw DEFLATE data (ZIP compression method 8, no zlib/gzip wrapper).
 * Implemented per-platform via zlib's inflate with windowBits = -15.
 */
internal expect fun deflateInflate(compressed: ByteArray, uncompressedSize: Int): ByteArray
