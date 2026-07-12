package io.aequicor.aikit.e2e

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Discovers the CLI version and the repository's local simple-kit fixture.
 * Cached for the lifetime of the test JVM to avoid repeated process spawns.
 */
object Discovery {

    private val versionRegex = Regex("""[0-9]+\.[0-9]+\.[0-9]+""")

    val aikitVersion: String by lazy {
        val result = KitRunner.run("--version")
        assertSuccess(result, "--version")
        versionRegex.find(result.combined)?.value
            ?: error("Could not parse aikit version from output: ${result.combined}")
    }

    val simpleKitRef: String get() = simpleKitPath.toString()

    val simpleKitPath: Path by lazy {
        System.getProperty("kit.bundlesDir")?.let { configured ->
            val candidate = Paths.get(configured).resolve("simple-kit/0.0.1")
            if (Files.isRegularFile(candidate.resolve("bundle.json"))) return@lazy candidate
        }
        var current: Path? = Paths.get(System.getProperty("user.dir")).toAbsolutePath()
        while (current != null) {
            val candidate = current.resolve("bundles/simple-kit/0.0.1")
            if (Files.isRegularFile(candidate.resolve("bundle.json"))) return@lazy candidate
            current = current.parent
        }
        error("Could not locate bundles/simple-kit/0.0.1 from ${System.getProperty("user.dir")}")
    }

    val simpleKitVersion: String = "0.0.1"

    /** No official OpenCode bundle is currently shipped. */
    fun hasOpenCodeBundle(): Boolean {
        return false
    }
}
