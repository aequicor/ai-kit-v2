package io.aequicor.aikit.parsing.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ManifestDto(
    val schemaVersion: Int,
    val name: String,
    val version: String,
    val agents: Map<String, AgentSectionDto>,
)

@Serializable
internal data class AgentSectionDto(
    val templates: List<TemplateDtoItem>,
)

@Serializable
internal data class TemplateDtoItem(
    val id: String,
    val kind: String,
    val source: String,
)
