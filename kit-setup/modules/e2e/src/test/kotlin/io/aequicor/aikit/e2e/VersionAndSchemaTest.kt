package io.aequicor.aikit.e2e

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue

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
    fun `schema bundle --list contains embedded simple-kit`() {
        val result = KitRunner.run("schema", "bundle", "--list")
        assertSuccess(result)
        assertStdoutContains(result, "embedded:simple-kit@")
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
}
