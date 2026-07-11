# Тестирование Decompose-компонентов

Компоненты — обычные классы: тестируются на JVM без инструментария и UI.

## Каркас теста

```kotlin
class ListComponentTest {

    private val lifecycle = LifecycleRegistry()

    private fun createComponent(
        repository: FakeRepository = FakeRepository(),
        onItemSelected: (ItemId) -> Unit = {},
    ): DefaultListComponent =
        DefaultListComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            repository = repository,
            dispatchers = TestDispatcherProvider(),   // инжектированные диспетчеры!
            onItemSelected = onItemSelected,
        )

    @Test
    fun `loads items on resume`() = runTest {
        val component = createComponent()
        lifecycle.resume()                            // управляем lifecycle руками

        advanceUntilIdle()

        assertEquals(3, component.model.value.items.size)
    }
}
```

Ключевое:

- `LifecycleRegistry()` + `resume()`/`stop()`/`destroy()` — полный контроль жизненного цикла в тесте.
- Все зависимости — через конструктор: фейки репозиториев, `TestDispatcherProvider` на `StandardTestDispatcher`.
- Ассерты — на `component.model.value` и на выходные колбэки.

## Тест навигации

```kotlin
@Test
fun `opens details on item click`() = runTest {
    val root = DefaultRootComponent(DefaultComponentContext(lifecycle))
    lifecycle.resume()

    val list = root.childStack.value.active.instance as RootComponent.Child.ListChild
    list.component.onItemClicked(ItemId(42))

    val active = root.childStack.value.active.instance
    assertIs<RootComponent.Child.DetailsChild>(active)
    assertEquals(42, (root.childStack.value.active.configuration as Config.Details).id)
}
```

- Проверяй `childStack.value.active` (+ `backStack` при необходимости) — навигация синхронна на Main.
- В `runTest` Main подменяй `Dispatchers.setMain(...)`, иначе `Main.immediate` недоступен (см. скил **kotlin-testing**).

## Наблюдение Value

```kotlin
val states = mutableListOf<Model>()
val cancellation = component.model.subscribe { states += it }
…
cancellation.cancel()
```

`Value<T>` — не Flow; подписка синхронная, для большинства тестов достаточно читать `.value` после `advanceUntilIdle()`.

## Что НЕ делать

- Не поднимать Robolectric/эмулятор ради логики компонента — это чистый JVM-код.
- Не тестировать `Default*`-компонент через Compose-UI — UI тестируется отдельно, компонент — отдельно.
- Не забывать `lifecycle.resume()` — компонент в CREATED может не запускать наблюдения.
