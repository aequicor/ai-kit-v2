# bundles/

Бандл — пакет шаблонов и манифестов, который CLI AI-Kit раскатывает в конфигурацию AI-агента (Claude Code, OpenCode, …). Эта папка содержит исходники всех бандлов репозитория.

## Прежде чем создавать или менять бандл — прочитай

Спецификации формата — источник истины. Сначала читай их, потом этот README:

- [`../BUNDLE_JSON.md`](../BUNDLE_JSON.md) — манифест `bundle.json`: метаданные, `targets`, `inputs`.
- [`../CONFIG_JSON.md`](../CONFIG_JSON.md) — `config.json` агента + язык выражений AKEL для `when`.
- [`../TEMPLATE_MD.md`](../TEMPLATE_MD.md) — шаблонизация `.md`: подстановки `${bundle.input.<id>}` и условные блоки `<!-- when: … --> … <!-- end -->`.
- [`../MANIFEST_JSON.md`](../MANIFEST_JSON.md) — `.aikit/manifest.json` пользовательского проекта (контекст потребителя бандла).

Этот README — навигатор и чек-лист, он не дублирует спецификации.

## Раскладка папки бандла

Референс — [`simple_kit-0_0_1/`](simple_kit-0_0_1/).

```
<bundle-name>-<X_Y_Z>/
  bundle.json                    # манифест бандла
  <agent-id>/                    # одна папка на каждый id из targets
    config.json                  # маппинг в конфигурацию агента
    CLAUDE.md                    # или AGENTS.md и т.п. — по агенту
    commands/  skills/  subagents/  hooks/
    <component>/…                # папки под boolean/select/multiselect inputs
```

Конвенции, которых нет в спецификациях:

- **Имя папки бандла** — `<name>-<version с _ вместо .>` (например, `simple-kit` версии `0.0.1` → `simple_kit-0_0_1`).
- Для каждого id из `targets` обязана существовать одноимённая папка в корне бандла.

## inputs → файлы (шпаргалка)

Полные правила — в [`../BUNDLE_JSON.md`](../BUNDLE_JSON.md). Краткая связь между типом `input` и раскладкой:

| Тип | Раскладка | Применяется |
|---|---|---|
| `boolean` | `<agent>/<id>/…` | если `true` |
| `select` | `<agent>/<id>/<value>/…` | только выбранная подпапка |
| `multiselect` | `<agent>/<id>/<value>/…` | все выбранные подпапки |
| `string` / `int` / `double` | не управляет файлами | подставляется через `${bundle.input.<id>}` |

## Шаблонизация и условия

- В `.md`-шаблонах: подстановки `${bundle.input.<id>}` и блоки `<!-- when: <AKEL> --> … <!-- end -->`. Детали — [`../TEMPLATE_MD.md`](../TEMPLATE_MD.md).
- В `config.json`: поле `when` с AKEL-выражением включает/исключает элемент массива. Детали — [`../CONFIG_JSON.md`](../CONFIG_JSON.md).

## Версионирование бандла

SemVer `MAJOR.MINOR.PATCH` в поле `version` манифеста:

| Что изменилось | Тип бампа |
|---|---|
| Сломана обратная совместимость манифеста (изменился `schemaVersion`, удалены/переименованы поля `bundle.json`/`config.json`) | **MAJOR** |
| Добавлены опциональные поля/значения `enum`, новые `inputs` с дефолтами — без breaking changes | **MINOR** |
| Изменился **только** контент шаблонов (`*.md`) | **PATCH** |

После бампа: переименуй папку бандла под новую версию (`<name>-<X_Y_Z>`) и подними `version` в `bundle.json`.

## Чек-лист перед коммитом

- [ ] Все id из `targets` имеют одноимённую папку в корне бандла.
- [ ] Все id из `inputs` уникальны.
- [ ] `default` валиден: входит в `options` (для `select`/`multiselect`) и попадает в `[min, max]` (для `int`/`double`).
- [ ] Id всех `boolean`/`select`/`multiselect` inputs соответствуют существующим папкам-компонентам хотя бы у одного агента.
- [ ] Шаблоны и `config.json` ссылаются только на существующие файлы внутри бандла.
- [ ] Папка бандла переименована под новую версию, `version` в `bundle.json` поднят.
