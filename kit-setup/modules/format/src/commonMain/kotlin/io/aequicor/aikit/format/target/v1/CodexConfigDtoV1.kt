package io.aequicor.aikit.format.target.v1

import kotlinx.serialization.Serializable

/**
 * Top-level DTO for a Codex `config.json` inside an AI-Kit bundle.
 *
 * Codex has no skill directories and no lifecycle hooks, so unlike the Claude DTO there are
 * no `skills` / `hooks` sections. Declarative settings are written to `.codex/config.toml`
 * by the engine's TOML builder; `memory`, `agents` and `commands` entries are routed as files
 * (`AGENTS.md`, `.codex/agents/<name>.toml`, `.codex/prompts/<name>.md`).
 */
@Serializable
internal data class CodexConfigDtoV1(
    val schemaVersion: Int = 1,
    val agent: String? = null,
    val minVersion: String? = null,
    val scope: String? = null,
    val settings: CodexSettingsDtoV1? = null,
    val memory: List<FileRefDtoV1>? = null,
    val mcpServers: List<McpServerDtoV1>? = null,
    val agents: List<FileRefDtoV1>? = null,
    val commands: List<FileRefDtoV1>? = null,
)

/**
 * Declarative settings mapped into `.codex/config.toml`.
 *
 * Field names use the bundle's camelCase convention; the engine emits the snake_case TOML keys
 * Codex expects (`model_reasoning_effort`, `approval_policy`, …).
 */
@Serializable
internal data class CodexSettingsDtoV1(
    val model: String? = null,
    val modelReasoningEffort: String? = null,
    val approvalPolicy: String? = null,
    val sandboxMode: String? = null,
    val webSearch: String? = null,
    val personality: String? = null,
    val features: Map<String, Boolean>? = null,
)
