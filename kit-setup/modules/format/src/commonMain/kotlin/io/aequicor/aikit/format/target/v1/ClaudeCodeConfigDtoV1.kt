package io.aequicor.aikit.format.target.v1

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** Top-level DTO for a Claude Code `config.json` inside an AI-Kit bundle. */
@Serializable
internal data class ClaudeCodeConfigDtoV1(
    val schemaVersion: Int = 1,
    val agent: String? = null,
    val minVersion: String? = null,
    val scope: String? = null,
    val settings: ClaudeSettingsDtoV1? = null,
    val memory: List<FileRefDtoV1>? = null,
    val mcpServers: List<McpServerDtoV1>? = null,
    val agents: List<FileRefDtoV1>? = null,
    val commands: List<FileRefDtoV1>? = null,
    val skills: List<FileRefDtoV1>? = null,
    val hooks: Map<String, List<HookGroupDtoV1>>? = null,
)

@Serializable
internal data class ClaudeSettingsDtoV1(
    val model: String? = null,
    val includeCoAuthoredBy: Boolean? = null,
    val env: Map<String, String>? = null,
    val permissions: ClaudePermissionsDtoV1? = null,
)

@Serializable
internal data class ClaudePermissionsDtoV1(
    val defaultMode: String? = null,
    /** Each element is either a plain string or an object `{"value":"…","when":"…"}`. */
    val allow: List<JsonElement>? = null,
    val deny: List<JsonElement>? = null,
    val ask: List<JsonElement>? = null,
    val additionalDirectories: List<String>? = null,
)

/**
 * Declarative reference to a file inside the bundle, with an optional AKEL `when` condition.
 *
 * Used for `agents`, `commands`, `skills`, and `memory` entries in `config.json`.
 * When `when` evaluates to `false` at generation time the entry is excluded entirely.
 */
@Serializable
internal data class FileRefDtoV1(
    val name: String,
    val source: String,
    val `when`: String? = null,
)
