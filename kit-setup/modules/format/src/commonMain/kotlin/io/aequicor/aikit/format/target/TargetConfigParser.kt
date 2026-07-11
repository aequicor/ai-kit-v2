package io.aequicor.aikit.format.target

import io.aequicor.aikit.core.domain.targets.ClaudeCode
import io.aequicor.aikit.core.domain.targets.Codex
import io.aequicor.aikit.core.domain.targets.OpenCode
import io.aequicor.aikit.core.domain.targets.QwenCode
import io.aequicor.aikit.core.domain.targets.Target
import io.aequicor.aikit.format.error.FormatError
import io.aequicor.aikit.format.target.v1.ClaudeCodeConfigDtoV1
import io.aequicor.aikit.format.target.v1.ClaudeCodeConfigMapperV1
import io.aequicor.aikit.format.target.v1.CodexConfigDtoV1
import io.aequicor.aikit.format.target.v1.CodexConfigMapperV1
import io.aequicor.aikit.format.target.v1.OpenCodeConfigDtoV1
import io.aequicor.aikit.format.target.v1.OpenCodeConfigMapperV1
import io.aequicor.aikit.format.target.v1.QwenCodeConfigDtoV1
import io.aequicor.aikit.format.target.v1.QwenCodeConfigMapperV1
import kotlinx.serialization.json.Json

/**
 * Parses a target's `config.json` and merges the result into an existing stub [Target].
 *
 * Fills in the config-specific fields: model, permissions, hooks, MCP servers, etc.
 * Template file handling (commands, skills, subagents, plugins) is done separately in
 * [io.aequicor.aikit.format.bundle.v1.BundleManifestMapperV1].
 */
internal class TargetConfigParser(private val json: Json) {

    fun parse(configJson: String, stub: Target): Result<Target> = runCatching {
        when (stub) {
            is ClaudeCode -> parseClaudeConfig(configJson, stub)
            is OpenCode -> parseOpenCodeConfig(configJson, stub)
            is QwenCode -> parseQwenConfig(configJson, stub)
            is Codex -> parseCodexConfig(configJson, stub)
        }
    }

    private fun parseClaudeConfig(configJson: String, stub: ClaudeCode): ClaudeCode {
        val dto = decodeOrThrow<ClaudeCodeConfigDtoV1>(configJson, "claude config.json")
        return ClaudeCodeConfigMapperV1.merge(stub, dto)
    }

    private fun parseOpenCodeConfig(configJson: String, stub: OpenCode): OpenCode {
        val dto = decodeOrThrow<OpenCodeConfigDtoV1>(configJson, "opencode config.json")
        return OpenCodeConfigMapperV1.merge(stub, dto)
    }

    private fun parseQwenConfig(configJson: String, stub: QwenCode): QwenCode {
        val dto = decodeOrThrow<QwenCodeConfigDtoV1>(configJson, "qwen config.json")
        return QwenCodeConfigMapperV1.merge(stub, dto)
    }

    private fun parseCodexConfig(configJson: String, stub: Codex): Codex {
        val dto = decodeOrThrow<CodexConfigDtoV1>(configJson, "codex config.json")
        return CodexConfigMapperV1.merge(stub, dto)
    }

    private inline fun <reified T> decodeOrThrow(configJson: String, context: String): T =
        try {
            json.decodeFromString<T>(configJson)
        } catch (e: IllegalArgumentException) {
            throw FormatError.BadJson("cannot decode $context: ${e.message}", e)
        } catch (e: IllegalStateException) {
            throw FormatError.BadJson("cannot decode $context: ${e.message}", e)
        }
}
