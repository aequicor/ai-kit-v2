package io.aequicor.aikit.layout

/**
 * Native filesystem layout for Qwen Code.
 *
 * - Memory files (`QWEN.md`) live at the project root.
 * - Subagents live under `.qwen/agents/<name>.md`.
 * - Slash commands live under `.qwen/commands/<name>.md`.
 * - Skill files live under `.qwen/skills/<name>/<file>`.
 * - Hook scripts referenced from `config.json` keep their declared path.
 * - Settings are aggregated into `.qwen/settings.json`; MCP servers are embedded inside the
 *   same file, so [mcpFile] returns `null`.
 */
object QwenCodeLayout : AgentLayout {

    private const val AGENT_DIR = ".qwen"
    private const val AGENTS_SUBDIR = "agents"
    private const val COMMANDS_SUBDIR = "commands"
    private const val SKILLS_SUBDIR = "skills"
    private const val SETTINGS_FILE = "$AGENT_DIR/settings.json"

    override fun destinationFor(kind: FileKind, name: String, source: String): String = when (kind) {
        FileKind.MEMORY -> name
        FileKind.SUBAGENT -> "$AGENT_DIR/$AGENTS_SUBDIR/$name.md"
        FileKind.COMMAND -> "$AGENT_DIR/$COMMANDS_SUBDIR/$name.md"
        FileKind.SKILL -> "$AGENT_DIR/$SKILLS_SUBDIR/$name/${source.trimStart('/')}"
        FileKind.HOOK_SCRIPT -> name.trimStart('/')
    }

    override fun settingsFile(): String = SETTINGS_FILE
    override fun mcpFile(): String? = null
}
