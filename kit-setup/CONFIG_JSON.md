# config.json

Манифест поддержки конкретного ИИ-агента внутри бандла. Лежит в `<bundle>/<agent>/config.json` (например, `<bundle>/claude/config.json`). Описывает фичи агента, которые бандл устанавливает: settings, MCP-серверы, subagents, slash-команды, скилы, хуки, права инструментов и т. д.

Формат — обычный JSON с двумя расширениями: подстановка значений и условный блок `when`. Никаких других управляющих конструкций нет.

## Манифест ≠ нативный конфиг агента

`config.json` — **собственный формат бандла**, а не прямой `~/.claude/settings.json` или `.mcp.json`. После обработки `when` и подстановок CLI передаёт результат **генератору агента**, который транслирует промежуточное дерево в каноническую конфигурацию конкретного агента (Claude Code, OpenCode и т. д.).

Практические следствия:

- В манифесте можно использовать формы, удобные для условной активации (например, элементы массива как объекты `{ "value": "…", "when": "…" }`), даже если в нативном конфиге агента ожидается массив строк. Генератор разворачивает обёртки в нативную форму.
- JSON-schema нативного конфига агента **не применяется** к манифесту. Валидация манифеста — отдельная (по схеме `config.json`).
- Все ключи в манифесте — это контракт между бандлом и генератором, не контракт между бандлом и агентом напрямую.

## Управляющие конструкции

### 1. Подстановка значения: `${bundle.input.<id>}`

В любой строке манифеста плейсхолдер заменяется на значение input'а во время генерации.

```json
{
  "settings": {
    "model": "${bundle.input.model}"
  },
  "mcpServers": [
    {
      "name": "github",
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github", "--token", "${bundle.input.githubToken}"]
    }
  ]
}
```

Свойства:

- Работает только внутри **строк**. Если значение нужно подставить как число/массив/объект — сериализуется в JSON.
- Тип подставляемого значения определяется типом input'а (см. `BUNDLE_JSON.md`).
- Префикс `bundle.input.` зарезервирован для будущих источников значений (`bundle.meta.*`, `project.*` и т. п.). Сейчас доступен только `bundle.input.<id>`.
- Ссылка на несуществующий `<id>` → ошибка генерации (если выражение фактически вычисляется — см. правила вырезания).

### 2. Условный блок: поле `when`

`when` — мета-ключ, допустимый в **любом объекте** манифеста. Его значение — булево выражение в виде строки.

```json
{
  "name": "github",
  "when": "${bundle.input.githubMcp}",
  "command": "npx"
}
```

Правила обработки:

1. Парсер обходит дерево манифеста сверху вниз.
2. Для каждого объекта вычисляется `when` (если есть).
3. Если `when` → `false`: **весь объект вырезается** из дерева. Вложенные `when` не вычисляются.
4. Если `when` → `true` или отсутствует: ключ `when` удаляется, остальные поля передаются дальше.
5. Интерполяция `${bundle.input.<id>}` в строках выполняется **после** фильтрации `when` — по уже урезанному дереву.

После обработки дерево не содержит ни одного `when` и ни одной ветки, помеченной `false`. Генератор работает с чистым JSON и о существовании `when` не знает.

Дополнительно:

- `when` работает только на объектах. Чтобы условно убрать элемент массива примитивов — оборачивай его в объект; генератор агента развернёт обёртку в нативную форму:
  ```json
  "allow": [
    { "value": "Bash(npm run test:*)" },
    { "value": "Bash(git push:*)", "when": "${bundle.input.profile} == 'trusted'" }
  ]
  ```
  Имя поля-носителя (`value` в примере) определяется генератором конкретного агента и фиксируется в его спецификации поддержки.
- Удаление элемента массива сдвигает индексы: `[a, b{when:false}, c]` → `[a, c]`. Никаких `null`-дырок.
- Пустой массив после фильтра остаётся пустым массивом, не удаляется.

## Язык выражений (AKEL)

Используется в значении `when` (и только там).

### Литералы

| Тип | Синтаксис | Пример |
|---|---|---|
| boolean | `true`, `false` | `true` |
| int | `[-]?[0-9]+` | `42`, `-7` |
| double | `[-]?[0-9]+\.[0-9]+` | `0.7` |
| string | `'…'` (одинарные кавычки) | `'full'` |
| list | `[ expr, expr, … ]` | `['ci', 'full']` |

Строки только в `'…'` — двойные кавычки уже занимает JSON-обёртка.

### Ссылка на input

```
${bundle.input.<id>}
```

Тип ссылки = тип input'а:

| Тип input | Тип в AKEL |
|---|---|
| `boolean` | boolean |
| `string` | string |
| `int` | int |
| `double` | double |
| `select` | string |
| `multiselect` | list of string |

### Операторы

| Категория | Операторы | Применимо к |
|---|---|---|
| Логика | `&&`, `\|\|`, `!` | boolean |
| Равенство | `==`, `!=` | одинаковые типы |
| Сравнение | `<`, `<=`, `>`, `>=` | int, double, string (лексикографически) |
| Членство | `in` | `<value> in <list>` |
| Группировка | `(`, `)` | — |

Приоритет (от высшего к низшему): `!` → сравнения → `==`/`!=` → `in` → `&&` → `||`.

Арифметики, функций, тернарного оператора нет.

### Семантика

- **Жёсткая типизация.** Никаких автоприведений: `${bundle.input.count} == '5'` при int → ошибка.
- **Boolean как `when`.** `"when": "${bundle.input.flag}"` эквивалентно `"when": "${bundle.input.flag} == true"`.
- **Короткое замыкание.** `false && X` и `true || X` не вычисляют правую часть.
- **`in`** — правая часть list, левая — элемент совместимого типа.

### Грамматика (EBNF)

```
expr        = or_expr ;
or_expr     = and_expr  { "||" and_expr } ;
and_expr    = in_expr   { "&&" in_expr } ;
in_expr     = eq_expr   [ "in" list_lit ] ;
eq_expr     = cmp_expr  [ ( "==" | "!=" ) cmp_expr ] ;
cmp_expr    = unary     [ ( "<" | "<=" | ">" | ">=" ) unary ] ;
unary       = [ "!" ] primary ;
primary     = literal
            | ref
            | list_lit
            | "(" expr ")" ;

literal     = bool_lit | int_lit | double_lit | string_lit ;
bool_lit    = "true" | "false" ;
int_lit     = [ "-" ] digit { digit } ;
double_lit  = [ "-" ] digit { digit } "." digit { digit } ;
string_lit  = "'" { any_char_except_quote } "'" ;
list_lit    = "[" [ expr { "," expr } ] "]" ;
ref         = "${" "bundle" "." "input" "." identifier "}" ;
identifier  = ( letter | "_" ) { letter | digit | "_" | "-" } ;
```

## Порядок обработки

1. JSON-парсинг манифеста.
2. Top-down проход по дереву: вычисление `when` каждого объекта.
   - `false` → объект удаляется, вложенные `when` не вычисляются.
   - `true` / отсутствует → ключ `when` удаляется, идём вглубь.
3. Интерполяция `${bundle.input.<id>}` во всех оставшихся строках.
4. Результат — чистый JSON — передаётся генератору агента.

## Ошибки

| Когда | Поведение |
|---|---|
| Синтаксическая ошибка в `when` | прерывание установки с указанием позиции |
| Несуществующий `${bundle.input.<id>}` в активной ветке | прерывание |
| Конфликт типов в выражении (`int == string`, `!int`, `x in (не-list)`) | прерывание |
| Несуществующий `${bundle.input.<id>}` в ветке, отрезанной `when` | игнорируется (не вычисляется) |

## Пример (Claude Code)

```json
{
  "schemaVersion": 1,
  "agent": "claude-code",
  "minVersion": "1.0.0",
  "scope": "project",

  "settings": {
    "model": "${bundle.input.model}",
    "includeCoAuthoredBy": false,

    "permissions": {
      "defaultMode": "acceptEdits",
      "allow": [
        { "value": "Bash(npm run test:*)" },
        { "value": "Bash(git push:*)", "when": "${bundle.input.profile} == 'trusted'" }
      ],
      "deny": [
        { "value": "Read(./.env)" },
        { "value": "Read(./secrets/**)", "when": "!${bundle.input.allowSecrets}" }
      ]
    }
  },

  "mcpServers": [
    {
      "when": "${bundle.input.githubMcp}",
      "name": "github",
      "transport": "stdio",
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"]
    },
    {
      "when": "${bundle.input.knowledgeOs}",
      "name": "knowledge-os",
      "transport": "sse",
      "url": "http://localhost:8080/sse"
    }
  ],

  "agents": [
    { "when": "${bundle.input.reviewer}",          "name": "code-reviewer", "source": "files/code-reviewer.md" },
    { "when": "${bundle.input.profile} == 'full'", "name": "test-runner",   "source": "files/test-runner.md" }
  ],

  "commands": [
    { "when": "${bundle.input.profile} in ['ci','full']", "name": "release", "source": "files/release.md" },
    { "when": "${bundle.input.deploy}",                    "name": "deploy",  "source": "files/deploy.md" }
  ],

  "skills": [
    { "when": "'review' in ${bundle.input.skills}",          "name": "review",          "source": "files/review/" },
    { "when": "'security-review' in ${bundle.input.skills}", "name": "security-review", "source": "files/security-review/" }
  ],

  "hooks": {
    "PreToolUse": [
      {
        "when": "${bundle.input.strict}",
        "matcher": "Bash",
        "command": ".claude/hooks/block-dangerous.sh"
      }
    ]
  }
}
```

## Агент `codex` (OpenAI Codex CLI)

Папка таргета — `codex/`, ключ в манифесте проекта — `"codex"`. Особенности:

- **Нативный конфиг — TOML.** Секции `settings` и `mcpServers` из `config.json` транслируются генератором в `.codex/config.toml` (project-override, Codex подхватывает его для доверенных проектов). Ключи `settings` пишутся в camelCase и конвертируются в snake_case: `modelReasoningEffort` → `model_reasoning_effort`, `approvalPolicy` → `approval_policy`, `sandboxMode` → `sandbox_mode`, `webSearch` → `web_search`. Объект `features` становится таблицей `[features]`. Каждый MCP-сервер — таблицей `[mcp_servers.<name>]` (поля `command`, `args`, `env`, `url`, `timeout` → `timeout_secs`); per-entry `when` работает как у остальных агентов.
- **Раскладка файлов:** `memory` → `AGENTS.md` в корне application; `agents` → `.codex/agents/<name>.toml` (сабагенты Codex — TOML-файлы с полями `name`, `description`, `developer_instructions`); `commands` → `.codex/prompts/<name>.md`.
- **Нет `skills` и `hooks`** — у Codex нет скилов и lifecycle-хуков; эти секции в `codex/config.json` не поддерживаются.

Минимальный пример `codex/config.json`:

```json
{
  "schemaVersion": 1,
  "agent": "codex",
  "settings": {
    "approvalPolicy": "on-request",
    "sandboxMode": "workspace-write",
    "features": { "multi_agent": true }
  },
  "memory": [ { "name": "AGENTS.md", "source": "AGENTS.md" } ],
  "mcpServers": [
    {
      "when": "${bundle.input.githubMcp}",
      "name": "github",
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "timeout": 30
    }
  ],
  "agents": [ { "name": "code-reviewer", "source": "agents/code-reviewer.toml" } ],
  "commands": [ { "name": "review", "source": "prompts/review.md" } ]
}
```

## Чего намеренно нет

| Не входит | Почему |
|---|---|
| `fileExists`, `envExists`, доступ к ФС/env | сложная реализация, security-риск; пользователь сам отвечает за условие через input |
| Арифметика (`+`, `-`, `*`) | манифест — декларация, не вычислитель |
| Тернарный оператор `? :` | дублирует `when` |
| Функции (`length`, `startsWith`, regex) | избыточная поверхность поддержки, отложено |
| Циклы, шаблоны типа `{{#each}}` | для повторов используются `multiselect` + конвенция файлов |
| Глобальные переменные (`env`, `cwd`, `os`) | как `fileExists` — выносится в input'ы |
