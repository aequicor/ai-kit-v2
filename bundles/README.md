# bundles/

Бандл — пакет шаблонов и манифестов, который CLI AI-Kit раскатывает в конфигурацию AI-агента (Claude Code, OpenCode, …). Эта папка содержит исходники всех бандлов репозитория.

## Прежде чем создавать или менять бандл — прочитай

Спецификации формата — источник истины. Сначала читай их, потом этот README:

- [`../kit-setup/BUNDLE_JSON.md`](../kit-setup/BUNDLE_JSON.md) — манифест `bundle.json`: совместимость, метаданные, `targets`, `inputs`.
- [`../kit-setup/CONFIG_JSON.md`](../kit-setup/CONFIG_JSON.md) — `config.json` агента + язык выражений AKEL для `when`.
- [`../kit-setup/TEMPLATE_MD.md`](../kit-setup/TEMPLATE_MD.md) — шаблонизация `.md`.
- [`../kit-setup/MANIFEST_JSON.md`](../kit-setup/MANIFEST_JSON.md) — `.aikit/manifest.json` пользовательского проекта.

Этот README — навигатор и чек-лист, он не дублирует спецификации.

## Раскладка папки бандла

Референс — [`simple-kit/0.0.1/`](simple-kit/0.0.1/).

```
<bundle-name>/
  <X.Y.Z>/
    bundle.json                  # schemaVersion 2 + kitSetup
    <agent-id>/                  # одна папка на каждый id из targets
      config.json
      CLAUDE.md                  # или AGENTS.md и т.п. — по агенту
      commands/  skills/  subagents/  hooks/
```

Конвенции, которых нет в спецификациях:

- Путь обязан совпадать с `name` и `version`: `bundles/<name>/<version>/`.
- Каждый официальный бандл обязан объявить `schemaVersion: 2`, `kitSetup`, `tags`, `bestFor` и `notFor`.
- Для каждого id из `targets` обязана существовать одноимённая папка в корне бандла.

## inputs → файлы (шпаргалка)

Полные правила — в [`../kit-setup/BUNDLE_JSON.md`](../kit-setup/BUNDLE_JSON.md). Краткая связь между типом `input` и раскладкой:

| Тип | Раскладка | Применяется |
|---|---|---|
| `boolean` | `<agent>/<id>/…` | если `true` |
| `select` | `<agent>/<id>/<value>/…` | только выбранная подпапка |
| `multiselect` | `<agent>/<id>/<value>/…` | все выбранные подпапки |
| `string` / `int` / `double` | не управляет файлами | подставляется через `${bundle.input.<id>}` |

## Шаблонизация и условия

- В `.md`-шаблонах: подстановки `${bundle.input.<id>}` и условные блоки. Детали — [`../kit-setup/TEMPLATE_MD.md`](../kit-setup/TEMPLATE_MD.md).
- В `config.json`: поле `when` с AKEL-выражением включает/исключает элемент массива. Детали — [`../kit-setup/CONFIG_JSON.md`](../kit-setup/CONFIG_JSON.md).

## Способы распространения бандла

| Способ | `source` в манифесте потребителя | Когда использовать |
|---|---|---|
| **Official remote** — версия из этого каталога | `"remote"` | CLI разрешает `name@version` в `bundles/<name>/<version>/` и фиксирует commit sha |
| **Third-party remote** — папка в GitHub-репозитории | `remote:<owner>/<repo>/<path>[@<branch>]` | Публичные сторонние бандлы |
| **Локальный** — папка или `.zip` в `.aikit/bundles/` проекта | путь (`./.aikit/bundles/…`) | Приватные/экспериментальные бандлы |

`internal` и `embedded:` больше не поддерживаются. Скачанный отдельно официальный бандл можно установить как обычную локальную папку или ZIP. Детали — в [`../kit-setup/MANIFEST_JSON.md`](../kit-setup/MANIFEST_JSON.md).

## Версионирование бандла

SemVer `MAJOR.MINOR.PATCH` в поле `version` манифеста:

| Что изменилось | Тип бампа |
|---|---|
| Сломана обратная совместимость манифеста (изменился `schemaVersion`, удалены/переименованы поля `bundle.json`/`config.json`) | **MAJOR** |
| Добавлены опциональные поля/значения `enum`, новые `inputs` с дефолтами — без breaking changes | **MINOR** |
| Изменился **только** контент шаблонов (`*.md`) | **PATCH** |

После бампа создай новую папку `<name>/<X.Y.Z>/`; опубликованные версии не изменяй задним числом.

## Чек-лист перед коммитом

- [ ] Все id из `targets` имеют одноимённую папку в корне бандла.
- [ ] Все id из `inputs` уникальны.
- [ ] `default` валиден: входит в `options` (для `select`/`multiselect`) и попадает в `[min, max]` (для `int`/`double`).
- [ ] Id всех `boolean`/`select`/`multiselect` inputs соответствуют существующим папкам-компонентам хотя бы у одного агента.
- [ ] Шаблоны и `config.json` ссылаются только на существующие файлы внутри бандла.
- [ ] Папка бандла переименована под новую версию, `version` в `bundle.json` поднят.
