package io.aequicor.aikit.e2e

/**
 * Discovers the aikit binary version and the embedded simple-kit bundle ref by querying the CLI itself.
 * Cached for the lifetime of the test JVM to avoid repeated process spawns.
 */
object Discovery {

    private val versionRegex = Regex("""[0-9]+\.[0-9]+\.[0-9]+""")
    private val simpleKitRegex = Regex("""embedded:simple-kit@([0-9]+\.[0-9]+\.[0-9]+)""")

    val aikitVersion: String by lazy {
        val result = KitRunner.run("--version")
        assertSuccess(result, "--version")
        versionRegex.find(result.combined)?.value
            ?: error("Could not parse aikit version from output: ${result.combined}")
    }

    val simpleKitRef: String by lazy {
        val result = KitRunner.run("schema", "bundle", "--list")
        assertSuccess(result, "schema bundle --list")
        simpleKitRegex.find(result.combined)?.value
            ?: error("No embedded:simple-kit@<version> in --list output:\n${result.combined}")
    }

    val simpleKitVersion: String get() = simpleKitRef.substringAfter('@')

    /** Optional: scan `schema bundle --list` for opencode-capable bundles. */
    fun hasOpenCodeBundle(): Boolean {
        val result = KitRunner.run("schema", "bundle", "--list")
        if (result.exitCode != 0) return false
        return result.combined.lineSequence().any { line ->
            line.contains("opencode", ignoreCase = true)
        }
    }
}
