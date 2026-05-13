package io.aequicor.aikit.format.target.v1

import io.aequicor.aikit.core.domain.targets.McpServer
import io.aequicor.aikit.core.domain.targets.PermissionRule
import io.aequicor.aikit.format.error.FormatError
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Maps a [McpServerDtoV1] to the sealed [McpServer] domain type. */
internal fun McpServerDtoV1.toDomain(): McpServer {
    val isStdio = command != null || transport == "stdio"
    return if (isStdio) {
        McpServer.Stdio(
            name = name,
            condition = `when`,
            enabled = enabled,
            timeout = timeout,
            command = command ?: error("stdio MCP server '$name' missing 'command'"),
            args = args ?: emptyList(),
            env = env ?: emptyMap(),
        )
    } else {
        McpServer.Sse(
            name = name,
            condition = `when`,
            enabled = enabled,
            timeout = timeout,
            url = url ?: httpUrl ?: error("MCP server '$name' missing 'url'"),
            headers = headers ?: emptyMap(),
        )
    }
}

/**
 * Maps a JSON element from a permission list to a [PermissionRule].
 *
 * Accepts two forms:
 * - Plain string: `"Bash(npm run test:*)"` → rule with no condition.
 * - Object: `{"value":"…","when":"…"}` → rule with optional condition.
 */
internal fun JsonElement.toPermissionRule(): PermissionRule = when (this) {
    is JsonPrimitive -> PermissionRule(value = content, condition = null)
    else -> {
        val obj = jsonObject
        val value = obj["value"]?.jsonPrimitive?.contentOrNull
            ?: throw FormatError.MissingField("value", "permission rule object")
        PermissionRule(value = value, condition = obj["when"]?.jsonPrimitive?.contentOrNull)
    }
}
