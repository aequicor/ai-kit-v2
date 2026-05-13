package io.aequicor.aikit.core.domain.projectManifest


/**
 * Parsed representation of `.aikit/manifest.json` — the user-project source of truth for AI-Kit.
 *
 * The CLI reads this file during `apply`, `validate`, and `update` to reproduce the state of all
 * generated agent configuration files. Committed to the project repository alongside the lock file.
 *
 * @property aikitVersion Version of the AI-Kit CLI that last wrote this manifest. Used for
 *   compatibility diagnostics; a MAJOR bump in AI-Kit may indicate a breaking manifest format change.
 * @property applications Ordered list of bundle applications. May be empty (valid, but `apply`
 *   produces no output).
 */
data class ProjectManifest(
    val aikitVersion: String,
    val applications: List<ApplicationEntry>,
)
