package io.aequicor.aikit.format.target.v1

import io.aequicor.aikit.core.domain.targets.McpServer
import io.aequicor.aikit.core.domain.targets.OpenCode
import io.aequicor.aikit.core.domain.targets.OpenCodeAgentDef
import io.aequicor.aikit.core.domain.targets.OpenCodeAgentMode
import io.aequicor.aikit.core.domain.targets.OpenCodeCompaction
import io.aequicor.aikit.core.domain.targets.OpenCodePermissionLevel
import io.aequicor.aikit.core.domain.targets.OpenCodeProvider
import io.aequicor.aikit.core.domain.targets.OpenCodeShareMode
import io.aequicor.aikit.format.error.FormatError
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

internal object OpenCodeConfigMapperV1 {

    fun merge(stub: OpenCode, dto: OpenCodeConfigDtoV1): OpenCode = stub.copy(
        minVersion = dto.minVersion,
        mcpServers = dto.mcp?.values?.map { it.toDomain() } ?: emptyList(),
        model = dto.model,
        smallModel = dto.smallModel,
        defaultAgent = dto.defaultAgent,
        shell = dto.shell,
        share = dto.share?.let { parseShareMode(it) },
        snapshot = dto.snapshot,
        instructions = dto.instructions,
        provider = dto.provider?.mapValues { OpenCodeProvider(it.value.options ?: emptyMap()) },
        tools = dto.tools,
        permission = dto.permission?.mapValues { parsePermissionLevel(it.value.jsonPrimitive.contentOrNull ?: "") },
        agents = dto.agent?.mapValues { mapAgent(it.value) },
        compaction = dto.compaction?.let { OpenCodeCompaction(it.auto, it.prune, it.reserved) },
    )

    private fun OpenCodeMcpDtoV1.toDomain(): McpServer {
        val isLocal = type == "local" || command != null
        return if (isLocal) {
            McpServer.Stdio(
                name = "mcp",
                condition = `when`,
                enabled = enabled,
                timeout = timeout,
                command = command ?: error("OpenCode local MCP server missing 'command'"),
                args = args ?: emptyList(),
                env = env?.associate { it.substringBefore("=") to it.substringAfter("=", "") } ?: emptyMap(),
            )
        } else {
            McpServer.Sse(
                name = "mcp",
                condition = `when`,
                enabled = enabled,
                timeout = timeout,
                url = url ?: error("OpenCode remote MCP server missing 'url'"),
                headers = headers ?: emptyMap(),
            )
        }
    }

    private fun mapAgent(dto: OpenCodeAgentDtoV1): OpenCodeAgentDef = OpenCodeAgentDef(
        mode = OpenCodeAgentMode.SUBAGENT,
        description = dto.description,
        model = dto.model,
        prompt = dto.prompt,
        tools = dto.tools ?: emptyMap(),
    )

    private fun parseShareMode(value: String): OpenCodeShareMode = when (value) {
        "manual" -> OpenCodeShareMode.MANUAL
        "auto" -> OpenCodeShareMode.AUTO
        "disabled" -> OpenCodeShareMode.DISABLED
        else -> throw FormatError.UnknownEnum(value, "share", setOf("manual", "auto", "disabled"))
    }

    private fun parsePermissionLevel(value: String): OpenCodePermissionLevel = when (value) {
        "allow" -> OpenCodePermissionLevel.ALLOW
        "ask" -> OpenCodePermissionLevel.ASK
        "deny" -> OpenCodePermissionLevel.DENY
        else -> OpenCodePermissionLevel.ASK
    }
}
