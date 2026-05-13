package io.aequicor.aikit.core.domain.targets


/**
 * A single tool permission rule used in Claude Code and Qwen Code permission lists.
 *
 * The raw [value] may contain `${bundle.input.<id>}` placeholders; substitution happens in the
 * linking layer, outside this domain model.
 *
 * @property value Tool permission pattern, e.g. `"Bash(npm run test:*)"`, `"Read(./.env)"`.
 * @property condition Raw AKEL expression; if non-null, the rule is emitted only when it evaluates to `true`.
 */
data class PermissionRule(
    val value: String,
    val condition: String?,
)
