package io.aequicor.aikit.engine.impl

import io.aequicor.aikit.format.AiKitFormat
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefaultBundleCatalogTest {
    private val root = Path("build/catalog-test")

    @AfterTest
    fun cleanUp() = delete(root)

    @Test
    fun `catalog filters incompatible bundles and exposes recommendation metadata`() {
        writeBundle("starter", "1.0.0", ">=1.0.0 <2.0.0")
        writeBundle("future", "2.0.0", ">=2.0.0 <3.0.0")
        val catalog = DefaultBundleCatalog(AiKitFormat.create(), "1.2.0", localRepositoryRoot = root)

        val compatible = catalog.list(".", includeIncompatible = false).getOrThrow()
        assertEquals(listOf("starter"), compatible.bundles.map { it.name })
        assertEquals(listOf("starter"), compatible.bundles.single().tags)

        val all = catalog.list(".", includeIncompatible = true).getOrThrow()
        assertEquals(2, all.bundles.size)
        assertTrue(all.bundles.first { it.name == "starter" }.compatible)
        assertFalse(all.bundles.first { it.name == "future" }.compatible)
    }

    private fun writeBundle(name: String, version: String, range: String) {
        val dir = Path(root, "bundles", name, version)
        SystemFileSystem.createDirectories(dir)
        val json = """{
          "schemaVersion": 2,
          "name": "$name",
          "version": "$version",
          "description": "$name bundle",
          "kitSetup": "$range",
          "tags": ["$name"],
          "bestFor": ["tests"],
          "notFor": [],
          "targets": [],
          "inputs": []
        }"""
        SystemFileSystem.sink(Path(dir, "bundle.json")).buffered().use { it.writeString(json) }
    }

    private fun delete(path: Path) {
        val metadata = SystemFileSystem.metadataOrNull(path) ?: return
        if (metadata.isDirectory) SystemFileSystem.list(path).forEach(::delete)
        SystemFileSystem.delete(path, mustExist = false)
    }
}
