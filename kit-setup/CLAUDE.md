# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

`kit-setup/` is a **Kotlin Native CLI** (Kotlin Multiplatform, targets: `linuxX64`, `macosX64`, `macosArm64`, `mingwX64`). It generates configuration files for AI agents (Claude Code, OpenCode) from **bundles** — packages of templates and a `bundle.json` manifest.

Current version is stored in [`gradle.properties`](gradle.properties) (`version=…`).

## Commands

All commands run from `kit-setup/`.

```bash
# Build all targets
./gradlew build

# Run tests (commonTest, runs on the host platform)
./gradlew :modules:core:allTests

# Run a single test class
./gradlew :modules:core:allTests --tests "io.aequicor.aikit.core.AgentTest"

# Build native binary for the host platform (macOS arm64 → macosArm64)
./gradlew :modules:core:linkDebugExecutableMacosArm64
```

There is no linter configured. Code style is enforced by `kotlin.code.style=official` in `gradle.properties`.

## Architecture

### Convention plugins (`build-logic/`)

Two precompiled script plugins in [`build-logic/src/main/kotlin/`](build-logic/src/main/kotlin/):

- `ai-kit.kotlin-module` — applies `kotlin("multiplatform")`, sets all four native targets, adds `kotlin-test` to `commonTest`.
- `ai-kit.kotlin-serialization` — extends the above with `kotlin("plugin.serialization")`.

Every Gradle module applies one of these two plugins; no module configures the Kotlin multiplatform extension directly.

### Module: `modules/core`

Package root: `io.aequicor.aikit.core`

**Domain model** (pure data, no I/O):

| File | What it holds |
|---|---|
| [`Values.kt`](modules/core/src/commonMain/kotlin/io/aequicor/aikit/core/Values.kt) | Inline value classes: `AgentId`, `TemplateId`, `BundleName`, `Version`, `SchemaVersion` |
| [`TemplateKind.kt`](modules/core/src/commonMain/kotlin/io/aequicor/aikit/core/TemplateKind.kt) | Enum: `MEMORY`, `SLASH_COMMAND`, `SUBAGENT`, `HOOK`, `MCP_CONFIG`, `SETTINGS` |
| [`Agent.kt`](modules/core/src/commonMain/kotlin/io/aequicor/aikit/core/Agent.kt) | Sealed class with `ClaudeCode` and `OpenCode` singletons; each declares its supported `TemplateKind`s |
| [`Bundle.kt`](modules/core/src/commonMain/kotlin/io/aequicor/aikit/core/Bundle.kt) | `Template`, `AgentBundle`, `Bundle` — the fully resolved in-memory representation of a bundle |
| [`Errors.kt`](modules/core/src/commonMain/kotlin/io/aequicor/aikit/core/Errors.kt) | `AiKitError` sealed interface: `ParseError`, `ValidationError`, `ResolverError`, `GenerationError`, `UnknownAgent` |

**Ports (interfaces for future implementations)**:

- [`api/Interfaces.kt`](modules/core/src/commonMain/kotlin/io/aequicor/aikit/core/api/Interfaces.kt) — `ManifestParser`, `BundleResolver`, `Validator`, `AgentLayout`, `Generator`
- [`io/BundleSource.kt`](modules/core/src/commonMain/kotlin/io/aequicor/aikit/core/io/BundleSource.kt) — `BundleSource` (reads raw bytes from a directory or zip)

All interfaces return `Result<T>` — never throw. The domain layer has zero platform dependencies.

### Bundle format (source of truth in `*.md` specs)

| File | Describes |
|---|---|
| [`BUNDLE_JSON.md`](BUNDLE_JSON.md) | `bundle.json` — bundle manifest: metadata, supported agents, `inputs` schema |
| [`CONFIG_JSON.md`](CONFIG_JSON.md) | `config.json` per agent — settings, MCP servers, hooks, etc.; AKEL expression language for `when` conditions |
| [`TEMPLATE_MD.md`](TEMPLATE_MD.md) | `.md` template files — `${bundle.input.<id>}` substitution and `<!-- when: … -->…<!-- end -->` conditional blocks |
| [`MANIFEST_JSON.md`](MANIFEST_JSON.md) | `.aikit/manifest.json` — user-project manifest referencing bundles and their `inputs` |

### Templates (`templates/`)

Ready-made template files shipped inside the CLI binary, one folder per agent:

- `templates/claude-code/` — `CLAUDE.md`, `commands/review.md`
- `templates/opencode/` — `AGENTS.md`

## Code rules

- **SOLID + Clean Architecture**: domain model in `core`, I/O behind interfaces (`BundleSource`, `Generator`, …). Implementations live outside the core module.
- No magic numbers — use named constants or value classes.
- Never use `Dispatchers.*` directly; inject a dispatcher or use platform-agnostic coroutine primitives.
- KDoc on all public classes, interfaces, methods, and properties.
