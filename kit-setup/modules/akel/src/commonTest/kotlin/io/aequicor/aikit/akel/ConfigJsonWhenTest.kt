package io.aequicor.aikit.akel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Tests using `when` expressions verbatim from the CONFIG_JSON.md reference example.
// Each test maps to a specific `when` field in that example config.json.
class ConfigJsonWhenTest {

    // ---------- helpers ----------

    private fun eval(expression: String, vararg inputs: Pair<String, AkelValue>): Boolean =
        Akel.evaluate(expression, AkelContext.of(inputs.toMap())).getOrThrow()

    private fun input(key: String, value: AkelValue) =
        "bundle.input.$key" to value

    // ---------- settings.permissions.allow ----------

    @Test
    fun allowBashGitPush_trustedProfile() {
        val expr = "\${bundle.input.profile} == 'trusted'"
        assertTrue(eval(expr, input("profile", AkelValue.Str("trusted"))))
        assertEquals(false, eval(expr, input("profile", AkelValue.Str("ci"))))
        assertEquals(false, eval(expr, input("profile", AkelValue.Str("full"))))
    }

    // ---------- settings.permissions.deny ----------

    @Test
    fun denyReadSecrets_whenSecretsNotAllowed() {
        val expr = "!\${bundle.input.allowSecrets}"
        assertTrue(eval(expr, input("allowSecrets", AkelValue.Bool(false))))
        assertEquals(false, eval(expr, input("allowSecrets", AkelValue.Bool(true))))
    }

    // ---------- mcpServers ----------

    @Test
    fun mcpServer_github() {
        val expr = "\${bundle.input.githubMcp}"
        assertTrue(eval(expr, input("githubMcp", AkelValue.Bool(true))))
        assertEquals(false, eval(expr, input("githubMcp", AkelValue.Bool(false))))
    }

    @Test
    fun mcpServer_knowledgeOs() {
        val expr = "\${bundle.input.knowledgeOs}"
        assertTrue(eval(expr, input("knowledgeOs", AkelValue.Bool(true))))
        assertEquals(false, eval(expr, input("knowledgeOs", AkelValue.Bool(false))))
    }

    // ---------- agents ----------

    @Test
    fun agent_codeReviewer() {
        val expr = "\${bundle.input.reviewer}"
        assertTrue(eval(expr, input("reviewer", AkelValue.Bool(true))))
        assertEquals(false, eval(expr, input("reviewer", AkelValue.Bool(false))))
    }

    @Test
    fun agent_testRunner_fullProfile() {
        val expr = "\${bundle.input.profile} == 'full'"
        assertTrue(eval(expr, input("profile", AkelValue.Str("full"))))
        assertEquals(false, eval(expr, input("profile", AkelValue.Str("ci"))))
        assertEquals(false, eval(expr, input("profile", AkelValue.Str("trusted"))))
    }

    // ---------- commands ----------

    @Test
    fun command_release_ciOrFullProfile() {
        val expr = "\${bundle.input.profile} in ['ci','full']"
        assertTrue(eval(expr, input("profile", AkelValue.Str("ci"))))
        assertTrue(eval(expr, input("profile", AkelValue.Str("full"))))
        assertEquals(false, eval(expr, input("profile", AkelValue.Str("trusted"))))
        assertEquals(false, eval(expr, input("profile", AkelValue.Str(""))))
    }

    @Test
    fun command_deploy() {
        val expr = "\${bundle.input.deploy}"
        assertTrue(eval(expr, input("deploy", AkelValue.Bool(true))))
        assertEquals(false, eval(expr, input("deploy", AkelValue.Bool(false))))
    }

    // ---------- skills ----------

    @Test
    fun skill_review() {
        val expr = "'review' in \${bundle.input.skills}"
        val withReview = input("skills", AkelValue.Lst(listOf(AkelValue.Str("review"))))
        val withBoth = input("skills", AkelValue.Lst(listOf(AkelValue.Str("review"), AkelValue.Str("security-review"))))
        val withoutReview = input("skills", AkelValue.Lst(listOf(AkelValue.Str("security-review"))))
        val empty = input("skills", AkelValue.Lst(emptyList()))

        assertTrue(eval(expr, withReview))
        assertTrue(eval(expr, withBoth))
        assertEquals(false, eval(expr, withoutReview))
        assertEquals(false, eval(expr, empty))
    }

    @Test
    fun skill_securityReview() {
        val expr = "'security-review' in \${bundle.input.skills}"
        val withSecurity = input("skills", AkelValue.Lst(listOf(AkelValue.Str("security-review"))))
        val withBoth = input("skills", AkelValue.Lst(listOf(AkelValue.Str("review"), AkelValue.Str("security-review"))))
        val withoutSecurity = input("skills", AkelValue.Lst(listOf(AkelValue.Str("review"))))

        assertTrue(eval(expr, withSecurity))
        assertTrue(eval(expr, withBoth))
        assertEquals(false, eval(expr, withoutSecurity))
    }

    // ---------- hooks ----------

    @Test
    fun hook_preToolUse_strict() {
        val expr = "\${bundle.input.strict}"
        assertTrue(eval(expr, input("strict", AkelValue.Bool(true))))
        assertEquals(false, eval(expr, input("strict", AkelValue.Bool(false))))
    }

    // ---------- combined profiles ----------

    @Test
    fun profileFullEnablesAgentAndCommand() {
        val profile = input("profile", AkelValue.Str("full"))
        assertTrue(eval("\${bundle.input.profile} == 'full'", profile))
        assertTrue(eval("\${bundle.input.profile} in ['ci','full']", profile))
    }

    @Test
    fun profileCiEnablesCommandButNotAgent() {
        val profile = input("profile", AkelValue.Str("ci"))
        assertEquals(false, eval("\${bundle.input.profile} == 'full'", profile))
        assertTrue(eval("\${bundle.input.profile} in ['ci','full']", profile))
    }

    @Test
    fun profileTrustedEnablesOnlyBashPush() {
        val profile = input("profile", AkelValue.Str("trusted"))
        assertTrue(eval("\${bundle.input.profile} == 'trusted'", profile))
        assertEquals(false, eval("\${bundle.input.profile} == 'full'", profile))
        assertEquals(false, eval("\${bundle.input.profile} in ['ci','full']", profile))
    }

    // Short-circuit: disabled mcp server should not fail on unrelated missing refs
    @Test
    fun shortCircuit_disabledMcpDoesNotRequireOtherInputs() {
        val ctx = AkelContext.of(mapOf("bundle.input.githubMcp" to AkelValue.Bool(false)))
        // false && <anything> → false without resolving rhs
        val result = Akel.evaluate("false && \${bundle.input.knowledgeOs}", ctx)
        assertEquals(false, result.getOrThrow())
    }
}
