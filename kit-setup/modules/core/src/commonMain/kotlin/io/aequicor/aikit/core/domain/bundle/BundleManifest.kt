package io.aequicor.aikit.core.domain.bundle

import io.aequicor.aikit.core.domain.targets.Target

/**
 * Parsed representation of `bundle.json` — the top-level manifest of an AI-Kit bundle.
 *
 * @property schemaVersion Version of the `bundle.json` format. Increments on breaking structural changes.
 * @property name Unique kebab-case bundle identifier.
 * @property version SemVer string of the bundle.
 * @property description One-line human-readable description.
 * @property author Optional free-form author string.
 * @property license Optional SPDX license identifier (e.g. `"MIT"`, `"Apache-2.0"`).
 * @property targets Targets supported by this bundle. Each target's [Target.folder] must
 *   correspond to a same-named directory in the bundle root.
 * @property inputs Parameters the CLI presents to the user during interactive installation.
 *   Empty list means the bundle takes no user input.
 */
data class BundleManifest(
    val schemaVersion: Int,
    val name: String,
    val version: String,
    val description: String,
    val author: String?,
    val license: String?,
    val kitSetup: String?,
    val tags: List<String>,
    val bestFor: List<String>,
    val notFor: List<String>,
    val targets: List<Target>,
    val inputs: List<InputSpec>,
)
