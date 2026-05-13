package io.aequicor.aikit.core.domain.targets

import io.aequicor.aikit.core.domain.template.Template

// ── Agent config ──────────────────────────────────────────────────────────────

/**
 * [Target] implementation targeting [OpenCode](https://opencode.ai).
 * Native config: `opencode.json` (project).
 *
 * OpenCode unifies primary agents and subagents into a single map keyed by name, with a
 * `mode: primary | subagent | all` discriminator on each entry — agent definitions therefore
 * live inside [agents] rather than as a top-level subagent list. OpenCode has no declarative
 * lifecycle-hook system; hooks are expressed as JavaScript/TypeScript plugin modules under
 * `.opencode/plugins/`, shipped as raw [Template] files in [plugins]. Tool control is expressed
 * via [tools] and [permission].
 *
 * Model string format: `"provider/model-id"` (e.g. `"anthropic/claude-sonnet-4-5"`).
 *
 * @property plugins JavaScript/TypeScript plugin source files (`.js` / `.ts`) installed under
 *   `.opencode/plugins/`. Treated as opaque templates — the linker copies them verbatim or
 *   applies substitutions per [Template] rules.
 * @property model Primary model used for the main agent loop.
 * @property smallModel Lighter model used for cheap/fast sub-tasks. `null` = same as [model].
 * @property defaultAgent Name of the agent from [agents] to use by default. `null` = built-in coder.
 * @property shell Shell binary used for Bash tool execution (e.g. `"/bin/bash"`).
 * @property share Session-sharing behaviour.
 * @property snapshot Whether to take session snapshots for recovery.
 * @property instructions Paths (relative to project root) to additional instruction files
 *   appended to the system prompt (e.g. `["AGENTS.md", ".cursor/rules/style.md"]`).
 * @property provider Provider-specific connection and authentication options.
 * @property tools Per-tool enable/disable overrides. Keys are tool names or glob patterns.
 *   `true` enables, `false` disables. MCP tool key format: `"serverName"` or `"serverName__toolName"`.
 * @property permission Per-pattern permission levels. Keys are tool-name patterns.
 * @property agents Named agent definitions available to the session.
 * @property compaction Context-compaction settings.
 */
data class OpenCode(
    override val schemaVersion: Int,
    override val minVersion: String?,
    override val mcpServers: List<McpServer>,
    override val commands: List<Template>,
    override val skills: List<Template>,
    val plugins: List<Template>,
    val model: String?,
    val smallModel: String?,
    val defaultAgent: String?,
    val shell: String?,
    val share: OpenCodeShareMode?,
    val snapshot: Boolean?,
    val instructions: List<String>?,
    val provider: Map<String, OpenCodeProvider>?,
    val tools: Map<String, Boolean>?,
    val permission: Map<String, OpenCodePermissionLevel>?,
    val agents: Map<String, OpenCodeAgentDef>?,
    val compaction: OpenCodeCompaction?,
) : Target

// ── Enums ─────────────────────────────────────────────────────────────────────

/**
 * Session-sharing mode for OpenCode.
 * Controls whether sessions are shared with other users via the OpenCode share service.
 */
enum class OpenCodeShareMode {
    /** Session must be shared manually by the user. */
    MANUAL,

    /** Session is shared automatically on creation. */
    AUTO,

    /** Sharing is disabled entirely. */
    DISABLED,
}

/**
 * Permission level for a tool in OpenCode.
 * Applied via the [OpenCode.permission] map.
 */
enum class OpenCodePermissionLevel {
    /** Tool runs without prompting. */
    ALLOW,

    /** The user is asked before each tool invocation. */
    ASK,

    /** Tool is blocked unconditionally. */
    DENY,
}

// ── Provider ──────────────────────────────────────────────────────────────────

/**
 * Configuration for a single model provider in [OpenCode.provider].
 * Keyed by provider name (e.g. `"anthropic"`, `"openai"`, `"aws"`).
 *
 * Connection options (API keys, timeouts, base URLs) are stored as raw strings so they
 * can reference `{env:VAR}` placeholders resolved by OpenCode at runtime.
 *
 * @property options Provider-specific key-value options (e.g. `apiKey`, `baseUrl`, `timeout`).
 *   OpenCode resolves `{env:VAR}` and `{file:path}` placeholders in string values.
 */
data class OpenCodeProvider(
    val options: Map<String, String>,
)

// ── Agent definition ──────────────────────────────────────────────────────────

/**
 * A named agent definition available to OpenCode sessions.
 * Declared in [OpenCode.agents] and referenced by [OpenCode.defaultAgent].
 *
 * @property mode Role of this agent — primary (drives a session), subagent (spawned by another
 *   agent), or all (registered in both roles).
 * @property description Short human-readable description of the agent's purpose.
 * @property model Model override for this agent. `null` = inherits [OpenCode.model].
 * @property prompt System-prompt override prepended to every session with this agent.
 * @property tools Per-tool enable/disable overrides scoped to this agent. Merged with the
 *   global [OpenCode.tools] map (agent-level takes precedence).
 */
data class OpenCodeAgentDef(
    val mode: OpenCodeAgentMode,
    val description: String?,
    val model: String?,
    val prompt: String?,
    val tools: Map<String, Boolean>,
)

/**
 * Role of an [OpenCodeAgentDef]. OpenCode merges the Claude-style primary/subagent split into
 * a single `agents` map keyed by name, with this field distinguishing the role.
 */
enum class OpenCodeAgentMode {
    /** Agent that drives a top-level session. Has no Claude Code equivalent — Claude has no primary-agent definitions. */
    PRIMARY,

    /** Agent spawned as a subagent by another agent (analogous to a Claude `.claude/agents` entry). */
    SUBAGENT,

    /** Agent registered in both primary and subagent roles. */
    ALL,
}

// ── Compaction ────────────────────────────────────────────────────────────────

/**
 * Context-compaction settings for OpenCode.
 *
 * @property auto Whether to compact automatically when the context window fills up.
 * @property prune Whether to prune redundant messages during compaction.
 * @property reserved Number of tokens reserved for the model response after compaction.
 */
data class OpenCodeCompaction(
    val auto: Boolean?,
    val prune: Boolean?,
    val reserved: Int?,
)
