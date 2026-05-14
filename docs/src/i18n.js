// ---------------------------------------------------------------------------
// All translations. Each page has its own key.
// ---------------------------------------------------------------------------

export const T = {
  en: {
    nav: {
      home: 'What is it?',
      start: 'Get started',
      claude: 'Claude Code',
      opencode: 'OpenCode',
      qwen: 'Qwen Code',
      cursor: 'Cursor',
      aider: 'Aider',
    },
    common: {
      copy: 'Copy',
      copied: 'Copied',
      copyFull: 'Copy full prompt',
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
        badge: 'v4.2 · Apache 2.0',
        title: 'AI Kit',
        lead: 'A toolkit that gives your coding agent a project description, a structured workflow, and three commands it can follow. Describe the project once — the agent reads it at the start of every session.',
        ctaStart: 'Get started',
        ctaGithub: 'Open on GitHub',
      },
      problem: {
        eyebrow: 'Why this exists',
        title: 'What goes wrong without a process',
        items: [
          {
            icon: '🌪',
            title: 'Chaos on large tasks',
            body: 'Without a plan, the agent picks a starting point at random and works in all directions. Halfway through, it loses track of what it started.',
          },
          {
            icon: '🧠',
            title: 'No memory between sessions',
            body: 'Every session starts blank. You re-explain the stack, the off-limits folders, the conventions — every time.',
          },
          {
            icon: '💥',
            title: 'Breaks neighboring code',
            body: 'Fixing one thing clips three others. You find out when tests go red.',
          },
          {
            icon: '🎯',
            title: 'Misses the target',
            body: 'It misreads the task, writes 500 lines, and the result doesn\'t match what you needed. Revert and start over.',
          },
        ],
      },
      solution: {
        eyebrow: 'What AI Kit does',
        title: 'Three commands that give the agent a process',
        lead: 'Not a plugin, not a service — just files in your project that the agent reads on session start and follows.',
        items: [
          {
            icon: '📐',
            title: 'Knows the project',
            body: 'Describe the stack once in .aikit/manifest.yaml. The agent reads it at session start: language, framework, build commands, forbidden paths, conventions.',
          },
          {
            icon: '📋',
            title: 'Plans before coding',
            body: 'Every task starts with a plan: artifacts, steps, definition of done. One step, one commit — you can roll back at any point.',
          },
          {
            icon: '🛑',
            title: 'Waits at checkpoints',
            body: 'After the plan, after risky steps, before commits. You decide what happens next — the agent holds until you say go.',
          },
        ],
      },
      how: {
        eyebrow: 'What it looks like',
        title: 'A simple loop',
        steps: [
          {
            num: '1',
            title: 'You describe the task',
            body: 'One sentence in chat. The agent reads the project and proposes a plan.',
          },
          {
            num: '2',
            title: 'You review the plan',
            body: 'A list of steps and artifacts. Approve, adjust, or cancel.',
          },
          {
            num: '3',
            title: 'The agent executes step by step',
            body: 'One step, one commit. At checkpoints it stops and shows you the result.',
          },
          {
            num: '4',
            title: 'You confirm and move on',
            body: 'At each step: continue, correct, or stop. You stay in control.',
          },
        ],
      },
      runners: {
        eyebrow: 'Supported environments',
        title: 'One process — five agents',
        lead: 'The same commands work everywhere. Switching from Cursor to Claude Code? The manifest and workflow stay the same.',
        items: [
          { id: 'claude', name: 'Claude Code', note: 'Full support' },
          { id: 'opencode', name: 'OpenCode', note: 'Full support' },
          { id: 'qwen', name: 'Qwen Code', note: 'Full support' },
          { id: 'cursor', name: 'Cursor', note: 'Rules and MCP' },
          { id: 'aider', name: 'Aider', note: 'Conventions and prompts' },
        ],
      },
      cta: {
        title: 'Want to try it?',
        body: 'Setup takes a minute: open the agent in your project, paste the prompt, answer two questions.',
        button: 'Get started',
      },
    },
    start: {
      hero: {
        eyebrow: 'Get started',
        title: 'Set up in a minute',
        lead: 'Copy the full prompt, paste it into Claude Code or OpenCode inside your project, answer two questions. The agent handles the rest.',
      },
      setup: {
        title: 'Setup prompt',
        subtitle: 'Copy → paste into your agent\'s chat → answer two questions',
        previewLabel: 'Beginning of the prompt (preview):',
        stepsTitle: 'What happens next',
        steps: [
          'The agent asks which language to use for setup (English / Russian).',
          'Scans your project and asks which agent and model family to use.',
          'Shows a draft .aikit/manifest.yaml and waits for your approval.',
          'Downloads kit-setup, verifies the manifest, generates files, commits.',
        ],
      },
      files: {
        eyebrow: 'Result',
        title: 'What lands in your project',
        lead: 'One manifest and a set of files for the chosen agent. Your existing source code is left alone.',
        tree: [
          { path: '.aikit/manifest.yaml', note: 'single YAML for the whole project' },
          { path: '.aikit/bin/kit-setup', note: 'binary (in .gitignore)' },
          { path: 'CLAUDE.md / AGENTS.md / CONVENTIONS.md', note: 'agent-facing instructions, varies by agent' },
          { path: '.claude/ or .opencode/ or .qwen/ or .cursor/', note: 'sub-agents, slash commands, skills' },
        ],
      },
      commands: {
        eyebrow: 'Three commands',
        title: 'Day-to-day work',
        lead: 'The same commands across every agent. Each one runs in a fresh chat session.',
        items: [
          {
            name: '/kit',
            subtitle: 'Planning',
            body: 'Describe the task in one sentence — the agent reads the code, assesses risk, and drafts a plan with artifacts, steps, and definition of done. It stops for your approval before doing anything.',
          },
          {
            name: '/kit-do',
            subtitle: 'Execution',
            body: 'Walks through the approved plan step by step, one commit per step, with verification. At each checkpoint it shows what it built and waits for you to continue.',
          },
          {
            name: '/kit-fix',
            subtitle: 'Diagnostic recovery',
            body: '4-stage flow: anamnesis + cause options → fix options → diff preview → commit. Skips trivial steps automatically; the confirmation before commit is always required.',
          },
        ],
      },
      scenarios: {
        eyebrow: 'Scenarios',
        title: 'What this looks like in practice',
        items: [
          {
            title: 'New feature',
            body: '/kit "add CSV export for orders". The agent reads the modules, drafts a 5-step plan, waits for approval, then /kit-do walks through them.',
          },
          {
            title: 'Fix a bug',
            body: '/kit-fix "the save button stops responding on the second click". Isolated session, doesn\'t disturb the current plan.',
          },
          {
            title: 'Change stack or bump dependencies',
            body: 'Edit stack.profiles in the manifest (e.g. add nextjs), run kit-setup generate — every config is re-rendered.',
          },
          {
            title: 'Lock the agent out of legacy/',
            body: 'Add legacy/ to policies.forbidden_patterns — the rule lands consistently in every generated file.',
          },
          {
            title: 'Add a sub-agent',
            body: 'Describe the role in agents[] (e.g. review-agent), run generate — a new file appears under .claude/agents/ or .opencode/agents/.',
          },
        ],
      },
      cli: {
        eyebrow: 'For the hands-on path',
        title: 'kit-setup CLI',
        lead: 'If you\'d rather not hand setup to an agent, drive everything yourself. The binary is on the releases page — one file per platform.',
        subcommands: [
          {
            cmd: 'kit-setup verify [<path>]',
            body: 'Validates the manifest. Returns JSON {valid, errors[]}. Use in an edit-then-verify loop.',
          },
          {
            cmd: 'kit-setup generate [<path>]',
            body: 'Renders files. Returns JSON {ok, generated[]}. Idempotent — overwrites its own output cleanly.',
          },
          {
            cmd: 'kit-setup schema [--format json|human]',
            body: 'Catalog of bundled templates: profiles, adapters, dialects, commands, skills.',
          },
        ],
        exitTitle: 'Exit codes',
        exit: [
          { code: '0', body: 'Success.' },
          { code: '1', body: 'Manifest invalid (verify) or validation refused (generate).' },
          { code: '2', body: 'Usage, manifest load, parse, or I/O failure.' },
        ],
        releasesLink: 'Download the binary →',
      },
    },
    claude: {
      hero: {
        eyebrow: 'Claude Code',
        title: 'AI Kit in Claude Code',
        lead: 'What gets written to .claude/, how the Anthropic-specific features — sub-agents, skills, hooks — are used, and how the dialect differs from the generic one.',
      },
      files: {
        eyebrow: 'Artifacts',
        title: 'What gets written to your project',
        lead: 'Running kit-setup generate with render_targets: [claude-code] writes exactly these files. Existing source code is untouched.',
        rows: [
          { path: 'CLAUDE.md', purpose: 'Main instruction file: project conventions, forbidden patterns, the body of the Main agent. Claude Code reads this at the start of every session.' },
          { path: '.claude/agents/*.md', purpose: 'Sub-agents — isolated one-shot contexts that Main dispatches via the Task tool. Default: Researcher.' },
          { path: '.claude/commands/*.md', purpose: 'Slash commands /kit, /kit-do, /kit-fix. Each file\'s body is the prompt Claude Code substitutes when invoked.' },
          { path: '.claude/skills/*/SKILL.md', purpose: 'Skills with auto-triggers driven by their descriptions. Covers plan format, debug flow, cause hypotheses, fix options, and more.' },
          { path: '.claude/prompts/*.md', purpose: 'Helper prompts for manual use. Not auto-invoked — paste into chat when needed.' },
          { path: '.claude/settings.json', purpose: 'Permissions, Bash allowlist, env. Ships with sensible defaults.' },
          { path: '.mcp.json', purpose: 'Project-scoped MCP servers. Only emitted when manifest tools[] has an enabled mcp-* entry.' },
          { path: '.claude/hooks/*.mjs', purpose: 'Optional. Auto-checks on PreToolUse / PostToolUse / Stop. For example: block Edit outside permitted directories.' },
        ],
      },
      features: {
        eyebrow: 'Claude Code features',
        title: 'What AI Kit uses in this environment',
        items: [
          {
            title: 'Sub-agents via the Task tool',
            body: 'Each file in .claude/agents/ is an isolated one-shot context. Main dispatches them with the built-in Task tool when it needs to keep heavy research out of the main conversation. They return one message — a compact digest.',
          },
          {
            title: 'Auto-triggered skills',
            body: 'Each SKILL.md has a description. Claude Code matches user intent against it and loads the skill automatically. AI Kit uses this to enforce the format of planning blocks.',
          },
          {
            title: 'Hooks for guardrails',
            body: 'PreToolUse can block a tool call (e.g. Bash with rm -rf), PostToolUse can add a check after Edit, Stop can do a final validation. All hooks are plain .mjs files inside the project.',
          },
          {
            title: 'Permissions in settings.json, MCP in .mcp.json',
            body: 'Bash allowlist and write permissions live in .claude/settings.json. Project-scoped MCP servers live in .mcp.json at the repo root. Both are plain files reviewable like any other code.',
          },
        ],
      },
      dialect: {
        eyebrow: 'Anthropic dialect',
        title: 'How prompts differ from generic',
        lead: 'Wrappers in dialects/anthropic/ are written for Claude\'s native APIs: they reference the Task tool by name, use structured XML tags for long context, and read more like instructions to a colleague than directives to a machine. The generic dialect for non-Anthropic models is more directive and skips Claude-specific affordances.',
      },
    },
    opencode: {
      hero: {
        eyebrow: 'OpenCode',
        title: 'AI Kit in OpenCode',
        lead: 'Same three commands, same manifest — different files. OpenCode supports sub-agents and slash commands, but uses TypeScript plugins instead of hooks.',
      },
      files: {
        eyebrow: 'Artifacts',
        title: 'What gets written to your project',
        lead: 'Running kit-setup generate with render_targets: [opencode] writes these files.',
        rows: [
          { path: 'AGENTS.md', purpose: 'Main instruction file — the OpenCode equivalent of CLAUDE.md.' },
          { path: '.opencode/agents/*.md', purpose: 'Sub-agents, each with their own permission set.' },
          { path: '.opencode/commands/*.md', purpose: 'Slash commands /kit, /kit-do, /kit-fix.' },
          { path: '.opencode/skills/*/SKILL.md', purpose: 'Skills in the same format as Claude Code.' },
          { path: 'opencode.json', purpose: 'Provider, model, and MCP configuration.' },
          { path: '.opencode/plugins/*.ts', purpose: 'TypeScript plugins in place of hooks. Same guardrail effect via TypeScript modules.' },
        ],
      },
      diff: {
        eyebrow: 'Differences from Claude Code',
        title: 'What changes and what stays the same',
        items: [
          {
            title: 'No hooks — TypeScript plugins instead',
            body: 'OpenCode doesn\'t have a hooks system. AI Kit emits .opencode/plugins/*.ts files that achieve the same guardrail effect via TypeScript modules.',
          },
          {
            title: 'AGENTS.md instead of CLAUDE.md',
            body: 'The main instruction file is AGENTS.md at the project root. Content is the same; the filename follows OpenCode\'s convention.',
          },
          {
            title: 'opencode.json for model and MCP config',
            body: 'Provider, model family, and MCP server configuration live in opencode.json at the project root.',
          },
        ],
      },
    },
    qwen: {
      hero: {
        eyebrow: 'Qwen Code',
        title: 'AI Kit in Qwen Code',
        lead: 'Same three commands, same manifest. Qwen Code mirrors the OpenCode structure but everything lives under .qwen/.',
      },
      files: {
        eyebrow: 'Artifacts',
        title: 'What gets written to your project',
        lead: 'Running kit-setup generate with render_targets: [qwen-code] writes these files.',
        rows: [
          { path: '.qwen/AGENTS.md', purpose: 'Main instruction file. Path is declared in settings.json via contextFileName.' },
          { path: '.qwen/agents/*.md', purpose: 'Sub-agents.' },
          { path: '.qwen/commands/*.md', purpose: 'Slash commands.' },
          { path: '.qwen/skills/*/SKILL.md', purpose: 'Skills.' },
          { path: '.qwen/settings.json', purpose: 'Model settings (modelProviders) and MCP servers.' },
        ],
      },
      diff: {
        eyebrow: 'Differences from OpenCode',
        title: 'What changes',
        items: [
          {
            title: 'Everything under .qwen/',
            body: 'All agent files — agents, commands, skills, settings — live under .qwen/ instead of .opencode/.',
          },
          {
            title: 'contextFileName in settings.json',
            body: 'Qwen Code reads the main instruction file path from settings.json via contextFileName, pointing to .qwen/AGENTS.md.',
          },
          {
            title: 'Generic dialect with Qwen tweaks',
            body: 'Prompts come from dialects/qwen/ — a subdirectory of the generic dialect with minor adjustments for the model\'s context window and instruction style.',
          },
        ],
      },
    },
    cursor: {
      hero: {
        eyebrow: 'Cursor',
        title: 'AI Kit in Cursor',
        lead: 'Partial support. No sub-agents, no slash commands. AI Kit writes the project rules as .cursor/rules/*.mdc files and adds .cursor/mcp.json for MCP servers.',
      },
      files: {
        eyebrow: 'Artifacts',
        title: 'What gets written to your project',
        lead: 'Running kit-setup generate with render_targets: [cursor] writes these files.',
        rows: [
          { path: '.cursor/rules/*.mdc', purpose: 'Project rules with alwaysApply: true. Replaces CLAUDE.md / AGENTS.md for Cursor.' },
          { path: '.cursor/mcp.json', purpose: 'MCP server configuration for Cursor.' },
        ],
      },
      limits: {
        eyebrow: 'Limitations',
        title: 'What partial support means',
        items: [
          {
            title: 'No sub-agents',
            body: 'Cursor doesn\'t have a Task tool or equivalent. The Researcher and other sub-agents aren\'t emitted — the agent works from a single context.',
          },
          {
            title: 'No slash commands',
            body: '/kit, /kit-do, and /kit-fix aren\'t available as proper slash commands. You can still paste the prompt content manually, but the experience is manual.',
          },
          {
            title: 'Rules instead of a memory file',
            body: 'The project description and conventions land in .cursor/rules/*.mdc with alwaysApply: true. Cursor loads them automatically, so the agent knows the stack — but the planning and execution commands don\'t have dedicated triggers.',
          },
        ],
      },
    },
    aider: {
      hero: {
        eyebrow: 'Aider',
        title: 'AI Kit in Aider',
        lead: 'Partial support. No sub-agents, no slash commands. AI Kit writes CONVENTIONS.md, .aider.conf.yml, and user prompts under .aider/prompts/.',
      },
      files: {
        eyebrow: 'Artifacts',
        title: 'What gets written to your project',
        lead: 'Running kit-setup generate with render_targets: [aider] writes these files.',
        rows: [
          { path: 'CONVENTIONS.md', purpose: 'Main instruction file. Aider loads it automatically at session start.' },
          { path: '.aider.conf.yml', purpose: 'Global model setting and Aider configuration.' },
          { path: '.aider/prompts/*.md', purpose: 'User prompts for the /kit, /kit-do, /kit-fix workflow. Paste into chat manually.' },
        ],
      },
      limits: {
        eyebrow: 'Limitations',
        title: 'What partial support means',
        items: [
          {
            title: 'No sub-agents',
            body: 'Aider doesn\'t support isolated sub-agent contexts. All work happens in a single session.',
          },
          {
            title: 'No slash commands',
            body: '/kit, /kit-do, and /kit-fix don\'t have dedicated triggers. The prompts are in .aider/prompts/ — copy and paste them manually.',
          },
          {
            title: 'CONVENTIONS.md is the foundation',
            body: 'Aider reads CONVENTIONS.md automatically. The stack description, conventions, and forbidden patterns land there — the agent knows the project from session start.',
          },
        ],
      },
    },
  },

  ru: {
    nav: {
      home: 'Что это?',
      start: 'Начать',
      claude: 'Claude Code',
      opencode: 'OpenCode',
      qwen: 'Qwen Code',
      cursor: 'Cursor',
      aider: 'Aider',
    },
    common: {
      copy: 'Копировать',
      copied: 'Скопировано',
      copyFull: 'Скопировать полный промпт',
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
        badge: 'v4.2 · Apache 2.0',
        title: 'AI Kit',
        lead: 'Инструментарий, который даёт кодовому агенту описание проекта, рабочий процесс и три команды. Описываешь проект один раз — агент читает это в начале каждой сессии.',
        ctaStart: 'Начать',
        ctaGithub: 'Открыть на GitHub',
      },
      problem: {
        eyebrow: 'Зачем это нужно',
        title: 'Что идёт не так без процесса',
        items: [
          {
            icon: '🌪',
            title: 'Хаос на больших задачах',
            body: 'Без плана агент берётся за всё сразу в произвольном порядке. Где-то на середине теряет нить.',
          },
          {
            icon: '🧠',
            title: 'Нет памяти между сессиями',
            body: 'Каждая новая сессия начинается с нуля. Снова объясняешь стек, запрещённые папки, конвенции.',
          },
          {
            icon: '💥',
            title: 'Ломает соседний код',
            body: 'Чинит одно, задевает три других. Узнаёшь, когда падают тесты.',
          },
          {
            icon: '🎯',
            title: 'Строит не то',
            body: 'Неверно читает задачу, пишет 500 строк — результат мимо цели. Откат и сначала.',
          },
        ],
      },
      solution: {
        eyebrow: 'Что делает AI Kit',
        title: 'Три команды, которые дают агенту процесс',
        lead: 'Не плагин и не сервис — просто файлы в проекте, которые агент читает при старте сессии и следует им.',
        items: [
          {
            icon: '📐',
            title: 'Знает проект',
            body: 'Описываешь стек один раз в .aikit/manifest.yaml. Агент читает при старте: язык, фреймворк, команды сборки, запрещённые пути, конвенции.',
          },
          {
            icon: '📋',
            title: 'Сначала план, потом код',
            body: 'Каждая задача начинается с плана: артефакты, шаги, критерий завершения. Один шаг — один коммит, откат в любой точке.',
          },
          {
            icon: '🛑',
            title: 'Ждёт на чекпоинтах',
            body: 'После плана, после рискованных шагов, перед коммитами. Ты решаешь, что дальше — агент стоит, пока не дашь команду.',
          },
        ],
      },
      how: {
        eyebrow: 'Как это выглядит',
        title: 'Простой цикл',
        steps: [
          {
            num: '1',
            title: 'Описываешь задачу',
            body: 'Одним предложением в чате. Агент изучает проект и предлагает план.',
          },
          {
            num: '2',
            title: 'Смотришь план',
            body: 'Список шагов и артефактов. Одобряешь, правишь или отменяешь.',
          },
          {
            num: '3',
            title: 'Агент выполняет пошагово',
            body: 'Один шаг, один коммит. На чекпоинтах останавливается и показывает результат.',
          },
          {
            num: '4',
            title: 'Подтверждаешь и двигаешься дальше',
            body: 'На каждом шаге: продолжить, поправить или остановить. Контроль остаётся у тебя.',
          },
        ],
      },
      runners: {
        eyebrow: 'Поддерживаемые среды',
        title: 'Один процесс — пять агентов',
        lead: 'Одинаковые команды везде. Переходишь с Cursor на Claude Code? Манифест и процесс остаются те же.',
        items: [
          { id: 'claude', name: 'Claude Code', note: 'Полная поддержка' },
          { id: 'opencode', name: 'OpenCode', note: 'Полная поддержка' },
          { id: 'qwen', name: 'Qwen Code', note: 'Полная поддержка' },
          { id: 'cursor', name: 'Cursor', note: 'Правила и MCP' },
          { id: 'aider', name: 'Aider', note: 'Конвенции и промпты' },
        ],
      },
      cta: {
        title: 'Хочешь попробовать?',
        body: 'Настройка занимает минуту: открой агента в своём проекте, вставь промпт, ответь на два вопроса.',
        button: 'Начать',
      },
    },
    start: {
      hero: {
        eyebrow: 'Начало работы',
        title: 'Настройка за минуту',
        lead: 'Скопируй полный промпт, вставь в Claude Code или OpenCode внутри своего проекта, ответь на два вопроса. Агент сделает всё остальное.',
      },
      setup: {
        title: 'Промпт для установки',
        subtitle: 'Скопировать → вставить в чат агента → ответить на два вопроса',
        previewLabel: 'Начало промпта (предпросмотр):',
        stepsTitle: 'Что произойдёт дальше',
        steps: [
          'Агент спросит, на каком языке работать (English / Русский).',
          'Изучит проект и спросит, какой агент и семейство моделей использовать.',
          'Покажет черновик .aikit/manifest.yaml и подождёт твоего одобрения.',
          'Скачает kit-setup, проверит манифест, сгенерирует файлы, сделает коммит.',
        ],
      },
      files: {
        eyebrow: 'Результат',
        title: 'Что появится в проекте',
        lead: 'Один манифест и набор файлов для выбранного агента. Существующий исходный код остаётся нетронутым.',
        tree: [
          { path: '.aikit/manifest.yaml', note: 'единый YAML для всего проекта' },
          { path: '.aikit/bin/kit-setup', note: 'бинарник (в .gitignore)' },
          { path: 'CLAUDE.md / AGENTS.md / CONVENTIONS.md', note: 'инструкции для агента, зависят от выбранной среды' },
          { path: '.claude/ или .opencode/ или .qwen/ или .cursor/', note: 'субагенты, слэш-команды, скилы' },
        ],
      },
      commands: {
        eyebrow: 'Три команды',
        title: 'Повседневная работа',
        lead: 'Одинаковые команды для любого агента. Каждая запускается в свежей сессии чата.',
        items: [
          {
            name: '/kit',
            subtitle: 'Планирование',
            body: 'Описываешь задачу одним предложением — агент читает код, оценивает риски, составляет план с артефактами, шагами и критерием завершения. Останавливается и ждёт твоего одобрения.',
          },
          {
            name: '/kit-do',
            subtitle: 'Исполнение',
            body: 'Проходит по утверждённому плану пошагово, один коммит на шаг, с проверкой. На каждом чекпоинте показывает результат и ждёт, когда скажешь продолжить.',
          },
          {
            name: '/kit-fix',
            subtitle: 'Диагностика и фикс',
            body: '4 этапа: анамнез + варианты причины → варианты фикса → предпросмотр диффа → коммит. Тривиальные шаги пропускает автоматически; подтверждение перед коммитом — всегда.',
          },
        ],
      },
      scenarios: {
        eyebrow: 'Сценарии',
        title: 'Как это выглядит на практике',
        items: [
          {
            title: 'Новая фича',
            body: '/kit "добавить CSV-экспорт для заказов". Агент читает модули, составляет план из 5 шагов, ждёт одобрения, затем /kit-do проходит по ним.',
          },
          {
            title: 'Исправить баг',
            body: '/kit-fix "кнопка сохранения перестаёт реагировать при втором клике". Изолированная сессия, не мешает текущему плану.',
          },
          {
            title: 'Сменить стек или обновить зависимости',
            body: 'Правишь stack.profiles в манифесте (например, добавляешь nextjs), запускаешь kit-setup generate — все конфиги перегенерируются.',
          },
          {
            title: 'Закрыть агенту доступ к legacy/',
            body: 'Добавляешь legacy/ в policies.forbidden_patterns — правило попадает во все сгенерированные файлы.',
          },
          {
            title: 'Добавить субагента',
            body: 'Описываешь роль в agents[] (например, review-agent), запускаешь generate — новый файл появляется в .claude/agents/ или .opencode/agents/.',
          },
        ],
      },
      cli: {
        eyebrow: 'Для тех, кто предпочитает контроль',
        title: 'CLI kit-setup',
        lead: 'Если хочешь сам управлять установкой, можно сделать всё через CLI. Бинарник на странице релизов — один файл под платформу.',
        subcommands: [
          {
            cmd: 'kit-setup verify [<path>]',
            body: 'Проверяет манифест. Возвращает JSON {valid, errors[]}. Удобен в цикле «правишь — проверяешь».',
          },
          {
            cmd: 'kit-setup generate [<path>]',
            body: 'Генерирует файлы. Возвращает JSON {ok, generated[]}. Идемпотентен — свои файлы перезаписывает чисто.',
          },
          {
            cmd: 'kit-setup schema [--format json|human]',
            body: 'Каталог встроенных шаблонов: профили, адаптеры, диалекты, команды, скилы.',
          },
        ],
        exitTitle: 'Коды выхода',
        exit: [
          { code: '0', body: 'Успех.' },
          { code: '1', body: 'Манифест невалиден (verify) или генерация отклонена (generate).' },
          { code: '2', body: 'Ошибка использования, загрузки, парсинга или ввода-вывода.' },
        ],
        releasesLink: 'Скачать бинарник →',
      },
    },
    claude: {
      hero: {
        eyebrow: 'Claude Code',
        title: 'AI Kit в Claude Code',
        lead: 'Что попадает в .claude/, как используются субагенты, скилы и хуки, и чем диалект Anthropic отличается от универсального.',
      },
      files: {
        eyebrow: 'Артефакты',
        title: 'Что появится в проекте',
        lead: 'Запуск kit-setup generate с render_targets: [claude-code] записывает ровно эти файлы. Существующий исходный код не трогается.',
        rows: [
          { path: 'CLAUDE.md', purpose: 'Главный файл инструкций: конвенции проекта, запрещённые паттерны, тело основного агента. Claude Code читает его в начале каждой сессии.' },
          { path: '.claude/agents/*.md', purpose: 'Субагенты — изолированные однократные контексты, которые основной агент запускает через Task tool. По умолчанию: Researcher.' },
          { path: '.claude/commands/*.md', purpose: 'Слэш-команды /kit, /kit-do, /kit-fix. Тело каждого файла — это промпт, который Claude Code подставляет при вызове.' },
          { path: '.claude/skills/*/SKILL.md', purpose: 'Скилы с авто-триггерами через описания. Покрывают формат планов, дебаг-цикл, варианты причин, варианты фикса и другое.' },
          { path: '.claude/prompts/*.md', purpose: 'Вспомогательные промпты для ручного использования. Не вызываются автоматически — вставляешь в чат сам.' },
          { path: '.claude/settings.json', purpose: 'Разрешения, allowlist для Bash, переменные окружения. Поставляется с разумными значениями по умолчанию.' },
          { path: '.mcp.json', purpose: 'MCP-серверы уровня проекта. Генерируется только если в manifest tools[] есть активная запись mcp-*.' },
          { path: '.claude/hooks/*.mjs', purpose: 'Опционально. Автопроверки на PreToolUse / PostToolUse / Stop. Например: блокировать Edit вне разрешённых директорий.' },
        ],
      },
      features: {
        eyebrow: 'Возможности Claude Code',
        title: 'Что AI Kit использует в этой среде',
        items: [
          {
            title: 'Субагенты через Task tool',
            body: 'Каждый файл в .claude/agents/ — изолированный однократный контекст. Основной агент делегирует им задачи через встроенный Task tool, когда нужно не засорять основной разговор. Они возвращают одно сообщение — компактный дайджест.',
          },
          {
            title: 'Авто-триггеры скилов',
            body: 'У каждого SKILL.md есть описание. Claude Code сопоставляет намерение пользователя с ним и загружает скил автоматически. AI Kit использует это, чтобы гарантировать формат блоков планирования.',
          },
          {
            title: 'Хуки-ограничители',
            body: 'PreToolUse может заблокировать вызов инструмента (например, Bash с rm -rf), PostToolUse — добавить проверку после Edit, Stop — сделать финальную валидацию. Все хуки — обычные .mjs-файлы внутри проекта.',
          },
          {
            title: 'Разрешения в settings.json, MCP в .mcp.json',
            body: 'Allowlist для Bash и явные права на запись — в .claude/settings.json. MCP-серверы уровня проекта — в .mcp.json в корне репозитория. Оба файла рецензируются как обычный код.',
          },
        ],
      },
      dialect: {
        eyebrow: 'Диалект Anthropic',
        title: 'Чем промпты отличаются от универсальных',
        lead: 'Обёртки в dialects/anthropic/ написаны под нативные API Claude: они ссылаются на Task tool по имени, используют структурированные XML-теги для длинного контекста и читаются скорее как инструкции коллеге, а не директивы машине. Универсальный диалект для не-Anthropic моделей — более директивный, без Claude-специфичных возможностей.',
      },
    },
    opencode: {
      hero: {
        eyebrow: 'OpenCode',
        title: 'AI Kit в OpenCode',
        lead: 'Те же три команды, тот же манифест — другие файлы. OpenCode поддерживает субагентов и слэш-команды, но использует TypeScript-плагины вместо хуков.',
      },
      files: {
        eyebrow: 'Артефакты',
        title: 'Что появится в проекте',
        lead: 'Запуск kit-setup generate с render_targets: [opencode] записывает эти файлы.',
        rows: [
          { path: 'AGENTS.md', purpose: 'Главный файл инструкций — аналог CLAUDE.md для OpenCode.' },
          { path: '.opencode/agents/*.md', purpose: 'Субагенты с собственным набором разрешений.' },
          { path: '.opencode/commands/*.md', purpose: 'Слэш-команды /kit, /kit-do, /kit-fix.' },
          { path: '.opencode/skills/*/SKILL.md', purpose: 'Скилы в том же формате, что и для Claude Code.' },
          { path: 'opencode.json', purpose: 'Провайдер, модель и MCP-конфигурация.' },
          { path: '.opencode/plugins/*.ts', purpose: 'TypeScript-плагины вместо хуков. Тот же ограничительный эффект через TypeScript-модули.' },
        ],
      },
      diff: {
        eyebrow: 'Отличия от Claude Code',
        title: 'Что меняется, что остаётся',
        items: [
          {
            title: 'Нет хуков — TypeScript-плагины вместо них',
            body: 'У OpenCode нет системы хуков. AI Kit генерирует .opencode/plugins/*.ts — TypeScript-модули с тем же ограничительным эффектом.',
          },
          {
            title: 'AGENTS.md вместо CLAUDE.md',
            body: 'Главный файл инструкций — AGENTS.md в корне проекта. Содержимое то же, имя файла следует конвенции OpenCode.',
          },
          {
            title: 'opencode.json для модели и MCP',
            body: 'Провайдер, семейство моделей и MCP-серверы — в opencode.json в корне проекта.',
          },
        ],
      },
    },
    qwen: {
      hero: {
        eyebrow: 'Qwen Code',
        title: 'AI Kit в Qwen Code',
        lead: 'Те же три команды, тот же манифест. Qwen Code повторяет структуру OpenCode, но все файлы лежат в .qwen/.',
      },
      files: {
        eyebrow: 'Артефакты',
        title: 'Что появится в проекте',
        lead: 'Запуск kit-setup generate с render_targets: [qwen-code] записывает эти файлы.',
        rows: [
          { path: '.qwen/AGENTS.md', purpose: 'Главный файл инструкций. Путь объявлен в settings.json через contextFileName.' },
          { path: '.qwen/agents/*.md', purpose: 'Субагенты.' },
          { path: '.qwen/commands/*.md', purpose: 'Слэш-команды.' },
          { path: '.qwen/skills/*/SKILL.md', purpose: 'Скилы.' },
          { path: '.qwen/settings.json', purpose: 'Настройки модели (modelProviders) и MCP-серверы.' },
        ],
      },
      diff: {
        eyebrow: 'Отличия от OpenCode',
        title: 'Что меняется',
        items: [
          {
            title: 'Всё в .qwen/',
            body: 'Все файлы агента — субагенты, команды, скилы, настройки — лежат в .qwen/ вместо .opencode/.',
          },
          {
            title: 'contextFileName в settings.json',
            body: 'Qwen Code читает путь к главному файлу инструкций из settings.json через contextFileName, который указывает на .qwen/AGENTS.md.',
          },
          {
            title: 'Универсальный диалект с Qwen-правками',
            body: 'Промпты берутся из dialects/qwen/ — поддиректории универсального диалекта с небольшими поправками под контекстное окно и стиль инструкций модели.',
          },
        ],
      },
    },
    cursor: {
      hero: {
        eyebrow: 'Cursor',
        title: 'AI Kit в Cursor',
        lead: 'Частичная поддержка. Нет субагентов, нет слэш-команд. AI Kit записывает правила проекта как .cursor/rules/*.mdc и добавляет .cursor/mcp.json для MCP-серверов.',
      },
      files: {
        eyebrow: 'Артефакты',
        title: 'Что появится в проекте',
        lead: 'Запуск kit-setup generate с render_targets: [cursor] записывает эти файлы.',
        rows: [
          { path: '.cursor/rules/*.mdc', purpose: 'Правила проекта с alwaysApply: true. Заменяет CLAUDE.md / AGENTS.md для Cursor.' },
          { path: '.cursor/mcp.json', purpose: 'Конфигурация MCP-серверов для Cursor.' },
        ],
      },
      limits: {
        eyebrow: 'Ограничения',
        title: 'Что значит частичная поддержка',
        items: [
          {
            title: 'Нет субагентов',
            body: 'В Cursor нет Task tool или аналога. Researcher и другие субагенты не генерируются — агент работает в одном контексте.',
          },
          {
            title: 'Нет слэш-команд',
            body: '/kit, /kit-do и /kit-fix недоступны как нативные слэш-команды. Содержимое промптов можно вставить вручную, но это ручная работа.',
          },
          {
            title: 'Правила вместо файла памяти',
            body: 'Описание проекта и конвенции попадают в .cursor/rules/*.mdc с alwaysApply: true. Cursor загружает их автоматически, агент знает стек — но у команд планирования и исполнения нет выделенных триггеров.',
          },
        ],
      },
    },
    aider: {
      hero: {
        eyebrow: 'Aider',
        title: 'AI Kit в Aider',
        lead: 'Частичная поддержка. Нет субагентов, нет слэш-команд. AI Kit записывает CONVENTIONS.md, .aider.conf.yml и пользовательские промпты в .aider/prompts/.',
      },
      files: {
        eyebrow: 'Артефакты',
        title: 'Что появится в проекте',
        lead: 'Запуск kit-setup generate с render_targets: [aider] записывает эти файлы.',
        rows: [
          { path: 'CONVENTIONS.md', purpose: 'Главный файл инструкций. Aider загружает его автоматически при старте сессии.' },
          { path: '.aider.conf.yml', purpose: 'Глобальная настройка модели и конфигурация Aider.' },
          { path: '.aider/prompts/*.md', purpose: 'Пользовательские промпты для рабочего цикла /kit, /kit-do, /kit-fix. Вставляй в чат вручную.' },
        ],
      },
      limits: {
        eyebrow: 'Ограничения',
        title: 'Что значит частичная поддержка',
        items: [
          {
            title: 'Нет субагентов',
            body: 'Aider не поддерживает изолированные контексты субагентов. Вся работа — в одной сессии.',
          },
          {
            title: 'Нет слэш-команд',
            body: '/kit, /kit-do и /kit-fix не имеют выделенных триггеров. Промпты лежат в .aider/prompts/ — копируй и вставляй вручную.',
          },
          {
            title: 'CONVENTIONS.md — основа',
            body: 'Aider читает CONVENTIONS.md автоматически. Описание стека, конвенции и запрещённые паттерны попадают туда — агент знает проект с первого старта.',
          },
        ],
      },
    },
  },
}
