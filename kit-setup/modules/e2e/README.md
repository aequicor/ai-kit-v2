# modules/e2e

End-to-end тесты CLI `kit-setup`. Запускают **уже собранный нативный бинарь** как subprocess
и проверяют весь пайплайн (`--version` → `schema` → `verify` → `generate` → `remove`)
на временных проектах. Гейтят релиз: см. job `test` в
[`.github/workflows/release.yml`](../../../.github/workflows/release.yml).

## Свойства

- **JVM-only** (`kotlin("jvm")`, JUnit 5) — не часть Kotlin Multiplatform-сборки.
- **Отвязаны от `check`/`build`**: `./gradlew build` из корня `kit-setup/` не требует нативного
  бинаря и не запускает E2E. Тесты гоняются только явно.
- **Чёрный ящик**: модуль не зависит ни от `:modules:cli`, ни от других модулей — тестирует
  тот файл, который попадёт в GitHub Releases.

## Запуск локально

```bash
cd kit-setup

# 1) Собрать нативный бинарь под текущую платформу
./gradlew :modules:cli:linkReleaseExecutableMacosArm64   # macOS arm64
# или :linkReleaseExecutableLinuxX64 / :linkReleaseExecutableMingwX64

# 2) Запустить E2E, указав путь к бинарю
./gradlew :modules:e2e:test \
  -Pkit.binary="$(pwd)/modules/cli/build/bin/macosArm64/releaseExecutable/cli.kexe"

# Точечный прогон одного класса/теста
./gradlew :modules:e2e:test --tests "*Negative*" \
  -Pkit.binary="$(pwd)/modules/cli/build/bin/macosArm64/releaseExecutable/cli.kexe"
```

Если `-Pkit.binary` не задан, модуль попытается найти бинарь в стандартных путях
`modules/cli/build/bin/<target>/{release,debug}Executable/`.

## Структура тестов

| Класс | Что проверяет |
|---|---|
| [`VersionAndSchemaTest`](src/test/kotlin/io/aequicor/aikit/e2e/VersionAndSchemaTest.kt) | `--version`, `schema bundle --list`, поля схемы `simple-kit`. |
| [`HappyPathGenerateTest`](src/test/kotlin/io/aequicor/aikit/e2e/HappyPathGenerateTest.kt) | `verify` + `generate` на `simple-kit`; проверка layout, условных файлов, `strict`/`githubMcp` вариантов. |
| [`ContentValidationTest`](src/test/kotlin/io/aequicor/aikit/e2e/ContentValidationTest.kt) | Контент сгенерированных файлов: подстановки `${bundle.input.*}`, условные блоки `<!-- when: ... -->`, структура `settings.json` / `.mcp.json`, отсутствие остатков шаблонного синтаксиса. |
| [`LifecycleTest`](src/test/kotlin/io/aequicor/aikit/e2e/LifecycleTest.kt) | Идемпотентность повторного `generate`; чистота после `remove`. |
| [`NegativeTest`](src/test/kotlin/io/aequicor/aikit/e2e/NegativeTest.kt) | Битый JSON, неизвестный bundle, отсутствующий файл manifest'а. |
| [`MultiAppTest`](src/test/kotlin/io/aequicor/aikit/e2e/MultiAppTest.kt) | Manifest с двумя `applications` в разных subdir, изоляция деревьев. |
| [`OpenCodeTargetTest`](src/test/kotlin/io/aequicor/aikit/e2e/OpenCodeTargetTest.kt) | Каркас под OpenCode-бандл — активируется, когда такой бандл появится в репозитории (сейчас `@Assumptions.assumeTrue` пропускает). |

## Хелперы

- [`KitRunner`](src/test/kotlin/io/aequicor/aikit/e2e/KitRunner.kt) — единая точка вызова
  `ProcessBuilder` с разделением stdout/stderr и таймаутом.
- [`Fixtures`](src/test/kotlin/io/aequicor/aikit/e2e/Fixtures.kt) — генерация temp-проектов
  и `manifest.json` для разных сценариев.
- [`Assertions`](src/test/kotlin/io/aequicor/aikit/e2e/Assertions.kt) — `assertSuccess`,
  `assertFailure`, `assertFileExists`, `assertFileContains`, `assertFileAbsent`,
  `assertStdoutContains`.
- [`Discovery`](src/test/kotlin/io/aequicor/aikit/e2e/Discovery.kt) — кэшированное определение
  версии бинаря через CLI и локального `bundles/simple-kit/<v>` из репозитория.

## Добавление сценария

1. Создать новый `*.kt` в `src/test/kotlin/io/aequicor/aikit/e2e/`.
2. Управлять файловой системой только через `Fixtures.newSandbox()` + `@AfterEach { sandbox.toFile().deleteRecursively() }`.
3. Вызывать бинарь только через `KitRunner.run(...)`. Не дёргать процессы напрямую.
4. Ассертить через `assertSuccess(...)` / `assertFileExists(...)` — это даёт читаемые
   сообщения с дампом stdout/stderr при падении.

## CI

Job `test` в [`release.yml`](../../../.github/workflows/release.yml) на `ubuntu-latest`:

1. `./gradlew allTests` — KMP-юниты всех модулей.
2. `./gradlew :modules:cli:linkReleaseExecutableLinuxX64` — релизный Linux-бинарь.
3. `./gradlew :modules:e2e:test -Pkit.binary=<linuxX64 cli.kexe>` — E2E.
4. Загрузка JUnit XML-репортов как артефакта `test-reports` (даже при падении).

Job-ы `build-linux`, `build-macos`, `build-windows` объявляют `needs: test`, поэтому при
провале E2E ни одна нативка не собирается и `release` не создаётся.
