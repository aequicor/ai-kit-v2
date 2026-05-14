package io.aequicor.aikit.e2e

import java.nio.file.Path
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NegativeTest {

    private lateinit var sandbox: Path

    @BeforeEach
    fun setUp() {
        sandbox = Fixtures.newSandbox("aikit-e2e-neg-")
    }

    @AfterEach
    fun tearDown() {
        sandbox.toFile().deleteRecursively()
    }

    @Test
    fun `verify rejects malformed manifest JSON`() {
        val manifest = Fixtures.writeManifest(sandbox, "{ this is : not json")
        val result = KitRunner.run("verify", manifest.toString(), cwd = sandbox)
        assertFailure(result, "verify on malformed json")
    }

    @Test
    fun `verify rejects manifest referencing unknown bundle`() {
        val manifest = Fixtures.writeManifest(
            sandbox,
            Fixtures.simpleKitManifest(
                aikitVersion = Discovery.aikitVersion,
                bundleVersion = "9.9.9",
            ).replace("simple-kit@9.9.9", "no-such-bundle@1.0.0"),
        )
        val result = KitRunner.run("verify", manifest.toString(), cwd = sandbox)
        assertFailure(result, "verify on unknown bundle")
    }

    @Test
    fun `verify fails when manifest file does not exist`() {
        val missing = sandbox.resolve("nope/.aikit/manifest.json")
        val result = KitRunner.run("verify", missing.toString(), cwd = sandbox)
        assertFailure(result, "verify on missing file")
    }
}
