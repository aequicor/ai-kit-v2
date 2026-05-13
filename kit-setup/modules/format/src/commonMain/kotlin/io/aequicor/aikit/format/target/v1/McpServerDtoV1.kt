package io.aequicor.aikit.format.target.v1

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * DTO for a single MCP server entry in a bundle's `config.json`.
 *
 * The transport is determined by which fields are present:
 * - [command] present → stdio transport
 * - [url] present (and no [command]) → SSE transport
 * - [httpUrl] present → HTTP Streamable transport (Qwen Code only)
 *
 * The `when` field is the raw AKEL condition; if it evaluates to `false` at generation time,
 * this server is excluded from the native config output.
 *
 * All string fields may contain `${bundle.input.<id>}` placeholders — resolved by the engine.
 */
@Serializable
internal data class McpServerDtoV1(
    val name: String,
    val transport: String? = null,
    val command: String? = null,
    val args: List<String>? = null,
    val env: Map<String, String>? = null,
    val url: String? = null,
    val httpUrl: String? = null,
    val headers: Map<String, String>? = null,
    val enabled: Boolean? = null,
    val timeout: Int? = null,
    val trust: Boolean? = null,
    val description: String? = null,
    val `when`: String? = null,
)

/**
 * Raw hook-group entry in a bundle's `config.json` hooks map.
 *
 * Each entry represents one group whose handler details live in [hooks]. The [matcher] controls
 * which tool invocations trigger the group. The [condition] is the raw AKEL `when` expression.
 *
 * The [hooks] list contains raw [JsonObject]s discriminated by the `"type"` field; the mapper
 * routes each one to the correct [io.aequicor.aikit.core.domain.targets.ClaudeHookHandler] or
 * [io.aequicor.aikit.core.domain.targets.QwenHookHandler] subtype.
 */
@Serializable
internal data class HookGroupDtoV1(
    val matcher: String? = null,
    val sequential: Boolean = false,
    val hooks: List<JsonObject> = emptyList(),
    val `when`: String? = null,
)
