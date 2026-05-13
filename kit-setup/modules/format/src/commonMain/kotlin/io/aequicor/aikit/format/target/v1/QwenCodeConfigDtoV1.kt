package io.aequicor.aikit.format.target.v1

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** Top-level DTO for a Qwen Code `config.json` inside an AI-Kit bundle. */
@Serializable
internal data class QwenCodeConfigDtoV1(
    val schemaVersion: Int = 1,
    val minVersion: String? = null,
    val model: QwenModelDtoV1? = null,
    val modelProviders: List<QwenModelProviderDtoV1>? = null,
    val mcpServers: List<McpServerDtoV1>? = null,
    val permissions: QwenPermissionsDtoV1? = null,
    val tools: QwenToolsDtoV1? = null,
    val general: QwenGeneralDtoV1? = null,
    val context: QwenContextDtoV1? = null,
    val telemetry: QwenTelemetryDtoV1? = null,
    val hooks: Map<String, List<HookGroupDtoV1>>? = null,
)

@Serializable
internal data class QwenModelDtoV1(
    val name: String,
    val maxSessionTurns: Int? = null,
    val generationConfig: QwenGenerationConfigDtoV1? = null,
    val chatCompression: QwenChatCompressionDtoV1? = null,
    val skipNextSpeakerCheck: Boolean? = null,
    val skipLoopDetection: Boolean? = null,
    val skipStartupContext: Boolean? = null,
)

@Serializable
internal data class QwenGenerationConfigDtoV1(
    val timeout: Int? = null,
    val maxRetries: Int? = null,
    val contextWindowSize: Int? = null,
    val reasoning: String? = null,
    val samplingParams: QwenSamplingParamsDtoV1? = null,
)

@Serializable
internal data class QwenSamplingParamsDtoV1(
    val temperature: Double? = null,
    val topP: Double? = null,
    val maxTokens: Int? = null,
)

@Serializable
internal data class QwenChatCompressionDtoV1(
    val contextPercentageThreshold: Double? = null,
)

@Serializable
internal data class QwenModelProviderDtoV1(
    val authType: String,
    val id: String,
    val name: String? = null,
    val envKey: String? = null,
    val baseUrl: String? = null,
    val generationConfig: QwenGenerationConfigDtoV1? = null,
)

@Serializable
internal data class QwenPermissionsDtoV1(
    /** Each element is either a plain string or `{"value":"…","when":"…"}`. */
    val allow: List<JsonElement>? = null,
    val deny: List<JsonElement>? = null,
    val ask: List<JsonElement>? = null,
)

@Serializable
internal data class QwenToolsDtoV1(
    val approvalMode: String? = null,
    val sandbox: Boolean? = null,
    val sandboxImage: String? = null,
    val useRipgrep: Boolean? = null,
    val truncateToolOutputThreshold: Int? = null,
    val truncateToolOutputLines: Int? = null,
)

@Serializable
internal data class QwenGeneralDtoV1(
    val vimMode: Boolean? = null,
    val enableAutoUpdate: Boolean? = null,
    val gitCoAuthor: QwenGitCoAuthorDtoV1? = null,
)

@Serializable
internal data class QwenGitCoAuthorDtoV1(
    val commit: Boolean? = null,
    val pr: Boolean? = null,
)

@Serializable
internal data class QwenContextDtoV1(
    val fileName: String? = null,
    val fileFiltering: QwenFileFilteringDtoV1? = null,
)

@Serializable
internal data class QwenFileFilteringDtoV1(
    val respectGitIgnore: Boolean? = null,
    val respectQwenIgnore: Boolean? = null,
    val enableRecursiveFileSearch: Boolean? = null,
    val enableFuzzySearch: Boolean? = null,
)

@Serializable
internal data class QwenTelemetryDtoV1(
    val enabled: Boolean? = null,
    val target: String? = null,
    val otlpEndpoint: String? = null,
    val otlpProtocol: String? = null,
    val logPrompts: Boolean? = null,
)
