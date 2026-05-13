package io.aequicor.aikit.core

value class TemplateContent(val bytes: ByteArray)

data class Template(
    val id: TemplateId,
    val kind: TemplateKind,
    val content: TemplateContent,
)

data class AgentBundle(
    val agent: Agent,
    val templates: List<Template>,
)

data class Bundle(
    val schemaVersion: SchemaVersion,
    val name: BundleName,
    val version: Version,
    val agents: List<AgentBundle>,
)
