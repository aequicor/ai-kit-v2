package io.aequicor.aikit.e2e

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class VersionAndSchemaTest {

    @Test
    fun `--version prints semver`() {
        val result = KitRunner.run("--version")
        assertSuccess(result)
        assertTrue(Regex("""[0-9]+\.[0-9]+\.[0-9]+""").containsMatchIn(result.combined)) {
            "expected X.Y.Z in output, got: ${result.combined}"
        }
    }

    @Test
    fun `legacy embedded source explains remote and local migration`() {
        val result = KitRunner.run("schema", "bundle", "embedded:simple-kit@0.0.1")
        assertTrue(result.exitCode != 0)
        assertTrue(result.combined.contains("source 'remote'"))
        assertTrue(result.combined.contains("local directory or ZIP"))
    }

    @Test
    fun `schema bundle exposes all simple-kit inputs`() {
        val ref = Discovery.simpleKitRef
        val result = KitRunner.run("schema", "bundle", ref)
        assertSuccess(result)
        for (field in listOf("projectName", "skills", "subagents", "githubMcp", "strict")) {
            assertStdoutContains(result, "\"$field\"")
        }
    }

    @Test
    fun `schema bundle accepts a separately downloaded local ZIP`() {
        val sandbox = Fixtures.newSandbox()
        val archive = sandbox.resolve("simple-kit.zip")
        ZipOutputStream(Files.newOutputStream(archive)).use { zip ->
            Files.walk(Discovery.simpleKitPath).use { paths ->
                paths.filter(Files::isRegularFile).forEach { file ->
                    val relative = Discovery.simpleKitPath.relativize(file).toString().replace('\\', '/')
                    zip.putNextEntry(ZipEntry(relative))
                    Files.copy(file, zip)
                    zip.closeEntry()
                }
            }
        }

        val result = KitRunner.run("schema", "bundle", archive.toString(), cwd = sandbox)
        assertSuccess(result)
        assertStdoutContains(result, "\"projectName\"")
    }
}
