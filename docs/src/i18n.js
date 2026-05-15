// ---------------------------------------------------------------------------
// All translations. Each page has its own key.
// ---------------------------------------------------------------------------

export const T = {
  en: {
    nav: {
      home: 'What is it?',
      start: 'Get started',
      features: 'Features',
      cli: 'CLI',
      bundles: 'Bundles',
      claude: 'Claude Code',
      architecture: 'Architecture',
    },
    common: {
      copy: 'Copy',
      copied: 'Copied',
      copyFull: 'Copy',
      copyError: "Couldn't copy — select and copy manually",
      github: 'GitHub',
      releases: 'Releases',
    },
    footer: {
      repo: 'Repository',
      releases: 'Releases',
      license: 'Apache 2.0',
    },
    home: {
      hero: {
        suffix: ' · Apache 2.0',
        title: 'AI Kit',
        lead: 'A safe installer, updater, and uninstaller for AI-coding-agent configs — driven by a single manifest and reusable bundles. One command sets the project up, another updates it, a third removes it cleanly.',
        ctaStart: 'Get started',
        ctaGithub: 'Open on GitHub',
      },
      problem: {
        eyebrow: 'Why this exists',
        title: 'What goes wrong without it',
        items: [
          {
            icon: '🧩',
            title: 'Configs scattered everywhere',
            body: 'CLAUDE.md, skills, sub-agents, hooks, MCP servers — every agent expects a different layout. Doing it by hand is tedious and easy to get wrong.',
          },
          {
            icon: '👥',
            title: 'Drift between teammates',
            body: 'Without a single source of truth, every developer ends up with a slightly different setup. Rules diverge, skills go stale.',
          },
          {
            icon: '💥',
            title: 'Hand edits get blown away',
            body: 'Regenerating configs with a script usually means overwriting manual fixes. Nobody trusts a re-run.',
          },
          {
            icon: '🧹',
            title: 'No clean uninstall',
            body: 'Trying a new preset means hunting down stale files by hand. Half-removed configs poison the next attempt.',
          },
        ],
      },
      solution: {
        eyebrow: 'What AI Kit does',
        title: 'One manifest, three commands',
        lead: 'A native CLI binary that reads .aikit/manifest.json and renders agent configs from reusable bundles. Install, update, remove — that\'s it.',
        items: [
          {
            icon: '📐',
            title: 'Config as code',
            body: 'A single .aikit/manifest.json describes every application in your repo, the target agents, and the chosen inputs. Edit the manifest, not the generated files.',
          },
          {
            icon: '♻️',
            title: 'Reproducible',
            body: 'Same manifest → same output for everyone on the team. Update or roll back with one command. No drift.',
          },
          {
            icon: '🔒',
            title: 'Safe by default',
            body: 'Writes only to .aikit/ and the target agent directories. A SHA256 lock file protects your manual edits — nothing gets overwritten without --force.',
          },
        ],
      },
      why: {
        eyebrow: 'Why it beats hand-rolling',
        title: 'Four guarantees you don\'t get from copy-paste',
        items: [
          {
            title: 'SHA256 lock file',
            body: 'Every generated file is recorded with its SHA256 hash in .aikit/manifest.lock.json. The CLI knows exactly what it owns.',
          },
          {
            title: 'Drift detection',
            body: 'If you hand-edit a generated file, the next generate skips it instead of clobbering your changes. You opt in with --force.',
          },
          {
            title: 'Clean uninstall',
            body: 'remove deletes only what\'s in the lock file. User-created files stay untouched. No half-removed configs.',
          },
          {
            title: 'Anti-prompt-injection installer',
            body: 'The installer prompt has hard invariants: never runs git, never writes outside .aikit/ and agent dirs, verifies the binary via SHA256 before running it.',
          },
        ],
      },
      runners: {
        eyebrow: 'Supported agent',
        title: 'Claude Code today',
        lead: 'Bundles currently ship configs for Claude Code. The architecture is target-agnostic — more agents land as bundle targets stabilize.',
        items: [
          { id: 'claude', name: 'Claude Code', note: 'Supported' },
        ],
      },
      cta: {
        title: 'Try it on your repo',
        body: 'Paste one prompt into your agent, or download the binary and run kit-setup generate. Either way, less than a minute.',
        button: 'Get started',
      },
    },
    start: {
      hero: {
        eyebrow: 'Install · Update · Remove',
        title: 'Three commands',
        lead: 'AI Kit is a single native binary (kit-setup) plus one JSON manifest. Drop the binary in your PATH, write a minimal manifest, then install, update, or remove.',
      },
      prompt: {
        title: 'Easiest path: ask your agent',
        subtitle: 'Paste this into any AI coding agent with tool use, opened in your project:',
        body: `Read prompts/install.md from https://github.com/aequicor/ai-kit-v2 and follow
the installation instructions for this project.`,
      },
      install: {
        eyebrow: 'Install',
        title: 'kit-setup generate',
        lead: 'Renders the configs declared in .aikit/manifest.json into the locations the target agent expects. Writes only inside .aikit/ and the agent\'s own directories.',
        manifestLabel: 'Minimal .aikit/manifest.json',
        manifest: `{
  "aikitVersion": "0.1.4",
  "applications": [
    {
      "id": "root",
      "path": ".",
      "targets": {
        "claude-code": {
          "bundle": "simple-kit@0.0.1",
          "source": "internal",
          "inputs": {
            "projectName": "my-app",
            "skills": [],
            "subagents": [],
            "githubMcp": false,
            "strict": true
          }
        }
      }
    }
  ]
}`,
        runLabel: 'Run',
        run: 'kit-setup generate',
        notes: [
          'Idempotent: re-running with the same manifest is a no-op.',
          'Drift-safe: hand-edited files are preserved unless you pass --force.',
          '--dry-run shows the plan without touching the filesystem.',
        ],
      },
      update: {
        eyebrow: 'Update',
        title: 'kit-setup update',
        lead: 'Refresh the install from the current manifest — picks up bundle/version changes and re-applies inputs. Use --dry-run first to see the diff.',
        sub: [
          { cmd: 'kit-setup update', body: 'Re-render from the current manifest.' },
          { cmd: 'kit-setup update --dry-run', body: 'Show what would change without writing.' },
          { cmd: 'kit-setup update self --check', body: 'Show the current CLI version and the latest release link.' },
        ],
      },
      remove: {
        eyebrow: 'Remove',
        title: 'kit-setup remove',
        lead: 'Uninstalls everything AI Kit added. Only files recorded in .aikit/manifest.lock.json are deleted — nothing else.',
        sub: [
          { cmd: 'kit-setup remove', body: 'Delete every tracked file plus the manifest and lock.' },
          { cmd: 'kit-setup remove --keep-manifest', body: 'Delete generated files but keep .aikit/manifest.json and the lock.' },
          { cmd: 'kit-setup remove --dry-run', body: 'Show what would be removed without touching the filesystem.' },
          { cmd: 'kit-setup remove --force', body: 'Also delete files you hand-edited (drift); without --force, drifted files stay.' },
        ],
      },
      install_binary: {
        title: 'Get the binary',
        body: 'One native executable per platform — no JVM required. Download from the releases page; the filename is kit-setup-<linux|macos|windows.exe>.',
        link: 'Latest release →',
      },
    },
    features: {
      hero: {
        eyebrow: 'Features',
        title: 'What you get',
        lead: 'Every item below is wired in the current binary. Each card maps to a real piece of code — no roadmap items, no maybes.',
      },
      sections: [
        {
          group: 'Manifest & inputs',
          items: [
            { title: 'Manifest as source of truth', body: '.aikit/manifest.json describes every application in the repo: its id, path, target agents, and the input values for each bundle. Edit the manifest, not the generated files.' },
            { title: 'Multiple manifests / modes', body: 'Pick the manifest via --manifest, --mode <name>, env AIKIT_MANIFEST or AIKIT_MODE, .aikit/local.properties, or the default .aikit/manifest.json. Resolved in that order.' },
            { title: 'Monorepo-friendly', body: 'The applications array lets you describe backend / frontend / infra in one manifest and generate a distinct configuration for each path.' },
            { title: 'Bundle inputs', body: 'Bundles declare bool / select / multiselect / string / int / double inputs. Inputs gate which files end up in the project and are substituted into templates via ${bundle.input.<id>}.' },
            { title: 'Conditional generation (akel)', body: 'Tiny expression language used in config.json when-clauses and templates — flip files or sections on/off depending on input values.' },
          ],
        },
        {
          group: 'Bundles',
          items: [
            { title: 'Internal registry + external sources', body: 'source: "internal" picks bundles compiled into the binary; a relative path points at your own .zip or folder. Reference format is name@version.' },
            { title: 'JSON Schema export', body: 'kit-setup schema manifest and kit-setup schema bundle <ref> emit valid JSON Schema for IDE autocomplete and validation.' },
            { title: 'Bundle catalog', body: 'kit-setup schema bundle --list prints every bundle compiled into the binary, with its version.' },
          ],
        },
        {
          group: 'Safety',
          items: [
            { title: 'Idempotent generate', body: 'A SHA256 hash of every generated file is recorded in .aikit/manifest.lock.json. Re-running generate touches nothing if content is identical.' },
            { title: 'Drift detection', body: 'Hand-edit a generated file and the next generate leaves it alone. Override with --force only when you mean it.' },
            { title: 'Dry-run everywhere', body: 'generate, update, and remove all accept --dry-run — print the plan, write nothing.' },
            { title: 'Safe wipe on bundle change', body: 'When you switch bundle or version, old files are deleted only after the new plan builds successfully. A broken new manifest never destroys a working install.' },
            { title: 'Clean uninstall', body: 'remove deletes only files in the lock. User-created files are never touched. --keep-manifest preserves the manifest; --force overrides drift protection.' },
            { title: 'Verify without writing', body: 'kit-setup verify (or verify --all) validates one or every manifest in .aikit/ — no rendering, no writes.' },
            { title: 'Anti-prompt-injection installer', body: 'The installer prompt has hard invariants: never runs git, never writes YAML, verifies the binary via SHA256 before executing, and only writes to .aikit/ and target agent directories.' },
          ],
        },
        {
          group: 'Distribution',
          items: [
            { title: 'Native binary, no JVM', body: 'Built with Kotlin Multiplatform for linuxX64, macosArm64, mingwX64. Drop one file in your PATH and you\'re done.' },
            { title: 'Self-update check', body: 'kit-setup update self --check reports the current version and a link to the latest release.' },
          ],
        },
      ],
    },
    cli: {
      hero: {
        eyebrow: 'CLI reference',
        title: 'kit-setup',
        lead: 'A handful of subcommands. Every operation that writes accepts --dry-run, every manifest-aware command accepts --manifest and --mode.',
      },
      resolution: {
        title: 'Manifest resolution',
        lead: 'The manifest file is picked in this order — first match wins:',
        steps: [
          '--manifest <path> (explicit)',
          '--mode <name> → .aikit/manifest.<name>.json',
          'env AIKIT_MANIFEST',
          'env AIKIT_MODE',
          '.aikit/local.properties → manifest= field',
          '.aikit/manifest.json (default)',
        ],
      },
      commands: [
        {
          name: 'generate [MANIFEST]',
          summary: 'Render configs from the manifest.',
          flags: [
            ['--manifest <path>', 'Explicit manifest file.'],
            ['--mode <name>', 'Shorthand for --manifest .aikit/manifest.<name>.json.'],
            ['--dry-run', 'Show the plan without writing files.'],
            ['--force', 'Overwrite files modified since last generate (drift).'],
            ['--clean', 'Wipe previously generated files before applying.'],
          ],
        },
        {
          name: 'update [MANIFEST]',
          summary: 'Refresh the install from the current manifest.',
          flags: [
            ['--manifest <path>', 'Explicit manifest file.'],
            ['--mode <name>', 'Mode shorthand.'],
            ['--dry-run', 'Show the plan only.'],
            ['--force', 'Overwrite drifted files.'],
          ],
        },
        {
          name: 'update self',
          summary: 'Show current CLI version and latest release link.',
          flags: [
            ['--check', 'Print current version and link to the latest release.'],
          ],
        },
        {
          name: 'remove [MANIFEST]',
          summary: 'Uninstall every file recorded in the lock.',
          flags: [
            ['--manifest <path>', 'Explicit manifest file.'],
            ['--mode <name>', 'Mode shorthand.'],
            ['--dry-run', 'Show what would be deleted.'],
            ['--force', 'Also delete files you hand-edited (drift).'],
            ['--keep-manifest', 'Keep .aikit/manifest.json and the lock file.'],
          ],
        },
        {
          name: 'verify [MANIFEST]',
          summary: 'Validate a manifest without writing anything.',
          flags: [
            ['--manifest <path>', 'Explicit manifest file.'],
            ['--mode <name>', 'Mode shorthand.'],
            ['--all', 'Validate every manifest file under .aikit/.'],
          ],
        },
        {
          name: 'schema manifest',
          summary: 'Print the JSON Schema for .aikit/manifest.json.',
          flags: [],
        },
        {
          name: 'schema bundle [REF]',
          summary: 'Print the JSON Schema for a bundle (or list bundled ones).',
          flags: [
            ['REF', 'Bundle reference: directory path, .zip, zip:<path>, or embedded:<name>[@<version>].'],
            ['--base-dir <dir>', 'Base directory for resolving relative REF paths.'],
            ['--list', 'List every bundle compiled into the binary.'],
          ],
        },
      ],
      exitTitle: 'Exit codes',
      exit: [
        { code: '0', body: 'Success.' },
        { code: '1', body: 'Manifest invalid or validation failed.' },
        { code: '2', body: 'Usage error or I/O failure.' },
      ],
    },
    bundles: {
      hero: {
        eyebrow: 'Bundles',
        title: 'Reusable presets',
        lead: 'A bundle is a folder with a bundle.json manifest and one subfolder per target agent. Bundles ship inside the CLI binary or you can point at a .zip / directory of your own.',
      },
      manifest: {
        title: 'bundle.json',
        lead: 'The bundle manifest declares schema version, identity, targets, and inputs.',
        fields: [
          ['schemaVersion', 'int', 'Format version; bump on breaking changes.'],
          ['name', 'string', 'Unique kebab-case identifier.'],
          ['version', 'string', 'SemVer of the bundle.'],
          ['description', 'string', 'One-line summary.'],
          ['author', 'string?', 'Optional author info.'],
          ['license', 'string?', 'Optional SPDX identifier.'],
          ['targets', 'string[]', 'Supported agents — one folder per target in the bundle root.'],
          ['inputs', 'InputSpec[]', 'User-facing parameters. Empty array means no inputs.'],
        ],
      },
      inputs: {
        title: 'Input types',
        rows: [
          ['bool', 'Boolean flag. Toggles presence of files under <agent>/<id>/...'],
          ['select', 'One value from a list of options. Activates <agent>/<id>/<value>/...'],
          ['multiselect', 'Multiple values; each activates a matching subfolder.'],
          ['string', 'Free-form text. Substituted into templates via ${bundle.input.<id>}.'],
          ['int', 'Integer with optional min/max bounds.'],
          ['double', 'Floating-point with optional min/max bounds.'],
        ],
      },
      layout: {
        title: 'On-disk layout',
        body: `<bundle-root>/
  bundle.json
  <agent-id>/                    # e.g. claude-code
    config.json                  # optional layout / when-rules
    <files and folders>...       # rendered into target paths
  <other-agent>/...`,
      },
      sources: {
        title: 'Where bundles come from',
        items: [
          { title: 'internal', body: 'Compiled into the kit-setup binary. List them with kit-setup schema bundle --list.' },
          { title: 'External path', body: 'A relative path to a folder or .zip in your repo. Use source: "<relative-path>" in the manifest and reference name@version.' },
        ],
      },
      shipped: {
        title: 'Shipped with the binary',
        items: [
          {
            name: 'simple-kit@0.0.1',
            desc: 'Minimal starter: CLAUDE.md, optional skills, optional sub-agents, optional GitHub MCP, optional strict hooks.',
            inputs: 'projectName, skills, subagents, githubMcp, strict',
            targets: 'claude-code',
          },
          {
            name: 'modern-kit@0.0.1',
            desc: 'Kotlin-flavored: ktlint / detekt hooks, kotlin-specialist & gradle-troubleshooter sub-agents, optional Serena & KnowledgeOS MCP.',
            inputs: 'see kit-setup schema bundle embedded:modern-kit',
            targets: 'claude-code',
          },
        ],
      },
      custom: {
        title: 'Build your own',
        steps: [
          'Create a folder with bundle.json at the top and one subfolder per target agent.',
          'Declare inputs and reference them in templates as ${bundle.input.<id>}.',
          'Validate with kit-setup schema bundle <path>.',
          'Reference it from a project manifest via source: "<relative-path>" and bundle: "<name>@<version>".',
        ],
      },
    },
    architecture: {
      hero: {
        eyebrow: 'Architecture',
        title: 'How it\'s built',
        lead: 'kit-setup is a Kotlin Multiplatform CLI with native targets for Linux, macOS arm64, and Windows. The docs site you\'re reading is a Vite + React SPA. Both live in one monorepo.',
      },
      modules: {
        title: 'Gradle modules',
        rows: [
          ['core', 'Pure domain model. Bundle / project manifests, input specs, supported targets. No I/O.'],
          ['format', 'Kotlinx serialization for manifests + JSON Schema generation.'],
          ['io', 'Filesystem abstraction. Reads manifests and bundle sources (directories + zips).'],
          ['engine', 'Plan → diff → apply generator, verifier, remover. Owns the lock file and SHA256 hashing.'],
          ['layout', 'Routes generated files to the agent\'s native paths.'],
          ['akel', 'Tiny expression language used in config.json when-clauses and templates.'],
          ['cli', 'Clikt-based entry point. Compiles to native binary for linuxX64 / macosArm64 / mingwX64.'],
          ['e2e', 'End-to-end tests driving the real built binary (JVM, not multiplatform).'],
        ],
      },
      flow: {
        title: 'generate flow',
        steps: [
          'Load and parse .aikit/manifest.json.',
          'Resolve every referenced bundle (internal registry or external path).',
          'Validate inputs against each bundle\'s schema.',
          'Render templates: akel conditionals + ${bundle.input.*} substitution.',
          'Diff the rendered plan against .aikit/manifest.lock.json.',
          'If a bundle reference changed: wipe old files only after the plan is built successfully.',
          'Apply the diff and write a fresh lock file.',
        ],
      },
      safety: {
        title: 'Safety mechanisms',
        items: [
          { title: 'SHA256 hashing', body: 'Pure-Kotlin SHA256 (multiplatform). Same hash on every target. Used for drift detection and lock identity.' },
          { title: 'Lock-driven removal', body: 'ProjectRemover only deletes files listed in .aikit/manifest.lock.json. Anything else is preserved.' },
          { title: 'Plan-then-wipe', body: 'On bundle/version change, the old install is wiped only after the new plan is built. A failure aborts before any destructive write.' },
          { title: 'Anti-prompt-injection', body: 'The installer prompt enforces strict invariants and SHA256-verifies the binary before running it.' },
        ],
      },
      docs: {
        title: 'docs/ stack',
        body: 'Vite 5, React 18, react-router-dom (HashRouter), Tailwind CSS. Content lives as JSX + bilingual i18n.js (en/ru). The version badge fetches the latest tag from the GitHub Releases API with a six-hour localStorage cache and falls back to docs/package.json at build time.',
      },
    },
    claude: {
      hero: {
        eyebrow: 'Claude Code',
        title: 'AI Kit in Claude Code',
        lead: 'Today Claude Code is the supported target. Both shipped bundles render configs into the locations Claude Code expects — CLAUDE.md, .claude/agents/, .claude/commands/, .claude/skills/, .claude/hooks/, and .mcp.json.',
      },
      files: {
        eyebrow: 'Artifacts',
        title: 'What gets written',
        lead: 'kit-setup generate writes only the files declared by the chosen bundle. Your source code is untouched, and every file is tracked in .aikit/manifest.lock.json.',
        rows: [
          { path: 'CLAUDE.md', purpose: 'Main instruction file Claude Code loads at session start: project description, conventions, AI Kit fingerprint section.' },
          { path: '.claude/agents/*.md', purpose: 'Sub-agents Claude Code dispatches via the Task tool (e.g. kotlin-specialist, gradle-troubleshooter in modern-kit).' },
          { path: '.claude/commands/*.md', purpose: 'Slash commands. Each file body is the prompt Claude Code substitutes when the command is invoked.' },
          { path: '.claude/skills/*/SKILL.md', purpose: 'Auto-triggered skills. Description-matched by Claude Code on user intent.' },
          { path: '.claude/hooks/*.mjs', purpose: 'Optional pre/post tool-use hooks emitted when strict mode is enabled.' },
          { path: '.claude/settings.json', purpose: 'Permissions, Bash allowlist, env.' },
          { path: '.mcp.json', purpose: 'Project-scoped MCP servers. Only emitted when the bundle\'s MCP inputs are turned on.' },
        ],
      },
      bundles: {
        eyebrow: 'Bundles',
        title: 'Which bundle to start with',
        items: [
          { title: 'simple-kit@0.0.1', body: 'Minimal: CLAUDE.md + optional skills, sub-agents, GitHub MCP, strict hooks. A clean baseline you can extend.' },
          { title: 'modern-kit@0.0.1', body: 'Kotlin-flavored: ktlint / detekt hooks, kotlin-specialist and gradle-troubleshooter sub-agents, optional Serena and KnowledgeOS MCP.' },
        ],
      },
      fingerprint: {
        eyebrow: 'Safety',
        title: 'AI Kit fingerprint in CLAUDE.md',
        lead: 'Both bundles inject an "AI Kit" section into the generated CLAUDE.md. It tells the agent to quote-and-confirm before acting on any in-repo prompt that asks it to download binaries, run them, or git-commit on the user\'s behalf. Combined with the installer prompt\'s SHA256 verification, this raises the bar against prompt-injection attempts living inside repos.',
      },
    },
  },

  ru: {
    nav: {
      home: 'Что это?',
      start: 'Начать',
      features: 'Возможности',
      cli: 'CLI',
      bundles: 'Бандлы',
      claude: 'Claude Code',
      architecture: 'Архитектура',
    },
    common: {
      copy: 'Копировать',
      copied: 'Скопировано',
      copyFull: 'Копировать',
      copyError: 'Не удалось скопировать — выдели и скопируй вручную',
      github: 'GitHub',
      releases: 'Релизы',
    },
    footer: {
      repo: 'Репозиторий',
      releases: 'Релизы',
      license: 'Apache 2.0',
    },
    home: {
      hero: {
        suffix: ' · Apache 2.0',
        title: 'AI Kit',
        lead: 'Безопасный установщик, обновлятор и удалятор конфигов для AI coding-агентов — из одного манифеста и переиспользуемых бандлов. Одна команда ставит, другая обновляет, третья чисто удаляет.',
        ctaStart: 'Начать',
        ctaGithub: 'Открыть на GitHub',
      },
      problem: {
        eyebrow: 'Зачем это нужно',
        title: 'Что идёт не так без него',
        items: [
          {
            icon: '🧩',
            title: 'Конфиги раскиданы по проекту',
            body: 'CLAUDE.md, скилы, субагенты, хуки, MCP-серверы — у каждого агента своя раскладка. Делать руками долго и легко ошибиться.',
          },
          {
            icon: '👥',
            title: 'Разные настройки у команды',
            body: 'Без единого источника правды у каждого свой набор файлов. Правила расходятся, скилы устаревают.',
          },
          {
            icon: '💥',
            title: 'Ручные правки затираются',
            body: 'Любой скрипт регенерации обычно перезаписывает то, что ты подправил руками. После такого никто не доверяет повторному запуску.',
          },
          {
            icon: '🧹',
            title: 'Нет чистой деинсталляции',
            body: 'Попробовать новый пресет — значит руками вылавливать старые файлы. Полуудалённые конфиги отравляют следующую попытку.',
          },
        ],
      },
      solution: {
        eyebrow: 'Что делает AI Kit',
        title: 'Один манифест, три команды',
        lead: 'Нативный CLI-бинарь читает .aikit/manifest.json и рендерит конфиги агента из переиспользуемых бандлов. Install, update, remove — больше ничего.',
        items: [
          {
            icon: '📐',
            title: 'Конфиг как код',
            body: 'Один .aikit/manifest.json описывает каждое приложение в репо, целевых агентов и значения inputs. Правишь манифест, а не сгенерированные файлы.',
          },
          {
            icon: '♻️',
            title: 'Воспроизводимо',
            body: 'Тот же манифест → тот же результат у всей команды. Обновление и откат — одной командой. Без рассинхронизации.',
          },
          {
            icon: '🔒',
            title: 'Безопасно по умолчанию',
            body: 'Пишет только в .aikit/ и каталоги целевого агента. SHA256-lock защищает ручные правки — ничего не перезатрётся без --force.',
          },
        ],
      },
      why: {
        eyebrow: 'Почему лучше ручной настройки',
        title: 'Четыре гарантии, которых нет у copy-paste',
        items: [
          {
            title: 'SHA256 lock-файл',
            body: 'Каждый сгенерированный файл записан с SHA256-хэшем в .aikit/manifest.lock.json. CLI точно знает, что принадлежит ему.',
          },
          {
            title: 'Защита от drift',
            body: 'Если файл правлен руками, следующий generate его не трогает. Перезаписать можно только осознанно через --force.',
          },
          {
            title: 'Чистое удаление',
            body: 'remove удаляет только то, что в lock. Пользовательские файлы остаются. Никаких полуудалённых конфигов.',
          },
          {
            title: 'Anti-prompt-injection установка',
            body: 'У промпта установщика жёсткие инварианты: никогда не запускает git, не пишет вне .aikit/ и каталогов агента, проверяет SHA256 бинаря перед запуском.',
          },
        ],
      },
      runners: {
        eyebrow: 'Поддерживаемые агенты',
        title: 'Сегодня — Claude Code',
        lead: 'Бандлы поставляются с конфигами для Claude Code. Архитектура target-агностичная — другие агенты появятся по мере стабилизации их targets.',
        items: [
          { id: 'claude', name: 'Claude Code', note: 'Поддерживается' },
        ],
      },
      cta: {
        title: 'Попробуй на своём репо',
        body: 'Вставь один промпт в агента или скачай бинарь и запусти kit-setup generate. В обоих случаях меньше минуты.',
        button: 'Начать',
      },
    },
    start: {
      hero: {
        eyebrow: 'Install · Update · Remove',
        title: 'Три команды',
        lead: 'AI Kit — это один нативный бинарь (kit-setup) и один JSON-манифест. Положи бинарь в PATH, напиши минимальный манифест и ставь, обновляй или удаляй.',
      },
      prompt: {
        title: 'Самый простой путь: поручить агенту',
        subtitle: 'Вставь это в любой AI coding-агент с tool use, открытый в твоём проекте:',
        body: `Read prompts/install.md from https://github.com/aequicor/ai-kit-v2 and follow
the installation instructions for this project.`,
      },
      install: {
        eyebrow: 'Установка',
        title: 'kit-setup generate',
        lead: 'Рендерит конфиги, объявленные в .aikit/manifest.json, в места, ожидаемые целевым агентом. Пишет только в .aikit/ и в каталоги самого агента.',
        manifestLabel: 'Минимальный .aikit/manifest.json',
        manifest: `{
  "aikitVersion": "0.1.4",
  "applications": [
    {
      "id": "root",
      "path": ".",
      "targets": {
        "claude-code": {
          "bundle": "simple-kit@0.0.1",
          "source": "internal",
          "inputs": {
            "projectName": "my-app",
            "skills": [],
            "subagents": [],
            "githubMcp": false,
            "strict": true
          }
        }
      }
    }
  ]
}`,
        runLabel: 'Запуск',
        run: 'kit-setup generate',
        notes: [
          'Идемпотентен: повторный запуск с тем же манифестом — no-op.',
          'Бережёт ручные правки: drifted-файлы не перезатираются без --force.',
          '--dry-run показывает план без записи в файловую систему.',
        ],
      },
      update: {
        eyebrow: 'Обновление',
        title: 'kit-setup update',
        lead: 'Обновляет установку из текущего манифеста — подхватывает смены бандла/версии и перерендерит inputs. --dry-run покажет дифф до применения.',
        sub: [
          { cmd: 'kit-setup update', body: 'Перерендерить из текущего манифеста.' },
          { cmd: 'kit-setup update --dry-run', body: 'Показать, что изменится, без записи.' },
          { cmd: 'kit-setup update self --check', body: 'Текущая версия CLI и ссылка на последний релиз.' },
        ],
      },
      remove: {
        eyebrow: 'Удаление',
        title: 'kit-setup remove',
        lead: 'Удаляет всё, что добавил AI Kit. Стирается только то, что записано в .aikit/manifest.lock.json — больше ничего.',
        sub: [
          { cmd: 'kit-setup remove', body: 'Удалить все трекаемые файлы вместе с манифестом и lock.' },
          { cmd: 'kit-setup remove --keep-manifest', body: 'Удалить сгенерированные файлы, но сохранить манифест и lock.' },
          { cmd: 'kit-setup remove --dry-run', body: 'Показать, что было бы удалено, без изменений в FS.' },
          { cmd: 'kit-setup remove --force', body: 'Удалить и drifted-файлы (правленые руками). Без --force такие файлы остаются.' },
        ],
      },
      install_binary: {
        title: 'Скачать бинарь',
        body: 'По одному нативному файлу на платформу — JVM не нужен. Бери со страницы релизов, имя файла: kit-setup-<linux|macos|windows.exe>.',
        link: 'Последний релиз →',
      },
    },
    features: {
      hero: {
        eyebrow: 'Возможности',
        title: 'Что ты получаешь',
        lead: 'Всё ниже реально работает в текущем бинаре. Каждая карточка ссылается на конкретный код — без roadmap-обещаний.',
      },
      sections: [
        {
          group: 'Манифест и inputs',
          items: [
            { title: 'Манифест как источник правды', body: '.aikit/manifest.json описывает каждое приложение в репо: id, путь, целевых агентов и значения inputs для бандла. Правишь манифест, а не сгенерированные файлы.' },
            { title: 'Несколько манифестов / режимов', body: 'Выбор манифеста через --manifest, --mode <name>, env AIKIT_MANIFEST / AIKIT_MODE, .aikit/local.properties или дефолтный .aikit/manifest.json. В этом порядке.' },
            { title: 'Поддержка монорепо', body: 'Массив applications позволяет описать backend / frontend / инфру в одном манифесте и сгенерировать свой конфиг для каждого пути.' },
            { title: 'Bundle inputs', body: 'Бандлы объявляют inputs типов bool / select / multiselect / string / int / double. Inputs управляют присутствием файлов и подставляются в шаблоны через ${bundle.input.<id>}.' },
            { title: 'Условная генерация (akel)', body: 'Маленький язык выражений в config.json when-условиях и шаблонах — включает/выключает файлы и секции по значениям inputs.' },
          ],
        },
        {
          group: 'Бандлы',
          items: [
            { title: 'Встроенный реестр + внешние источники', body: 'source: "internal" — бандлы, вкомпиленные в бинарь; относительный путь — твой собственный .zip или папка. Ссылка в формате name@version.' },
            { title: 'JSON Schema export', body: 'kit-setup schema manifest и kit-setup schema bundle <ref> отдают валидные JSON Schema для автокомплита и валидации в IDE.' },
            { title: 'Каталог бандлов', body: 'kit-setup schema bundle --list печатает все бандлы, вкомпиленные в бинарь, с их версиями.' },
          ],
        },
        {
          group: 'Безопасность',
          items: [
            { title: 'Идемпотентный generate', body: 'SHA256-хэш каждого сгенерированного файла лежит в .aikit/manifest.lock.json. Повторный generate не трогает ничего, если контент тот же.' },
            { title: 'Drift detection', body: 'Поправил файл руками — следующий generate его не тронет. Перезаписать можно только через --force.' },
            { title: 'Dry-run везде', body: 'У generate, update и remove есть --dry-run — печатает план, ничего не пишет.' },
            { title: 'Безопасный wipe при смене бандла', body: 'При смене бандла или версии старые файлы стираются только после успешного построения нового плана. Битый манифест не сломает рабочую установку.' },
            { title: 'Чистое удаление', body: 'remove удаляет только файлы из lock. Пользовательские не трогает. --keep-manifest сохраняет манифест, --force снимает защиту от drift.' },
            { title: 'Verify без записи', body: 'kit-setup verify (или verify --all) проверяет один или все манифесты в .aikit/ — без рендеринга и без записи.' },
            { title: 'Anti-prompt-injection установка', body: 'У промпта установщика жёсткие инварианты: никогда не запускает git, не пишет YAML, проверяет SHA256 бинаря перед запуском и пишет только в .aikit/ и каталоги агента.' },
          ],
        },
        {
          group: 'Дистрибуция',
          items: [
            { title: 'Нативный бинарь, без JVM', body: 'Kotlin Multiplatform для linuxX64, macosArm64, mingwX64. Положи один файл в PATH — готово.' },
            { title: 'Проверка обновления CLI', body: 'kit-setup update self --check показывает текущую версию и ссылку на последний релиз.' },
          ],
        },
      ],
    },
    cli: {
      hero: {
        eyebrow: 'Справочник CLI',
        title: 'kit-setup',
        lead: 'Несколько подкоманд. Каждая операция записи поддерживает --dry-run, каждая команда с манифестом — --manifest и --mode.',
      },
      resolution: {
        title: 'Как выбирается манифест',
        lead: 'Манифест ищется в этом порядке — первое совпадение побеждает:',
        steps: [
          '--manifest <path> (явно)',
          '--mode <name> → .aikit/manifest.<name>.json',
          'env AIKIT_MANIFEST',
          'env AIKIT_MODE',
          '.aikit/local.properties → поле manifest=',
          '.aikit/manifest.json (по умолчанию)',
        ],
      },
      commands: [
        {
          name: 'generate [MANIFEST]',
          summary: 'Рендерит конфиги из манифеста.',
          flags: [
            ['--manifest <path>', 'Явный файл манифеста.'],
            ['--mode <name>', 'Сокращение для --manifest .aikit/manifest.<name>.json.'],
            ['--dry-run', 'Показать план без записи файлов.'],
            ['--force', 'Перезаписать файлы, изменённые после последнего generate (drift).'],
            ['--clean', 'Стереть ранее сгенерированные файлы перед применением.'],
          ],
        },
        {
          name: 'update [MANIFEST]',
          summary: 'Обновляет установку из текущего манифеста.',
          flags: [
            ['--manifest <path>', 'Явный файл манифеста.'],
            ['--mode <name>', 'Сокращение для режима.'],
            ['--dry-run', 'Только показать план.'],
            ['--force', 'Перезаписать drifted-файлы.'],
          ],
        },
        {
          name: 'update self',
          summary: 'Текущая версия CLI и ссылка на последний релиз.',
          flags: [
            ['--check', 'Печатает текущую версию и ссылку на последний релиз.'],
          ],
        },
        {
          name: 'remove [MANIFEST]',
          summary: 'Удаляет каждый файл, записанный в lock.',
          flags: [
            ['--manifest <path>', 'Явный файл манифеста.'],
            ['--mode <name>', 'Сокращение для режима.'],
            ['--dry-run', 'Показать, что было бы удалено.'],
            ['--force', 'Удалить и drifted-файлы.'],
            ['--keep-manifest', 'Сохранить .aikit/manifest.json и lock.'],
          ],
        },
        {
          name: 'verify [MANIFEST]',
          summary: 'Проверяет манифест без записи.',
          flags: [
            ['--manifest <path>', 'Явный файл манифеста.'],
            ['--mode <name>', 'Сокращение для режима.'],
            ['--all', 'Проверить каждый файл манифеста в .aikit/.'],
          ],
        },
        {
          name: 'schema manifest',
          summary: 'Печатает JSON Schema для .aikit/manifest.json.',
          flags: [],
        },
        {
          name: 'schema bundle [REF]',
          summary: 'Печатает JSON Schema для бандла (или список встроенных).',
          flags: [
            ['REF', 'Ссылка на бандл: путь к папке, .zip, zip:<path> или embedded:<name>[@<version>].'],
            ['--base-dir <dir>', 'База для относительных REF-путей.'],
            ['--list', 'Список всех встроенных бандлов.'],
          ],
        },
      ],
      exitTitle: 'Коды выхода',
      exit: [
        { code: '0', body: 'Успех.' },
        { code: '1', body: 'Манифест невалиден или проверка не прошла.' },
        { code: '2', body: 'Ошибка использования или ввода-вывода.' },
      ],
    },
    bundles: {
      hero: {
        eyebrow: 'Бандлы',
        title: 'Переиспользуемые пресеты',
        lead: 'Бандл — это папка с манифестом bundle.json и по одной подпапке на каждого целевого агента. Бандлы могут жить внутри бинаря или быть собственным .zip / папкой.',
      },
      manifest: {
        title: 'bundle.json',
        lead: 'Манифест бандла описывает версию схемы, идентификацию, targets и inputs.',
        fields: [
          ['schemaVersion', 'int', 'Версия формата; повышается при ломающих изменениях.'],
          ['name', 'string', 'Уникальный kebab-case идентификатор.'],
          ['version', 'string', 'SemVer бандла.'],
          ['description', 'string', 'Краткое описание.'],
          ['author', 'string?', 'Опционально.'],
          ['license', 'string?', 'Опциональный SPDX-идентификатор.'],
          ['targets', 'string[]', 'Поддерживаемые агенты — по одной папке на target в корне бандла.'],
          ['inputs', 'InputSpec[]', 'Пользовательские параметры. Пустой массив — нет inputs.'],
        ],
      },
      inputs: {
        title: 'Типы inputs',
        rows: [
          ['bool', 'Булев флаг. Включает/выключает файлы под <agent>/<id>/...'],
          ['select', 'Одно значение из списка опций. Активирует <agent>/<id>/<value>/...'],
          ['multiselect', 'Несколько значений; каждое активирует свою подпапку.'],
          ['string', 'Произвольный текст. Подставляется в шаблоны через ${bundle.input.<id>}.'],
          ['int', 'Целое число с опциональными min/max.'],
          ['double', 'Дробное число с опциональными min/max.'],
        ],
      },
      layout: {
        title: 'Раскладка на диске',
        body: `<bundle-root>/
  bundle.json
  <agent-id>/                    # например claude-code
    config.json                  # опциональные when-правила
    <файлы и папки>...           # рендерятся в пути агента
  <другой-агент>/...`,
      },
      sources: {
        title: 'Откуда берутся бандлы',
        items: [
          { title: 'internal', body: 'Вкомпилены в kit-setup. Список — kit-setup schema bundle --list.' },
          { title: 'Внешний путь', body: 'Относительный путь к папке или .zip в репо. В манифесте указываешь source: "<relative-path>" и ссылку name@version.' },
        ],
      },
      shipped: {
        title: 'Поставляются с бинарём',
        items: [
          {
            name: 'simple-kit@0.0.1',
            desc: 'Минимальный стартер: CLAUDE.md, опциональные скилы, субагенты, GitHub MCP, strict-хуки.',
            inputs: 'projectName, skills, subagents, githubMcp, strict',
            targets: 'claude-code',
          },
          {
            name: 'modern-kit@0.0.1',
            desc: 'Kotlin-flavored: ktlint / detekt хуки, kotlin-specialist и gradle-troubleshooter, опциональные Serena и KnowledgeOS MCP.',
            inputs: 'см. kit-setup schema bundle embedded:modern-kit',
            targets: 'claude-code',
          },
        ],
      },
      custom: {
        title: 'Собрать свой',
        steps: [
          'Создай папку с bundle.json на верхнем уровне и по подпапке на каждого целевого агента.',
          'Объяви inputs и ссылайся на них в шаблонах как ${bundle.input.<id>}.',
          'Проверь kit-setup schema bundle <path>.',
          'Подключи в манифесте проекта через source: "<relative-path>" и bundle: "<name>@<version>".',
        ],
      },
    },
    architecture: {
      hero: {
        eyebrow: 'Архитектура',
        title: 'Как устроено',
        lead: 'kit-setup — Kotlin Multiplatform CLI с нативными сборками для Linux, macOS arm64 и Windows. Сайт, который ты сейчас читаешь, — Vite + React SPA. Оба живут в одном монорепо.',
      },
      modules: {
        title: 'Gradle-модули',
        rows: [
          ['core', 'Чистая доменная модель. Манифесты бандла/проекта, спецификации inputs, поддерживаемые targets. Без I/O.'],
          ['format', 'Сериализация через kotlinx.serialization + генерация JSON Schema.'],
          ['io', 'Абстракция файловой системы. Чтение манифестов и источников бандлов (папки + zip).'],
          ['engine', 'Генератор план→дифф→применение, verifier, remover. Владелец lock-файла и SHA256.'],
          ['layout', 'Раскладывает сгенерированные файлы по родным путям агента.'],
          ['akel', 'Маленький язык выражений в config.json when-условиях и шаблонах.'],
          ['cli', 'Точка входа на Clikt. Собирается в нативный бинарь для linuxX64 / macosArm64 / mingwX64.'],
          ['e2e', 'E2E-тесты по реальному собранному бинарю (JVM, не multiplatform).'],
        ],
      },
      flow: {
        title: 'Поток generate',
        steps: [
          'Загрузить и распарсить .aikit/manifest.json.',
          'Резолвить каждый ссылочный бандл (внутренний реестр или внешний путь).',
          'Проверить inputs против схемы каждого бандла.',
          'Отрендерить шаблоны: akel-условия + подстановка ${bundle.input.*}.',
          'Сравнить план с .aikit/manifest.lock.json.',
          'Если ссылка на бандл изменилась — стереть старые файлы только после успешного плана.',
          'Применить дифф и записать новый lock.',
        ],
      },
      safety: {
        title: 'Механизмы безопасности',
        items: [
          { title: 'SHA256-хэширование', body: 'Чистый Kotlin SHA256 (multiplatform). Одинаковый хэш на любой платформе. Используется для drift detection и идентичности lock.' },
          { title: 'Удаление по lock', body: 'ProjectRemover стирает только файлы из .aikit/manifest.lock.json. Всё остальное сохраняется.' },
          { title: 'План → wipe', body: 'При смене бандла/версии старая установка стирается только после успешного построения нового плана. Падение — abort до любой деструктивной записи.' },
          { title: 'Anti-prompt-injection', body: 'Промпт установщика навязывает строгие инварианты и SHA256-верифицирует бинарь перед запуском.' },
        ],
      },
      docs: {
        title: 'Стек docs/',
        body: 'Vite 5, React 18, react-router-dom (HashRouter), Tailwind CSS. Контент — JSX + двуязычный i18n.js (en/ru). Бэйдж версии тянет последний tag из GitHub Releases API (кэш 6 часов в localStorage), fallback — версия из docs/package.json во время сборки.',
      },
    },
    claude: {
      hero: {
        eyebrow: 'Claude Code',
        title: 'AI Kit в Claude Code',
        lead: 'Сегодня Claude Code — единственный поддерживаемый target. Оба поставляемых бандла рендерят файлы в места, ожидаемые Claude Code: CLAUDE.md, .claude/agents/, .claude/commands/, .claude/skills/, .claude/hooks/ и .mcp.json.',
      },
      files: {
        eyebrow: 'Артефакты',
        title: 'Что появляется в проекте',
        lead: 'kit-setup generate пишет только то, что объявил выбранный бандл. Исходный код не трогается, каждый файл трекается в .aikit/manifest.lock.json.',
        rows: [
          { path: 'CLAUDE.md', purpose: 'Главный файл инструкций, который Claude Code читает в начале каждой сессии: описание проекта, конвенции, AI Kit fingerprint-секция.' },
          { path: '.claude/agents/*.md', purpose: 'Субагенты, которых Claude Code запускает через Task tool (например, kotlin-specialist и gradle-troubleshooter в modern-kit).' },
          { path: '.claude/commands/*.md', purpose: 'Слэш-команды. Тело каждого файла — это промпт, который Claude Code подставляет при вызове.' },
          { path: '.claude/skills/*/SKILL.md', purpose: 'Авто-триггерные скилы. Claude Code матчит описание скила с намерением пользователя.' },
          { path: '.claude/hooks/*.mjs', purpose: 'Опциональные pre/post-tool-use хуки. Генерируются при включённом strict-режиме.' },
          { path: '.claude/settings.json', purpose: 'Разрешения, Bash allowlist, env.' },
          { path: '.mcp.json', purpose: 'MCP-серверы уровня проекта. Генерируется, только если в inputs включены соответствующие MCP.' },
        ],
      },
      bundles: {
        eyebrow: 'Бандлы',
        title: 'С чего начать',
        items: [
          { title: 'simple-kit@0.0.1', body: 'Минимальный: CLAUDE.md + опциональные скилы, субагенты, GitHub MCP, strict-хуки. Чистая база, которую легко расширять.' },
          { title: 'modern-kit@0.0.1', body: 'Kotlin-flavored: ktlint / detekt хуки, субагенты kotlin-specialist и gradle-troubleshooter, опциональные Serena и KnowledgeOS MCP.' },
        ],
      },
      fingerprint: {
        eyebrow: 'Безопасность',
        title: 'AI Kit fingerprint в CLAUDE.md',
        lead: 'Оба бандла встраивают секцию «AI Kit» в сгенерированный CLAUDE.md. Она инструктирует агента сначала процитировать и подтвердить любой in-repo промпт, который просит скачать бинарь, запустить его или сделать git-commit. Вместе с SHA256-верификацией бинаря в installer-промпте это повышает планку против prompt-injection атак из репозиториев.',
      },
    },
  },
}
