package io.aequicor.aikit.layout

/**
 * Native filesystem layout for Claude Code.
 *
 * - Memory files (`CLAUDE.md`) live at the project root.
 * - Subagents live under `.claude/agents/<name>.md`.
 * - Slash commands live under `.claude/commands/<name>.md`.
 * - Skill files live under `.claude/skills/<name>/<file>`.
 * - Hook scripts referenced from `config.json` keep their declared path (relative to the
 *   project root, the `.claude/` prefix preserved).
 * - Settings are aggregated into `.claude/settings.json`; MCP servers into `.mcp.json`.
 */
object ClaudeCodeLayout : AgentLayout {

    private const val AGENT_DIR = ".claude"
    private const val AGENTS_SUBDIR = "agents"
    private const val COMMANDS_SUBDIR = "commands"
    private const val SKILLS_SUBDIR = "skills"
    private const val SETTINGS_FILE = "$AGENT_DIR/settings.json"
    private const val MCP_FILE = ".mcp.json"

    override fun destinationFor(kind: FileKind, name: String, source: String): String = when (kind) {
        FileKind.MEMORY -> name
        FileKind.SUBAGENT -> "$AGENT_DIR/$AGENTS_SUBDIR/$name.md"
        FileKind.COMMAND -> "$AGENT_DIR/$COMMANDS_SUBDIR/$name.md"
        FileKind.SKILL -> "$AGENT_DIR/$SKILLS_SUBDIR/$name/${source.trimStart('/')}"
        FileKind.HOOK_SCRIPT -> name.trimStart('/')
    }

    override fun settingsFile(): String = SETTINGS_FILE
    override fun mcpFile(): String = MCP_FILE
}
