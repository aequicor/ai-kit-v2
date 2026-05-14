package io.aequicor.aikit.e2e

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VerifyAllTest {

    private lateinit var sandbox: Path

    @BeforeEach
    fun setUp() {
        sandbox = Fixtures.newSandbox("aikit-e2e-verifyall-")
    }

    @AfterEach
    fun tearDown() {
        sandbox.toFile().deleteRecursively()
    }

    @Test
    fun `verify --all validates all manifest files and exits zero when all pass`() {
        val aikit = sandbox.resolve(".aikit")
        aikit.createDirectories()

        val valid = Fixtures.simpleKitManifest(
            aikitVersion = Discovery.aikitVersion,
            bundleVersion = Discovery.simpleKitVersion,
        )
        aikit.resolve("manifest.json").writeText(valid)
        aikit.resolve("manifest.dev.json").writeText(valid)
        aikit.resolve("manifest.staging.json").writeText(valid)

        val result = KitRunner.run("verify", "--all", cwd = sandbox)
        assertSuccess(result, "verify --all all valid")
        assertStdoutContains(result, "manifest.json: OK")
        assertStdoutContains(result, "manifest.dev.json: OK")
        assertStdoutContains(result, "manifest.staging.json: OK")
    }

    @Test
    fun `verify --all reports per-file errors and fails when any manifest is invalid`() {
        val aikit = sandbox.resolve(".aikit")
        aikit.createDirectories()

        aikit.resolve("manifest.dev.json").writeText(
            Fixtures.simpleKitManifest(
                aikitVersion = Discovery.aikitVersion,
                bundleVersion = Discovery.simpleKitVersion,
            ),
        )
        // Deliberately broken manifest.
        aikit.resolve("manifest.broken.json").writeText("{ not valid json }")

        val result = KitRunner.run("verify", "--all", cwd = sandbox)
        assertFailure(result, "verify --all with broken manifest")
        assertStdoutContains(result, "manifest.dev.json: OK")
        assertStdoutContains(result, "manifest.broken.json: ERROR")
    }

    @Test
    fun `verify --all with no manifest files exits zero`() {
        val aikit = sandbox.resolve(".aikit")
        aikit.createDirectories()
        // Only a non-manifest file in .aikit/ — must be ignored.
        aikit.resolve("local.properties").writeText("manifest=something\n")

        val result = KitRunner.run("verify", "--all", cwd = sandbox)
        assertSuccess(result, "verify --all no manifests")
        assertStdoutContains(result, "No manifest files found")
    }

    @Test
    fun `verify --all cannot be combined with a positional manifest path`() {
        val manifest = Fixtures.writeManifest(
            sandbox,
            Fixtures.simpleKitManifest(
                aikitVersion = Discovery.aikitVersion,
                bundleVersion = Discovery.simpleKitVersion,
            ),
        )

        val result = KitRunner.run("verify", "--all", manifest.toString(), cwd = sandbox)
        assertFailure(result, "verify --all with positional arg")
    }

    @Test
    fun `verify --all cannot be combined with --mode`() {
        val aikit = sandbox.resolve(".aikit")
        aikit.createDirectories()
        aikit.resolve("manifest.dev.json").writeText(
            Fixtures.simpleKitManifest(
                aikitVersion = Discovery.aikitVersion,
                bundleVersion = Discovery.simpleKitVersion,
            ),
        )

        val result = KitRunner.run("verify", "--all", "--mode", "dev", cwd = sandbox)
        assertFailure(result, "verify --all with --mode")
    }
}
