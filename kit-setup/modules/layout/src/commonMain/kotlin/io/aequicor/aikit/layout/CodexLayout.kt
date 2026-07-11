package io.aequicor.aikit.layout

/**
 * Native filesystem layout for OpenAI Codex CLI.
 *
 * - Memory files (`AGENTS.md`) live at the project root.
 * - Subagents live under `.codex/agents/<name>.toml` (Codex subagents are TOML files).
 * - Custom prompts (slash commands) live under `.codex/prompts/<name>.md`.
 * - Codex has no skill directories and no lifecycle hooks, so [FileKind.SKILL] and
 *   [FileKind.HOOK_SCRIPT] are unsupported.
 * - Declarative settings (model, approval policy, sandbox, MCP servers) are aggregated into
 *   `.codex/config.toml`; MCP servers are embedded there, so [mcpFile] returns `null`.
 */
object CodexLayout : AgentLayout {

    private const val AGENT_DIR = ".codex"
    private const val AGENTS_SUBDIR = "agents"
    private const val PROMPTS_SUBDIR = "prompts"
    private const val SETTINGS_FILE = "$AGENT_DIR/config.toml"

    override fun destinationFor(kind: FileKind, name: String, source: String): String = when (kind) {
        FileKind.MEMORY -> name
        FileKind.SUBAGENT -> "$AGENT_DIR/$AGENTS_SUBDIR/$name.toml"
        FileKind.COMMAND -> "$AGENT_DIR/$PROMPTS_SUBDIR/$name.md"
        FileKind.SKILL, FileKind.HOOK_SCRIPT ->
            throw UnsupportedLayoutKind("Codex does not support $kind files")
    }

    override fun settingsFile(): String = SETTINGS_FILE
    override fun mcpFile(): String? = null
}
