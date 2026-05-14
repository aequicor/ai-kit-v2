package io.aequicor.aikit.e2e

import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Validates the *content* generated from bundle templates: variable substitution,
 * conditional blocks, settings.json / .mcp.json structure, and absence of leftover
 * template syntax in any generated text file.
 */
class ContentValidationTest {

    private lateinit var sandbox: Path
    private val json = Json { ignoreUnknownKeys = true }

    @BeforeEach
    fun setUp() {
        sandbox = Fixtures.newSandbox("aikit-e2e-content-")
    }

    @AfterEach
    fun tearDown() {
        sandbox.toFile().deleteRecursively()
    }

    // ───────────────────────── full-feature scenario ─────────────────────────

    @Test
    fun `full-feature manifest produces fully substituted content`() {
        generate(
            projectName = "contentcheck",
            skills = listOf("review", "security-review"),
            subagents = listOf("code-reviewer", "test-runner"),
            githubMcp = true,
            strict = true,
        )

        val claude = read("CLAUDE.md")

        // ── substitution: projectName appears, no placeholder remains
        assertContainsAll(
            claude,
            "# CLAUDE.md — contentcheck",
            "конституция агента для проекта **contentcheck**",
            "Скилы подхватываются автоматически по описанию: review, security-review.",
        )

        // ── conditional blocks: all four `when` branches included
        assertContainsAll(
            claude,
            "## Strict-режим включён",
            "## GitHub MCP",
            "## Доступные субагенты",
            "- **code-reviewer** — ревью изменений против конвенций репозитория.",
            "- **test-runner** — прогон тестов и краткий отчёт.",
        )

        // ── command/review.md: both nested `when` blocks present
        val reviewCmd = read(".claude/commands/review.md")
        assertContainsAll(
            reviewCmd,
            "проекта **contentcheck**",
            "активируй скил **security-review**",
            "субагенту **code-reviewer**",
        )

        // ── skill/review SKILL.md: strict step #5 present
        val skill = read(".claude/skills/review/SKILL.md")
        assertContainsAll(
            skill,
            "review the current branch's diff against repo conventions in contentcheck",
            "5. Strict-режим: дополнительно проверь",
        )

        // ── subagent code-reviewer: project name substituted in description frontmatter
        val codeReviewer = read(".claude/agents/code-reviewer.md")
        assertTrue(codeReviewer.contains("Independent code review against repo conventions in contentcheck"))

        // ── strict hook script: literal content preserved (no templating in shell scripts)
        val strictHook = read(".claude/strict/hooks/block-dangerous.sh")
        assertContainsAll(
            strictHook,
            "rm -rf is not allowed",
            "force push is not allowed",
            "git reset --hard is not allowed",
        )

        // ── settings.json: strict deny rules present + hooks wired up
        val settings = readJson(".claude/settings.json")
        val denyValues = settings.permissionsList("deny")
        assertTrue("Bash(rm -rf:*)" in denyValues) { "strict deny missing: rm -rf, got $denyValues" }
        assertTrue("Bash(git push --force:*)" in denyValues) { "strict deny missing: force-push, got $denyValues" }

        val preToolUse = settings.hookCommands("PreToolUse")
        assertTrue(".claude/strict/hooks/block-dangerous.sh" in preToolUse) {
            "expected PreToolUse hook for strict, got $preToolUse"
        }
        val sessionStart = settings.hookCommands("SessionStart")
        assertTrue(".claude/hooks/session-start.sh" in sessionStart) {
            "expected SessionStart hook, got $sessionStart"
        }

        // ── .mcp.json: github server present
        val mcp = readJson(".mcp.json")
        val servers = mcp["mcpServers"]?.jsonObject ?: error(".mcp.json missing mcpServers: $mcp")
        assertTrue("github" in servers.keys) { "expected mcpServers.github, got ${servers.keys}" }
        assertEquals("npx", servers["github"]!!.jsonObject["command"]!!.jsonPrimitive.content)

        // ── no leftover template syntax anywhere in generated text
        assertNoTemplateLeftovers()
    }

    // ───────────────────────── minimal scenario (toggles off) ─────────────────────────

    @Test
    fun `minimal manifest excludes conditional blocks and strict rules`() {
        generate(
            projectName = "minimal-app",
            skills = listOf("review"),
            subagents = emptyList(),
            githubMcp = false,
            strict = false,
        )

        val claude = read("CLAUDE.md")

        // included
        assertTrue(claude.contains("# CLAUDE.md — minimal-app"))
        assertTrue(claude.contains("по описанию: review."))

        // excluded conditional blocks
        assertNotContainsAll(
            claude,
            "## Strict-режим включён",
            "## GitHub MCP",
            "## Доступные субагенты",
            "code-reviewer",
            "test-runner",
        )

        // /review command: nested `when` branches removed
        val reviewCmd = read(".claude/commands/review.md")
        assertNotContainsAll(
            reviewCmd,
            "security-review",
            "code-reviewer",
        )
        assertTrue(reviewCmd.contains("проекта **minimal-app**"))

        // skill SKILL.md: strict step #5 absent
        val skill = read(".claude/skills/review/SKILL.md")
        assertFalse(skill.contains("5. Strict-режим")) {
            "strict step should be absent when strict=false, but found in:\n$skill"
        }

        // settings.json: strict deny rules absent, PreToolUse absent
        val settings = readJson(".claude/settings.json")
        val denyValues = settings.permissionsList("deny")
        assertFalse("Bash(rm -rf:*)" in denyValues) { "strict deny should be absent, got $denyValues" }
        assertFalse("Bash(git push --force:*)" in denyValues) { "strict deny should be absent, got $denyValues" }
        assertTrue(settings.hookCommands("PreToolUse").isEmpty()) {
            "PreToolUse hooks should be absent when strict=false"
        }
        // SessionStart is unconditional
        assertTrue(".claude/hooks/session-start.sh" in settings.hookCommands("SessionStart"))

        // no .mcp.json when githubMcp=false
        assertFalse(Files.exists(sandbox.resolve(".mcp.json"))) { ".mcp.json must not be created" }

        // strict hook script must not exist
        assertFalse(Files.exists(sandbox.resolve(".claude/strict/hooks/block-dangerous.sh"))) {
            "strict hook script must not exist when strict=false"
        }

        assertNoTemplateLeftovers()
    }

    // ───────────────────────── helpers ─────────────────────────

    private fun generate(
        projectName: String,
        skills: List<String>,
        subagents: List<String>,
        githubMcp: Boolean,
        strict: Boolean,
    ) {
        val manifest = Fixtures.writeManifest(
            sandbox,
            Fixtures.simpleKitManifest(
                aikitVersion = Discovery.aikitVersion,
                bundleVersion = Discovery.simpleKitVersion,
                projectName = projectName,
                skills = skills,
                subagents = subagents,
                githubMcp = githubMcp,
                strict = strict,
            ),
        )
        assertSuccess(KitRunner.run("generate", manifest.toString(), cwd = sandbox), "generate")
    }

    private fun read(relative: String): String =
        Files.readString(sandbox.resolve(relative))

    private fun readJson(relative: String): JsonObject =
        json.parseToJsonElement(read(relative)).jsonObject

    private fun JsonObject.permissionsList(key: String): List<String> {
        val perms = this["permissions"]?.jsonObject ?: return emptyList()
        val arr = perms[key] as? JsonArray ?: return emptyList()
        return arr.map { it.jsonObject["value"]!!.jsonPrimitive.content }
    }

    /**
     * Returns the flat list of hook commands for a given event (e.g. `PreToolUse`),
     * supporting the nested `{matcher, hooks:[{type,command}]}` shape used in settings.json.
     */
    private fun JsonObject.hookCommands(event: String): List<String> {
        val hooks = this["hooks"]?.jsonObject ?: return emptyList()
        val arr = hooks[event] as? JsonArray ?: return emptyList()
        return arr.flatMap { entry ->
            val obj = entry.jsonObject
            val nested = obj["hooks"] as? JsonArray
            if (nested != null) {
                nested.mapNotNull { it.jsonObject["command"]?.jsonPrimitive?.content }
            } else {
                listOfNotNull(obj["command"]?.jsonPrimitive?.content)
            }
        }
    }

    private fun assertContainsAll(haystack: String, vararg needles: String) {
        for (needle in needles) {
            assertTrue(haystack.contains(needle, ignoreCase = true)) {
                "expected to contain \"$needle\".\n--- actual ---\n$haystack"
            }
        }
    }

    private fun assertNotContainsAll(haystack: String, vararg needles: String) {
        for (needle in needles) {
            assertFalse(haystack.contains(needle, ignoreCase = true)) {
                "expected NOT to contain \"$needle\".\n--- actual ---\n$haystack"
            }
        }
    }

    /**
     * Walks all generated text files and asserts there are no unresolved
     * template markers — `${bundle.input.*}`, `<!-- when: ... -->`, or `<!-- end -->`.
     */
    private fun assertNoTemplateLeftovers() {
        val textExtensions = setOf("md", "json", "sh", "txt", "yml", "yaml")
        val offenders = mutableListOf<String>()
        Files.walk(sandbox).use { stream ->
            stream
                .filter { Files.isRegularFile(it) }
                .filter { sandbox.relativize(it).toString().substringAfterLast('.', "") in textExtensions }
                .filter { !sandbox.relativize(it).toString().startsWith(".aikit/") }
                .forEach { file ->
                    val content = Files.readString(file)
                    val rel = sandbox.relativize(file).toString()
                    if (content.contains("\${bundle.")) offenders += "$rel: \${bundle.*} placeholder"
                    if (content.contains("<!-- when:")) offenders += "$rel: <!-- when: marker"
                    if (content.contains("<!-- end -->")) offenders += "$rel: <!-- end --> marker"
                }
        }
        assertTrue(offenders.isEmpty()) {
            "found unresolved template syntax in generated files:\n" + offenders.joinToString("\n")
        }
    }
}
