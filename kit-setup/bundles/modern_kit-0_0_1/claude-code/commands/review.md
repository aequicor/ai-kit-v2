---
description: Review the current branch against Kotlin repo conventions
argument-hint: [base-branch]
---

# /review

Сделай ревью текущей ветки Kotlin-проекта **${bundle.input.projectName}** против `$1` (по умолчанию `main`).

Активируй скил **review** и следуй его инструкциям. Дополнительно проверь Kotlin-специфику: `!!`, `GlobalScope`, прямое использование `Dispatchers.*`, отсутствие KDoc на новых публичных API.

<!-- when: 'security-review' in ${bundle.input.skills} -->
После основного ревью — отдельным проходом активируй скил **security-review**.
<!-- end -->

<!-- when: 'code-reviewer' in ${bundle.input.subagents} -->
Если изменения крупные — параллельно делегируй второй проход субагенту **code-reviewer** для независимой оценки.
<!-- end -->
