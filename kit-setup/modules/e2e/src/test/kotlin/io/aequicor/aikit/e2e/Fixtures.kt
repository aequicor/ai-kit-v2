package io.aequicor.aikit.e2e

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

object Fixtures {

    fun newSandbox(prefix: String = "aikit-e2e-"): Path =
        Files.createTempDirectory(prefix)

    fun writeManifest(sandbox: Path, manifestJson: String): Path {
        val target = sandbox.resolve(".aikit/manifest.json")
        target.parent.createDirectories()
        target.writeText(manifestJson)
        return target
    }

    fun simpleKitManifest(
        aikitVersion: String,
        bundleVersion: String,
        projectName: String = "my-sandbox-app",
        skills: List<String> = listOf("review"),
        subagents: List<String> = listOf("code-reviewer"),
        githubMcp: Boolean = false,
        strict: Boolean = true,
        applicationId: String = "root",
        applicationPath: String = ".",
    ): String = """
        {
          "aikitVersion": "$aikitVersion",
          "applications": [
            {
              "id": "$applicationId",
              "path": "$applicationPath",
              "targets": {
                "claude": {
                  "bundle": "simple-kit@$bundleVersion",
                  "source": "${Discovery.simpleKitPath.toJsonPath()}",
                  "inputs": {
                    "projectName": "$projectName",
                    "skills": ${skills.toJsonArray()},
                    "subagents": ${subagents.toJsonArray()},
                    "githubMcp": $githubMcp,
                    "strict": $strict
                  }
                }
              }
            }
          ]
        }
    """.trimIndent()

    fun multiAppManifest(
        aikitVersion: String,
        bundleVersion: String,
    ): String = """
        {
          "aikitVersion": "$aikitVersion",
          "applications": [
            {
              "id": "root",
              "path": ".",
              "targets": {
                "claude": {
                  "bundle": "simple-kit@$bundleVersion",
                  "source": "${Discovery.simpleKitPath.toJsonPath()}",
                  "inputs": {
                    "projectName": "root-app",
                    "skills": ["review"],
                    "subagents": ["code-reviewer"],
                    "githubMcp": false,
                    "strict": true
                  }
                }
              }
            },
            {
              "id": "api",
              "path": "./api",
              "targets": {
                "claude": {
                  "bundle": "simple-kit@$bundleVersion",
                  "source": "${Discovery.simpleKitPath.toJsonPath()}",
                  "inputs": {
                    "projectName": "api-app",
                    "skills": ["review"],
                    "subagents": ["test-runner"],
                    "githubMcp": false,
                    "strict": false
                  }
                }
              }
            }
          ]
        }
    """.trimIndent()

    private fun List<String>.toJsonArray(): String =
        joinToString(prefix = "[", postfix = "]") { "\"$it\"" }

    private fun Path.toJsonPath(): String = toAbsolutePath().toString().replace("\\", "\\\\")
}
