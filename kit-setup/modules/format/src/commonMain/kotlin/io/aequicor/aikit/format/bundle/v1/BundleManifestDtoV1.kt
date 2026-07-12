package io.aequicor.aikit.format.bundle.v1

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class BundleManifestDtoV1(
    val schemaVersion: Int,
    val name: String,
    val version: String,
    val description: String,
    val author: String? = null,
    val license: String? = null,
    val kitSetup: String? = null,
    val tags: List<String> = emptyList(),
    val bestFor: List<String> = emptyList(),
    val notFor: List<String> = emptyList(),
    val targets: List<String>,
    val inputs: List<InputSpecDtoV1> = emptyList(),
)

@Serializable
internal data class InputSpecDtoV1(
    val id: String,
    val type: String,
    val title: String,
    val description: String? = null,
    val default: JsonElement? = null,
    val required: Boolean? = null,
    val options: List<String>? = null,
    val min: JsonElement? = null,
    val max: JsonElement? = null,
)
