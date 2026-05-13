package io.aequicor.aikit.format.target.v1

import io.aequicor.aikit.core.domain.targets.ClaudeHookEvent
import io.aequicor.aikit.core.domain.targets.ClaudeHookGroup
import io.aequicor.aikit.core.domain.targets.ClaudeHookHandler
import io.aequicor.aikit.format.error.FormatError
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
        hooks = dto.hooks.map { mapHandler(it) },
        condition = dto.`when`,
    )

    private fun mapHandler(obj: JsonObject): ClaudeHookHandler {
        val type = obj["type"]?.jsonPrimitive?.contentOrNull ?: "command"
        val condition = obj["if"]?.jsonPrimitive?.contentOrNull
        return when (type) {
            "command" -> mapCommandHandler(obj, condition)
            "http" -> mapHttpHandler(obj, condition)
            "mcp_tool" -> mapMcpToolHandler(obj, condition)
            "prompt" -> mapPromptHandler(obj, condition)
            "agent" -> mapAgentHandler(obj, condition)
            else -> throw FormatError.UnknownEnum(type, "hook.type", setOf("command", "http", "mcp_tool", "prompt", "agent"))
        }
    }

    private fun mapCommandHandler(obj: JsonObject, condition: String?): ClaudeHookHandler.Command {
        val command = obj["command"]?.jsonPrimitive?.contentOrNull
            ?: throw FormatError.MissingField("command", "hook (type=command)")
        return ClaudeHookHandler.Command(
            condition = condition,
            command = command,
            args = obj["args"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
            shell = obj["shell"]?.jsonPrimitive?.contentOrNull,
            async = obj["async"]?.jsonPrimitive?.booleanOrNull ?: false,
            asyncRewake = obj["asyncRewake"]?.jsonPrimitive?.booleanOrNull ?: false,
            timeout = obj["timeout"]?.jsonPrimitive?.intOrNull,
            statusMessage = obj["statusMessage"]?.jsonPrimitive?.contentOrNull,
            once = obj["once"]?.jsonPrimitive?.booleanOrNull ?: false,
        )
    }

    private fun mapHttpHandler(obj: JsonObject, condition: String?): ClaudeHookHandler.Http {
        val url = obj["url"]?.jsonPrimitive?.contentOrNull
            ?: throw FormatError.MissingField("url", "hook (type=http)")
        return ClaudeHookHandler.Http(
            condition = condition,
            url = url,
            headers = obj["headers"]?.jsonObject?.mapValues { it.value.jsonPrimitive.content } ?: emptyMap(),
            allowedEnvVars = obj["allowedEnvVars"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
            timeout = obj["timeout"]?.jsonPrimitive?.intOrNull,
        )
    }

    private fun mapMcpToolHandler(obj: JsonObject, condition: String?): ClaudeHookHandler.McpTool {
        val server = obj["server"]?.jsonPrimitive?.contentOrNull
            ?: throw FormatError.MissingField("server", "hook (type=mcp_tool)")
        val tool = obj["tool"]?.jsonPrimitive?.contentOrNull
            ?: throw FormatError.MissingField("tool", "hook (type=mcp_tool)")
        return ClaudeHookHandler.McpTool(
            condition = condition,
            server = server,
            tool = tool,
            input = obj["input"]?.jsonObject?.mapValues { it.value.jsonPrimitive.content } ?: emptyMap(),
        )
    }

    private fun mapPromptHandler(obj: JsonObject, condition: String?): ClaudeHookHandler.Prompt {
        val prompt = obj["prompt"]?.jsonPrimitive?.contentOrNull
            ?: throw FormatError.MissingField("prompt", "hook (type=prompt)")
        return ClaudeHookHandler.Prompt(
            condition = condition,
            prompt = prompt,
            model = obj["model"]?.jsonPrimitive?.contentOrNull,
        )
    }

    private fun mapAgentHandler(obj: JsonObject, condition: String?): ClaudeHookHandler.Agent {
        val prompt = obj["prompt"]?.jsonPrimitive?.contentOrNull
            ?: throw FormatError.MissingField("prompt", "hook (type=agent)")
        return ClaudeHookHandler.Agent(
            condition = condition,
            prompt = prompt,
            model = obj["model"]?.jsonPrimitive?.contentOrNull,
        )
    }
}
