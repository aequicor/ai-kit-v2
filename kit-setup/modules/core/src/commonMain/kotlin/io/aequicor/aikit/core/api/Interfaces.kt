package io.aequicor.aikit.core.api

import io.aequicor.aikit.core.*
import io.aequicor.aikit.core.io.BundleSource

data class ParsedManifest(
    val schemaVersion: SchemaVersion,
    val name: BundleName,
    val version: Version,
    val agents: List<ParsedAgentSection>,
)

data class ParsedAgentSection(val agentId: AgentId, val templates: List<ParsedTemplate>)

data class ParsedTemplate(val id: TemplateId, val kind: TemplateKind, val source: String)

interface ManifestParser {
    fun parse(bytes: ByteArray): Result<ParsedManifest>
}

interface BundleResolver {
    fun resolve(source: BundleSource): Result<Bundle>
}

interface Validator {
    fun validate(bundle: Bundle): Result<Bundle>
}

interface AgentLayout {
    fun destinationPath(agent: Agent, kind: TemplateKind, id: TemplateId): String
}

interface Generator {
    /** targetDir — абсолютный путь к корню проекта пользователя */
    fun write(bundle: Bundle, targetDir: String, force: Boolean): Result<Unit>
}
