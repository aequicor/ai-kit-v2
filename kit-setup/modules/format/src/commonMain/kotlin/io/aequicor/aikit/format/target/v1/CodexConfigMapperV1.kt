package io.aequicor.aikit.format.target.v1

import io.aequicor.aikit.core.domain.targets.Codex

/** Merges a parsed Codex `config.json` DTO into the routed-file stub [Codex] target. */
internal object CodexConfigMapperV1 {

    fun merge(stub: Codex, dto: CodexConfigDtoV1): Codex = stub.copy(
        minVersion = dto.minVersion,
        mcpServers = dto.mcpServers?.map { it.toDomain() } ?: emptyList(),
        model = dto.settings?.model,
        modelReasoningEffort = dto.settings?.modelReasoningEffort,
        approvalPolicy = dto.settings?.approvalPolicy,
        sandboxMode = dto.settings?.sandboxMode,
        webSearch = dto.settings?.webSearch,
        features = dto.settings?.features,
    )
}
