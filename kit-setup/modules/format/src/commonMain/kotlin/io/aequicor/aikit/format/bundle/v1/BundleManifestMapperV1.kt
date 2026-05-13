package io.aequicor.aikit.format.bundle.v1

import io.aequicor.aikit.core.domain.bundle.BundleManifest
import io.aequicor.aikit.core.domain.bundle.InputSpec
import io.aequicor.aikit.core.domain.targets.ClaudeCode
import io.aequicor.aikit.core.domain.targets.OpenCode
import io.aequicor.aikit.core.domain.targets.QwenCode
import io.aequicor.aikit.core.domain.targets.Target
import io.aequicor.aikit.core.domain.template.Template
import io.aequicor.aikit.format.error.FormatError
import io.aequicor.aikit.format.target.TargetConfigParser
import io.aequicor.aikit.io.BundleSource
import kotlinx.io.readByteArray
import kotlinx.io.readString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

internal class BundleManifestMapperV1(private val configParser: TargetConfigParser) {

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
        val configJson = bundleSource.openTargetConfig(targetFolder)
            .getOrThrow()
            ?.use { it.readString() }

        val templates = loadTemplates(targetFolder, bundleSource)
        val commands = templates.filter { isCommandFile(it.path) }
        val skills = templates.filter { it.path.contains("/skills/") }

        val stub = when (targetFolder) {
            "claude" -> buildClaudeCode(commands, skills, templates)
            "opencode" -> buildOpenCode(commands, skills, templates)
            "qwen" -> buildQwenCode(commands, skills, templates)
            else -> throw FormatError.UnknownEnum(targetFolder, "targets[]", setOf("claude", "opencode", "qwen"))
        }

        if (configJson != null) {
            configParser.parse(configJson, stub).getOrThrow()
        } else {
            stub
        }
    }

    private fun buildClaudeCode(commands: List<Template>, skills: List<Template>, all: List<Template>) =
        ClaudeCode(
            schemaVersion = 1, minVersion = null, mcpServers = emptyList(),
            commands = commands, skills = skills,
            subagents = all.filter { it.path.contains("/agents/") },
            model = null, includeCoAuthoredBy = null, env = null, permissions = null, hooks = emptyMap(),
        )

    private fun buildOpenCode(commands: List<Template>, skills: List<Template>, all: List<Template>) =
        OpenCode(
            schemaVersion = 1, minVersion = null, mcpServers = emptyList(),
            commands = commands, skills = skills,
            plugins = all.filter { it.path.endsWith(".js") || it.path.endsWith(".ts") },
            model = null, smallModel = null, defaultAgent = null, shell = null,
            share = null, snapshot = null, instructions = null,
            provider = null, tools = null, permission = null, agents = null, compaction = null,
        )

    private fun buildQwenCode(commands: List<Template>, skills: List<Template>, all: List<Template>) =
        QwenCode(
            schemaVersion = 1, minVersion = null, mcpServers = emptyList(),
            commands = commands, skills = skills,
            subagents = all.filter { it.path.contains("/agents/") },
            model = null, modelProviders = null, permissions = null, tools = null,
            general = null, context = null, telemetry = null, hooks = emptyMap(),
        )

    private fun mapInputSpec(dto: InputSpecDtoV1): Result<InputSpec> = runCatching {
        val id = dto.id
        val title = dto.title
        val desc = dto.description
        when (dto.type) {
            "boolean" -> InputSpec.BoolInput(id, title, desc, dto.default?.jsonPrimitive?.booleanOrNull)
            "select" -> InputSpec.SelectInput(
                id, title, desc, requireOptions(dto), dto.default?.jsonPrimitive?.content,
            )
            "multiselect" -> InputSpec.MultiSelectInput(
                id, title, desc, requireOptions(dto),
                dto.default?.jsonArray?.map { it.jsonPrimitive.content },
            )
            "string" -> InputSpec.StringInput(id, title, desc, dto.default?.jsonPrimitive?.content, dto.required)
            "int" -> InputSpec.IntInput(
                id, title, desc, dto.default?.jsonPrimitive?.intOrNull, dto.required,
                dto.min?.jsonPrimitive?.intOrNull, dto.max?.jsonPrimitive?.intOrNull,
            )
            "double" -> InputSpec.DoubleInput(
                id, title, desc, dto.default?.jsonPrimitive?.doubleOrNull, dto.required,
                dto.min?.jsonPrimitive?.doubleOrNull, dto.max?.jsonPrimitive?.doubleOrNull,
            )
            else -> throw FormatError.UnknownEnum(
                dto.type, "inputs[$id].type",
                setOf("boolean", "select", "multiselect", "string", "int", "double"),
            )
        }
    }

    private fun loadTemplates(targetFolder: String, bundleSource: BundleSource): List<Template> =
        bundleSource.listTargetFiles(targetFolder).getOrThrow()
            .filter { !it.endsWith("config.json") }
            .map { relativePath ->
                val bytes = bundleSource.openTargetFile(targetFolder, relativePath).getOrThrow().use { it.readByteArray() }
                Template(path = "$targetFolder/$relativePath", bytes = bytes)
            }

    private fun isCommandFile(path: String): Boolean =
        path.contains("/commands/") || path.endsWith("CLAUDE.md") || path.endsWith("AGENTS.md") || path.endsWith("QWEN.md")

    private fun requireOptions(dto: InputSpecDtoV1): List<String> =
        dto.options ?: throw FormatError.MissingField("options", "inputs[${dto.id}] (type=${dto.type})")
}
