package io.aequicor.aikit.core.domain.targets

import io.aequicor.aikit.core.domain.template.Template

// ── Agent config ──────────────────────────────────────────────────────────────

/**
 * [Target] implementation targeting [Qwen Code](https://github.com/QwenLM/qwen-code) —
 * Alibaba's Claude Code fork. Native config: `.qwen/settings.json`.
 *
 * Inherits Claude Code's agent model: a flat list of **subagents** (no primary-agent
 * definitions) and a declarative lifecycle-hook system (a subset of Claude's).
 *
 * @property subagents Subagent definition templates installed under `.qwen/agents/`.
 * @property model Model selection and generation parameters.
 * @property modelProviders Additional model provider definitions. The active provider is chosen
 *   by matching [QwenModelConfig.name] against provider [QwenModelProvider.id].
 * @property permissions Tool allow/deny/ask rules (same pattern format as Claude Code).
 * @property tools Tool execution and sandbox configuration.
 * @property general General behaviour settings (auto-update, vim mode, co-authoring).
 * @property context Context-file settings (instruction file name, search options).
 * @property telemetry Observability and telemetry export settings.
 * @property hooks Lifecycle hooks keyed by [QwenHookEvent]. Each event maps to an ordered
 *   list of [QwenHookGroup] entries that share a tool matcher.
 */
data class QwenCode(
    override val schemaVersion: Int,
    override val minVersion: String?,
    override val mcpServers: List<McpServer>,
    override val commands: List<Template>,
    override val skills: List<Template>,
    val subagents: List<Template>,
    val model: QwenModelConfig?,
    val modelProviders: List<QwenModelProvider>?,
    val permissions: QwenPermissions?,
    val tools: QwenToolsConfig?,
    val general: QwenGeneralConfig?,
    val context: QwenContextConfig?,
    val telemetry: QwenTelemetryConfig?,
    val hooks: Map<QwenHookEvent, List<QwenHookGroup>>,
) : Target

// ── Model ─────────────────────────────────────────────────────────────────────

/**
 * Active model configuration for Qwen Code.
 *
 * @property name Model identifier (e.g. `"qwen-max"`, `"claude-sonnet-4-5"`). Must match
 *   [QwenModelProvider.id] of one of the configured providers.
 * @property maxSessionTurns Maximum conversation turns before the session is force-compacted.
 *   `-1` means unlimited.
 * @property generationConfig Low-level generation parameters. Overrides provider-level defaults
 *   for the active model.
 * @property chatCompression Context-compaction thresholds.
 * @property skipNextSpeakerCheck Disables next-speaker detection heuristics when `true`.
 * @property skipLoopDetection Disables infinite-loop detection when `true`.
 * @property skipStartupContext Skips loading the startup context file (e.g. `QWEN.md`) when `true`.
 */
data class QwenModelConfig(
    val name: String,
    val maxSessionTurns: Int?,
    val generationConfig: QwenGenerationConfig?,
    val chatCompression: QwenChatCompression?,
    val skipNextSpeakerCheck: Boolean?,
    val skipLoopDetection: Boolean?,
    val skipStartupContext: Boolean?,
)

/**
 * Low-level generation parameters used by Qwen Code.
 *
 * @property timeout Request timeout in milliseconds.
 * @property maxRetries Number of automatic retries on transient errors.
 * @property contextWindowSize Maximum context window size in tokens.
 * @property reasoning Reasoning effort level. `null` = model default.
 * @property samplingParams Sampling hyperparameters (temperature, top-p, max tokens).
 */
data class QwenGenerationConfig(
    val timeout: Int?,
    val maxRetries: Int?,
    val contextWindowSize: Int?,
    val reasoning: QwenReasoningLevel?,
    val samplingParams: QwenSamplingParams?,
)

/**
 * Sampling hyperparameters for Qwen Code generation.
 *
 * @property temperature Randomness of output. Typical range 0.0–2.0.
 * @property topP Nucleus sampling threshold. Typical range 0.0–1.0.
 * @property maxTokens Maximum number of tokens in a single model response.
 */
data class QwenSamplingParams(
    val temperature: Double?,
    val topP: Double?,
    val maxTokens: Int?,
)

/** Reasoning effort applied by the model for each response. */
enum class QwenReasoningLevel {
    /** Light reasoning — short chain-of-thought before answering. */
    LOW,

    /** Balanced reasoning effort. */
    MEDIUM,

    /** Maximum reasoning effort — longest chain-of-thought. */
    HIGH,

    /** Reasoning disabled entirely. */
    NONE,
}

/**
 * Context-compaction thresholds for Qwen Code.
 *
 * @property contextPercentageThreshold Fraction of the context window (0.0–1.0) at which
 *   compaction is triggered automatically.
 */
data class QwenChatCompression(
    val contextPercentageThreshold: Double?,
)

// ── Model providers ───────────────────────────────────────────────────────────

/**
 * A model provider definition in [QwenCode.modelProviders].
 *
 * Qwen Code supports multiple providers through a unified OpenAI-compatible interface.
 * The [authType] selects the authentication and API protocol to use.
 *
 * @property authType Protocol and authentication scheme for this provider.
 * @property id Model identifier used to select this provider at runtime (e.g. `"qwen-max"`,
 *   `"gpt-4o"`). Must be unique across all providers.
 * @property name Human-readable display name. `null` = use [id].
 * @property envKey Environment variable holding the API key.
 * @property baseUrl Base URL of the API endpoint. `null` = protocol default.
 * @property generationConfig Generation parameter overrides applied when this provider is active.
 *   Overrides the top-level [QwenModelConfig.generationConfig] entirely (not merged).
 */
data class QwenModelProvider(
    val authType: QwenAuthType,
    val id: String,
    val name: String?,
    val envKey: String?,
    val baseUrl: String?,
    val generationConfig: QwenGenerationConfig?,
)

/** Authentication and API protocol for a Qwen Code model provider. */
enum class QwenAuthType {
    /** OpenAI-compatible REST API (`/v1/chat/completions`). */
    OPENAI,

    /** Anthropic Messages API. */
    ANTHROPIC,

    /** Google Gemini API. */
    GEMINI,

    /** Alibaba DashScope OAuth (used for `qwen-*` models). */
    DASHSCOPE,
}

// ── Permissions ───────────────────────────────────────────────────────────────

/**
 * Tool-use permission configuration for Qwen Code.
 * Uses the same [PermissionRule] pattern format as Claude Code (`"ToolName(specifier)"`).
 * Priority order for conflicting rules: deny > ask > allow.
 *
 * @property allow Rules for which the tool runs automatically without prompting.
 * @property deny Rules for which the tool is unconditionally blocked.
 * @property ask Rules for which the user is asked before each invocation.
 */
data class QwenPermissions(
    val allow: List<PermissionRule>,
    val deny: List<PermissionRule>,
    val ask: List<PermissionRule>,
)

// ── Tools ─────────────────────────────────────────────────────────────────────

/**
 * Tool execution and sandbox configuration for Qwen Code.
 *
 * @property approvalMode Default approval mode governing how tool invocations are handled.
 * @property sandbox Whether to run Bash commands inside an isolated container.
 * @property sandboxImage Container image used when [sandbox] is `true` (e.g. `"ubuntu:22.04"`).
 * @property useRipgrep Whether to use `ripgrep` for file-search operations.
 * @property truncateToolOutputThreshold Maximum characters of tool output shown in the context.
 * @property truncateToolOutputLines Maximum lines of tool output shown in the context.
 */
data class QwenToolsConfig(
    val approvalMode: QwenApprovalMode?,
    val sandbox: Boolean?,
    val sandboxImage: String?,
    val useRipgrep: Boolean?,
    val truncateToolOutputThreshold: Int?,
    val truncateToolOutputLines: Int?,
)

/** Approval mode controlling how Qwen Code handles tool invocations. */
enum class QwenApprovalMode {
    /** Prompt the user before every tool invocation. */
    DEFAULT,

    /** Auto-approve read-only edits; prompt for everything else. */
    AUTO_EDIT,

    /** Auto-approve all tool invocations — use only in trusted environments. */
    YOLO,
}

// ── General ───────────────────────────────────────────────────────────────────

/**
 * General behaviour settings for Qwen Code.
 *
 * @property vimMode Whether to enable Vim key-bindings in the interactive TUI.
 * @property enableAutoUpdate Whether to automatically install CLI updates.
 * @property gitCoAuthor Co-authoring attribution settings for commits and pull requests.
 */
data class QwenGeneralConfig(
    val vimMode: Boolean?,
    val enableAutoUpdate: Boolean?,
    val gitCoAuthor: QwenGitCoAuthor?,
)

/**
 * Git co-authoring attribution for Qwen Code.
 *
 * @property commit Whether to append `Co-authored-by:` to commit messages.
 * @property pr Whether to append `Co-authored-by:` to pull-request descriptions.
 */
data class QwenGitCoAuthor(
    val commit: Boolean?,
    val pr: Boolean?,
)

// ── Context ───────────────────────────────────────────────────────────────────

/**
 * Context-file settings for Qwen Code.
 *
 * @property fileName Name of the root instruction file loaded as the project context
 *   (analogous to `CLAUDE.md`). Defaults to `"QWEN.md"`.
 * @property fileFiltering Options controlling how the project file tree is searched and filtered.
 */
data class QwenContextConfig(
    val fileName: String?,
    val fileFiltering: QwenFileFiltering?,
)

/**
 * File-tree search and filtering options for Qwen Code.
 *
 * @property respectGitIgnore Whether to exclude `.gitignore`-listed files from context.
 * @property respectQwenIgnore Whether to exclude `.qwenignore`-listed files from context.
 * @property enableRecursiveFileSearch Whether to search for context files recursively.
 * @property enableFuzzySearch Whether to use fuzzy matching when searching for files.
 */
data class QwenFileFiltering(
    val respectGitIgnore: Boolean?,
    val respectQwenIgnore: Boolean?,
    val enableRecursiveFileSearch: Boolean?,
    val enableFuzzySearch: Boolean?,
)

// ── Telemetry ─────────────────────────────────────────────────────────────────

/**
 * Observability and telemetry settings for Qwen Code.
 *
 * @property enabled Whether telemetry data is collected and exported.
 * @property target Export target identifier (e.g. `"local"`, `"remote"`).
 * @property otlpEndpoint OpenTelemetry collector endpoint URL.
 * @property otlpProtocol OTLP transport protocol (`"grpc"` or `"http/protobuf"`).
 * @property logPrompts Whether to include full prompt text in telemetry spans.
 */
data class QwenTelemetryConfig(
    val enabled: Boolean?,
    val target: String?,
    val otlpEndpoint: String?,
    val otlpProtocol: String?,
    val logPrompts: Boolean?,
)

// ── Hooks ─────────────────────────────────────────────────────────────────────

/**
 * Lifecycle events that Qwen Code exposes for hook registration.
 * Used as keys in [QwenCode.hooks].
 *
 * Qwen Code's hook system is a subset of Claude Code's: it supports [QwenHookHandler.Command]
 * and [QwenHookHandler.Http] handler types but not `mcp_tool`, `prompt`, or `agent`.
 */
enum class QwenHookEvent {
    /** Fires before a tool use is dispatched. */
    PRE_TOOL_USE,

    /** Fires after a tool use completes. */
    POST_TOOL_USE,

    /** Fires when the user submits a prompt. */
    USER_PROMPT_SUBMIT,

    /** Fires when a notification is shown in the UI. */
    NOTIFICATION,

    /** Fires when the agent stops its main loop. */
    STOP,

    /** Fires when a new session starts. */
    SESSION_START,

    /** Fires when a session ends. */
    SESSION_END,
}

/**
 * A group of hook handlers sharing a common [matcher] pattern, registered for a single [QwenHookEvent].
 *
 * @property matcher Regex pattern matched against the tool name. `null` = match all tools.
 * @property sequential If `true`, handlers run sequentially; otherwise in parallel.
 * @property hooks Ordered list of handlers executed when the matcher fires.
 * @property condition Raw AKEL expression; if non-null, the whole group is included only when it evaluates to `true`.
 */
data class QwenHookGroup(
    val matcher: String?,
    val sequential: Boolean,
    val hooks: List<QwenHookHandler>,
    val condition: String?,
)

/**
 * A single hook handler inside a [QwenHookGroup].
 * Qwen Code supports two public handler types: [Command] and [Http].
 */
sealed interface QwenHookHandler {

    /** Raw AKEL expression; if non-null, this handler is active only when the expression is `true`. */
    val condition: String?

    /**
     * Runs a local shell command when the hook fires.
     *
     * @property command Executable path or shell command.
     * @property name Optional label shown in logs for this hook.
     * @property description Optional human-readable description of what the hook does.
     * @property shell Shell used to execute [command] (e.g. `"bash"`). `null` = agent default.
     * @property async If `true`, the command runs without blocking the agent.
     * @property timeout Maximum execution time in milliseconds. `null` = agent default (60 000 ms).
     * @property env Additional environment variables injected into the hook process.
     * @property statusMessage Message shown in the UI while the command is running.
     */
    data class Command(
        override val condition: String?,
        val command: String,
        val name: String?,
        val description: String?,
        val shell: String?,
        val async: Boolean,
        val timeout: Int?,
        val env: Map<String, String>,
        val statusMessage: String?,
    ) : QwenHookHandler

    /**
     * Sends an HTTP request to a remote endpoint when the hook fires.
     *
     * @property url Target URL.
     * @property headers HTTP headers sent with the request.
     * @property allowedEnvVars Environment variable names whose values may appear in [headers].
     * @property timeout Request timeout in seconds. `null` = agent default (600 s).
     * @property once If `true`, the hook fires at most once per session.
     */
    data class Http(
        override val condition: String?,
        val url: String,
        val headers: Map<String, String>,
        val allowedEnvVars: List<String>,
        val timeout: Int?,
        val once: Boolean,
    ) : QwenHookHandler
}
