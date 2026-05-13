package io.aequicor.aikit.resolver

import io.aequicor.aikit.core.io.BundleSource

class LocalDirBundleSource(private val path: String) : BundleSource {
    override fun readManifest(): Result<ByteArray> {
        TODO("not implemented")
    }

    override fun readTemplate(sourcePath: String): Result<ByteArray> {
        TODO("not implemented")
    }
}
