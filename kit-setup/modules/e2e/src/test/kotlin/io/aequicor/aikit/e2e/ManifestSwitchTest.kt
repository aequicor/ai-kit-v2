package io.aequicor.aikit.e2e

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ManifestSwitchTest {

    private lateinit var sandbox: Path

    @BeforeEach
    fun setUp() {
        sandbox = Fixtures.newSandbox("aikit-e2e-switch-")
    }

    @AfterEach
    fun tearDown() {
        sandbox.toFile().deleteRecursively()
    }

    @Test
    fun `switching manifests wipes previous installation`() {
        val aikit = sandbox.resolve(".aikit")
        aikit.createDirectories()

        // Alpha: skills=["review"] generates .claude/commands/review.md.
        aikit.resolve("manifest.alpha.json").writeText(
            Fixtures.simpleKitManifest(
                aikitVersion = Discovery.aikitVersion,
                bundleVersion = Discovery.simpleKitVersion,
                skills = listOf("review"),
            ),
        )
        assertSuccess(KitRunner.run("generate", "--mode", "alpha", cwd = sandbox), "generate alpha")
        assertFileExists(sandbox.resolve(".claude/commands/review.md"))

        // Beta: skills=["security-review"] — review.md is not in the plan.
        aikit.resolve("manifest.beta.json").writeText(
            Fixtures.simpleKitManifest(
                aikitVersion = Discovery.aikitVersion,
                bundleVersion = Discovery.simpleKitVersion,
                skills = listOf("security-review"),
            ),
        )
        val result = KitRunner.run("generate", "--mode", "beta", cwd = sandbox)
        assertSuccess(result, "generate beta (manifest switch)")
        assertStdoutContains(result, "Switched manifest")
        // review.md was produced by the alpha installation and is not in the beta plan.
        // The plan-first wipe must have removed it before applying beta.
        assertFileAbsent(sandbox.resolve(".claude/commands/review.md"))
        // The beta plan creates the security-review skill.
        assertFileExists(sandbox.resolve(".claude/skills/security-review/SKILL.md"))
    }

    @Test
    fun `generate --clean forces wipe and reports it`() {
        val manifest = Fixtures.writeManifest(
            sandbox,
            Fixtures.simpleKitManifest(
                aikitVersion = Discovery.aikitVersion,
                bundleVersion = Discovery.simpleKitVersion,
            ),
        )
        assertSuccess(KitRunner.run("generate", manifest.toString(), cwd = sandbox), "generate #1")

        val result = KitRunner.run("generate", "--clean", manifest.toString(), cwd = sandbox)
        assertSuccess(result, "generate --clean")
        assertStdoutContains(result, "previous installation wiped")
        assertFileExists(sandbox.resolve("CLAUDE.md"))
    }

    @Test
    fun `lock file contains manifestRef after generate`() {
        val aikit = sandbox.resolve(".aikit")
        aikit.createDirectories()
        aikit.resolve("manifest.dev.json").writeText(
            Fixtures.simpleKitManifest(
                aikitVersion = Discovery.aikitVersion,
                bundleVersion = Discovery.simpleKitVersion,
            ),
        )

        assertSuccess(KitRunner.run("generate", "--mode", "dev", cwd = sandbox), "generate --mode dev")

        val lockFile = sandbox.resolve(".aikit/manifest.lock.json")
        assertFileExists(lockFile)
        assertFileContains(lockFile, "manifestRef")
        assertFileContains(lockFile, "manifest.dev.json")
    }

    @Test
    fun `broken new manifest does not destroy working installation`() {
        val aikit = sandbox.resolve(".aikit")
        aikit.createDirectories()

        // First: generate a working installation with alpha.
        aikit.resolve("manifest.alpha.json").writeText(
            Fixtures.simpleKitManifest(
                aikitVersion = Discovery.aikitVersion,
                bundleVersion = Discovery.simpleKitVersion,
            ),
        )
        assertSuccess(KitRunner.run("generate", "--mode", "alpha", cwd = sandbox), "generate alpha")
        assertFileExists(sandbox.resolve("CLAUDE.md"))

        // Now attempt to switch to a broken manifest — plan phase must fail.
        aikit.resolve("manifest.beta.json").writeText("{ broken json !!!")
        val result = KitRunner.run("generate", "--mode", "beta", cwd = sandbox)
        assertFailure(result, "generate with broken manifest")

        // The alpha installation must still be intact — no wipe should have occurred.
        assertFileExists(sandbox.resolve("CLAUDE.md"))
    }
}
