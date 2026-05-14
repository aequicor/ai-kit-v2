package io.aequicor.aikit.layout

/**
 * Semantic kind of a bundle file as declared in `config.json`.
 *
 * Layouts translate a [FileKind] together with a logical `name` and the bundle-relative `source`
 * path into the destination path the native agent expects.
 */
enum class FileKind {
    /** Project-level memory file (e.g. `CLAUDE.md`, `AGENTS.md`, `QWEN.md`). */
    MEMORY,

    /** Subagent definition (Claude Code `.claude/agents/`, Qwen Code `.qwen/agents/`). */
    SUBAGENT,

    /** Slash command / prompt template. */
    COMMAND,

    /** A single file inside a skill directory. The skill `name` identifies the skill. */
    SKILL,

    /** A shell script referenced from a hook entry's `command` field. */
    HOOK_SCRIPT,
}

/**
 * Maps a bundle file to the location its native agent expects on disk.
 *
 * All paths returned by [destinationFor], [settingsFile] and [mcpFile] are relative to
 * the application path (i.e. the project root the bundle is being installed into).
 */
interface AgentLayout {

    /**
     * Where a bundle file declared in `config.json` should land on disk.
     *
     * @param kind Semantic file kind.
     * @param name Logical name from the `config.json` entry (`memory[*].name`, `commands[*].name`, …).
     *   For [FileKind.SKILL] and [FileKind.HOOK_SCRIPT] this is the skill/hook identifier.
     * @param source Bundle-relative source path of the file. For skill files this is the
     *   path **inside** the skill directory (e.g. `SKILL.md`, `scripts/run.sh`).
     * @return App-path-relative destination using POSIX separators.
     */
    fun destinationFor(kind: FileKind, name: String, source: String): String

    /**
     * App-path-relative location of the native settings file, or `null` if the agent has none.
     */
    fun settingsFile(): String?

    /**
     * App-path-relative location of the native MCP server config file, or `null` if MCP is
     * embedded inside the settings file.
     */
    fun mcpFile(): String?
}
