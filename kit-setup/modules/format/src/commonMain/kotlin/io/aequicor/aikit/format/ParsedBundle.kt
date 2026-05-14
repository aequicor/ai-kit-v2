package io.aequicor.aikit.format

import io.aequicor.aikit.core.domain.bundle.BundleManifest
import io.aequicor.aikit.format.template.TemplateSource

/**
 * Result of parsing a bundle — the domain manifest paired with raw template sources.
 *
 * [manifest] holds the fully parsed domain model (agent config, MCP servers, hooks, inputs).
 * [targetSources] holds the raw template files keyed by target folder name (e.g. `"claude-code"`,
 * `"opencode"`, `"qwen-code"`), with AKEL conditions already attached from `config.json`.
 *
 * The engine evaluates conditions, renders substitutions, and writes final
 * [io.aequicor.aikit.core.domain.template.Template] files to disk.
 */
data class ParsedBundle(
    val manifest: BundleManifest,
    val targetSources: Map<String, List<TemplateSource>>,
)
