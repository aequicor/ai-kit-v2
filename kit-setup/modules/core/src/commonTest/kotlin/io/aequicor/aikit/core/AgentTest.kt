package io.aequicor.aikit.core

import kotlin.test.Test
import kotlin.test.assertEquals

class AgentTest {

    @Test
    fun `byId returns ClaudeCode for claude-code`() {
        assertEquals(Agent.ClaudeCode, Agent.byId(AgentId("claude-code")))
    }

    @Test
    fun `byId returns OpenCode for opencode`() {
        assertEquals(Agent.OpenCode, Agent.byId(AgentId("opencode")))
    }

    @Test
    fun `byId returns null for unknown id`() {
        assertEquals(null, Agent.byId(AgentId("unknown")))
    }
}
