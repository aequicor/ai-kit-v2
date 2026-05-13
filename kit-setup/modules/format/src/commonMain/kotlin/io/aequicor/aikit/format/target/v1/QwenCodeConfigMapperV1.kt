package io.aequicor.aikit.format.target.v1

import io.aequicor.aikit.core.domain.targets.QwenApprovalMode
import io.aequicor.aikit.core.domain.targets.QwenAuthType
import io.aequicor.aikit.core.domain.targets.QwenChatCompression
import io.aequicor.aikit.core.domain.targets.QwenCode
import io.aequicor.aikit.core.domain.targets.QwenContextConfig
import io.aequicor.aikit.core.domain.targets.QwenFileFiltering
import io.aequicor.aikit.core.domain.targets.QwenGeneralConfig
import io.aequicor.aikit.core.domain.targets.QwenGenerationConfig
import io.aequicor.aikit.core.domain.targets.QwenGitCoAuthor
import io.aequicor.aikit.core.domain.targets.QwenModelConfig
import io.aequicor.aikit.core.domain.targets.QwenModelProvider
import io.aequicor.aikit.core.domain.targets.QwenPermissions
import io.aequicor.aikit.core.domain.targets.QwenReasoningLevel
import io.aequicor.aikit.core.domain.targets.QwenSamplingParams
import io.aequicor.aikit.core.domain.targets.QwenTelemetryConfig
import io.aequicor.aikit.core.domain.targets.QwenToolsConfig
import io.aequicor.aikit.format.error.FormatError

internal object QwenCodeConfigMapperV1 {

    fun merge(stub: QwenCode, dto: QwenCodeConfigDtoV1): QwenCode = stub.copy(
        minVersion = dto.minVersion,
        mcpServers = dto.mcpServers?.map { it.toDomain() } ?: emptyList(),
        model = dto.model?.let { mapModel(it) },
        modelProviders = dto.modelProviders?.map { mapProvider(it) },
        permissions = dto.permissions?.let { mapPermissions(it) },
        tools = dto.tools?.let { mapTools(it) },
        general = dto.general?.let { mapGeneral(it) },
        context = dto.context?.let { mapContext(it) },
        telemetry = dto.telemetry?.let { mapTelemetry(it) },
        hooks = dto.hooks?.let { QwenHookMapperV1.mapHooks(it) } ?: emptyMap(),
    )

    private fun mapModel(dto: QwenModelDtoV1) = QwenModelConfig(
        name = dto.name,
        maxSessionTurns = dto.maxSessionTurns,
        generationConfig = dto.generationConfig?.let { mapGenerationConfig(it) },
        chatCompression = dto.chatCompression?.let { QwenChatCompression(it.contextPercentageThreshold) },
        skipNextSpeakerCheck = dto.skipNextSpeakerCheck,
        skipLoopDetection = dto.skipLoopDetection,
        skipStartupContext = dto.skipStartupContext,
    )

    private fun mapGenerationConfig(dto: QwenGenerationConfigDtoV1) = QwenGenerationConfig(
        timeout = dto.timeout,
        maxRetries = dto.maxRetries,
        contextWindowSize = dto.contextWindowSize,
        reasoning = dto.reasoning?.let { parseReasoningLevel(it) },
        samplingParams = dto.samplingParams?.let {
            QwenSamplingParams(it.temperature, it.topP, it.maxTokens)
        },
    )

    private fun mapProvider(dto: QwenModelProviderDtoV1) = QwenModelProvider(
        authType = when (dto.authType) {
            "openai", "OPENAI" -> QwenAuthType.OPENAI
            "anthropic", "ANTHROPIC" -> QwenAuthType.ANTHROPIC
            "gemini", "GEMINI" -> QwenAuthType.GEMINI
            "dashscope", "DASHSCOPE" -> QwenAuthType.DASHSCOPE
            else -> throw FormatError.UnknownEnum(
                dto.authType, "modelProviders[].authType",
                setOf("openai", "anthropic", "gemini", "dashscope"),
            )
        },
        id = dto.id,
        name = dto.name,
        envKey = dto.envKey,
        baseUrl = dto.baseUrl,
        generationConfig = dto.generationConfig?.let { mapGenerationConfig(it) },
    )

    private fun mapPermissions(dto: QwenPermissionsDtoV1) = QwenPermissions(
        allow = dto.allow?.map { it.toPermissionRule() } ?: emptyList(),
        deny = dto.deny?.map { it.toPermissionRule() } ?: emptyList(),
        ask = dto.ask?.map { it.toPermissionRule() } ?: emptyList(),
    )

    private fun mapTools(dto: QwenToolsDtoV1) = QwenToolsConfig(
        approvalMode = dto.approvalMode?.let { v ->
            when (v) {
                "default" -> QwenApprovalMode.DEFAULT
                "auto-edit", "autoEdit", "AUTO_EDIT" -> QwenApprovalMode.AUTO_EDIT
                "yolo", "YOLO" -> QwenApprovalMode.YOLO
                else -> throw FormatError.UnknownEnum(v, "tools.approvalMode", setOf("default", "auto-edit", "yolo"))
            }
        },
        sandbox = dto.sandbox,
        sandboxImage = dto.sandboxImage,
        useRipgrep = dto.useRipgrep,
        truncateToolOutputThreshold = dto.truncateToolOutputThreshold,
        truncateToolOutputLines = dto.truncateToolOutputLines,
    )

    private fun mapGeneral(dto: QwenGeneralDtoV1) = QwenGeneralConfig(
        vimMode = dto.vimMode,
        enableAutoUpdate = dto.enableAutoUpdate,
        gitCoAuthor = dto.gitCoAuthor?.let { QwenGitCoAuthor(it.commit, it.pr) },
    )

    private fun mapContext(dto: QwenContextDtoV1) = QwenContextConfig(
        fileName = dto.fileName,
        fileFiltering = dto.fileFiltering?.let {
            QwenFileFiltering(
                respectGitIgnore = it.respectGitIgnore,
                respectQwenIgnore = it.respectQwenIgnore,
                enableRecursiveFileSearch = it.enableRecursiveFileSearch,
                enableFuzzySearch = it.enableFuzzySearch,
            )
        },
    )

    private fun mapTelemetry(dto: QwenTelemetryDtoV1) = QwenTelemetryConfig(
        enabled = dto.enabled,
        target = dto.target,
        otlpEndpoint = dto.otlpEndpoint,
        otlpProtocol = dto.otlpProtocol,
        logPrompts = dto.logPrompts,
    )

    private fun parseReasoningLevel(value: String): QwenReasoningLevel = when (value) {
        "low" -> QwenReasoningLevel.LOW
        "medium" -> QwenReasoningLevel.MEDIUM
        "high" -> QwenReasoningLevel.HIGH
        "none" -> QwenReasoningLevel.NONE
        else -> throw FormatError.UnknownEnum(value, "model.generationConfig.reasoning", setOf("low", "medium", "high", "none"))
    }

}
