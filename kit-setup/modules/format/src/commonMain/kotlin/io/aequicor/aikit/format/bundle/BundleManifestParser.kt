package io.aequicor.aikit.format.bundle

import io.aequicor.aikit.format.ParsedBundle
import io.aequicor.aikit.format.bundle.v1.BundleManifestDtoV1
import io.aequicor.aikit.format.bundle.v1.BundleManifestMapperV1
import io.aequicor.aikit.format.error.FormatError
import io.aequicor.aikit.format.target.TargetConfigParser
import io.aequicor.aikit.io.BundleSource
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class BundleManifestParser(private val json: Json) {

    private val configParser = TargetConfigParser(json)

    fun parse(bundleSource: BundleSource): Result<ParsedBundle> = runCatching {
        val manifestText = bundleSource.openManifest().getOrThrow().use { it.readString() }

        val raw = try {
            json.parseToJsonElement(manifestText).jsonObject
        } catch (e: IllegalArgumentException) {
            throw FormatError.BadJson("invalid bundle.json: ${e.message}", e)
        } catch (e: IllegalStateException) {
            throw FormatError.BadJson("invalid bundle.json: ${e.message}", e)
        }

        val version = raw["schemaVersion"]?.jsonPrimitive?.int
            ?: throw FormatError.MissingField("schemaVersion", "bundle.json")

        when (version) {
            1 -> {
                val dto = try {
                    json.decodeFromString(BundleManifestDtoV1.serializer(), manifestText)
                } catch (e: IllegalArgumentException) {
                    throw FormatError.BadJson("cannot decode bundle.json v1: ${e.message}", e)
                } catch (e: IllegalStateException) {
                    throw FormatError.BadJson("cannot decode bundle.json v1: ${e.message}", e)
                }
                BundleManifestMapperV1(configParser, json).map(dto, bundleSource).getOrThrow()
            }
            else -> throw FormatError.UnsupportedSchemaVersion(version, SUPPORTED_VERSIONS)
        }
    }

    private companion object {
        val SUPPORTED_VERSIONS = 1..1
    }
}
