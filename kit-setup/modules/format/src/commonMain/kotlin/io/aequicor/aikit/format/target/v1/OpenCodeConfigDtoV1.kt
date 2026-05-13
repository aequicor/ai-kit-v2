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
    /** Values are `"allow"` | `"ask"` | `"deny"` or nested rule objects. Kept as raw JSON. */
    val permission: Map<String, JsonElement>? = null,
    val agent: Map<String, OpenCodeAgentDtoV1>? = null,
    val compaction: OpenCodeCompactionDtoV1? = null,
    val mcp: Map<String, OpenCodeMcpDtoV1>? = null,
    val plugin: List<String>? = null,
    /** Map of formatter name → entry (or `true`/`false` to enable/disable all). Kept as raw JSON. */
    val formatter: JsonElement? = null,
    /** Map of LSP server name → entry (or `true`/`false` to enable/disable all). Kept as raw JSON. */
    val lsp: JsonElement? = null,
)

@Serializable
internal data class OpenCodeProviderDtoV1(
    val options: Map<String, String>? = null,
)

@Serializable
internal data class OpenCodeAgentDtoV1(
    val model: String? = null,
    val mode: String? = null,
    val description: String? = null,
    val prompt: String? = null,
    val tools: Map<String, Boolean>? = null,
    val steps: Int? = null,
    /** @deprecated use steps */
    val maxTokens: Int? = null,
    val reasoningEffort: String? = null,
    val disable: Boolean? = null,
    val hidden: Boolean? = null,
)

@Serializable
internal data class OpenCodeCompactionDtoV1(
    val auto: Boolean? = null,
    val prune: Boolean? = null,
    val reserved: Int? = null,
    @SerialName("tail_turns") val tailTurns: Int? = null,
    @SerialName("preserve_recent_tokens") val preserveRecentTokens: Int? = null,
)

/**
 * OpenCode MCP server entry. Discriminated by [type]:
 * - `"local"` — local stdio process; [command] is the full argv array.
 * - `"remote"` — remote HTTP/SSE endpoint; [url] is the endpoint.
 */
@Serializable
internal data class OpenCodeMcpDtoV1(
    val type: String? = null,
    /** Full command argv (local servers). Replaces separate `command` + `args`. */
    val command: List<String>? = null,
    /** Environment variables injected into the server process (local servers). */
    val environment: Map<String, String>? = null,
    /** Remote endpoint URL (remote servers). */
    val url: String? = null,
    val headers: Map<String, String>? = null,
    val enabled: Boolean? = null,
    val timeout: Int? = null,
    val `when`: String? = null,
)
