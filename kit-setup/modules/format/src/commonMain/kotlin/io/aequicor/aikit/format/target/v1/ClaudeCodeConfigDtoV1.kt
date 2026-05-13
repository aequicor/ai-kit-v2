package io.aequicor.aikit.format.target.v1

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** Top-level DTO for a Claude Code `config.json` inside an AI-Kit bundle. */
@Serializable
internal data class ClaudeCodeConfigDtoV1(
    val schemaVersion: Int = 1,
    val minVersion: String? = null,
    val settings: ClaudeSettingsDtoV1? = null,
    val mcpServers: List<McpServerDtoV1>? = null,
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
