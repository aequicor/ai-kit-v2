package io.aequicor.aikit.format.projectManifest

import io.aequicor.aikit.format.error.FormatError
import io.aequicor.aikit.format.projectManifest.v1.ProjectManifestDtoV1
import kotlinx.serialization.json.Json

internal class ProjectManifestParser(private val json: Json) {

    fun parse(text: String): Result<RawProjectManifest> = runCatching {
        val dto = try {
            json.decodeFromString(ProjectManifestDtoV1.serializer(), text)
        } catch (e: IllegalArgumentException) {
            throw FormatError.BadJson("invalid manifest.json: ${e.message}", e)
        } catch (e: IllegalStateException) {
            throw FormatError.BadJson("invalid manifest.json: ${e.message}", e)
        }

        RawProjectManifest(
            aikitVersion = dto.aikitVersion,
            applications = dto.applications.map { app ->
                RawApplicationEntry(
                    id = app.id,
                    path = app.path,
                    targets = app.targets.mapValues { (_, t) ->
                        RawBundleTarget(
                            bundle = t.bundle,
                            source = t.source,
                            inputs = t.inputs,
                        )
                    },
                )
            },
        )
    }
}
