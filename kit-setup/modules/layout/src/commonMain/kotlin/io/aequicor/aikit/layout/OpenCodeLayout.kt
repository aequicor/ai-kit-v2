package io.aequicor.aikit.layout

/**
 * Native filesystem layout for OpenCode.
 *
 * - Memory files (`AGENTS.md`) live at the project root.
 * - Slash commands live under `.opencode/command/<name>.md`.
 * - Skill files live under `.opencode/skills/<name>/<file>`.
 * - OpenCode has no subagent file layout (subagents are declared inside `opencode.json`).
 * - OpenCode has no separate hook script layout (hooks are not part of its config model).
 * - All declarative config goes into a single `opencode.json` at the project root; OpenCode
 *   embeds MCP servers inside that file, so [mcpFile] returns `null`.
 */
object OpenCodeLayout : AgentLayout {

    private const val AGENT_DIR = ".opencode"
    private const val COMMANDS_SUBDIR = "command"
    private const val SKILLS_SUBDIR = "skills"
    private const val SETTINGS_FILE = "opencode.json"

    override fun destinationFor(kind: FileKind, name: String, source: String): String = when (kind) {
        FileKind.MEMORY -> name
        FileKind.COMMAND -> "$AGENT_DIR/$COMMANDS_SUBDIR/$name.md"
        FileKind.SKILL -> "$AGENT_DIR/$SKILLS_SUBDIR/$name/${source.trimStart('/')}"
        FileKind.SUBAGENT, FileKind.HOOK_SCRIPT ->
            throw UnsupportedLayoutKind("OpenCode does not support $kind files")
    }

    override fun settingsFile(): String = SETTINGS_FILE
    override fun mcpFile(): String? = null
}

/** Thrown when a layout is asked to place a [FileKind] it does not support. */
class UnsupportedLayoutKind(message: String) : IllegalArgumentException(message)
