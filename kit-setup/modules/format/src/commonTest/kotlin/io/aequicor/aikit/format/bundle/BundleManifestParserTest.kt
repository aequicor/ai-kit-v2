package io.aequicor.aikit.format.bundle

import io.aequicor.aikit.core.domain.bundle.InputSpec
import io.aequicor.aikit.core.domain.targets.ClaudeCode
import io.aequicor.aikit.format.AiKitFormat
import io.aequicor.aikit.format.error.FormatError
import io.aequicor.aikit.io.BundleSource
import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.io.writeString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BundleManifestParserTest {

    private val format = AiKitFormat.create()

    @Test
    fun parse_minimal_bundle() {
        val json = """
            {
              "schemaVersion": 1,
              "name": "my-bundle",
              "version": "1.0.0",
              "description": "A test bundle",
              "targets": ["claude-code"]
            }
        """.trimIndent()

        val parsed = format.parseBundleManifest(MemoryBundleSource(json)).getOrThrow()
        val manifest = parsed.manifest

        assertEquals("my-bundle", manifest.name)
        assertEquals("1.0.0", manifest.version)
        assertEquals(1, manifest.targets.size)
        assertIs<ClaudeCode>(manifest.targets[0])
        assertTrue(manifest.inputs.isEmpty())
    }

    @Test
    fun parse_all_input_types() {
        val json = """
            {
              "schemaVersion": 1,
              "name": "full",
              "version": "0.1.0",
              "description": "All input types",
              "targets": ["claude-code"],
              "inputs": [
                { "id": "flag",  "type": "boolean",     "title": "Flag?",  "default": true },
                { "id": "env",   "type": "select",      "title": "Env",    "options": ["dev","prod"], "default": "dev" },
                { "id": "feats", "type": "multiselect", "title": "Feats",  "options": ["a","b"], "default": ["a"] },
                { "id": "name",  "type": "string",      "title": "Name",   "default": "hello" },
                { "id": "n",     "type": "int",         "title": "N",      "default": 4096, "min": 1, "max": 200000 },
                { "id": "t",     "type": "double",      "title": "Temp",   "default": 0.7,  "min": 0.0, "max": 2.0 }
              ]
            }
        """.trimIndent()

        val manifest = format.parseBundleManifest(MemoryBundleSource(json)).getOrThrow().manifest
        assertEquals(6, manifest.inputs.size)

        assertIs<InputSpec.BoolInput>(manifest.inputs[0]).also { assertEquals(true, it.default) }
        assertIs<InputSpec.SelectInput>(manifest.inputs[1]).also { assertEquals("dev", it.default) }
        assertIs<InputSpec.MultiSelectInput>(manifest.inputs[2]).also {
            assertEquals(listOf("a"), it.default)
        }
        assertIs<InputSpec.StringInput>(manifest.inputs[3]).also { assertEquals("hello", it.default) }
        assertIs<InputSpec.IntInput>(manifest.inputs[4]).also {
            assertEquals(4096, it.default)
            assertEquals(1, it.min)
            assertEquals(200000, it.max)
        }
        assertIs<InputSpec.DoubleInput>(manifest.inputs[5]).also {
            assertEquals(0.7, it.default)
            assertEquals(0.0, it.min)
            assertEquals(2.0, it.max)
        }
    }

    @Test
    fun parse_fails_on_unsupported_schema_version() {
        val json = """{"schemaVersion": 99, "name": "x", "version": "1.0", "description": "y", "targets": []}"""
        val err = format.parseBundleManifest(MemoryBundleSource(json)).exceptionOrNull()
        assertNotNull(err)
        assertIs<FormatError.UnsupportedSchemaVersion>(err)
    }

    @Test
    fun parse_fails_on_bad_json() {
        val err = format.parseBundleManifest(MemoryBundleSource("not json")).exceptionOrNull()
        assertNotNull(err)
        assertIs<FormatError.BadJson>(err)
    }

    @Test
    fun parse_fails_on_unknown_input_type() {
        val json = """
            {
              "schemaVersion": 1, "name": "x", "version": "1.0", "description": "d", "targets": [],
              "inputs": [{"id": "x", "type": "mystery", "title": "?"}]
            }
        """.trimIndent()
        val err = format.parseBundleManifest(MemoryBundleSource(json)).exceptionOrNull()
        assertNotNull(err)
        assertIs<FormatError.UnknownEnum>(err)
    }
}

/** In-memory [BundleSource] that serves [manifestJson] as `bundle.json` and returns empty for all targets. */
private class MemoryBundleSource(private val manifestJson: String) : BundleSource {
    override fun openManifest(): Result<Source> =
        Result.success(Buffer().also { it.writeString(manifestJson) })

    override fun openTargetConfig(targetFolder: String): Result<Source?> =
        Result.success(null)

    override fun listTargetFiles(targetFolder: String): Result<List<String>> =
        Result.success(emptyList())

    override fun openTargetFile(targetFolder: String, relativePath: String): Result<Source> =
        Result.failure(UnsupportedOperationException("MemoryBundleSource has no template files"))

    override fun close() = Unit
}
