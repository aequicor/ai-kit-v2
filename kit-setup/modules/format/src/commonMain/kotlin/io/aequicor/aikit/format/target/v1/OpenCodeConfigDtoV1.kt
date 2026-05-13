package io.aequicor.aikit.format.target.v1

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** Top-level DTO for an OpenCode `config.json` inside an AI-Kit bundle. */
@Serializable
internal data class OpenCodeConfigDtoV1(
    val schemaVersion: Int = 1,
    val minVersion: String? = null,
    val model: String? = null,
    @SerialName("small_model") val smallModel: String? = null,
    @SerialName("default_agent") val defaultAgent: String? = null,
    val shell: String? = null,
    val share: String? = null,
    val snapshot: Boolean? = null,
    val instructions: List<String>? = null,
    val provider: Map<String, OpenCodeProviderDtoV1>? = null,
    val tools: Map<String, Boolean>? = null,
    /** Values are `String` (permission level) or nested `Map<String, String>`. Kept as raw JSON. */
    val permission: Map<String, JsonElement>? = null,
    val agent: Map<String, OpenCodeAgentDtoV1>? = null,
    val compaction: OpenCodeCompactionDtoV1? = null,
    val mcp: Map<String, OpenCodeMcpDtoV1>? = null,
    val plugin: List<String>? = null,
)

@Serializable
internal data class OpenCodeProviderDtoV1(
    val options: Map<String, String>? = null,
)

@Serializable
internal data class OpenCodeAgentDtoV1(
    val description: String? = null,
    val model: String? = null,
    val prompt: String? = null,
    val tools: Map<String, Boolean>? = null,
    val maxTokens: Int? = null,
    val reasoningEffort: String? = null,
)

@Serializable
internal data class OpenCodeCompactionDtoV1(
    val auto: Boolean? = null,
    val prune: Boolean? = null,
    val reserved: Int? = null,
)

@Serializable
internal data class OpenCodeMcpDtoV1(
    val type: String? = null,
    val command: String? = null,
    val args: List<String>? = null,
    val env: List<String>? = null,
    val url: String? = null,
    val headers: Map<String, String>? = null,
    val enabled: Boolean? = null,
    val timeout: Int? = null,
    val `when`: String? = null,
)
