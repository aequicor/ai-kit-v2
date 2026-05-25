---
description: Run ktlint + detekt + tests for the project and report
argument-hint: [gradle-module]
---

# /build-check

Прогони цикл качества для **${bundle.input.projectName}** и верни короткий отчёт.

Если передан `$1` — гоняй на этом Gradle-модуле (`:$1:...`), иначе на корневом проекте.

## Шаги

<!-- when: 'ktlint' in ${bundle.input.qualityTools} -->
1. `./gradlew ktlintCheck` — формат. Упало — сначала `./gradlew ktlintFormat`, потом повтори `ktlintCheck`.
<!-- end -->
<!-- when: 'detekt' in ${bundle.input.qualityTools} -->
2. `./gradlew detekt` — статанализ.
<!-- end -->
3. Тесты под тип проекта:
<!-- when: ${bundle.input.projectType} == 'ktor-server' -->`./gradlew test`<!-- end --><!-- when: ${bundle.input.projectType} in ['compose-app', 'kmp-fullstack', 'kmp-library'] -->`./gradlew allTests`<!-- end -->.

Долгие команды — в фоне с опросом статуса, не блокируй чат.

<!-- when: 'gradle-troubleshooter' in ${bundle.input.subagents} -->
Упала сборка с непонятной ошибкой Gradle (configuration cache, конфликт версий, плагины) — делегируй разбор субагенту **gradle-troubleshooter**.
<!-- end -->

## Формат отчёта

```
## ktlint
pass | fail (N issues)

## detekt
pass | fail (N issues)

## tests
pass | fail (N of M failed)

## Failures
- <test> — <file:line> — <одна строка причины>
```

Все прошли — «Status: green».
