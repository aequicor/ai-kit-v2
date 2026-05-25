---
name: kotlin-build
description: How to drive Gradle builds in KMP+Ktor project ${bundle.input.projectName} efficiently — daemon, configuration cache, build cache, --offline, and task selection per project type (Ktor server / Compose app / KMP fullstack / KMP library). Use when asked to build, test, or troubleshoot Gradle performance.
---

# kotlin-build

Гид по корректной работе с Gradle в **${bundle.input.projectName}**.

## Базовые правила

- Используй wrapper `./gradlew`, не глобальный `gradle`.
- Не отключай configuration cache и build cache без причины — они дают кратный выигрыш.
- Долгие сборки — в фоне с опросом статуса, не блокируй чат.
- Никогда не передавай секреты через `-P`/env — они попадают в configuration cache.

## Полезные флаги

| Флаг | Когда |
|---|---|
| `--no-configuration-cache` | Только для диагностики, если падает на configuration cache |
| `--offline` | Изолировать от сети, воспроизвести «у меня работает» |
| `--stacktrace` | Полный stack trace падения |
| `--warning-mode all` | Видеть deprecations |
| `--rerun-tasks` | Принудительный re-run, игнорируя up-to-date |
| `--scan` | **Не использовать без явного запроса** — утечка данных проекта на scans.gradle.com |

## Выбор task под тип проекта

<!-- when: ${bundle.input.projectType} == 'ktor-server' -->
- Сборка: `./gradlew build`
- Тесты: `./gradlew test` (эндпоинты — через `testApplication`)
- Запуск локально: `./gradlew run` (или `:server:run`)
- Fat-jar/дистрибутив: `./gradlew shadowJar` / `installDist` (если плагин подключён)
<!-- end -->
<!-- when: ${bundle.input.projectType} == 'compose-app' -->
- Все тесты: `./gradlew allTests`
- Desktop-запуск: ищи run-таску (`:desktopApp:run` / `:composeApp:run` / `runDistributable`) через `./gradlew tasks --all`
- Android debug: `./gradlew assembleDebug`, unit — `testDebugUnitTest`
<!-- end -->
<!-- when: ${bundle.input.projectType} == 'kmp-fullstack' -->
- Сборка: `./gradlew build`
- Все тесты: `./gradlew allTests`; только JVM для скорости — `./gradlew :shared:jvmTest`
- Сервер: `./gradlew :server:run`; клиент — см. run-таску Compose-модуля
- Не используй `Dispatchers.*` в `commonMain` — недоступны на части таргетов
<!-- end -->
<!-- when: ${bundle.input.projectType} == 'kmp-library' -->
- Сборка: `./gradlew build`
- Все тесты: `./gradlew allTests`
- Проверка бинарной совместимости (если подключён binary-compatibility-validator): `./gradlew apiCheck`
- Публикация локально для проверки: `./gradlew publishToMavenLocal`
<!-- end -->

## Гигиена daemon

- Daemon живёт между запусками — это норма.
- Зависло/странно себя ведёт: `./gradlew --stop`, повторить.
- Не используй `--no-daemon` без причины — замедляет повторные сборки.

## Что НЕ делать

- Не править `gradle/wrapper/gradle-wrapper.properties` без явного запроса (смена версии Gradle).
- Не апгрейдить плагины «заодно» — Kotlin / AGP / KSP / Compose Compiler жёстко связаны по версиям.
