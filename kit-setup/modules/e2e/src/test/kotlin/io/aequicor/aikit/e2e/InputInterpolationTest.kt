package io.aequicor.aikit.e2e

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Verifies that inline `${env:X}`, `${file:X}`, and `${envFile:X:KEY}` references in
 * manifest `inputs` are expanded end-to-end: manifest parsing → InputResolver →
 * StringExpander → FsValueSourceReader → template rendering → generated file content.
 */
class InputInterpolationTest {

    private lateinit var sandbox: Path

    @BeforeEach
    fun setUp() {
        sandbox = Fixtures.newSandbox("aikit-e2e-interpolation-")
    }

    @AfterEach
    fun tearDown() {
        sandbox.toFile().deleteRecursively()
    }

    // ─────────────────────────── happy-path ───────────────────────────

    @Test
    fun `env ref resolves projectName from environment variable`() {
        val claude = generate(
            projectNameValue = "\${env:AIKIT_E2E_APP_NAME}",
            env = mapOf("AIKIT_E2E_APP_NAME" to "env-project-name"),
        )
        assertTrue(claude.contains("env-project-name")) {
            "expected CLAUDE.md to contain 'env-project-name', got:\n$claude"
        }
    }

    @Test
    fun `file ref resolves projectName from file content`() {
        sandbox.resolve("inputs").createDirectories()
        sandbox.resolve("inputs/name.txt").writeText("file-project-name")

        val claude = generate(projectNameValue = "\${file:inputs/name.txt}")
        assertTrue(claude.contains("file-project-name")) {
            "expected CLAUDE.md to contain 'file-project-name', got:\n$claude"
        }
    }

    @Test
    fun `envFile ref resolves projectName from dot-env file`() {
        sandbox.resolve("inputs").createDirectories()
        sandbox.resolve("inputs/app.env").writeText("APP=envfile-project\n")

        val claude = generate(projectNameValue = "\${envFile:inputs/app.env:APP}")
        assertTrue(claude.contains("envfile-project")) {
            "expected CLAUDE.md to contain 'envfile-project', got:\n$claude"
        }
    }

    @Test
    fun `composite string with multiple env refs resolves correctly`() {
        val claude = generate(
            projectNameValue = "\${env:AIKIT_E2E_PREFIX}-\${env:AIKIT_E2E_SUFFIX}",
            env = mapOf(
                "AIKIT_E2E_PREFIX" to "hello",
                "AIKIT_E2E_SUFFIX" to "world",
            ),
        )
        assertTrue(claude.contains("hello-world")) {
            "expected CLAUDE.md to contain 'hello-world', got:\n$claude"
        }
    }

    // ─────────────────────────── negative ───────────────────────────

    @Test
    fun `generate fails for undefined environment variable`() {
        val manifest = buildManifest("\${env:AIKIT_E2E_NONEXISTENT_VAR_NEVER_SET_XYZ}")
        val result = KitRunner.run("generate", manifest.toString(), cwd = sandbox)
        assertFailure(result, "generate with undefined env var")
    }

    @Test
    fun `generate fails for missing file reference`() {
        val manifest = buildManifest("\${file:inputs/no-such-file.txt}")
        val result = KitRunner.run("generate", manifest.toString(), cwd = sandbox)
        assertFailure(result, "generate with missing file ref")
    }

    @Test
    fun `generate fails for path traversal in file reference`() {
        val manifest = buildManifest("\${file:../../etc/passwd}")
        val result = KitRunner.run("generate", manifest.toString(), cwd = sandbox)
        assertFailure(result, "generate with path traversal ref")
    }

    @Test
    fun `generate fails for missing key in envFile reference`() {
        sandbox.resolve("inputs").createDirectories()
        sandbox.resolve("inputs/app.env").writeText("OTHER_KEY=some-value\n")

        val manifest = buildManifest("\${envFile:inputs/app.env:NONEXISTENT_KEY}")
        val result = KitRunner.run("generate", manifest.toString(), cwd = sandbox)
        assertFailure(result, "generate with missing envFile key")
    }

    // ─────────────────────────── helpers ───────────────────────────

    /** Writes a manifest with the given raw JSON value for `projectName` and runs `generate`. */
    private fun generate(
        projectNameValue: String,
        env: Map<String, String> = emptyMap(),
    ): String {
        val manifest = buildManifest(projectNameValue)
        val result = KitRunner.run("generate", manifest.toString(), cwd = sandbox, env = env)
        assertSuccess(result, "generate")
        return Files.readString(sandbox.resolve("CLAUDE.md"))
    }

    private fun buildManifest(projectNameValue: String): Path {
        val json = Fixtures.simpleKitManifest(
            aikitVersion = Discovery.aikitVersion,
            bundleVersion = Discovery.simpleKitVersion,
            projectName = "__REPLACE_ME__",
        ).replace("\"__REPLACE_ME__\"", "\"$projectNameValue\"")
        return Fixtures.writeManifest(sandbox, json)
    }
}
