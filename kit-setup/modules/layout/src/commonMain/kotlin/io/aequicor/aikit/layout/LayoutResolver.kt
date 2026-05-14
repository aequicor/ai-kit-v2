package io.aequicor.aikit.layout

import io.aequicor.aikit.core.domain.targets.ClaudeCode
import io.aequicor.aikit.core.domain.targets.OpenCode
import io.aequicor.aikit.core.domain.targets.QwenCode
import io.aequicor.aikit.core.domain.targets.Target

/** Returns the [AgentLayout] implementation for the given [Target]. */
fun layoutFor(target: Target): AgentLayout = when (target) {
    is ClaudeCode -> ClaudeCodeLayout
    is OpenCode -> OpenCodeLayout
    is QwenCode -> QwenCodeLayout
}
