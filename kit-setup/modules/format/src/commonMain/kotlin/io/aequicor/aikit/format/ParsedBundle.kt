package io.aequicor.aikit.format

import io.aequicor.aikit.core.domain.bundle.BundleManifest
import io.aequicor.aikit.format.template.TemplateSource
import kotlinx.serialization.json.JsonElement

/**
 * Result of parsing a bundle — the domain manifest paired with per-target routing plans.
 *
 * [manifest] holds the fully parsed domain model (agent config, MCP servers, hooks, inputs).
 * [targetPlans] holds, for every target folder declared in `bundle.json`, the list of files
 * that must be installed and the raw `config.json` tree (used by the engine to render the
 * agent's native settings file).
 */
data class ParsedBundle(
    val manifest: BundleManifest,
    val targetPlans: Map<String, TargetPlan>,
)

/**
 * Routing plan for a single bundle target.
 *
 * @property targetFolder Folder name inside the bundle (e.g. `"claude-code"`).
 * @property routes Files declared in `config.json` (memory, agents, commands, skills, hook scripts),
 *   each tagged with its semantic [RouteKind].
 * @property rawConfig Parsed `config.json` tree, **before** AKEL `when` evaluation and
 *   `${bundle.input.*}` interpolation — the engine performs those passes with input values.
 *   `null` when the target has no `config.json`.
 */
data class TargetPlan(
    val targetFolder: String,
    val routes: List<FileRoute>,
    val rawConfig: JsonElement?,
)

/**
 * Semantic kind of a file inside a bundle target, as declared in `config.json`.
 *
 * Mirrors the high-level `FileKind` of the layout module, but lives in the format layer to keep
 * the parser independent of the layout abstraction.
 */
enum class RouteKind {
    /** Project-level memory file (`memory[*]` in config.json). */
    MEMORY,

    /** Subagent definition (`agents[*]` in config.json). */
    SUBAGENT,

    /** Slash command (`commands[*]` in config.json). */
    COMMAND,

    /** A single file inside a skill directory (`skills[*]` in config.json). */
    SKILL,

    /** Shell script referenced by a hook entry's `command` field. */
    HOOK_SCRIPT,
}

/**
 * A single file scheduled to be installed from a bundle target.
 *
 * @property kind Semantic category from `config.json`.
 * @property name Logical name. For commands/agents/memory/skills — the `name` field of the
 *   declaring entry. For [RouteKind.HOOK_SCRIPT] — the basename of the script (no semantic name).
 * @property source Raw bundle file (path + bytes).
 * @property skillRelPath Path **inside** the skill directory for [RouteKind.SKILL] entries;
 *   `null` for all other kinds.
 * @property condition AKEL `when` expression that must evaluate to `true` for this file to be
 *   installed. `null` means unconditional.
 */
data class FileRoute(
    val kind: RouteKind,
    val name: String,
    val source: TemplateSource,
    val skillRelPath: String? = null,
    val condition: String? = null,
)
