package io.aequicor.aikit.core.domain.targets

import io.aequicor.aikit.core.domain.template.Template

/**
 * Parsed and resolved representation of a bundle target — the agent-specific configuration
 * paired with the template files that will be installed.
 *
 * [commands] and [skills] hold the template files as loaded from the bundle. The `engine` module
 * renders [Template.bytes] against resolved inputs before writing to disk.
 *
 * Agent-specific concepts (subagents, hooks, permissions, plugins) belong to the concrete
 * implementations.
 */
sealed interface Target {

    /** Version of the `config.json` bundle format. */
    val schemaVersion: Int

    /** Minimum agent version required to apply this config. `null` means no constraint. */
    val minVersion: String?

    /** MCP server definitions to register with the agent. */
    val mcpServers: List<McpServer>

    /** Slash-command / prompt template files to install (e.g. `.claude/commands/`). */
    val commands: List<Template>

    /** Skill template files to install (e.g. `.claude/skills/`). */
    val skills: List<Template>
}
