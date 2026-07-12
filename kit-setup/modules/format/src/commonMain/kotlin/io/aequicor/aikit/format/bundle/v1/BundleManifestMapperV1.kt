package io.aequicor.aikit.format.bundle.v1

import io.aequicor.aikit.core.domain.bundle.BundleManifest
import io.aequicor.aikit.core.domain.bundle.InputSpec
import io.aequicor.aikit.core.domain.targets.ClaudeCode
import io.aequicor.aikit.core.domain.targets.Codex
import io.aequicor.aikit.core.domain.targets.OpenCode
import io.aequicor.aikit.core.domain.targets.QwenCode
import io.aequicor.aikit.core.domain.targets.Target
import io.aequicor.aikit.core.domain.template.Template
import io.aequicor.aikit.format.FileRoute
import io.aequicor.aikit.format.ParsedBundle
import io.aequicor.aikit.format.RouteKind
import io.aequicor.aikit.format.TargetPlan
import io.aequicor.aikit.format.error.FormatError
import io.aequicor.aikit.format.target.TargetConfigParser
import io.aequicor.aikit.format.target.v1.ClaudeCodeConfigDtoV1
import io.aequicor.aikit.format.target.v1.CodexConfigDtoV1
import io.aequicor.aikit.format.target.v1.FileRefDtoV1
import io.aequicor.aikit.format.target.v1.HookGroupDtoV1
import io.aequicor.aikit.format.target.v1.OpenCodeConfigDtoV1
import io.aequicor.aikit.format.target.v1.QwenCodeConfigDtoV1
import io.aequicor.aikit.format.template.TemplateSource
import io.aequicor.aikit.io.BundleSource
import kotlinx.io.readByteArray
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

private const val FOLDER_CLAUDE = "claude-code"
private const val FOLDER_OPENCODE = "opencode"
private const val FOLDER_QWEN = "qwen-code"
private const val FOLDER_CODEX = "codex"
private const val CLAUDE_PROJECT_PREFIX = ".claude/"
private const val QWEN_PROJECT_PREFIX = ".qwen/"

@Suppress("TooManyFunctions")
internal class BundleManifestMapperV1(
    private val configParser: TargetConfigParser,
    private val json: Json,
) {

    fun map(dto: BundleManifestDtoV1, bundleSource: BundleSource): Result<ParsedBundle> = runCatching {
        val targetPlans = mutableMapOf<String, TargetPlan>()
        val targets = dto.targets.map { folder ->
            val loaded = loadTarget(folder, bundleSource).getOrThrow()
            targetPlans[folder] = loaded.plan
            loaded.target
        }
        val manifest = BundleManifest(
            schemaVersion = dto.schemaVersion,
            name = dto.name,
            version = dto.version,
            description = dto.description,
            author = dto.author,
            license = dto.license,
            kitSetup = dto.kitSetup,
            tags = dto.tags,
            bestFor = dto.bestFor,
            notFor = dto.notFor,
            targets = targets,
            inputs = dto.inputs.map { mapInputSpec(it).getOrThrow() },
        )
        ParsedBundle(manifest = manifest, targetPlans = targetPlans)
    }

    private data class LoadedTarget(val target: Target, val plan: TargetPlan)

    private fun loadTarget(targetFolder: String, bundleSource: BundleSource): Result<LoadedTarget> = runCatching {
        val configJsonText = bundleSource.openTargetConfig(targetFolder)
            .getOrThrow()
            ?.use { it.readString() }

        val sources = loadTemplateSources(targetFolder, bundleSource)
        val rawConfig: JsonElement? = configJsonText?.let { parseJsonOrThrow(it) }

        val routes: List<FileRoute> = when {
            configJsonText == null -> emptyList()
            targetFolder == FOLDER_CLAUDE -> claudeRoutes(decodeClaudeConfig(configJsonText), sources)
            targetFolder == FOLDER_OPENCODE -> openCodeRoutes(decodeOpenCodeConfig(configJsonText), sources)
            targetFolder == FOLDER_QWEN -> qwenRoutes(decodeQwenConfig(configJsonText), sources)
            targetFolder == FOLDER_CODEX -> codexRoutes(decodeCodexConfig(configJsonText), sources)
            else -> throw FormatError.UnknownEnum(
                targetFolder,
                "targets[]",
                setOf(FOLDER_CLAUDE, FOLDER_OPENCODE, FOLDER_QWEN, FOLDER_CODEX),
            )
        }

        val stub = buildStub(targetFolder, routes)
        val target = if (configJsonText != null) configParser.parse(configJsonText, stub).getOrThrow() else stub
        LoadedTarget(target, TargetPlan(targetFolder, routes, rawConfig))
    }

    // ── Per-target route extraction ───────────────────────────────────────────

    private fun claudeRoutes(dto: ClaudeCodeConfigDtoV1, sources: List<TemplateSource>): List<FileRoute> {
        val byPath = sources.associateBy { stripFolder(it.path) }
        return buildList {
            addAll(fileRefRoutes(dto.memory, RouteKind.MEMORY, byPath))
            addAll(fileRefRoutes(dto.agents, RouteKind.SUBAGENT, byPath))
            addAll(fileRefRoutes(dto.commands, RouteKind.COMMAND, byPath))
            addAll(skillRoutes(dto.skills, sources))
            addAll(hookRoutes(dto.hooks, byPath, CLAUDE_PROJECT_PREFIX))
        }
    }

    private fun qwenRoutes(dto: QwenCodeConfigDtoV1, sources: List<TemplateSource>): List<FileRoute> {
        val byPath = sources.associateBy { stripFolder(it.path) }
        return buildList {
            addAll(fileRefRoutes(dto.memory, RouteKind.MEMORY, byPath))
            addAll(fileRefRoutes(dto.agents, RouteKind.SUBAGENT, byPath))
            addAll(fileRefRoutes(dto.commands, RouteKind.COMMAND, byPath))
            addAll(skillRoutes(dto.skills, sources))
            addAll(hookRoutes(dto.hooks, byPath, QWEN_PROJECT_PREFIX))
        }
    }

    private fun codexRoutes(dto: CodexConfigDtoV1, sources: List<TemplateSource>): List<FileRoute> {
        // Codex has no skill directories and no hook scripts — only memory (AGENTS.md),
        // subagent TOML files and custom prompts are routed.
        val byPath = sources.associateBy { stripFolder(it.path) }
        return buildList {
            addAll(fileRefRoutes(dto.memory, RouteKind.MEMORY, byPath))
            addAll(fileRefRoutes(dto.agents, RouteKind.SUBAGENT, byPath))
            addAll(fileRefRoutes(dto.commands, RouteKind.COMMAND, byPath))
        }
    }

    private fun openCodeRoutes(dto: OpenCodeConfigDtoV1, sources: List<TemplateSource>): List<FileRoute> {
        // OpenCode's bundle config does not yet declare standalone memory/commands/skills/hooks
        // file lists (its config is monolithic). Files in the bundle folder are kept as-is via
        // direct embedding into opencode.json — no routed files for now.
        @Suppress("UNUSED_PARAMETER") val unused = dto
        @Suppress("UNUSED_PARAMETER") val unused2 = sources
        return emptyList()
    }

    private fun fileRefRoutes(
        refs: List<FileRefDtoV1>?,
        kind: RouteKind,
        byPath: Map<String, TemplateSource>,
    ): List<FileRoute> = refs.orEmpty().mapNotNull { ref ->
        val key = ref.source.trimStart('/').trimEnd('/')
        val source = byPath[key] ?: return@mapNotNull null
        FileRoute(kind = kind, name = ref.name, source = source, condition = ref.`when`)
    }

    private fun skillRoutes(refs: List<FileRefDtoV1>?, sources: List<TemplateSource>): List<FileRoute> =
        refs.orEmpty().flatMap { ref ->
            val dir = ref.source.trimStart('/').trimEnd('/')
            val prefix = "$dir/"
            sources.mapNotNull { src ->
                val rel = stripFolder(src.path)
                if (!rel.startsWith(prefix)) return@mapNotNull null
                FileRoute(
                    kind = RouteKind.SKILL,
                    name = ref.name,
                    source = src,
                    skillRelPath = rel.removePrefix(prefix),
                    condition = ref.`when`,
                )
            }
        }

    private fun hookRoutes(
        hooks: Map<String, List<HookGroupDtoV1>>?,
        byPath: Map<String, TemplateSource>,
        agentPrefix: String,
    ): List<FileRoute> = hooks?.values?.flatten().orEmpty().mapNotNull { hook ->
        val command = hook.command ?: return@mapNotNull null
        val rel = command.removePrefix(agentPrefix).trimStart('/')
        val source = byPath[rel] ?: return@mapNotNull null
        FileRoute(
            kind = RouteKind.HOOK_SCRIPT,
            name = command,
            source = source,
            condition = hook.`when`,
        )
    }

    // ── Target stub from routes ───────────────────────────────────────────────

    private fun buildStub(targetFolder: String, routes: List<FileRoute>): Target {
        val commands = routes.filter { it.kind == RouteKind.COMMAND }.map { it.toTemplate() } +
            routes.filter { it.kind == RouteKind.MEMORY }.map { it.toTemplate() }
        val skills = routes.filter { it.kind == RouteKind.SKILL }.map { it.toTemplate() }
        val subagents = routes.filter { it.kind == RouteKind.SUBAGENT }.map { it.toTemplate() }
        return when (targetFolder) {
            FOLDER_CLAUDE -> ClaudeCode(
                schemaVersion = 1, minVersion = null, mcpServers = emptyList(),
                commands = commands, skills = skills, subagents = subagents,
                model = null, includeCoAuthoredBy = null, env = null, permissions = null, hooks = emptyMap(),
            )
            FOLDER_OPENCODE -> OpenCode(
                schemaVersion = 1, minVersion = null, mcpServers = emptyList(),
                commands = commands, skills = skills, plugins = emptyList(),
                model = null, smallModel = null, defaultAgent = null, shell = null,
                share = null, snapshot = null, instructions = null,
                provider = null, tools = null, permission = null, agents = null, compaction = null,
            )
            FOLDER_QWEN -> QwenCode(
                schemaVersion = 1, minVersion = null, mcpServers = emptyList(),
                commands = commands, skills = skills, subagents = subagents,
                model = null, modelProviders = null, permissions = null, tools = null,
                general = null, context = null, telemetry = null, hooks = emptyMap(),
            )
            FOLDER_CODEX -> Codex(
                schemaVersion = 1, minVersion = null, mcpServers = emptyList(),
                commands = commands, skills = skills, subagents = subagents,
                model = null, modelReasoningEffort = null, approvalPolicy = null,
                sandboxMode = null, webSearch = null, features = null,
            )
            else -> throw FormatError.UnknownEnum(
                targetFolder, "targets[]", setOf(FOLDER_CLAUDE, FOLDER_OPENCODE, FOLDER_QWEN, FOLDER_CODEX),
            )
        }
    }

    private fun FileRoute.toTemplate(): Template = Template(path = source.path, bytes = source.bytes)

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun stripFolder(path: String): String = path.substringAfter('/', missingDelimiterValue = path)

    private fun parseJsonOrThrow(text: String): JsonElement = try {
        json.parseToJsonElement(text)
    } catch (e: IllegalArgumentException) {
        throw FormatError.BadJson("cannot parse config.json: ${e.message}", e)
    } catch (e: IllegalStateException) {
        throw FormatError.BadJson("cannot parse config.json: ${e.message}", e)
    }

    private fun decodeClaudeConfig(configJson: String): ClaudeCodeConfigDtoV1 = try {
        json.decodeFromString(configJson)
    } catch (e: IllegalArgumentException) {
        throw FormatError.BadJson("cannot decode claude config.json: ${e.message}", e)
    } catch (e: IllegalStateException) {
        throw FormatError.BadJson("cannot decode claude config.json: ${e.message}", e)
    }

    private fun decodeOpenCodeConfig(configJson: String): OpenCodeConfigDtoV1 = try {
        json.decodeFromString(configJson)
    } catch (e: IllegalArgumentException) {
        throw FormatError.BadJson("cannot decode opencode config.json: ${e.message}", e)
    } catch (e: IllegalStateException) {
        throw FormatError.BadJson("cannot decode opencode config.json: ${e.message}", e)
    }

    private fun decodeQwenConfig(configJson: String): QwenCodeConfigDtoV1 = try {
        json.decodeFromString(configJson)
    } catch (e: IllegalArgumentException) {
        throw FormatError.BadJson("cannot decode qwen config.json: ${e.message}", e)
    } catch (e: IllegalStateException) {
        throw FormatError.BadJson("cannot decode qwen config.json: ${e.message}", e)
    }

    private fun decodeCodexConfig(configJson: String): CodexConfigDtoV1 = try {
        json.decodeFromString(configJson)
    } catch (e: IllegalArgumentException) {
        throw FormatError.BadJson("cannot decode codex config.json: ${e.message}", e)
    } catch (e: IllegalStateException) {
        throw FormatError.BadJson("cannot decode codex config.json: ${e.message}", e)
    }

    private fun loadTemplateSources(targetFolder: String, bundleSource: BundleSource): List<TemplateSource> =
        bundleSource.listTargetFiles(targetFolder).getOrThrow()
            .filter { !it.endsWith("config.json") }
            .map { relativePath ->
                val bytes = bundleSource.openTargetFile(targetFolder, relativePath).getOrThrow().use { it.readByteArray() }
                TemplateSource(path = "$targetFolder/$relativePath", bytes = bytes)
            }

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
