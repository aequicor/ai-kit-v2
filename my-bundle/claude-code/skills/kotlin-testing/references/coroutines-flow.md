# Тесты корутин и Flow: рецепты

## MainDispatcherRule (JUnit4)

```kotlin
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

// использование
@get:Rule val mainRule = MainDispatcherRule()
```

kotlin-test/JUnit5 — то же в `@BeforeTest`/`@AfterTest`.

## StateFlow во ViewModel/компоненте

```kotlin
@Test
fun `state goes loading then content`() = runTest {
    val vm = FeedViewModel(FakeRepo(), TestDispatcherProvider(testScheduler))

    vm.state.test {                                  // Turbine
        assertEquals(FeedState.Loading, awaitItem())
        assertEquals(FeedState.Content(items), awaitItem())
        cancelAndIgnoreRemainingEvents()             // StateFlow не завершается
    }
}
```

- `StateFlow` конфлирует: промежуточное состояние можно не увидеть — ассерти значимые снапшоты, не «каждый шаг».
- `stateIn(WhileSubscribed)` стартует только при подписчике — сам `.test {}` и есть подписка.

## Hot/бесконечный flow → backgroundScope

```kotlin
@Test
fun `emits ticks`() = runTest {
    val ticks = mutableListOf<Long>()
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        ticker.ticks().collect { ticks += it }       // бесконечный
    }

    advanceTimeBy(3_000)

    assertEquals(listOf(0L, 1L, 2L), ticks)
}
```

`backgroundScope` отменяется по концу теста — `runTest` не ждёт его завершения.

## Ловушки

| Симптом | Причина → лечение |
|---|---|
| Тест висит | сбор бесконечного flow не в `backgroundScope`; `awaitComplete()` на StateFlow |
| «Module with Main dispatcher had failed to initialize» | нет `Dispatchers.setMain` |
| Часы «не движутся» | второй TestDispatcher создан без `testScheduler` |
| `delay` реально ждёт | он под `withContext(Dispatchers.IO/Default)` — инжектируй диспетчер |
| Флейк на порядке | `UnconfinedTestDispatcher` → перейди на `StandardTestDispatcher` + `advance*` |
| Turbine timeout при виртуальном delay | таймаут Turbine wall-clock → `test(timeout = …)` |

## Фейки vs моки

- Предпочитай **фейки** (маленькая рабочая реализация интерфейса) мокам: устойчивее к рефакторингу, читаются как код.
- Мок-фреймворки в commonTest ограничены (JVM-only у большинства) — ещё один довод за фейки в KMP.

## Свойства хорошего теста

- Один сценарий — один тест; имя в backticks описывает поведение: ``fun `expired token yields 401`()``.
- Ассерты на **наблюдаемое** поведение (выход, состояние, вызовы порта), не на приватные детали.
- Никаких `Thread.sleep` — только виртуальное время.
