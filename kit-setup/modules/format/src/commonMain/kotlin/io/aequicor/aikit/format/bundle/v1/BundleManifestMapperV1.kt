package io.aequicor.aikit.format.bundle.v1

import io.aequicor.aikit.core.domain.bundle.BundleManifest
import io.aequicor.aikit.core.domain.bundle.InputSpec
import io.aequicor.aikit.core.domain.targets.ClaudeCode
import io.aequicor.aikit.core.domain.targets.OpenCode
import io.aequicor.aikit.core.domain.targets.QwenCode
import io.aequicor.aikit.core.domain.targets.Target
import io.aequicor.aikit.core.domain.template.Template
import io.aequicor.aikit.format.ParsedBundle
import io.aequicor.aikit.format.error.FormatError
import io.aequicor.aikit.format.target.TargetConfigParser
import io.aequicor.aikit.format.target.v1.ClaudeCodeConfigDtoV1
import io.aequicor.aikit.format.target.v1.FileRefDtoV1
import io.aequicor.aikit.format.template.TemplateSource
import io.aequicor.aikit.io.BundleSource
import kotlinx.io.readByteArray
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

internal class BundleManifestMapperV1(
    private val configParser: TargetConfigParser,
    private val json: Json,
) {

    fun map(dto: BundleManifestDtoV1, bundleSource: BundleSource): Result<ParsedBundle> = runCatching {
        val targetSources = mutableMapOf<String, List<TemplateSource>>()
        val targets = dto.targets.map { targetFolder ->
            loadTarget(targetFolder, bundleSource, targetSources).getOrThrow()
        }
        val manifest = BundleManifest(
            schemaVersion = dto.schemaVersion,
            name = dto.name,
            version = dto.version,
            description = dto.description,
            author = dto.author,
            license = dto.license,
            targets = targets,
            inputs = dto.inputs.map { mapInputSpec(it).getOrThrow() },
        )
        ParsedBundle(manifest = manifest, targetSources = targetSources)
    }

    private fun loadTarget(
        targetFolder: String,
        bundleSource: BundleSource,
        targetSources: MutableMap<String, List<TemplateSource>>,
    ): Result<Target> = runCatching {
        val configJson = bundleSource.openTargetConfig(targetFolder)
            .getOrThrow()
            ?.use { it.readString() }

        val rawSources = loadTemplateSources(targetFolder, bundleSource)

        val sources = when (targetFolder) {
            "claude-code" -> applyClaudeConditions(rawSources, configJson?.let { decodeClaudeConfig(it) })
            else -> rawSources
        }
        targetSources[targetFolder] = sources

        val templates = sources.map { Template(path = it.path, bytes = it.bytes) }
        val stub = buildStub(targetFolder, templates)
        if (configJson != null) configParser.parse(configJson, stub).getOrThrow() else stub
    }

    private fun buildStub(targetFolder: String, templates: List<Template>): Target {
        val commands = templates.filter { isCommandFile(it.path) }
        val skills = templates.filter { it.path.contains("/skills/") }
        return when (targetFolder) {
            "claude-code" -> ClaudeCode(
                schemaVersion = 1, minVersion = null, mcpServers = emptyList(),
                commands = commands, skills = skills,
                subagents = templates.filter { it.path.contains("/subagents/") },
                model = null, includeCoAuthoredBy = null, env = null, permissions = null, hooks = emptyMap(),
            )
            "opencode" -> OpenCode(
                schemaVersion = 1, minVersion = null, mcpServers = emptyList(),
                commands = commands, skills = skills,
                plugins = templates.filter { it.path.endsWith(".js") || it.path.endsWith(".ts") },
                model = null, smallModel = null, defaultAgent = null, shell = null,
                share = null, snapshot = null, instructions = null,
                provider = null, tools = null, permission = null, agents = null, compaction = null,
            )
            "qwen-code" -> QwenCode(
                schemaVersion = 1, minVersion = null, mcpServers = emptyList(),
                commands = commands, skills = skills,
                subagents = templates.filter { it.path.contains("/subagents/") },
                model = null, modelProviders = null, permissions = null, tools = null,
                general = null, context = null, telemetry = null, hooks = emptyMap(),
            )
            else -> throw FormatError.UnknownEnum(targetFolder, "targets[]", setOf("claude-code", "opencode", "qwen-code"))
        }
    }

    private fun decodeClaudeConfig(configJson: String): ClaudeCodeConfigDtoV1 = try {
        json.decodeFromString<ClaudeCodeConfigDtoV1>(configJson)
    } catch (e: IllegalArgumentException) {
        throw FormatError.BadJson("cannot decode claude config.json: ${e.message}", e)
    } catch (e: IllegalStateException) {
        throw FormatError.BadJson("cannot decode claude config.json: ${e.message}", e)
    }

    private fun applyClaudeConditions(
        sources: List<TemplateSource>,
        dto: ClaudeCodeConfigDtoV1?,
    ): List<TemplateSource> {
        if (dto == null) return sources
        val refs = (dto.memory ?: emptyList()) + (dto.commands ?: emptyList()) +
            (dto.skills ?: emptyList()) + (dto.agents ?: emptyList()) +
            hookFileRefs(dto, agentPrefix = ".claude/")
        return applyConditions(sources, refs)
    }

    private fun hookFileRefs(dto: ClaudeCodeConfigDtoV1, agentPrefix: String): List<FileRefDtoV1> =
        dto.hooks?.values?.flatten()
            .orEmpty()
            .filter { it.command != null && it.`when` != null }
            .map { hook ->
                FileRefDtoV1(
                    name = "",
                    source = hook.command!!.removePrefix(agentPrefix),
                    `when` = hook.`when`,
                )
            }

    private fun applyConditions(sources: List<TemplateSource>, refs: List<FileRefDtoV1>): List<TemplateSource> {
        if (refs.isEmpty()) return sources
        val conditionBySource = refs.associate { it.source.trimEnd('/') to it.`when` }
        return sources.map { src ->
            val relPath = src.path.substringAfter('/')
            val condition = conditionBySource.entries.firstOrNull { (key, _) ->
                relPath == key || relPath.startsWith("$key/")
            }?.value
            if (condition != null) src.copy(condition = condition) else src
        }
    }

    private fun loadTemplateSources(targetFolder: String, bundleSource: BundleSource): List<TemplateSource> =
        bundleSource.listTargetFiles(targetFolder).getOrThrow()
            .filter { !it.endsWith("config.json") }
            .map { relativePath ->
                val bytes = bundleSource.openTargetFile(targetFolder, relativePath).getOrThrow().use { it.readByteArray() }
                TemplateSource(path = "$targetFolder/$relativePath", bytes = bytes)
            }

    private fun isCommandFile(path: String): Boolean =
        path.contains("/commands/") || path.endsWith("CLAUDE.md") || path.endsWith("AGENTS.md") || path.endsWith("QWEN.md")

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

    private fun requireOptions(dto: InputSpecDtoV1): List<String> =
        dto.options ?: throw FormatError.MissingField("options", "inputs[${dto.id}] (type=${dto.type})")
}
