# Тесты Ktor-эндпоинтов и Decompose-компонентов

## Ktor: testApplication

```kotlin
@Test
fun `GET items returns 200 with body`() = testApplication {
    application { module() }                        // реальный module()
    val response = client.get("/items")
    assertEquals(HttpStatusCode.OK, response.status)
    assertTrue(response.bodyAsText().contains("\"items\""))
}
```

- Без реального порта, быстро, детерминированно (`ktor-server-test-host`).
- Конфиг/секреты теста — `environment { config = MapApplicationConfig("jwt.secret" to "test-secret", …) }`.
- JSON-клиент: `createClient { install(ContentNegotiation) { json() } }`; куки-сессии — `install(HttpCookies)`.
- `withTestApplication`/`handleRequest` удалены в Ktor 3 — не копируй из старых туториалов.
- Матрица кейсов: позитив (200/201, форма тела), плохой ввод (400/422 без стектрейса), без авторизации (401), чужой ресурс (403/404), несуществующий id (404), повтор POST (по контракту). Подробнее — скил **ktor-security**.

## Decompose: компонент как обычный класс

```kotlin
class ListComponentTest {

    private val lifecycle = LifecycleRegistry()

    @BeforeTest fun setUp() = Dispatchers.setMain(StandardTestDispatcher())
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `loads items on resume`() = runTest {
        val component = DefaultListComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            repository = FakeRepository(items = 3),
            dispatchers = TestDispatcherProvider(testScheduler),
            onItemSelected = {},
        )
        lifecycle.resume()

        advanceUntilIdle()

        assertEquals(3, component.model.value.items.size)
    }
}
```

- `LifecycleRegistry` + `resume()/stop()/destroy()` — lifecycle под контролем теста; без `resume()` компонент может не запустить наблюдения.
- `Dispatchers.setMain` обязателен: scope компонента сидит на `Main.immediate`.
- Навигация ассертится по `childStack.value.active` (`instance`/`configuration`) — синхронно.
- Никакого Robolectric/эмулятора для логики — это чистый JVM-тест.

## KMP-раскладка

- Логика — `commonTest` (kotlin-test: `@Test`, `assertEquals`); фейки — там же.
- Быстрая обратная связь — `./gradlew :module:jvmTest`; полный прогон по таргетам — `./gradlew allTests`.
- Платформенные `actual` — точечные тесты в `androidUnitTest`/`iosTest` только там, где есть платформенная логика.
