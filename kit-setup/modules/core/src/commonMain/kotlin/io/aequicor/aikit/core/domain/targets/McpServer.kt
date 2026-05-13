package io.aequicor.aikit.core.domain.targets

/**
 * An MCP server entry in a [Target]'s `mcpServers` list.
 *
 * The active variant determines the transport protocol. Raw `${bundle.input.<id>}` placeholders in
 * string-valued fields are resolved by the linking layer, not by the domain model.
 */
sealed interface McpServer {

    /** Server name used to identify it in the generated agent config. */
    val name: String

    /** If `false`, the server entry is written but disabled. `null` means agent default. */
    val enabled: Boolean?

    /** Connection timeout in milliseconds. `null` means agent default. */
    val timeout: Int?

    /**
     * MCP server using `stdio` transport — spawned as a local child process.
     *
     * @property command Executable to run.
     * @property args Command-line arguments.
     * @property env Environment variables injected into the subprocess.
     */
    data class Stdio(
        override val name: String,
        override val enabled: Boolean?,
        override val timeout: Int?,
        val command: String,
        val args: List<String>,
        val env: Map<String, String>,
    ) : McpServer

    /**
     * MCP server using `sse` transport — connected to a remote HTTP/SSE endpoint.
     *
     * @property url Full URL of the SSE endpoint.
     * @property headers HTTP headers sent with every request to [url].
     */
    data class Sse(
        override val name: String,
        override val enabled: Boolean?,
        override val timeout: Int?,
        val url: String,
        val headers: Map<String, String>,
    ) : McpServer
}
