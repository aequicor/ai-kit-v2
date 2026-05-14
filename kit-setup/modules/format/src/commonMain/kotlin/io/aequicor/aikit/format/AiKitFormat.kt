package io.aequicor.aikit.format

import io.aequicor.aikit.core.domain.bundle.BundleManifest
import io.aequicor.aikit.format.bundle.BundleManifestParser
import io.aequicor.aikit.format.bundle.renderBundleInputsSchema
import io.aequicor.aikit.format.projectManifest.ProjectManifestParser
import io.aequicor.aikit.format.projectManifest.RawProjectManifest
import io.aequicor.aikit.format.template.TemplateBodyParser
import io.aequicor.aikit.format.template.TemplatePart
import io.aequicor.aikit.io.BundleSource
import kotlinx.io.Source
import kotlinx.io.readString
import kotlinx.serialization.json.Json

/**
 * Stateless parsing façade for all AI-Kit file formats.
 *
 * All methods parse structure and validate syntax but do NOT:
 * - Evaluate AKEL expressions — `when` values and `${bundle.input.<id>}` placeholders are kept
 *   as opaque strings; the `engine` module evaluates them with resolved input context.
 * - Write any files.
 */
interface AiKitFormat {

    /**
     * Parse a bundle directory (via [BundleSource]) into a [ParsedBundle].
     *
     * Reads `bundle.json` and, for each declared target, reads `<target>/config.json` and
     * collects raw [io.aequicor.aikit.format.template.TemplateSource] entries.
     * The [BundleSource] is NOT closed by this method — the caller owns it.
     */
    fun parseBundleManifest(source: BundleSource): Result<ParsedBundle>

    /**
     * Parse `.aikit/manifest.json` into a [RawProjectManifest].
     *
     * Bundle references and input values are left unresolved — the caller (engine) must load
     * each referenced bundle and validate inputs against its [InputSpec] declarations.
     *
     * @param source open [Source] over the raw JSON bytes. Closed by this method.
     */
    fun parseProjectManifest(source: Source): Result<RawProjectManifest>

    /**
     * Parse a `.md` template body into an ordered list of [TemplatePart]s.
     *
     * Used by the `engine` module when rendering templates. Non-text files (`.js`, etc.) are
     * returned as a single [TemplatePart.Literal] wrapping the raw content.
     *
     * @param text raw UTF-8 content of the template file.
     * @param path bundle-relative path — used in error messages only.
     */
    fun parseTemplateBody(text: String, path: String): Result<List<TemplatePart>>

    /** JSON Schema string for `.aikit/manifest.json` (for the `aikit schema` command). */
    fun manifestJsonSchema(): String

    /**
     * JSON Schema string describing the `inputs` object accepted by a specific bundle.
     *
     * Generated from the [BundleManifest.inputs] declarations. The schema can be attached to the
     * `targets.<name>.inputs` block of a project manifest for IDE autocomplete and validation.
     */
    fun bundleInputsJsonSchema(manifest: BundleManifest): String

    companion object {
        fun create(): AiKitFormat = DefaultAiKitFormat(
            json = Json {
                ignoreUnknownKeys = false
                isLenient = false
                coerceInputValues = false
            },
        )
    }
}

internal class DefaultAiKitFormat(private val json: Json) : AiKitFormat {

    private val bundleParser = BundleManifestParser(json)
    private val manifestParser = ProjectManifestParser(json)
    private val templateParser = TemplateBodyParser()

    override fun parseBundleManifest(source: BundleSource): Result<ParsedBundle> =
        bundleParser.parse(source)

    override fun parseProjectManifest(source: Source): Result<RawProjectManifest> =
        source.use { manifestParser.parse(it.readString()) }

    override fun parseTemplateBody(text: String, path: String): Result<List<TemplatePart>> =
        templateParser.parse(text, path)

    override fun manifestJsonSchema(): String = MANIFEST_JSON_SCHEMA

    override fun bundleInputsJsonSchema(manifest: BundleManifest): String =
        renderBundleInputsSchema(manifest, prettyJson)

    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    private val prettyJson: Json = Json(from = json) {
        prettyPrint = true
        prettyPrintIndent = "  "
    }
}

private const val MANIFEST_JSON_SCHEMA = """{
  "${'$'}schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "required": ["aikitVersion", "applications"],
  "properties": {
    "aikitVersion": { "type": "string" },
    "applications": {
      "type": "array",
      "items": {
        "type": "object",
        "required": ["id", "path", "targets"],
        "properties": {
          "id":      { "type": "string" },
          "path":    { "type": "string" },
          "targets": {
            "type": "object",
            "additionalProperties": {
              "type": "object",
              "required": ["bundle", "source", "inputs"],
              "properties": {
                "bundle": { "type": "string" },
                "source": { "type": "string" },
                "inputs": { "type": "object" }
              }
            }
          }
        }
      }
    }
  }
}"""
