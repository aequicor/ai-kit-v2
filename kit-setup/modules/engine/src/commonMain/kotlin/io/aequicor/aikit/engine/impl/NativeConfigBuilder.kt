package io.aequicor.aikit.engine.impl

import io.aequicor.aikit.akel.AkelValue
import io.aequicor.aikit.core.domain.targets.ClaudeCode
import io.aequicor.aikit.core.domain.targets.OpenCode
import io.aequicor.aikit.core.domain.targets.QwenCode
import io.aequicor.aikit.core.domain.targets.Target
import io.aequicor.aikit.layout.AgentLayout
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * A native config file the engine should write alongside the routed bundle files.
 *
 * @property relPath Destination path relative to the application path.
 * @property bytes Serialized JSON content.
 */
internal data class NativeConfigFile(val relPath: String, val bytes: ByteArray)

/**
 * Builds the agent's native settings/MCP files from the bundle's `config.json` tree.
 *
 * The engine has already routed bundle files to disk based on `memory`, `agents`, `commands`,
 * `skills` and hook script entries. This builder produces the **declarative** configuration files
 * (e.g. `.claude/settings.json`, `.mcp.json`, `.qwen/settings.json`) by extracting the relevant
 * sections from the post-`when`-pass JSON tree.
 */
@Suppress("ReturnCount", "LoopWithTooManyJumpStatements")
internal class NativeConfigBuilder(private val jsonWriter: kotlinx.serialization.json.Json) {

    fun build(target: Target, rawConfig: JsonElement?, layout: AgentLayout, inputs: Map<String, AkelValue>): List<NativeConfigFile> {
        if (rawConfig == null) return emptyList()
        val processed = JsonTreeProcessor.process(rawConfig, inputs) as? JsonObject ?: return emptyList()
        return when (target) {
            is ClaudeCode -> buildClaude(processed, layout)
            is QwenCode -> buildQwen(processed, layout)
            is OpenCode -> buildOpenCode(processed, layout)
        }
    }

    // ── Claude Code ──────────────────────────────────────────────────────────

    private fun buildClaude(root: JsonObject, layout: AgentLayout): List<NativeConfigFile> {
        val files = mutableListOf<NativeConfigFile>()
        val settingsBody = buildClaudeSettings(root)
        if (settingsBody.isNotEmpty()) {
            layout.settingsFile()?.let { path ->
                files += NativeConfigFile(path, encode(JsonObject(settingsBody)))
            }
        }
        val mcpBody = buildMcpServersObject(root["mcpServers"])
        if (mcpBody != null) {
            layout.mcpFile()?.let { path ->
                files += NativeConfigFile(
                    path,
                    encode(JsonObject(mapOf("mcpServers" to mcpBody))),
                )
            }
        }
        return files
    }

    private fun buildClaudeSettings(root: JsonObject): Map<String, JsonElement> {
        val out = linkedMapOf<String, JsonElement>()
        (root["settings"] as? JsonObject)?.forEach { (k, v) -> out[k] = v }
        (root["hooks"] as? JsonObject)?.let { out["hooks"] = renderClaudeHooks(it) }
        return out
    }

    private fun renderClaudeHooks(hooksByEvent: JsonObject): JsonObject {
        val converted = hooksByEvent.mapValues { (_, groupsElement) ->
            val groups = groupsElement as? JsonArray ?: return@mapValues groupsElement
            JsonArray(groups.map { wrapHookGroup(it) })
        }
        return JsonObject(converted)
    }

    private fun wrapHookGroup(entry: JsonElement): JsonElement {
        val obj = entry as? JsonObject ?: return entry
        val matcher = obj["matcher"]
        val handler = obj.filterKeys { it != "matcher" && it != "when" && it != "sequential" }
        val handlerObj = JsonObject(handler.toMap().withInferredType())
        val out = linkedMapOf<String, JsonElement>()
        if (matcher != null) out["matcher"] = matcher
        out["hooks"] = JsonArray(listOf(handlerObj))
        return JsonObject(out)
    }

    private fun Map<String, JsonElement>.withInferredType(): Map<String, JsonElement> {
        if (containsKey("type")) return this
        val inferred = when {
            containsKey("command") -> "command"
            containsKey("url") -> "http"
            containsKey("server") -> "mcp_tool"
            containsKey("prompt") -> "prompt"
            else -> return this
        }
        val out = linkedMapOf<String, JsonElement>("type" to JsonPrimitive(inferred))
        out.putAll(this)
        return out
    }

    // ── Qwen Code ────────────────────────────────────────────────────────────

    private fun buildQwen(root: JsonObject, layout: AgentLayout): List<NativeConfigFile> {
        val out = linkedMapOf<String, JsonElement>()
        listOf("model", "modelProviders", "permissions", "tools", "general", "context", "telemetry").forEach { k ->
            root[k]?.let { out[k] = it }
        }
        buildMcpServersObject(root["mcpServers"])?.let { out["mcpServers"] = it }
        (root["hooks"] as? JsonObject)?.let { out["hooks"] = it }
        if (out.isEmpty()) return emptyList()
        val path = layout.settingsFile() ?: return emptyList()
        return listOf(NativeConfigFile(path, encode(JsonObject(out))))
    }

    // ── OpenCode ─────────────────────────────────────────────────────────────

    private fun buildOpenCode(root: JsonObject, layout: AgentLayout): List<NativeConfigFile> {
        // Pass through the full processed tree minus the bundle-specific sections that have been
        // turned into routed files. Useful for future expansion; for now produces opencode.json
        // when the bundle declares any non-routing config.
        val excluded = setOf("schemaVersion", "agent", "minVersion", "scope", "memory", "agents", "commands", "skills", "hooks")
        val out = root.filterKeys { it !in excluded }
        if (out.isEmpty()) return emptyList()
        val path = layout.settingsFile() ?: return emptyList()
        return listOf(NativeConfigFile(path, encode(JsonObject(out))))
    }

    // ── Shared helpers ───────────────────────────────────────────────────────

    /** Reshapes the bundle `mcpServers` array into a `{ "<name>": { ... } }` object. */
    private fun buildMcpServersObject(element: JsonElement?): JsonObject? {
        val arr = element as? JsonArray ?: return null
        if (arr.isEmpty()) return null
        val map = linkedMapOf<String, JsonElement>()
        for (entry in arr) {
            val obj = entry as? JsonObject ?: continue
            val name = obj["name"]?.jsonPrimitive?.contentOrNull ?: continue
            val body = obj.filterKeys { it != "name" && it != "when" }
            map[name] = JsonObject(body.toMap())
        }
        return if (map.isEmpty()) null else JsonObject(map)
    }

    private fun encode(element: JsonElement): ByteArray =
        jsonWriter.encodeToString(JsonElement.serializer(), element).encodeToByteArray()
}
