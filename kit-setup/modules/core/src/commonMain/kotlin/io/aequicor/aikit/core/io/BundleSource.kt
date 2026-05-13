package io.aequicor.aikit.core.io

interface BundleSource {
    fun readManifest(): Result<ByteArray>
    fun readTemplate(sourcePath: String): Result<ByteArray>
}
