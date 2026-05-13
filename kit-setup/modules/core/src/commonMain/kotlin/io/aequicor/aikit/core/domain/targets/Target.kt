package io.aequicor.aikit.core.domain.targets

import io.aequicor.aikit.core.domain.template.Template

/**
 * Parsed representation of `<bundle>/<agent>/config.json` — the agent-specific manifest
 * within a bundle.
 *
 * This is the bundle's own intermediate format, not the native agent config. The domain layer
 * stores raw string values that may contain `${bundle.input.<id>}` placeholders and `condition`
 * expressions; the linking layer evaluates `when` conditions, drops gated entries, applies
 * substitutions, and hands the resolved tree to the agent generator, which writes the canonical
 * native config files.
 *
 * Only fields that are uniformly meaningful across **every** supported agent (Claude Code,
 * OpenCode, Qwen Code, Codex, …) live here. Agent-specific concepts — the agent/subagent model,
 * lifecycle hooks, native settings blocks — belong to the concrete implementation, because their
 * shape differs between agents: e.g. Claude Code / Qwen Code / Codex expose a flat `subagents`
 * list, whereas OpenCode merges primary and subagent into a single `agents` map keyed by name
 * with a `mode: primary|subagent|all` discriminator; hooks are declarative in Claude/Qwen/Codex
 * but plugin-based (JS modules) in OpenCode.
 */
sealed interface Target {

    /** Version of the `config.json` bundle format. */
    val schemaVersion: Int

    /** Minimum agent version required to apply this config. `null` means no constraint. */
    val minVersion: String?

    /** MCP server definitions to register with the agent. */
    val mcpServers: List<McpServer>

    /**
     * Slash-command / prompt template files to install.
     * All supported agents implement custom slash commands as markdown files: Claude Code under
     * `.claude/commands/`, OpenCode under `.opencode/command/`, Qwen Code under `.qwen/commands/`,
     * Codex under `~/.codex/prompts/`. Each [Template]'s [Template.path] is the file's destination
     * relative to the agent root.
     */
    val commands: List<Template>

    /**
     * Skill template files to install. All supported agents implement skills as `<name>/SKILL.md`
     * directories with YAML frontmatter, loaded on demand by the native `skill` tool. Auxiliary
     * skill assets (additional `.md` pages, scripts) live alongside `SKILL.md` and ship as separate
     * [Template] entries.
     */
    val skills: List<Template>
}
