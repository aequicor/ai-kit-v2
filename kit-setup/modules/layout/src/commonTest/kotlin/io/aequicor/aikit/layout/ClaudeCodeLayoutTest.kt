package io.aequicor.aikit.layout

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ClaudeCodeLayoutTest {

    private val layout = ClaudeCodeLayout

    @Test fun memoryGoesToProjectRoot() {
        assertEquals("CLAUDE.md", layout.destinationFor(FileKind.MEMORY, "CLAUDE.md", "CLAUDE.md"))
    }

    @Test fun subagentGoesToAgentsDir() {
        assertEquals(
            ".claude/agents/code-reviewer.md",
            layout.destinationFor(FileKind.SUBAGENT, "code-reviewer", "subagents/code-reviewer/code-reviewer.md"),
        )
    }

    @Test fun commandGoesToCommandsDir() {
        assertEquals(
            ".claude/commands/review.md",
            layout.destinationFor(FileKind.COMMAND, "review", "commands/review.md"),
        )
    }

    @Test fun skillKeepsInternalLayout() {
        assertEquals(
            ".claude/skills/review/SKILL.md",
            layout.destinationFor(FileKind.SKILL, "review", "SKILL.md"),
        )
        assertEquals(
            ".claude/skills/review/scripts/run.sh",
            layout.destinationFor(FileKind.SKILL, "review", "scripts/run.sh"),
        )
    }

    @Test fun hookScriptUsesCommandPathAsName() {
        assertEquals(
            ".claude/hooks/session-start.sh",
            layout.destinationFor(FileKind.HOOK_SCRIPT, ".claude/hooks/session-start.sh", "claude-code/hooks/session-start.sh"),
        )
    }

    @Test fun nativeFilePaths() {
        assertEquals(".claude/settings.json", layout.settingsFile())
        assertEquals(".mcp.json", layout.mcpFile())
    }
}

class OpenCodeLayoutTest {

    private val layout = OpenCodeLayout

    @Test fun memoryGoesToProjectRoot() {
        assertEquals("AGENTS.md", layout.destinationFor(FileKind.MEMORY, "AGENTS.md", "AGENTS.md"))
    }

    @Test fun commandGoesToCommandDir() {
        assertEquals(
            ".opencode/command/review.md",
            layout.destinationFor(FileKind.COMMAND, "review", "command/review.md"),
        )
    }

    @Test fun skillGoesToSkillsDir() {
        assertEquals(
            ".opencode/skills/review/SKILL.md",
            layout.destinationFor(FileKind.SKILL, "review", "SKILL.md"),
        )
    }

    @Test fun subagentRejected() {
        assertFailsWith<UnsupportedLayoutKind> {
            layout.destinationFor(FileKind.SUBAGENT, "x", "x.md")
        }
    }

    @Test fun hookScriptRejected() {
        assertFailsWith<UnsupportedLayoutKind> {
            layout.destinationFor(FileKind.HOOK_SCRIPT, ".opencode/hooks/x.sh", "hooks/x.sh")
        }
    }

    @Test fun nativeFilePaths() {
        assertEquals("opencode.json", layout.settingsFile())
        assertEquals(null, layout.mcpFile())
    }
}

class QwenCodeLayoutTest {

    private val layout = QwenCodeLayout

    @Test fun memoryGoesToProjectRoot() {
        assertEquals("QWEN.md", layout.destinationFor(FileKind.MEMORY, "QWEN.md", "QWEN.md"))
    }

    @Test fun subagentGoesToAgentsDir() {
        assertEquals(
            ".qwen/agents/code-reviewer.md",
            layout.destinationFor(FileKind.SUBAGENT, "code-reviewer", "subagents/code-reviewer/code-reviewer.md"),
        )
    }

    @Test fun nativeFilePaths() {
        assertEquals(".qwen/settings.json", layout.settingsFile())
        assertEquals(null, layout.mcpFile())
    }
}
