package io.aequicor.aikit.generation

import io.aequicor.aikit.core.Bundle
import io.aequicor.aikit.core.api.AgentLayout
import io.aequicor.aikit.core.api.Generator

class FilesystemGenerator(private val layout: AgentLayout) : Generator {
    override fun write(bundle: Bundle, targetDir: String, force: Boolean): Result<Unit> {
        TODO("not implemented")
    }
}
