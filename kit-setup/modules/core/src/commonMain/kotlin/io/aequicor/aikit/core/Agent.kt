package io.aequicor.aikit.core

sealed class Agent {
    abstract val id: AgentId
    abstract val displayName: String
    abstract val supportedKinds: Set<TemplateKind>

    data object ClaudeCode : Agent() {
        override val id = AgentId("claude-code")
        override val displayName = "Claude Code"
        override val supportedKinds = setOf(
            TemplateKind.MEMORY, TemplateKind.SLASH_COMMAND, TemplateKind.SUBAGENT,
            TemplateKind.HOOK, TemplateKind.MCP_CONFIG, TemplateKind.SETTINGS,
        )
    }

    data object OpenCode : Agent() {
        override val id = AgentId("opencode")
        override val displayName = "OpenCode"
        override val supportedKinds = setOf(
            TemplateKind.MEMORY, TemplateKind.SLASH_COMMAND, TemplateKind.SUBAGENT,
            TemplateKind.MCP_CONFIG, TemplateKind.SETTINGS,
        )
    }

    companion object {
        val all: List<Agent> = listOf(ClaudeCode, OpenCode)
        fun byId(id: AgentId): Agent? = all.find { it.id == id }
    }
}
