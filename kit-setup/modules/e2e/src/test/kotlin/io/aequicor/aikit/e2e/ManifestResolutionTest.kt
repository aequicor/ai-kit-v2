package io.aequicor.aikit.e2e

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ManifestResolutionTest {

    private lateinit var sandbox: Path

    @BeforeEach
    fun setUp() {
        sandbox = Fixtures.newSandbox("aikit-e2e-resolution-")
    }

    @AfterEach
    fun tearDown() {
        sandbox.toFile().deleteRecursively()
    }

    @Test
    fun `--manifest flag resolves manifest and prints audit trail`() {
        val customManifest = sandbox.resolve("custom/manifest.json")
        customManifest.parent.createDirectories()
        customManifest.writeText(
            Fixtures.simpleKitManifest(
                aikitVersion = Discovery.aikitVersion,
                bundleVersion = Discovery.simpleKitVersion,
            ),
        )

        val result = KitRunner.run("verify", "--manifest", customManifest.toString(), cwd = sandbox)
        assertSuccess(result, "verify --manifest")
        assertStdoutContains(result, "from explicit arg")
    }

    @Test
    fun `--mode flag resolves to aikit manifest mode file`() {
        val aikit = sandbox.resolve(".aikit")
        aikit.createDirectories()
        aikit.resolve("manifest.dev.json").writeText(
            Fixtures.simpleKitManifest(
                aikitVersion = Discovery.aikitVersion,
                bundleVersion = Discovery.simpleKitVersion,
            ),
        )

        val result = KitRunner.run("generate", "--mode", "dev", cwd = sandbox)
        assertSuccess(result, "generate --mode dev")
        assertStdoutContains(result, "from --mode dev")
        assertFileExists(sandbox.resolve("CLAUDE.md"))
    }

    @Test
    fun `AIKIT_MANIFEST env var resolves manifest`() {
        val manifest = Fixtures.writeManifest(
            sandbox,
            Fixtures.simpleKitManifest(
                aikitVersion = Discovery.aikitVersion,
                bundleVersion = Discovery.simpleKitVersion,
            ),
        )

        val result = KitRunner.run(
            "verify",
            cwd = sandbox,
            env = mapOf("AIKIT_MANIFEST" to manifest.toString()),
        )
        assertSuccess(result, "verify via AIKIT_MANIFEST")
        assertStdoutContains(result, "from env AIKIT_MANIFEST")
    }

    @Test
    fun `AIKIT_MODE env var resolves mode manifest`() {
        val aikit = sandbox.resolve(".aikit")
        aikit.createDirectories()
        aikit.resolve("manifest.staging.json").writeText(
            Fixtures.simpleKitManifest(
                aikitVersion = Discovery.aikitVersion,
                bundleVersion = Discovery.simpleKitVersion,
            ),
        )

        val result = KitRunner.run(
            "generate",
            cwd = sandbox,
            env = mapOf("AIKIT_MODE" to "staging"),
        )
        assertSuccess(result, "generate via AIKIT_MODE")
        assertStdoutContains(result, "AIKIT_MODE=staging")
        assertFileExists(sandbox.resolve("CLAUDE.md"))
    }

    @Test
    fun `local properties manifest field resolves manifest`() {
        val aikit = sandbox.resolve(".aikit")
        aikit.createDirectories()
        aikit.resolve("manifest.local.json").writeText(
            Fixtures.simpleKitManifest(
                aikitVersion = Discovery.aikitVersion,
                bundleVersion = Discovery.simpleKitVersion,
            ),
        )
        aikit.resolve("local.properties").writeText("manifest=.aikit/manifest.local.json\n")

        val result = KitRunner.run("generate", cwd = sandbox)
        assertSuccess(result, "generate via local.properties")
        assertStdoutContains(result, "from .aikit/local.properties")
        assertFileExists(sandbox.resolve("CLAUDE.md"))
    }

    @Test
    fun `legacy manifest json is resolved without audit trail`() {
        val manifest = Fixtures.writeManifest(
            sandbox,
            Fixtures.simpleKitManifest(
                aikitVersion = Discovery.aikitVersion,
                bundleVersion = Discovery.simpleKitVersion,
            ),
        )

        val result = KitRunner.run("generate", cwd = sandbox)
        assertSuccess(result, "generate legacy manifest")
        assertFileExists(sandbox.resolve("CLAUDE.md"))
        // Legacy default source does not print an audit trail line.
        val hasAuditLine = result.combined.contains("Manifest:") && result.combined.contains("(from")
        assert(!hasAuditLine) {
            "Legacy default should not print audit trail, but got:\n${result.combined}"
        }
    }

    @Test
    fun `conflicting --manifest and --mode flags fail`() {
        val manifest = Fixtures.writeManifest(
            sandbox,
            Fixtures.simpleKitManifest(
                aikitVersion = Discovery.aikitVersion,
                bundleVersion = Discovery.simpleKitVersion,
            ),
        )

        val result = KitRunner.run(
            "generate", "--manifest", manifest.toString(), "--mode", "dev",
            cwd = sandbox,
        )
        assertFailure(result, "conflicting --manifest and --mode")
    }

    @Test
    fun `no manifest configured returns error`() {
        // Create .aikit/ dir but leave it empty — no manifest.json and no env vars.
        sandbox.resolve(".aikit").createDirectories()

        val result = KitRunner.run(
            "generate",
            cwd = sandbox,
            env = mapOf("AIKIT_MANIFEST" to "", "AIKIT_MODE" to ""),
        )
        assertFailure(result, "no manifest configured")
    }
}
