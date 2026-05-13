package io.aequicor.aikit.core.domain.targets

import io.aequicor.aikit.core.domain.template.Template

// ── Agent config ──────────────────────────────────────────────────────────────

/**
 * [Target] implementation targeting [Claude Code](https://claude.ai/code).
 * Native config: `.claude/settings.json`.
 *
 * Claude Code exposes a flat list of **subagents** installed as `.claude/agents/<name>.md`,
 * and a rich declarative lifecycle-hook system.
 *
 * @property subagents Subagent definition templates installed under `.claude/agents/`. Each
 *   [Template.path] is the file's destination relative to the agent root.
 * @property model Default model for the session (e.g. `"claude-sonnet-4-6"`).
 * @property includeCoAuthoredBy Whether to append `Co-authored-by:` trailers to commits.
 * @property env Extra environment variables injected into every tool invocation.
 * @property permissions Tool allow/deny/ask rules.
 * @property hooks Lifecycle hooks keyed by [ClaudeHookEvent]. Each event maps to an ordered
 *   list of [ClaudeHookGroup] entries that share a tool matcher.
 */
data class ClaudeCode(
    override val schemaVersion: Int,
    override val minVersion: String?,
    override val mcpServers: List<McpServer>,
    override val commands: List<Template>,
    override val skills: List<Template>,
    val subagents: List<Template>,
    val model: String?,
    val includeCoAuthoredBy: Boolean?,
    val env: Map<String, String>?,
    val permissions: ClaudePermissions?,
    val hooks: Map<ClaudeHookEvent, List<ClaudeHookGroup>>,
) : Target

/**
 * Tool-use permission configuration for Claude Code.
 *
 * @property defaultMode Fallback permission mode when no explicit rule matches.
 * @property allow Allowlist rules — tool use proceeds automatically without prompting.
 * @property deny Denylist rules — tool use is blocked outright.
 * @property ask Rules that require interactive user confirmation before the tool runs.
 * @property additionalDirectories Extra filesystem paths Claude Code may read/write beyond the project root.
 */
data class ClaudePermissions(
    val defaultMode: ClaudeDefaultMode?,
    val allow: List<PermissionRule>,
    val deny: List<PermissionRule>,
    val ask: List<PermissionRule>,
    val additionalDirectories: List<String>,
)

/** Fallback permission mode applied when no explicit allow/deny/ask rule matches. */
enum class ClaudeDefaultMode {
    /** Prompt the user for every tool use (normal interactive mode). */
    DEFAULT,

    /** Show a plan before executing; pause for approval at each tool call. */
    PLAN,

    /** Automatically accept file edits without prompting. */
    ACCEPT_EDITS,

    /** Skip all permission checks — use only in fully trusted, sandboxed environments. */
    BYPASS_PERMISSIONS,
}

// ── Hooks ─────────────────────────────────────────────────────────────────────

/**
 * Lifecycle events that Claude Code exposes for hook registration.
 * Used as keys in [ClaudeCode.hooks].
 *
 * See the Claude Code hooks reference for the canonical event list.
 */
enum class ClaudeHookEvent {
    /** Fires before a tool use is dispatched; handlers can deny or rewrite the request. */
    PRE_TOOL_USE,

    /** Fires after a tool use completes; handlers receive the result. */
    POST_TOOL_USE,

    /** Fires when the user submits a prompt. */
    USER_PROMPT_SUBMIT,

    /** Fires when a notification is shown in the UI. */
    NOTIFICATION,

    /** Fires when the agent stops its main loop. */
    STOP,

    /** Fires when a subagent stops. */
    SUBAGENT_STOP,

    /** Fires before the context is compacted. */
    PRE_COMPACT,

    /** Fires when a new session starts. */
    SESSION_START,

    /** Fires when a session ends. */
    SESSION_END,
}

/**
 * A group of hook handlers sharing a common [matcher] pattern, registered for a single [ClaudeHookEvent].
 *
 * @property matcher Regex or `|`-separated tool names that must match for the handlers to fire.
 *   An empty string matches everything.
 * @property sequential If `true`, handlers run sequentially; otherwise in parallel.
 * @property hooks Ordered list of handlers executed when the matcher fires.
 * @property condition Raw AKEL expression; if non-null, the whole group is included only when it evaluates to `true`.
 */
data class ClaudeHookGroup(
    val matcher: String,
    val sequential: Boolean,
    val hooks: List<ClaudeHookHandler>,
    val condition: String?,
)

/**
 * A single hook handler inside a [ClaudeHookGroup].
 * Claude Code supports five handler types.
 */
sealed interface ClaudeHookHandler {

    /** Raw AKEL expression; if non-null, this handler is active only when the expression is `true`. */
    val condition: String?

    /**
     * Runs a local shell command when the hook fires.
     *
     * @property command Executable path or shell command.
     * @property args Additional arguments passed to [command].
     * @property shell Shell used to execute the command (e.g. `"bash"`). `null` = agent default.
     * @property async If `true`, the command runs without blocking the agent.
     * @property asyncRewake If `true`, the agent re-wakes after the async command completes.
     * @property timeout Maximum execution time in seconds. `null` = agent default (600 s).
     * @property statusMessage Message shown in the UI while the command is running.
     * @property once If `true`, the hook runs at most once per session (relevant for skill hooks).
     */
    data class Command(
        override val condition: String?,
        val command: String,
        val args: List<String>,
        val shell: String?,
        val async: Boolean,
        val asyncRewake: Boolean,
        val timeout: Int?,
        val statusMessage: String?,
        val once: Boolean,
    ) : ClaudeHookHandler

    /**
     * Sends an HTTP request to a remote endpoint when the hook fires.
     *
     * @property url Target URL.
     * @property headers HTTP headers sent with the request. Values may reference env vars.
     * @property allowedEnvVars Environment variable names whose values may appear in [headers].
     * @property timeout Request timeout in seconds. `null` = agent default (30 s).
     */
    data class Http(
        override val condition: String?,
        val url: String,
        val headers: Map<String, String>,
        val allowedEnvVars: List<String>,
        val timeout: Int?,
    ) : ClaudeHookHandler

    /**
     * Invokes an MCP tool on a registered server when the hook fires.
     *
     * @property server Name of a server declared in [Target.mcpServers].
     * @property tool Tool name to invoke on [server].
     * @property input Key-value map passed as the tool input (values may reference `${tool_input.*}`).
     */
    data class McpTool(
        override val condition: String?,
        val server: String,
        val tool: String,
        val input: Map<String, String>,
    ) : ClaudeHookHandler

    /**
     * Runs a single-turn Claude evaluation (without tools) when the hook fires.
     * Useful for lightweight classification or policy checks.
     *
     * @property prompt Prompt sent to the model.
     * @property model Model to use for the evaluation. `null` = session default.
     */
    data class Prompt(
        override val condition: String?,
        val prompt: String,
        val model: String?,
    ) : ClaudeHookHandler

    /**
     * Spawns a full subagent session when the hook fires. The subagent has access to tools.
     *
     * @property prompt Initial prompt for the subagent.
     * @property model Model to use for the subagent session. `null` = session default.
     */
    data class Agent(
        override val condition: String?,
        val prompt: String,
        val model: String?,
    ) : ClaudeHookHandler
}
