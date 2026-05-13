package io.aequicor.aikit.core.domain.projectManifest


/**
 * A single bundle application within [ProjectManifest].
 *
 * An application binds one or more agent targets to a sub-path of the project.
 *
 * @property id Stable unique identifier within the manifest. Used in CLI flags (`--only <id>`,
 *   `--skip <id>`) and in the lock file for idempotent re-application and file cleanup.
 * @property path Project-relative path to the sub-project this application targets.
 *   Allowed values: `"."`, `"./backend"`, `"./apps/web"`, etc.
 *   Absolute paths and `../` escapes are rejected by the validator.
 * @property targets Map of agent name → [BundleTarget]. Keys are semantic agent names
 *   (`"claude"`, `"opencode"`, `"cursor"`, …).
 */
data class ApplicationEntry(
    val id: String,
    val path: String,
    val targets: Map<String, BundleTarget>,
)
