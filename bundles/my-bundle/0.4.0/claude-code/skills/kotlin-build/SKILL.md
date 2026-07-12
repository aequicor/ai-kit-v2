---
name: kotlin-build
description: How to drive Gradle builds in ${bundle.input.projectName} efficiently and debug failures — wrapper discipline, configuration/build cache, daemon hygiene, long builds in background, task selection, dependency conflicts and ktlint/detekt. Use when asked to build or test, when a Gradle build fails or hangs, on configuration-cache or dependency-resolution errors, or when builds are slow.
---

# kotlin-build

Работа с Gradle в **${bundle.input.projectName}**.

## Базовые правила

- Только wrapper: `./gradlew` из корня соответствующего модуля/проекта, не глобальный `gradle`.
- Не отключай configuration cache и build cache без причины — они дают кратный выигрыш.
- Долгие сборки — в фоне с опросом статуса; не блокируй чат ожиданием.
- Никогда не передавай секреты через `-P`/env сборки — они попадают в configuration cache.
- Точечные таски быстрее полного `build`: компиляция модуля — `:module:compileKotlin`/`compile*`, один тест-класс — `--tests "…ClassName"`.

## Типовые таски

| Что | Команда |
|---|---|
| Сборка | `./gradlew build` |
| Тесты JVM-модуля | `./gradlew :module:test` |
| Тесты KMP (все таргеты) | `./gradlew allTests` (быстрее: `:module:jvmTest`) |
| Список тасок | `./gradlew tasks --all` |
| Зависимости конфигурации | `./gradlew :module:dependencies --configuration runtimeClasspath` |
| Откуда пришла зависимость | `./gradlew :module:dependencyInsight --dependency <name>` |

## Полезные флаги

| Флаг | Когда |
|---|---|
| `--stacktrace` | полный stack trace падения |
| `--info` / `--debug` | больше контекста (шумно; сначала `--info`) |
| `--no-configuration-cache` | только диагностика падения на config cache |
| `--offline` | воспроизвести «у меня работает» без сети |
| `--rerun-tasks` | принудительный re-run, игнор up-to-date |
| `--warning-mode all` | увидеть deprecations перед апгрейдом |
| `--scan` | **не использовать без явного запроса** — публикует данные проекта |

## Разбор падений

1. Читай **первую** ошибку (`* What went wrong`), не хвост лога; повтори с `--stacktrace`.
2. Configuration cache: ошибка называет task и проблему (чтение env/файла на этапе конфигурации) — чини причину, `--no-configuration-cache` только чтобы подтвердить диагноз.
3. Конфликт версий: `dependencyInsight` покажет, кто притянул; фиксируй версию в каталоге (`libs.versions.toml`), а не разовыми `force`.
4. `Could not resolve`: сеть/репозитории/опечатка координат; проверь `--offline` не включён ли.
5. Странное состояние daemon'а: `./gradlew --stop`, повторить; кэш вручную не удалять (см. strict-хук).
6. OOM: `org.gradle.jvmargs=-Xmx…` в `gradle.properties`, не разовые env-хаки.

## Гигиена daemon

- Daemon между запусками — норма; `--no-daemon` без причины замедляет всё.
- Завис — `./gradlew --stop` и повторить, не `kill -9` и не чистка `~/.gradle`.

## ktlint / detekt (если подключены)

- Формат: `./gradlew ktlintFormat`; проверка: `ktlintCheck`; статанализ: `detekt`.
- Неисправимое правило — чини код; если правило не подходит проекту — точечное изменение `detekt.yml`/`.editorconfig` с обоснованием в коммите, не `@Suppress`.

## Что НЕ делать

- Не менять `gradle/wrapper/gradle-wrapper.properties` (версию Gradle) без явного запроса.
- Не апгрейдить плагины «заодно»: Kotlin / AGP / KSP / Compose — жёстко связаны по версиям.
- Не добавлять репозитории/зависимости для обхода одной ошибки, не поняв её причину.
