# AI-Kit v2

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Docs](https://img.shields.io/badge/docs-aequicor.github.io-informational)](https://aequicor.github.io/ai-kit-v2)

`kit-setup` is a Kotlin Native CLI that generates configuration files for AI coding targets (Claude Code, OpenCode, QwenCode) from reusable template packages called **bundles**.

How it works:

1. A bundle contains templates and declares what inputs a project needs to supply.
2. You write `.aikit/manifest.json` specifying which bundle to apply with which inputs.
3. `kit-setup generate` renders templates, evaluates conditionals, and writes native config files into the project.

Every file written by `kit-setup` is tracked in `.aikit/manifest.lock.json` by SHA-256 hash. If you edit a generated file, subsequent updates skip it unless you pass `--force`.

## Installation

Run the prompts below from inside the project with an AI coding target that has tool use (Claude Code, Cursor, Codex, etc.).

**Install:**

```
Read prompts/install.md from https://github.com/aequicor/ai-kit-v2 and follow
the installation instructions for this project.
```

**Update:**

```
Read prompts/update.md from https://github.com/aequicor/ai-kit-v2 and follow
the update instructions for this project.
```

**Uninstall:**

```
Read prompts/uninstall.md from https://github.com/aequicor/ai-kit-v2 and follow
the uninstall instructions for this project.
```

**Manual installation:** download a binary for your platform from [GitHub Releases](https://github.com/aequicor/ai-kit-v2/releases), make it executable (`chmod +x kit-setup`), and put it on `PATH`. Local build: `cd kit-setup && ./gradlew :modules:cli:linkReleaseExecutableMacosArm64`.

## Concepts

| Term | Description |
|------|-------------|
| Bundle | A zip or directory with `bundle.json`, a `config.json` per target, and template files |
| Project manifest | `.aikit/manifest.json` — which bundles to apply with which inputs |
| Lock file | `.aikit/manifest.lock.json` — SHA-256 registry of every generated file |
| Drift | A generated file edited by the user; skipped on update without `--force` |
| AKEL | Expression language used in `when` conditions in templates and `config.json` |

## Bundle manifest (`bundle.json`)

Lives at the root of every bundle. Describes the bundle and declares the inputs users configure at install time.

```json
{
  "schemaVersion": 1,
  "name": "simple-kit",
  "version": "0.0.1",
  "description": "Minimal starter: CLAUDE.md, skills, subagents, hooks",
  "author": "AI-Kit",
  "license": "MIT",
  "targets": ["claude-code"],
  "inputs": [
    {
      "id": "projectName",
      "type": "string",
      "title": "Project name",
      "default": "${cwd.basename}"
    },
    {
      "id": "githubMcp",
      "type": "boolean",
      "title": "Enable GitHub MCP?",
      "default": false
    },
    {
      "id": "skills",
      "type": "multiselect",
      "title": "Skills to install",
      "options": ["review", "security-review"],
      "default": ["review"]
    }
  ]
}
```

Input types: `boolean`, `select`, `multiselect`, `string`, `int`, `double`.

See [`kit-setup/BUNDLE_JSON.md`](kit-setup/BUNDLE_JSON.md) for the full specification.

## Project manifest (`.aikit/manifest.json`)

```json
{
  "aikitVersion": "0.0.11",
  "applications": [
    {
      "id": "root",
      "path": ".",
      "targets": {
        "claude": {
          "bundle": "simple-kit@0.0.1",
          "source": "internal",
          "inputs": {
            "projectName": "my-app",
            "githubMcp": true,
            "skills": ["review", "security-review"]
          }
        }
      }
    }
  ]
}
```

- `aikitVersion` — CLI version that wrote the manifest; used for compatibility diagnostics.
- `applications` — list of subprojects, each with a `path` and per-target bundle configuration.
- `source` — `"internal"` for built-in bundles; a path to a `.zip` or directory for third-party bundles.

A project with multiple subprojects adds more entries to `applications`. Each entry may target different agents independently.

See [`kit-setup/MANIFEST_JSON.md`](kit-setup/MANIFEST_JSON.md).

## Templates

Bundle templates are `.md` files and `config.json` entries that support two control structures evaluated before files are written to disk.

### Value substitution

Works anywhere in `.md` files and in any `config.json` string value:

```
${bundle.input.<id>}
```

Serialization by type: `boolean` → `true`/`false`, `multiselect` → comma-separated list, others → as-is.

### Conditional blocks in `.md` files

```md
<!-- when: ${bundle.input.githubMcp} -->
## GitHub integration

Configure the GitHub MCP server in `.mcp.json`.
<!-- end -->
```

Nesting is supported. Content between markers is removed when the condition is false; the comment markers themselves leave no trace in the output.

### Conditional blocks in `config.json`

Any object in `config.json` can carry a `when` field:

```json
{
  "when": "${bundle.input.githubMcp}",
  "name": "github",
  "command": "npx",
  "args": ["@modelcontextprotocol/server-github"]
}
```

The entire object is excluded when the expression evaluates to `false`.

### AKEL expression language

Used only inside `when` conditions.

| Construct | Examples |
|-----------|---------|
| Literals | `true`, `false`, `42`, `3.14`, `'text'`, `['a', 'b']` |
| Input reference | `${bundle.input.skills}` |
| Equality | `==`, `!=` |
| Comparison | `<`, `<=`, `>`, `>=` |
| Membership | `'review' in ${bundle.input.skills}` |
| Logic | `&&`, `\|\|`, `!` |
| Grouping | `(...)` |

Strict typing — no implicit coercion. See [`kit-setup/CONFIG_JSON.md`](kit-setup/CONFIG_JSON.md) and [`kit-setup/TEMPLATE_MD.md`](kit-setup/TEMPLATE_MD.md).

## Output paths

`kit-setup` maps each file kind to the target's native location. For Claude Code:

| Kind | Output path |
|------|-------------|
| Memory (`CLAUDE.md`) | `<app-path>/CLAUDE.md` |
| Subagent | `.claude/agents/<name>.md` |
| Command | `.claude/commands/<name>.md` |
| Skill | `.claude/skills/<name>/<file>` |
| Settings | `.claude/settings.json` |
| MCP config | `.mcp.json` |
| Hook script | path declared in `config.json` (relative to app root) |

## CLI reference

All commands operate relative to the current working directory.

### `kit-setup schema manifest`

Prints the JSON Schema for `.aikit/manifest.json`. Useful for IDE validation and autocomplete:

```bash
kit-setup schema manifest > .aikit/manifest.schema.json
```

### `kit-setup schema bundle [REF]`

Prints the JSON Schema for a bundle's `inputs` block.

```bash
kit-setup schema bundle --list                          # list built-in bundles
kit-setup schema bundle embedded:simple-kit@0.0.1      # schema for a built-in bundle
kit-setup schema bundle ./path/to/bundle               # schema for a local bundle
kit-setup schema bundle ./bundle.zip                   # schema for a zip bundle
```

Options:
- `--list` — list available built-in bundles instead of printing a schema.
- `--base-dir <DIR>` — base directory for resolving relative paths in `REF` (default: `.`).

`REF` forms: directory path, `.zip` path, `zip:<path>`, `embedded:<name>[@<version>]`.

The resulting schema (JSON Schema draft 2020-12) can validate the `inputs` block of any `.aikit/manifest.json` target entry.

### `kit-setup verify <MANIFEST>`

Validates the project manifest and all referenced bundles. Writes nothing. Exits non-zero on error.

```bash
kit-setup verify .aikit/manifest.json
```

### `kit-setup generate <MANIFEST>`

Renders templates and writes config files to the project. Saves `.aikit/manifest.lock.json` on success.

```bash
kit-setup generate .aikit/manifest.json
kit-setup generate .aikit/manifest.json --dry-run   # preview without writing
kit-setup generate .aikit/manifest.json --force     # overwrite drifted files
```

Files previously generated but no longer produced by the bundle (e.g. after removing an input option) are deleted, unless they have drifted.

### `kit-setup update [<MANIFEST>]`

Re-renders from the current manifest and reports a diff against the lock file. Equivalent to `generate` with explicit change reporting.

```bash
kit-setup update .aikit/manifest.json --dry-run
kit-setup update .aikit/manifest.json
kit-setup update .aikit/manifest.json --force
```

Default `MANIFEST` path: `.aikit/manifest.json`.

### `kit-setup update self [--check]`

Prints the current CLI version and the platform-specific command to install the latest release (curl on Linux/macOS, PowerShell on Windows). `--check` limits output to the current version and a link to the latest release; it downloads nothing.

### `kit-setup remove [<MANIFEST>]`

Removes all files tracked in the lock file.

```bash
kit-setup remove --dry-run          # show what would be deleted
kit-setup remove                    # delete tracked files, manifest, and lock
kit-setup remove --keep-manifest    # delete generated files; keep manifest and lock
kit-setup remove --force            # also delete drifted files
```

Default `MANIFEST` path: `.aikit/manifest.json`.

### `kit-setup --version` / `kit-setup --help`

Version and built-in help. Per-command help: `kit-setup <cmd> --help`.

## Drift and `--force`

Before overwriting a file on `generate`, `update`, or `remove`, `kit-setup` compares its current content against the SHA-256 hash recorded in the lock file. If they differ, the file has *drifted* — you edited it — and the operation skips it with a warning.

Pass `--force` to override drift protection and overwrite or delete drifted files.

See [`kit-setup/MANIFEST_LOCK_JSON.md`](kit-setup/MANIFEST_LOCK_JSON.md) for the lock file schema.

## License

Apache 2.0
