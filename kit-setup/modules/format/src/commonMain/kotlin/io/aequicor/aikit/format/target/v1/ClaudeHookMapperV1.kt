package io.aequicor.aikit.format.target.v1

import io.aequicor.aikit.core.domain.targets.ClaudeHookEvent
import io.aequicor.aikit.core.domain.targets.ClaudeHookGroup
import io.aequicor.aikit.core.domain.targets.ClaudeHookHandler
import io.aequicor.aikit.format.error.FormatError

internal object ClaudeHookMapperV1 {

    fun mapHooks(raw: Map<String, List<HookGroupDtoV1>>): Map<ClaudeHookEvent, List<ClaudeHookGroup>> =
        buildMap {
            for ((eventName, groups) in raw) {
                val event = parseEvent(eventName) ?: continue
                put(event, groups.map { mapGroup(it) })
            }
        }

    private fun parseEvent(name: String): ClaudeHookEvent? = when (name) {
        "PreToolUse" -> ClaudeHookEvent.PRE_TOOL_USE
        "PostToolUse" -> ClaudeHookEvent.POST_TOOL_USE
        "UserPromptSubmit" -> ClaudeHookEvent.USER_PROMPT_SUBMIT
        "Notification" -> ClaudeHookEvent.NOTIFICATION
        "Stop" -> ClaudeHookEvent.STOP
        "SubagentStop" -> ClaudeHookEvent.SUBAGENT_STOP
        "PreCompact" -> ClaudeHookEvent.PRE_COMPACT
        "SessionStart" -> ClaudeHookEvent.SESSION_START
        "SessionEnd" -> ClaudeHookEvent.SESSION_END
        else -> null
    }

    private fun mapGroup(dto: HookGroupDtoV1): ClaudeHookGroup = ClaudeHookGroup(
        matcher = dto.matcher ?: "",
        sequential = dto.sequential,
        hooks = listOf(mapHandler(dto)),
        condition = dto.`when`,
    )

    private fun mapHandler(dto: HookGroupDtoV1): ClaudeHookHandler = when (dto.type ?: inferType(dto)) {
        "command" -> mapCommandHandler(dto)
        "http" -> mapHttpHandler(dto)
        "mcp_tool" -> mapMcpToolHandler(dto)
        "prompt" -> mapPromptHandler(dto)
        "agent" -> mapAgentHandler(dto)
        else -> throw FormatError.UnknownEnum(
            dto.type ?: inferType(dto), "hook.type", setOf("command", "http", "mcp_tool", "prompt", "agent"),
        )
    }

    private fun mapCommandHandler(dto: HookGroupDtoV1) = ClaudeHookHandler.Command(
        condition = null,
        command = dto.command ?: throw FormatError.MissingField("command", "hook (type=command)"),
        args = dto.args ?: emptyList(),
        shell = dto.shell,
        async = dto.async,
        asyncRewake = dto.asyncRewake,
        timeout = dto.timeout,
        statusMessage = dto.statusMessage,
        once = dto.once,
    )

    private fun mapHttpHandler(dto: HookGroupDtoV1) = ClaudeHookHandler.Http(
        condition = null,
        url = dto.url ?: throw FormatError.MissingField("url", "hook (type=http)"),
        headers = dto.headers ?: emptyMap(),
        allowedEnvVars = dto.allowedEnvVars ?: emptyList(),
        timeout = dto.timeout,
    )

    private fun mapMcpToolHandler(dto: HookGroupDtoV1) = ClaudeHookHandler.McpTool(
        condition = null,
        server = dto.server ?: throw FormatError.MissingField("server", "hook (type=mcp_tool)"),
        tool = dto.tool ?: throw FormatError.MissingField("tool", "hook (type=mcp_tool)"),
        input = dto.input ?: emptyMap(),
    )

    private fun mapPromptHandler(dto: HookGroupDtoV1) = ClaudeHookHandler.Prompt(
        condition = null,
        prompt = dto.prompt ?: throw FormatError.MissingField("prompt", "hook (type=prompt)"),
        model = dto.model,
    )

    private fun mapAgentHandler(dto: HookGroupDtoV1) = ClaudeHookHandler.Agent(
        condition = null,
        prompt = dto.prompt ?: throw FormatError.MissingField("prompt", "hook (type=agent)"),
        model = dto.model,
    )

    private fun inferType(dto: HookGroupDtoV1): String = when {
        dto.url != null -> "http"
        dto.server != null -> "mcp_tool"
        dto.prompt != null -> "prompt"
        else -> "command"
    }
}
