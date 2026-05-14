package io.aequicor.aikit.e2e

import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test

/**
 * Activates only when at least one embedded bundle declares the `opencode` target.
 * Today simple-kit only targets `claude-code`, so this test is skipped — the scaffold is
 * kept ready so that when an OpenCode-capable bundle ships, coverage activates automatically.
 */
class OpenCodeTargetTest {

    @Test
    fun `opencode bundle generates AGENTS_md when available`() {
        assumeTrue(Discovery.hasOpenCodeBundle()) {
            "No embedded bundle declares the `opencode` target — skipping."
        }
        // When such a bundle exists, replace this stub with real assertions:
        //   - run `schema bundle --list`, pick the opencode-capable ref
        //   - write a manifest targeting that bundle's opencode target
        //   - run generate, assert AGENTS.md / .opencode/command/<name>.md / opencode.json
        error("OpenCode coverage stub: bundle detected but real assertions are not yet implemented.")
    }
}
