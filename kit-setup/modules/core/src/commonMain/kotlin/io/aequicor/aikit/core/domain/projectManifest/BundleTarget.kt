package io.aequicor.aikit.core.domain.projectManifest

import io.aequicor.aikit.core.domain.bundle.BundleManifest

/**
 * A bundle target within [ApplicationEntry.targets].
 *
 * Describes which bundle to apply for a specific agent and with which input values.
 *
 * @property bundle Parsed `bundle.json` manifest of the bundle to apply.
 * @property inputs Flat map of input id → concrete value, validated against the bundle's [InputSpec]
 *   declarations. Default values declared in the bundle are NOT duplicated here — this allows a
 *   bundle upgrade to transparently change defaults without requiring a manifest edit.
 */
data class BundleTarget(
    val bundle: BundleManifest,
    val inputs: Map<String, InputValue>,
)
