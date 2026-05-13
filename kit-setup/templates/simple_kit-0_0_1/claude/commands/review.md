---
description: Review the current branch against repo conventions
argument-hint: [base-branch]
---

# /review

Сделай ревью текущей ветки проекта **${bundle.input.projectName}** против `$1` (по умолчанию `main`).

Активируй скил **review** и следуй его инструкциям.

<!-- when: 'security-review' in ${bundle.input.skills} -->
После основного ревью — отдельным проходом активируй скил **security-review**.
<!-- end -->

<!-- when: 'code-reviewer' in ${bundle.input.subagents} -->
Если изменения крупные — параллельно делегируй второй проход субагенту **code-reviewer** для независимой оценки.
<!-- end -->
