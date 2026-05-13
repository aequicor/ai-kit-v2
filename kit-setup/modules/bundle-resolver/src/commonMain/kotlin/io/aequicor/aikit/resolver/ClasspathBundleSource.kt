package io.aequicor.aikit.resolver

import io.aequicor.aikit.core.io.BundleSource

class ClasspathBundleSource(private val resourceRoot: String) : BundleSource {
    override fun readManifest(): Result<ByteArray> {
        TODO("not implemented")
    }

    override fun readTemplate(sourcePath: String): Result<ByteArray> {
        TODO("not implemented")
    }
}
