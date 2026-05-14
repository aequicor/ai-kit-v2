package io.aequicor.aikit.format.target.v1

import io.aequicor.aikit.core.domain.targets.ClaudeCode
import io.aequicor.aikit.core.domain.targets.ClaudeDefaultMode
import io.aequicor.aikit.core.domain.targets.ClaudePermissions
import io.aequicor.aikit.core.domain.targets.McpServer
import io.aequicor.aikit.core.domain.targets.PermissionRule
import io.aequicor.aikit.core.domain.template.Template
import io.aequicor.aikit.format.error.FormatError

internal object ClaudeCodeConfigMapperV1 {

    fun merge(stub: ClaudeCode, dto: ClaudeCodeConfigDtoV1): ClaudeCode = stub.copy(
        minVersion = dto.minVersion,
        mcpServers = dto.mcpServers?.map { it.toDomain() } ?: emptyList(),
        model = dto.settings?.model,
        includeCoAuthoredBy = dto.settings?.includeCoAuthoredBy,
        env = dto.settings?.env,
        permissions = dto.settings?.permissions?.let { mapPermissions(it) },
        hooks = dto.hooks?.let { ClaudeHookMapperV1.mapHooks(it) } ?: emptyMap(),
        commands = applyConditions(stub.commands, (dto.memory ?: emptyList()) + (dto.commands ?: emptyList())),
        skills = applyConditions(stub.skills, dto.skills ?: emptyList()),
        subagents = applyConditions(stub.subagents, dto.agents ?: emptyList()),
    )

    private fun applyConditions(templates: List<Template>, refs: List<FileRefDtoV1>): List<Template> {
        if (refs.isEmpty()) return templates
        val conditionBySource = refs.associate { ref ->
            ref.source.trimEnd('/') to ref.`when`
        }
        return templates.map { tmpl ->
            val relPath = tmpl.path.substringAfter('/')
            val condition = conditionBySource.entries.firstOrNull { (src, _) ->
                relPath == src || relPath.startsWith("$src/")
            }?.value
            if (condition != null) tmpl.copy(condition = condition) else tmpl
        }
    }

    private fun mapPermissions(dto: ClaudePermissionsDtoV1): ClaudePermissions = ClaudePermissions(
        defaultMode = dto.defaultMode?.let { parseDefaultMode(it) },
        allow = dto.allow?.map { it.toPermissionRule() } ?: emptyList(),
        deny = dto.deny?.map { it.toPermissionRule() } ?: emptyList(),
        ask = dto.ask?.map { it.toPermissionRule() } ?: emptyList(),
        additionalDirectories = dto.additionalDirectories ?: emptyList(),
    )

    private fun parseDefaultMode(value: String): ClaudeDefaultMode = when (value) {
        "default" -> ClaudeDefaultMode.DEFAULT
        "plan" -> ClaudeDefaultMode.PLAN
        "acceptEdits", "accept_edits", "auto" -> ClaudeDefaultMode.ACCEPT_EDITS
        "bypassPermissions", "bypass_permissions", "dontAsk" -> ClaudeDefaultMode.BYPASS_PERMISSIONS
        else -> throw FormatError.UnknownEnum(
            value = value,
            field = "settings.permissions.defaultMode",
            known = setOf("default", "plan", "acceptEdits", "bypassPermissions"),
        )
    }
}
