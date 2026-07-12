---
name: review
description: Review the current branch's diff against repo conventions in ${bundle.input.projectName}. Use when the user asks to "review", "check the changes", "look at the diff", or before opening a PR.
---

# /review

Сделай ревью текущей ветки проекта **${bundle.input.projectName}**.

## Шаги

1. `git diff main...HEAD` — посмотри что изменилось.
2. `git log main..HEAD` — пойми намерение (по сообщениям коммитов).
3. Прочитай изменённые файлы целиком, не только diff-окна.
4. Проверь:
   - Соответствие конвенциям репозитория (см. CLAUDE.md).
   - Нет ли мёртвого кода / закомментированных блоков.
   - Покрыты ли изменения тестами.
   - Нет ли утечек секретов (`.env`, ключи, токены).

<!-- when: ${bundle.input.strict} -->
5. Strict-режим: дополнительно проверь, что нет `rm -rf`, force push, временных хаков `// TODO: fix later`.
<!-- end -->

## Формат отчёта

- **Critical** (блокеры) — что обязательно поправить до merge.
- **Suggestions** — необязательные улучшения.
- **Nits** — стилистика.

Если ничего не нашёл — так и скажи. Не выдумывай замечания ради объёма.
