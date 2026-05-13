package io.aequicor.aikit.format.target.v1

import io.aequicor.aikit.core.domain.targets.QwenHookEvent
import io.aequicor.aikit.core.domain.targets.QwenHookGroup
import io.aequicor.aikit.core.domain.targets.QwenHookHandler
import io.aequicor.aikit.format.error.FormatError
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
        hooks = dto.hooks.map { mapHandler(it) },
        condition = dto.`when`,
    )

    private fun mapHandler(obj: JsonObject): QwenHookHandler {
        val type = obj["type"]?.jsonPrimitive?.contentOrNull ?: "command"
        val condition = obj["if"]?.jsonPrimitive?.contentOrNull
        return when (type) {
            "command" -> mapCommandHandler(obj, condition)
            "http" -> mapHttpHandler(obj, condition)
            else -> throw FormatError.UnknownEnum(type, "qwen hook.type", setOf("command", "http"))
        }
    }

    private fun mapCommandHandler(obj: JsonObject, condition: String?): QwenHookHandler.Command {
        val command = obj["command"]?.jsonPrimitive?.contentOrNull
            ?: throw FormatError.MissingField("command", "qwen hook (type=command)")
        return QwenHookHandler.Command(
            condition = condition,
            command = command,
            name = obj["name"]?.jsonPrimitive?.contentOrNull,
            description = obj["description"]?.jsonPrimitive?.contentOrNull,
            shell = obj["shell"]?.jsonPrimitive?.contentOrNull,
            async = obj["async"]?.jsonPrimitive?.booleanOrNull ?: false,
            timeout = obj["timeout"]?.jsonPrimitive?.intOrNull,
            env = obj["env"]?.jsonObject?.mapValues { it.value.jsonPrimitive.content } ?: emptyMap(),
            statusMessage = obj["statusMessage"]?.jsonPrimitive?.contentOrNull,
        )
    }

    private fun mapHttpHandler(obj: JsonObject, condition: String?): QwenHookHandler.Http {
        val url = obj["url"]?.jsonPrimitive?.contentOrNull
            ?: throw FormatError.MissingField("url", "qwen hook (type=http)")
        return QwenHookHandler.Http(
            condition = condition,
            url = url,
            headers = obj["headers"]?.jsonObject?.mapValues { it.value.jsonPrimitive.content } ?: emptyMap(),
            allowedEnvVars = obj["allowedEnvVars"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
            timeout = obj["timeout"]?.jsonPrimitive?.intOrNull,
            once = obj["once"]?.jsonPrimitive?.booleanOrNull ?: false,
        )
    }
}
