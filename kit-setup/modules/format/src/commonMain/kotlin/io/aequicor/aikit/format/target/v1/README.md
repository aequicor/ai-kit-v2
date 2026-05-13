# target/v1 — bundle `config.json` parsers

This package contains the DTOs and mappers for parsing `config.json` files inside AI-Kit bundles.
Each bundle target folder (`claude-code/`, `opencode/`, `qwen-code/`) may contain a `config.json`
that configures the agent-specific features the bundle installs.

---

## Shared concepts

### AKEL `when` conditions

Any object in `config.json` may carry a `"when"` field with an AKEL boolean expression.
If it evaluates to `false` at generation time the object is dropped from the output entirely.

```json
{ "name": "github", "when": "${bundle.input.githubMcp}", "command": "npx", "..." }
```

Supported operators: `==`, `!=`, `!`, `&&`, `||`, `in`.
Placeholder syntax: `${bundle.input.<id>}`.

### File references

`agents`, `commands`, `skills`, and `memory` are declarative lists of files inside the bundle.
Each entry identifies which file to install and an optional condition.

```json
{
  "agents": [
    { "name": "code-reviewer", "source": "subagents/code-reviewer/code-reviewer.md",
      "when": "'code-reviewer' in ${bundle.input.subagents}" }
  ]
}
```

| Field | Type | Description |
|---|---|---|
| `name` | string | Logical name used in generated config. |
| `source` | string | Path relative to the target folder (`claude-code/`). Folder paths are copied recursively. |
| `when` | string? | AKEL condition; entry is skipped when `false`. |

### MCP servers (`mcpServers`)

Shared across Claude Code and Qwen Code. Two transport variants:

**stdio** — local process:
```json
{
  "name": "github",
  "transport": "stdio",
  "command": "npx",
  "args": ["-y", "@modelcontextprotocol/server-github"],
  "env": { "GITHUB_TOKEN": "${bundle.input.githubToken}" },
  "enabled": true,
  "timeout": 30,
  "when": "${bundle.input.githubMcp}"
}
```

**SSE / HTTP Streamable** — remote endpoint:
```json
{
  "name": "my-remote",
  "transport": "sse",
  "url": "https://mcp.example.com/sse",
  "headers": { "Authorization": "Bearer ${bundle.input.token}" },
  "timeout": 60
}
```

| Field | Type | Description |
|---|---|---|
| `name` | string | Identifier used in generated config. |
| `transport` | `"stdio"` \| `"sse"` \| `"http"` | Inferred from which key is present when omitted. |
| `command` | string? | Executable (stdio only). |
| `args` | string[]? | Arguments for `command`. |
| `env` | map? | Environment variables injected into the server process. |
| `url` | string? | Endpoint URL (SSE/HTTP only). |
| `httpUrl` | string? | Alternate HTTP Streamable URL (Qwen Code). |
| `headers` | map? | HTTP headers sent with each request. |
| `enabled` | bool? | Whether the server is active. Defaults to `true`. |
| `timeout` | int? | Request timeout in seconds. |
| `trust` | bool? | Trust all tools on this server without prompting (Claude Code). |
| `description` | string? | Human-readable description shown in the UI. |
| `when` | string? | AKEL condition. |

### Hook groups (flat format)

All targets that support hooks use the same flat format.
Each item in a hook-event array is simultaneously a **group** (`matcher`, `sequential`, `when`)
and a single **handler** (handler fields at the same level).

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "when":       "${bundle.input.strict}",
        "matcher":    "Bash",
        "sequential": false,
        "type":       "command",
        "command":    ".claude/hooks/block-dangerous.sh"
      }
    ]
  }
}
```

The `type` field selects the handler. When omitted it is inferred from which key field is present:
`command` → `"command"`, `url` → `"http"`, `server` → `"mcp_tool"`, `prompt` → `"prompt"`.

**Group fields:**

| Field | Type | Description |
|---|---|---|
| `matcher` | string? | Regex / `\|`-separated tool names that must match. Absent = match all. |
| `sequential` | bool | Run handlers sequentially (default `false` = parallel). |
| `when` | string? | AKEL condition for the whole group. |
| `type` | string? | Handler type discriminator (see below). |

**Handler: `command`**

| Field | Type | Description |
|---|---|---|
| `command` | string | Shell command or executable path. |
| `args` | string[]? | Extra arguments. |
| `shell` | string? | Shell binary (`"bash"`, `"zsh"`). `null` = agent default. |
| `async` | bool | Run without blocking the agent (default `false`). |
| `asyncRewake` | bool | Re-wake the agent after async completion (default `false`). |
| `timeout` | int? | Max execution time in seconds. |
| `statusMessage` | string? | Message shown in the UI while running. |
| `once` | bool | Fire at most once per session (default `false`). |

**Handler: `http`**

| Field | Type | Description |
|---|---|---|
| `url` | string | Target URL. |
| `headers` | map? | HTTP headers. |
| `allowedEnvVars` | string[]? | Env var names whose values may appear in headers. |
| `timeout` | int? | Request timeout in seconds. |

**Handler: `mcp_tool`** (Claude Code only)

| Field | Type | Description |
|---|---|---|
| `server` | string | MCP server name from `mcpServers`. |
| `tool` | string | Tool name to invoke. |
| `input` | map? | Key-value input passed to the tool. |

**Handler: `prompt`** (Claude Code only)

| Field | Type | Description |
|---|---|---|
| `prompt` | string | Prompt sent to the model (no tools, single turn). |
| `model` | string? | Model override. |

**Handler: `agent`** (Claude Code only)

| Field | Type | Description |
|---|---|---|
| `prompt` | string | Initial prompt for the spawned subagent (has full tool access). |
| `model` | string? | Model override. |

---

## Claude Code (`claude-code/config.json`)

```json
{
  "schemaVersion": 1,
  "agent":         "claude-code",
  "minVersion":    "1.0.0",
  "scope":         "project",

  "settings": {
    "model":                  "claude-sonnet-4-6",
    "effortLevel":            "high",
    "alwaysThinkingEnabled":  false,
    "availableModels":        ["claude-sonnet-4-6", "claude-haiku-4-5"],
    "modelOverrides":         { "claude-opus-4-7": "arn:aws:bedrock:us-east-1:..." },
    "includeCoAuthoredBy":    false,
    "env": {
      "MY_VAR": "value"
    },
    "attribution": {
      "commit": "🤖 Generated with Claude Code\n\nCo-Authored-By: Claude <noreply@anthropic.com>",
      "pr":     "🤖 Generated with Claude Code"
    },
    "permissions": {
      "defaultMode": "acceptEdits",
      "allow": [
        "Read(./**)",
        { "value": "Bash(git push:*)", "when": "${bundle.input.profile} == 'trusted'" }
      ],
      "deny": [
        "Read(./.env)",
        { "value": "Bash(rm -rf:*)", "when": "${bundle.input.strict}" }
      ],
      "ask":                  ["Bash(curl:*)"],
      "additionalDirectories": ["../docs/"]
    }
  },

  "memory":   [ { "name": "CLAUDE.md", "source": "CLAUDE.md" } ],

  "mcpServers": [
    {
      "when":      "${bundle.input.githubMcp}",
      "name":      "github",
      "transport": "stdio",
      "command":   "npx",
      "args":      ["-y", "@modelcontextprotocol/server-github"]
    }
  ],

  "agents": [
    { "when": "'code-reviewer' in ${bundle.input.subagents}",
      "name": "code-reviewer", "source": "subagents/code-reviewer/code-reviewer.md" }
  ],

  "commands": [
    { "when": "'review' in ${bundle.input.skills}",
      "name": "review", "source": "commands/review.md" }
  ],

  "skills": [
    { "when": "'review' in ${bundle.input.skills}",
      "name": "review", "source": "skills/review/" },
    { "when": "'security-review' in ${bundle.input.skills}",
      "name": "security-review", "source": "skills/security-review/" }
  ],

  "hooks": {
    "PreToolUse":    [ { "when": "${bundle.input.strict}", "matcher": "Bash",
                         "command": ".claude/hooks/block-dangerous.sh" } ],
    "PostToolUse":   [ { "matcher": "Write", "type": "http",
                         "url": "https://hooks.example.com/after-write" } ],
    "SessionStart":  [ { "matcher": "*", "command": ".claude/hooks/session-start.sh" } ],
    "SessionEnd":    [ { "matcher": "*", "command": ".claude/hooks/session-end.sh" } ],
    "Stop":          [ { "matcher": "*", "command": ".claude/hooks/on-stop.sh" } ],
    "SubagentStop":  [ { "matcher": "*", "command": ".claude/hooks/on-subagent-stop.sh" } ],
    "PreCompact":    [ { "matcher": "*", "command": ".claude/hooks/pre-compact.sh" } ],
    "UserPromptSubmit": [ { "matcher": "*", "type": "prompt",
                            "prompt": "Check the user prompt for policy violations." } ]
  }
}
```

### Settings fields

| Field | Type | Description |
|---|---|---|
| `model` | string? | Default model (e.g. `"claude-sonnet-4-6"`). |
| `effortLevel` | `"xhigh"` \| `"high"` \| `"medium"` \| `"low"`? | Reasoning effort. |
| `alwaysThinkingEnabled` | bool? | Enable extended thinking for every request. |
| `availableModels` | string[]? | Override the model selector list shown in the UI. |
| `modelOverrides` | map? | Map model ID to a Bedrock ARN or alias. |
| `includeCoAuthoredBy` | bool? | Append `Co-authored-by:` to commits. |
| `env` | map? | Environment variables injected into every tool invocation. |
| `attribution.commit` | string? | Custom commit message footer. |
| `attribution.pr` | string? | Custom PR description footer. |
| `permissions.defaultMode` | `"default"` \| `"plan"` \| `"acceptEdits"` \| `"bypassPermissions"`? | Fallback permission mode. |
| `permissions.allow` | (string \| rule)[]? | Allowlist. |
| `permissions.deny` | (string \| rule)[]? | Denylist. |
| `permissions.ask` | (string \| rule)[]? | Ask-before-run list. |
| `permissions.additionalDirectories` | string[]? | Extra readable/writable paths. |

### Hook events

| Event | Fires when |
|---|---|
| `PreToolUse` | Before a tool dispatches. Handlers may block or rewrite. |
| `PostToolUse` | After a tool completes successfully. |
| `UserPromptSubmit` | When the user sends a prompt. |
| `Notification` | When a UI notification fires. |
| `Stop` | When the main agent loop stops. |
| `SubagentStop` | When a subagent stops. |
| `PreCompact` | Before context compaction runs. |
| `SessionStart` | When a new session begins. |
| `SessionEnd` | When a session ends. |

---

## OpenCode (`opencode/config.json`)

```json
{
  "schemaVersion": 1,
  "minVersion":    "0.1.0",

  "model":         "anthropic/claude-sonnet-4-6",
  "small_model":   "anthropic/claude-haiku-4-5",
  "default_agent": "coder",
  "shell":         "/bin/bash",
  "share":         "manual",
  "snapshot":      false,
  "instructions":  ["AGENTS.md", ".cursor/rules/style.md"],

  "provider": {
    "anthropic": {
      "options": {
        "apiKey":   "{env:ANTHROPIC_API_KEY}",
        "baseURL":  "https://api.anthropic.com"
      }
    }
  },

  "tools": {
    "Bash":        true,
    "WebFetch":    false
  },

  "permission": {
    "bash":    "ask",
    "read":    "allow",
    "edit":    "allow",
    "webfetch": "deny"
  },

  "agent": {
    "coder": {
      "model":       "anthropic/claude-opus-4-7",
      "mode":        "primary",
      "description": "Primary coding agent",
      "prompt":      "You are an expert software engineer.",
      "tools":       { "WebFetch": false },
      "steps":       50
    },
    "reviewer": {
      "mode":        "subagent",
      "description": "Code review agent",
      "model":       "anthropic/claude-sonnet-4-6"
    }
  },

  "compaction": {
    "auto":                  true,
    "prune":                 true,
    "tail_turns":            10,
    "preserve_recent_tokens": 4096,
    "reserved":              4096
  },

  "mcp": {
    "github": {
      "type":    "local",
      "command": ["npx", "-y", "@modelcontextprotocol/server-github"],
      "environment": { "GITHUB_TOKEN": "{env:GITHUB_TOKEN}" },
      "enabled": true,
      "timeout": 30
    },
    "remote-svc": {
      "type":    "remote",
      "url":     "https://mcp.example.com",
      "headers": { "Authorization": "Bearer {env:MCP_TOKEN}" }
    }
  },

  "formatter": {
    "prettier": {
      "command":    ["npx", "prettier", "--write"],
      "extensions": [".ts", ".tsx", ".js", ".json"]
    }
  },

  "lsp": {
    "typescript": {
      "command":    ["typescript-language-server", "--stdio"],
      "extensions": [".ts", ".tsx"]
    }
  },

  "plugin": ["./plugins/my-plugin.js"]
}
```

### Top-level fields

| Field | Type | Description |
|---|---|---|
| `model` | string? | Primary model (`"provider/model-id"` format). |
| `small_model` | string? | Lighter model for cheap sub-tasks. |
| `default_agent` | string? | Key in `agent` to use by default. |
| `shell` | string? | Shell binary for Bash tool (`"/bin/bash"`). |
| `share` | `"manual"` \| `"auto"` \| `"disabled"`? | Session-sharing mode. |
| `snapshot` | bool? | Enable session snapshots for recovery. |
| `instructions` | string[]? | Paths to instruction files appended to system prompt. |
| `provider` | map? | Provider-specific connection options (keyed by provider name). |
| `tools` | map? | Per-tool enable/disable (`true` / `false`). |
| `permission` | map? | Per-tool permission level: `"allow"` \| `"ask"` \| `"deny"`. |
| `agent` | map? | Named agent definitions (keyed by agent name). |
| `compaction` | object? | Context-compaction settings. |
| `mcp` | map? | MCP server definitions (keyed by server name). |
| `formatter` | map? | Code formatters (keyed by formatter name). |
| `lsp` | map? | LSP servers (keyed by server name). |
| `plugin` | string[]? | Paths to JS/TS plugin files. |

### Agent fields

| Field | Type | Description |
|---|---|---|
| `model` | string? | Model override for this agent. |
| `mode` | `"primary"` \| `"subagent"` \| `"all"`? | Role of the agent. |
| `description` | string? | Human-readable description. |
| `prompt` | string? | System prompt prepended to every session. |
| `tools` | map? | Per-tool enable/disable overrides (merged with global `tools`). |
| `steps` | int? | Max agentic iterations before stopping. |

### MCP server fields (OpenCode format)

OpenCode uses a keyed map with a `type` discriminator:

**Local** (`type: "local"`):

| Field | Type | Description |
|---|---|---|
| `command` | string[] | Full command array (includes executable + args). |
| `environment` | map? | Environment variables. |
| `enabled` | bool? | Active (default `true`). |
| `timeout` | int? | Timeout in seconds. |

**Remote** (`type: "remote"`):

| Field | Type | Description |
|---|---|---|
| `url` | string | Endpoint URL. |
| `headers` | map? | HTTP headers. |
| `enabled` | bool? | Active (default `true`). |
| `timeout` | int? | Timeout in seconds. |

---

## Qwen Code (`qwen-code/config.json`)

```json
{
  "schemaVersion": 1,
  "minVersion":    "1.0.0",

  "model": {
    "name":           "qwen-max",
    "maxSessionTurns": 100,
    "generationConfig": {
      "timeout":           120000,
      "maxRetries":        3,
      "contextWindowSize": 128000,
      "reasoning":         "medium",
      "samplingParams": {
        "temperature": 0.7,
        "topP":        0.9,
        "maxTokens":   8192
      }
    },
    "chatCompression": { "contextPercentageThreshold": 0.85 },
    "skipNextSpeakerCheck": false,
    "skipLoopDetection":    false,
    "skipStartupContext":   false
  },

  "modelProviders": [
    {
      "authType": "dashscope",
      "id":       "qwen-max",
      "name":     "Qwen Max",
      "envKey":   "DASHSCOPE_API_KEY",
      "baseUrl":  "https://dashscope.aliyuncs.com/compatible-mode/v1"
    },
    {
      "authType": "anthropic",
      "id":       "claude-sonnet-4-6",
      "envKey":   "ANTHROPIC_API_KEY"
    }
  ],

  "mcpServers": [
    {
      "name":      "github",
      "transport": "stdio",
      "command":   "npx",
      "args":      ["-y", "@modelcontextprotocol/server-github"],
      "when":      "${bundle.input.githubMcp}"
    }
  ],

  "permissions": {
    "allow": ["Read(./**)", "Bash(git status)"],
    "deny":  [{ "value": "Bash(rm -rf:*)", "when": "${bundle.input.strict}" }],
    "ask":   ["Bash(curl:*)"]
  },

  "tools": {
    "approvalMode":                "auto_edit",
    "sandbox":                     false,
    "sandboxImage":                "ubuntu:22.04",
    "useRipgrep":                  true,
    "truncateToolOutputThreshold": 50000,
    "truncateToolOutputLines":     500
  },

  "general": {
    "vimMode":         false,
    "enableAutoUpdate": true,
    "gitCoAuthor": { "commit": true, "pr": false }
  },

  "context": {
    "fileName":     "QWEN.md",
    "fileFiltering": {
      "respectGitIgnore":          true,
      "respectQwenIgnore":         true,
      "enableRecursiveFileSearch": true,
      "enableFuzzySearch":         false
    }
  },

  "telemetry": {
    "enabled":      false,
    "target":       "local",
    "otlpEndpoint": "http://localhost:4317",
    "otlpProtocol": "grpc",
    "logPrompts":   false
  },

  "hooks": {
    "PreToolUse":  [ { "when": "${bundle.input.strict}", "matcher": "Bash",
                       "command": ".qwen/hooks/block-dangerous.sh" } ],
    "SessionStart": [ { "matcher": "*", "command": ".qwen/hooks/session-start.sh" } ],
    "SessionEnd":   [ { "matcher": "*", "command": ".qwen/hooks/session-end.sh" } ],
    "Stop":         [ { "matcher": "*", "command": ".qwen/hooks/on-stop.sh" } ]
  }
}
```

### Top-level fields

| Field | Type | Description |
|---|---|---|
| `model` | object? | Active model and generation settings. |
| `modelProviders` | object[]? | Provider definitions; active provider matched by `model.name`. |
| `mcpServers` | object[]? | MCP server list (same format as Claude Code). |
| `permissions` | object? | Tool allow / deny / ask rules. |
| `tools` | object? | Tool execution and sandbox configuration. |
| `general` | object? | General behaviour (vim mode, auto-update, co-author). |
| `context` | object? | Instruction-file name and file-tree filtering. |
| `telemetry` | object? | OTLP telemetry export settings. |
| `hooks` | map? | Lifecycle hooks. |

### `model` fields

| Field | Type | Description |
|---|---|---|
| `name` | string | Model identifier matching a provider's `id`. |
| `maxSessionTurns` | int? | Force-compact after this many turns (`-1` = unlimited). |
| `generationConfig.timeout` | int? | Request timeout in ms. |
| `generationConfig.maxRetries` | int? | Retries on transient errors. |
| `generationConfig.contextWindowSize` | int? | Max context in tokens. |
| `generationConfig.reasoning` | `"low"` \| `"medium"` \| `"high"` \| `"none"`? | Reasoning effort. |
| `generationConfig.samplingParams` | object? | `temperature`, `topP`, `maxTokens`. |
| `chatCompression.contextPercentageThreshold` | double? | Compaction trigger (0.0–1.0). |

### `modelProviders[]` fields

| Field | Type | Description |
|---|---|---|
| `authType` | `"openai"` \| `"anthropic"` \| `"gemini"` \| `"dashscope"` | Auth and API protocol. |
| `id` | string | Model identifier (must match `model.name` to be active). |
| `name` | string? | Display name. |
| `envKey` | string? | Environment variable holding the API key. |
| `baseUrl` | string? | API base URL. |
| `generationConfig` | object? | Per-provider generation overrides. |

### `tools` fields

| Field | Type | Description |
|---|---|---|
| `approvalMode` | `"default"` \| `"auto_edit"` \| `"yolo"`? | Global tool approval policy. |
| `sandbox` | bool? | Run Bash inside an isolated container. |
| `sandboxImage` | string? | Container image (`"ubuntu:22.04"`). |
| `useRipgrep` | bool? | Use `ripgrep` for file search. |
| `truncateToolOutputThreshold` | int? | Max chars of tool output in context. |
| `truncateToolOutputLines` | int? | Max lines of tool output in context. |

### Hook events

| Event | Fires when |
|---|---|
| `PreToolUse` | Before a tool dispatches. |
| `PostToolUse` | After a tool completes. |
| `UserPromptSubmit` | When the user sends a prompt. |
| `Notification` | When a UI notification fires. |
| `Stop` | When the main agent loop stops. |
| `SessionStart` | When a new session begins. |
| `SessionEnd` | When a session ends. |

Qwen Code supports `command` and `http` handler types only.

---

## DTO / domain coverage

| Feature | Claude Code | OpenCode | Qwen Code |
|---|---|---|---|
| Basic settings (model, env, permissions) | ✅ | ✅ | ✅ |
| `effortLevel`, `alwaysThinkingEnabled` | ⚠️ DTO missing | — | — |
| `attribution` | ⚠️ DTO missing | — | — |
| `modelOverrides`, `availableModels` | ⚠️ DTO missing | — | — |
| MCP servers (stdio + SSE) | ✅ | ✅ | ✅ |
| File refs (agents, commands, skills, memory) | ✅ parsed, gen pending | — | — |
| Hooks — command handler | ✅ | — | ✅ |
| Hooks — http handler | ✅ | — | ✅ |
| Hooks — mcp_tool / prompt / agent handlers | ✅ | — | — |
| `formatter` / `lsp` | — | ⚠️ DTO missing | — |
| OpenCode `mcp` (typed local/remote) | — | ⚠️ partial | — |
| OpenCode agent `mode`, `steps` | — | ⚠️ DTO missing | — |
| OpenCode `compaction` `tail_turns` | — | ⚠️ DTO missing | — |
| Qwen Code hooks (subset) | — | — | ✅ |

Legend: ✅ fully supported · ⚠️ gap documented below · — not applicable
