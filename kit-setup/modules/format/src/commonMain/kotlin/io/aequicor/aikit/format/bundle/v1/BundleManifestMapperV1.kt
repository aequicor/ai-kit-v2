package io.aequicor.aikit.format.bundle.v1

import io.aequicor.aikit.core.domain.bundle.BundleManifest
import io.aequicor.aikit.core.domain.bundle.InputSpec
import io.aequicor.aikit.core.domain.targets.ClaudeCode
import io.aequicor.aikit.core.domain.targets.McpServer
import io.aequicor.aikit.core.domain.targets.OpenCode
import io.aequicor.aikit.core.domain.targets.QwenCode
import io.aequicor.aikit.core.domain.targets.Target
import io.aequicor.aikit.core.domain.template.Template
import io.aequicor.aikit.format.error.FormatError
import io.aequicor.aikit.io.BundleSource
import kotlinx.io.readByteArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

internal object BundleManifestMapperV1 {

    fun map(dto: BundleManifestDtoV1, bundleSource: BundleSource): Result<BundleManifest> = runCatching {
        val targets = dto.targets.map { targetFolder ->
            loadTarget(targetFolder, bundleSource).getOrThrow()
        }

        BundleManifest(
            schemaVersion = dto.schemaVersion,
            name = dto.name,
            version = dto.version,
            description = dto.description,
            author = dto.author,
            license = dto.license,
            targets = targets,
            inputs = dto.inputs.map { mapInputSpec(it).getOrThrow() },
        )
    }

    private fun loadTarget(targetFolder: String, bundleSource: BundleSource): Result<Target> = runCatching {
        val templates = loadTemplates(targetFolder, bundleSource)
        val commands = templates.filter { isCommandFile(it.path) }
        val skills = templates.filter { it.path.contains("/skills/") }
        when (targetFolder) {
            "claude" -> buildClaudeCode(commands, skills, templates)
            "opencode" -> buildOpenCode(commands, skills, templates)
            "qwen" -> buildQwenCode(commands, skills, templates)
            else -> throw FormatError.UnknownEnum(
                value = targetFolder,
                field = "targets[]",
                known = setOf("claude", "opencode", "qwen"),
            )
        }
    }

    private fun buildClaudeCode(
        commands: List<Template>,
        skills: List<Template>,
        all: List<Template>,
    ) = ClaudeCode(
        schemaVersion = 1,
        minVersion = null,
        mcpServers = emptyList(),
        commands = commands,
        skills = skills,
        subagents = all.filter { it.path.contains("/agents/") },
        model = null,
        includeCoAuthoredBy = null,
        env = null,
        permissions = null,
        hooks = emptyMap(),
    )

    private fun buildOpenCode(
        commands: List<Template>,
        skills: List<Template>,
        all: List<Template>,
    ) = OpenCode(
        schemaVersion = 1,
        minVersion = null,
        mcpServers = emptyList(),
        commands = commands,
        skills = skills,
        plugins = all.filter { it.path.endsWith(".js") || it.path.endsWith(".ts") },
        model = null,
        smallModel = null,
        defaultAgent = null,
        shell = null,
        share = null,
        snapshot = null,
        instructions = null,
        provider = null,
        tools = null,
        permission = null,
        agents = null,
        compaction = null,
    )

    private fun buildQwenCode(
        commands: List<Template>,
        skills: List<Template>,
        all: List<Template>,
    ) = QwenCode(
        schemaVersion = 1,
        minVersion = null,
        mcpServers = emptyList(),
        commands = commands,
        skills = skills,
        subagents = all.filter { it.path.contains("/agents/") },
        model = null,
        modelProviders = null,
        permissions = null,
        tools = null,
        general = null,
        context = null,
        telemetry = null,
        hooks = emptyMap(),
    )

    private fun mapInputSpec(dto: InputSpecDtoV1): Result<InputSpec> = runCatching {
        when (dto.type) {
            "boolean" -> InputSpec.BoolInput(
                id = dto.id,
                title = dto.title,
                description = dto.description,
                default = dto.default?.jsonPrimitive?.booleanOrNull,
            )
            "select" -> InputSpec.SelectInput(
                id = dto.id,
                title = dto.title,
                description = dto.description,
                options = requireOptions(dto),
                default = dto.default?.jsonPrimitive?.content,
            )
            "multiselect" -> InputSpec.MultiSelectInput(
                id = dto.id,
                title = dto.title,
                description = dto.description,
                options = requireOptions(dto),
                default = dto.default?.jsonArray?.map { it.jsonPrimitive.content },
            )
            "string" -> InputSpec.StringInput(
                id = dto.id,
                title = dto.title,
                description = dto.description,
                default = dto.default?.jsonPrimitive?.content,
                required = dto.required,
            )
            "int" -> InputSpec.IntInput(
                id = dto.id,
                title = dto.title,
                description = dto.description,
                default = dto.default?.jsonPrimitive?.intOrNull,
                required = dto.required,
                min = dto.min?.jsonPrimitive?.intOrNull,
                max = dto.max?.jsonPrimitive?.intOrNull,
            )
            "double" -> InputSpec.DoubleInput(
                id = dto.id,
                title = dto.title,
                description = dto.description,
                default = dto.default?.jsonPrimitive?.doubleOrNull,
                required = dto.required,
                min = dto.min?.jsonPrimitive?.doubleOrNull,
                max = dto.max?.jsonPrimitive?.doubleOrNull,
            )
            else -> throw FormatError.UnknownEnum(
                value = dto.type,
                field = "inputs[${dto.id}].type",
                known = setOf("boolean", "select", "multiselect", "string", "int", "double"),
            )
        }
    }

    private fun loadTemplates(targetFolder: String, bundleSource: BundleSource): List<Template> {
        val templateFiles = bundleSource.listTargetFiles(targetFolder).getOrThrow()
        return templateFiles
            .filter { !it.endsWith("config.json") }
            .map { relativePath ->
                val bytes = bundleSource.openTargetFile(targetFolder, relativePath)
                    .getOrThrow()
                    .use { it.readByteArray() }
                Template(path = "$targetFolder/$relativePath", bytes = bytes)
            }
    }

    private fun isCommandFile(path: String): Boolean =
        path.contains("/commands/") ||
            path.endsWith("CLAUDE.md") ||
            path.endsWith("AGENTS.md") ||
            path.endsWith("QWEN.md")

    private fun requireOptions(dto: InputSpecDtoV1): List<String> =
        dto.options ?: throw FormatError.MissingField(
            field = "options",
            context = "inputs[${dto.id}] (type=${dto.type})",
        )
}
