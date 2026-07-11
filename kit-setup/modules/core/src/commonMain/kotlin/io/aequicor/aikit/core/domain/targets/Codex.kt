package io.aequicor.aikit.core.domain.targets

import io.aequicor.aikit.core.domain.template.Template

// ── Agent config ──────────────────────────────────────────────────────────────

/**
 * [Target] implementation targeting [OpenAI Codex CLI](https://developers.openai.com/codex/cli).
 * Native config: `.codex/config.toml` (project-level override, loaded for trusted projects).
 *
 * Codex reads project instructions from `AGENTS.md` at the repo root, project-scoped subagents
 * from `.codex/agents/<name>.toml`, and custom prompts from `.codex/prompts/<name>.md`.
 * Unlike Claude Code, Codex has no skill directories and no lifecycle-hook system, so
 * [skills] is always empty and there is no hooks field.
 *
 * @property subagents Subagent definition templates (TOML) installed under `.codex/agents/`.
 * @property model Default model for the session (e.g. `"gpt-5.6"`).
 * @property modelReasoningEffort Reasoning effort level (`"low"` / `"medium"` / `"high"`).
 * @property approvalPolicy When Codex asks for approval (`"untrusted"` / `"on-request"` / `"never"`).
 * @property sandboxMode Sandbox level for tool execution (e.g. `"workspace-write"`).
 * @property webSearch Web-search mode (`"cached"` / `"indexed"` / `"live"` / `"disabled"`).
 * @property features Feature toggles written to the `[features]` table (e.g. `multi_agent`).
 */
data class Codex(
    override val schemaVersion: Int,
    override val minVersion: String?,
    override val mcpServers: List<McpServer>,
    override val commands: List<Template>,
    override val skills: List<Template>,
    val subagents: List<Template>,
    val model: String?,
    val modelReasoningEffort: String?,
    val approvalPolicy: String?,
    val sandboxMode: String?,
    val webSearch: String?,
    val features: Map<String, Boolean>?,
) : Target
