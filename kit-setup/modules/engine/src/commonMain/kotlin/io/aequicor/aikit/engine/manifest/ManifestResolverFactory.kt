package io.aequicor.aikit.engine.manifest

import io.aequicor.aikit.engine.api.ManifestResolver

/** Creates the platform-backed [ManifestResolver] for the current target. */
internal expect fun createFsManifestResolver(): ManifestResolver
