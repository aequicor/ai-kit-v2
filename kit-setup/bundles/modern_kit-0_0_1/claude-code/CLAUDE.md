# CLAUDE.md — ${bundle.input.projectName}

Этот файл — конституция агента для Kotlin-проекта **${bundle.input.projectName}**.
Загружается автоматически в каждой сессии Claude Code.

## Принципы

- Делай минимально необходимое изменение под задачу. Не рефактори вокруг.
- Перед изменением — пойми контекст: прочитай связанные файлы, не только тот, в котором правишь.
- Никаких "заглушек на будущее" и спекулятивных абстракций.
- Комментарии — только если они объясняют **почему**, а не **что**. Для публичных API — KDoc, а не комментарии.

## SOLID

Принципы SOLID — не теоретический буллет-лист, а ежедневный фильтр для кода:

- **S — Single Responsibility**: один класс / функция — одна причина измениться. Если в имени появляется `And`/`Manager`/`Helper` — почти всегда нарушение.
- **O — Open/Closed**: расширение поведения — через новые типы (sealed-иерархии, стратегии), а не через `when`-каскад с новой веткой при каждой фиче.
- **L — Liskov**: подкласс / реализация должны полностью соблюдать контракт супер-типа. Если для подтипа нужен «специальный» вызов или `is`-проверка — иерархия неправильная.
- **I — Interface Segregation**: маленькие сфокусированные интерфейсы. Если реализация вынуждена кидать `UnsupportedOperationException` или возвращать `null` для части методов — split.
- **D — Dependency Inversion**: domain зависит от абстракций; конкретные реализации (БД, сеть, ФС, часы, рандом) — инжектятся. В domain-слое не должно быть импортов из infrastructure.

## Запрещено

| Что | Почему | Что вместо |
|---|---|---|
| `!!` | NPE без контекста | `?.let`, `requireNotNull(x) { "..." }`, smart cast |
| `GlobalScope` | утечки, неструктурированная конкуренция | инжектируемый `CoroutineScope`, `viewModelScope`, `lifecycleScope` |
| Прямой `Dispatchers.IO` / `Dispatchers.Main` / `Dispatchers.Default` / `Dispatchers.Unconfined` в коде | непроверяемо в тестах, ломает KMP | инжектить `CoroutineDispatcher` (или `CoroutineContext`) через конструктор; в тестах подменять на `StandardTestDispatcher` / `UnconfinedTestDispatcher` |
| `@Suppress(...)` | глушит сигнал линтера/компилятора | починить причину; если действительно неизбежно — обсудить с ревьюером и зафиксировать решение **не** через `@Suppress`, а через явную правку правила в `detekt.yml` / `.editorconfig` с обоснованием в коммите |
| `runBlocking` вне `main()` и тестов | блокирует поток, мешает структурированной конкуренции | `suspend` всю цепочку до точки запуска |
| `Pair`/`Triple` в публичных сигнатурах | теряются имена полей | `data class` с осмысленными именами |

## Kotlin-конвенции

- **Immutability**: `val` по умолчанию, `var` — только если без него нельзя. Коллекции — `List`/`Map`/`Set` (read-only) во внешнем API, `Mutable*` — внутри функций.
- **Data / sealed classes** для моделей предметной области.
- **Корутины**: структурированная конкуренция; `suspend`-функции main-safe либо явно документируют дозволенный диспетчер.
- **Extension functions** — для расширения чужих типов и DSL. Не превращать обычные методы в extensions без причины.
- **Explicit API mode** включается на library-модулях (`explicitApi()` в `kotlin {}`).
- **KDoc** обязателен на всех публичных классах, интерфейсах, методах и свойствах.

## Команды

| Что | Команда |
|---|---|
| Сборка | `./gradlew build` |
| Тесты | `./gradlew test` |
| Форматирование | `./gradlew ktlintFormat` |
| Линт + статанализ | `./gradlew detekt` |
| Полный прогон | `./gradlew check` |

<!-- when: ${bundle.input.kotlinFlavor} == 'jvm' -->
### JVM-специфика

- Целевая JVM — фиксируется в `kotlin { jvmToolchain(...) }` или через Gradle toolchains.
- Тесты: `./gradlew test`. Для одного класса — `./gradlew test --tests "fq.ClassName"`.
<!-- end -->

<!-- when: ${bundle.input.kotlinFlavor} == 'multiplatform' -->
### Kotlin Multiplatform

- Общий код — в `commonMain`. Платформенные различия — только через `expect/actual`, а не платформенные ветки `if`.
- Тесты `commonTest` гоняются на хостовой платформе через `./gradlew allTests` (или конкретный `:module:allTests`).
- Не использовать `Dispatchers.*` напрямую (см. «Запрещено») — часть из них недоступна на native/wasm.
- Зависимости: `kotlinx-coroutines-core`, `kotlinx-serialization-*`, `ktor-client-*` — multiplatform-варианты.

#### Clean Architecture

Слои и направление зависимостей — строго наружу-внутрь, **никогда** наоборот:

```
presentation/ui  →  application (use cases)  →  domain (entities, ports)
                                                       ↑
                              data / infrastructure  ──┘  (реализуют ports из domain)
```

- `domain` (в `commonMain`) — чистые модели, без зависимостей от Android SDK, Ktor, БД, ФС, корутин-диспетчеров. Только `kotlin-stdlib` и `kotlinx-coroutines-core` (для `Flow`/`suspend`-контрактов).
- `application` / `usecase` — оркестрация domain. Use case = одна функция / один class с `invoke`.
- `data` — реализации `Repository`/`*Gateway`-портов, объявленных в `domain`. Маппинг DTO ↔ domain — здесь, не в domain.
- `presentation` — ViewModel / state. Никакой бизнес-логики, только сборка use case'ов и маппинг в UI-state.
- Платформенный код (`androidMain`, `iosMain`, `jvmMain`) — только адаптеры: реализации портов через платформенные API.
- Module-граница (Gradle-модуль на слой) предпочтительнее package-границы — компилятор не пропустит обратный импорт.
<!-- end -->

<!-- when: ${bundle.input.kotlinFlavor} == 'android' -->
### Android-специфика

- UI — Jetpack Compose, MVVM или MVI; никакой логики в `@Composable`-функциях, только state-rendering.
- `lifecycleScope` / `viewModelScope` — для UI-корутин. Не `GlobalScope`.
- Ресурсы (`R.string.*`) — через `stringResource(...)` в Compose, никаких хардкод-строк в UI.
- Зависимости и версии — в `gradle/libs.versions.toml`.
- Тесты: `./gradlew testDebugUnitTest` для unit, `./gradlew connectedDebugAndroidTest` для instrumentation.

#### Clean Architecture

Слои и направление зависимостей — строго наружу-внутрь:

```
:app (DI, Activity, Compose)  →  :feature-*:presentation  →  :feature-*:domain
                                                                    ↑
                                          :feature-*:data  ─────────┘  (реализуют ports)
```

- `:domain` — pure Kotlin module (`java-library` или KMP `commonMain`). Никаких импортов `android.*`, `androidx.*`, Retrofit, Room. Только `kotlin-stdlib` и (опц.) `kotlinx-coroutines-core`.
- `:data` — Retrofit/Ktor, Room/SQLDelight, DataStore. Реализует репозитории из `:domain`. Маппинг DTO/Entity ↔ domain — здесь.
- `:presentation` — `ViewModel`, UI-state, обработка `Intent`/`Action` (MVI) или binding (MVVM). Только зовёт use case'ы, не лезет в Retrofit/Room напрямую.
- `:app` — точка сборки и DI-граф (Hilt/Koin). Никакой бизнес-логики.
- Use case = `class GetX(...) { suspend operator fun invoke(...) }`. По одному use case на сценарий, не «сервис на 10 методов».
- DI: зависимости домена (репозитории, диспетчеры, часы) — через конструктор. Никаких `@Inject lateinit var` в domain/data.
- Compose-функции — stateless, принимают state + лямбды; ViewModel не передаём в дочерние composable.
<!-- end -->

<!-- when: 'ktlint' in ${bundle.input.qualityTools} -->
## ktlint (автоматически)

`.kt` и `.kts` файлы автоформатируются хуком `PostToolUse` после каждого `Edit`/`Write` — вручную ничего вызывать не нужно. Если хук падает — значит правило `ktlint` не может быть автоисправлено: тогда чини руками и запусти `./gradlew ktlintCheck`.
<!-- end -->

<!-- when: 'detekt' in ${bundle.input.qualityTools} -->
## detekt (на завершении)

Перед концом сессии `Stop`-хук прогоняет `./gradlew detekt`. Если есть нарушения — они появятся в выводе хука, и сессия не закроется молча. Не игнорируй: правь либо код, либо (если правило не подходит проекту) `config/detekt/detekt.yml` с обоснованием в коммите.
<!-- end -->

<!-- when: ${bundle.input.strict} -->
## Strict-режим включён

- Запрещены `rm -rf`, `git push --force`, force-операции с тегами, `git reset --hard`, `git clean -f`, `chmod 777`.
- Все опасные bash-команды блокируются хуком `PreToolUse`.
- Перед деструктивным действием — спрашивай подтверждение в чате.
<!-- end -->

<!-- when: ${bundle.input.githubMcp} -->
## GitHub MCP

Доступен MCP-сервер `github` — используй его для работы с issues, PR и code search вместо `gh` где это удобнее.
<!-- end -->

<!-- when: ('kotlin-specialist' in ${bundle.input.subagents}) || ('gradle-troubleshooter' in ${bundle.input.subagents}) || ('code-reviewer' in ${bundle.input.subagents}) || ('test-runner' in ${bundle.input.subagents}) -->
## Доступные субагенты

<!-- when: 'kotlin-specialist' in ${bundle.input.subagents} -->
- **kotlin-specialist** — глубокий Kotlin: корутины, KMP, Compose, Arrow.kt, type-safe builders. Делегируй нетривиальные задачи с асинхронностью или multiplatform-кодом.
<!-- end -->
<!-- when: 'gradle-troubleshooter' in ${bundle.input.subagents} -->
- **gradle-troubleshooter** — разбор падений `./gradlew`: configuration cache, version conflicts, плагины, daemon.
<!-- end -->
<!-- when: 'code-reviewer' in ${bundle.input.subagents} -->
- **code-reviewer** — независимое ревью изменений против конвенций репозитория.
<!-- end -->
<!-- when: 'test-runner' in ${bundle.input.subagents} -->
- **test-runner** — прогон Gradle-тестов и краткий отчёт.
<!-- end -->
<!-- end -->

## Доступные команды

- `/build-check` — быстрый прогон ktlint + detekt + тестов с коротким отчётом.
<!-- when: 'review' in ${bundle.input.skills} -->
- `/review` — ревью текущей ветки.
<!-- end -->

## Доступные скилы

Скилы подхватываются автоматически по описанию: ${bundle.input.skills}.
