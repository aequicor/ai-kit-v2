package io.aequicor.aikit.format.projectManifest.v1

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class ProjectManifestDtoV1(
    val aikitVersion: String,
    val applications: List<ApplicationEntryDtoV1> = emptyList(),
)

@Serializable
internal data class ApplicationEntryDtoV1(
    val id: String,
    val path: String,
    val targets: Map<String, BundleTargetDtoV1> = emptyMap(),
)

@Serializable
internal data class BundleTargetDtoV1(
    val bundle: String,
    val source: String,
    val inputs: Map<String, JsonElement> = emptyMap(),
)
