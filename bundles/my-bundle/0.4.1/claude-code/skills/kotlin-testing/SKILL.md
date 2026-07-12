---
name: kotlin-testing
description: How to test coroutines, Flow and async code in ${bundle.input.projectName} — runTest, StandardTestDispatcher vs UnconfinedTestDispatcher, virtual time, Dispatchers.setMain, Turbine for Flow, plus testApplication for Ktor and LifecycleRegistry for Decompose components. Use when writing or fixing unit tests for suspend functions, Flows, ViewModels/components, or when tests hang, flake, or need a TestDispatcher.
---

# kotlin-testing

Тестирование асинхронного Kotlin-кода в **${bundle.input.projectName}**. Плейбуки: [references/coroutines-flow.md](references/coroutines-flow.md), [references/ktor-decompose.md](references/ktor-decompose.md).

## Каркас: runTest + инжекция

```kotlin
@Test
fun `loads data`() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)   // общий scheduler!
    val repo = Repository(api = FakeApi(), ioDispatcher = dispatcher)

    val result = repo.load()
    advanceUntilIdle()

    assertEquals(expected, result)
}
```

- Тестируемость начинается в продакшен-коде: диспетчеры **инжектируются** (см. скил **kotlin-coroutines**) — иначе тест не контролирует время.
- Все TestDispatcher'ы одного теста — на одном `TestCoroutineScheduler` (передавай `testScheduler`), иначе виртуальные часы рассинхронизируются.

## Выбор TestDispatcher

| | `StandardTestDispatcher` | `UnconfinedTestDispatcher` |
|---|---|---|
| Запуск корутин | по очереди, через `advance*` | энергично, до первого suspend |
| Когда | порядок/конкуренция важны | простые тесты «запусти и проверь» |

## Виртуальное время

- `advanceUntilIdle()` — выполнить всё; `advanceTimeBy(ms)` — прокрутить; `runCurrent()` — только текущие.
- `delay` скипается **только** на TestDispatcher: `delay` под `withContext(Dispatchers.Default)` идёт в реальном времени и упирается в таймаут `runTest` (60с).

## Main-диспетчер

`viewModelScope`/UI-scope сидят на `Dispatchers.Main.immediate` — подменяй:

```kotlin
@Before fun setUp() = Dispatchers.setMain(StandardTestDispatcher())
@After fun tearDown() = Dispatchers.resetMain()
```

Оформи общим правилом/базовым классом (`MainDispatcherRule`); новые TestDispatcher'ы после `setMain` автоматически подхватывают его scheduler.

## Flow — Turbine

```kotlin
repo.items().test {
    assertEquals(first, awaitItem())
    awaitComplete()
}
```

- Бесконечные/hot-флоу собирай в `backgroundScope` — иначе `runTest` зависнет, ожидая завершения.
- Таймаут Turbine — **wall-clock** (3с по умолчанию), виртуальное время на него не действует — настраивай `timeout` при больших виртуальных `delay`.

## Запрещено / устарело

- `runBlockingTest`, `TestCoroutineDispatcher`, `TestCoroutineScope` — удалены; только `runTest`/`TestScope`.
- `runBlocking` в тестах вместо `runTest` — реальное время, флейки.
- `kotlinx-coroutines-test` — только `testImplementation`.
- Подгонять ассерт под фактическое поведение, не понимая его, — красный тест сначала объясняется.

## Смежное

Ktor-эндпоинты и Decompose-компоненты — [references/ktor-decompose.md](references/ktor-decompose.md). KMP: тесты — в `commonTest`, прогон `./gradlew allTests` (или `:module:jvmTest` для скорости).
