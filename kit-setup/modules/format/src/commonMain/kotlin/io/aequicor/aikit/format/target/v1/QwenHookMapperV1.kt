package io.aequicor.aikit.format.target.v1

import io.aequicor.aikit.core.domain.targets.QwenHookEvent
import io.aequicor.aikit.core.domain.targets.QwenHookGroup
import io.aequicor.aikit.core.domain.targets.QwenHookHandler
import io.aequicor.aikit.format.error.FormatError

internal object QwenHookMapperV1 {

    fun mapHooks(raw: Map<String, List<HookGroupDtoV1>>): Map<QwenHookEvent, List<QwenHookGroup>> =
        buildMap {
            for ((eventName, groups) in raw) {
                val event = parseEvent(eventName) ?: continue
                put(event, groups.map { mapGroup(it) })
            }
        }

    private fun parseEvent(name: String): QwenHookEvent? = when (name) {
        "PreToolUse" -> QwenHookEvent.PRE_TOOL_USE
        "PostToolUse" -> QwenHookEvent.POST_TOOL_USE
        "UserPromptSubmit" -> QwenHookEvent.USER_PROMPT_SUBMIT
        "Notification" -> QwenHookEvent.NOTIFICATION
        "Stop" -> QwenHookEvent.STOP
        "SessionStart" -> QwenHookEvent.SESSION_START
        "SessionEnd" -> QwenHookEvent.SESSION_END
        else -> null
    }

    private fun mapGroup(dto: HookGroupDtoV1): QwenHookGroup = QwenHookGroup(
        matcher = dto.matcher,
        sequential = dto.sequential,
        hooks = listOf(mapHandler(dto)),
        condition = dto.`when`,
    )

    private fun mapHandler(dto: HookGroupDtoV1): QwenHookHandler = when (dto.type ?: inferType(dto)) {
        "command" -> mapCommandHandler(dto)
        "http" -> mapHttpHandler(dto)
        else -> throw FormatError.UnknownEnum(
            dto.type ?: inferType(dto), "qwen hook.type", setOf("command", "http"),
        )
    }

    private fun mapCommandHandler(dto: HookGroupDtoV1) = QwenHookHandler.Command(
        condition = null,
        command = dto.command ?: throw FormatError.MissingField("command", "qwen hook (type=command)"),
        name = dto.name,
        description = dto.description,
        shell = dto.shell,
        async = dto.async,
        timeout = dto.timeout,
        env = dto.env ?: emptyMap(),
        statusMessage = dto.statusMessage,
    )

    private fun mapHttpHandler(dto: HookGroupDtoV1) = QwenHookHandler.Http(
        condition = null,
        url = dto.url ?: throw FormatError.MissingField("url", "qwen hook (type=http)"),
        headers = dto.headers ?: emptyMap(),
        allowedEnvVars = dto.allowedEnvVars ?: emptyList(),
        timeout = dto.timeout,
        once = dto.once,
    )

    private fun inferType(dto: HookGroupDtoV1): String = when {
        dto.url != null -> "http"
        else -> "command"
    }
}
