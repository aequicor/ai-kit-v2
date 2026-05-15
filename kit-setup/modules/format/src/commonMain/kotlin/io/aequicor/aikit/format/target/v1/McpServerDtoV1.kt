package io.aequicor.aikit.format.target.v1

import kotlinx.serialization.Serializable

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
    val type: String? = null,
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
 * Flat hook entry in a bundle's `config.json` hooks map.
 *
 * Each entry simultaneously describes the group (via [matcher], [sequential], `when`) and
 * its single handler (via [type] plus handler-specific fields). This flat format matches the
 * spec; the mapper wraps it into a [io.aequicor.aikit.core.domain.targets.ClaudeHookGroup] or
 * [io.aequicor.aikit.core.domain.targets.QwenHookGroup] containing one handler.
 *
 * Handler type is determined by [type] when present, otherwise inferred from which key field
 * is non-null: [command] → `"command"`, [url] → `"http"`, [server] → `"mcp_tool"`,
 * [prompt] → `"prompt"` / `"agent"`.
 */
@Serializable
internal data class HookGroupDtoV1(
    // group fields
    val matcher: String? = null,
    val sequential: Boolean = false,
    val `when`: String? = null,
    // handler type discriminator
    val type: String? = null,
    // command handler (Claude + Qwen)
    val command: String? = null,
    val args: List<String>? = null,
    val shell: String? = null,
    val async: Boolean = false,
    val asyncRewake: Boolean = false,
    val timeout: Int? = null,
    val statusMessage: String? = null,
    val once: Boolean = false,
    // Qwen-specific command fields
    val name: String? = null,
    val description: String? = null,
    val env: Map<String, String>? = null,
    // http handler (Claude + Qwen)
    val url: String? = null,
    val headers: Map<String, String>? = null,
    val allowedEnvVars: List<String>? = null,
    // mcp_tool handler (Claude only)
    val server: String? = null,
    val tool: String? = null,
    val input: Map<String, String>? = null,
    // prompt / agent handler (Claude only)
    val prompt: String? = null,
    val model: String? = null,
)
