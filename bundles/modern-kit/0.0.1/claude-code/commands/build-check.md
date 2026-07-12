---
description: Run ktlint + detekt + tests for the Kotlin project and report
argument-hint: [gradle-module]
---

# /build-check

Прогони цикл качества для проекта **${bundle.input.projectName}** и верни короткий отчёт.

Если передан `$1` — гоняй на этом Gradle-модуле (`:$1:...`), иначе на корневом проекте.

## Шаги

<!-- when: 'ktlint' in ${bundle.input.qualityTools} -->
1. `./gradlew ktlintCheck` — формат. Если упало — сначала попробуй `./gradlew ktlintFormat`, потом повтори `ktlintCheck`.
<!-- end -->
<!-- when: 'detekt' in ${bundle.input.qualityTools} -->
2. `./gradlew detekt` — статанализ.
<!-- end -->
3. `./gradlew test` — тесты (для KMP — `allTests`; для Android — `testDebugUnitTest`).

Долгие команды запускай в фоне и опрашивай статус. Не ждать синхронно блокирующе.

<!-- when: 'gradle-troubleshooter' in ${bundle.input.subagents} -->
Если упала сборка с непонятной ошибкой Gradle (конфигурационный кэш, конфликт версий, плагины) — делегируй разбор субагенту **gradle-troubleshooter**.
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

Если все три прошли — «Status: green».
