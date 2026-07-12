# Платформенные зависимости через DI

## DispatcherProvider

`Dispatchers.IO` нет на JS/Wasm; прямые диспетчеры непроверяемы. Порт в core:

```kotlin
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val default: CoroutineDispatcher
    val io: CoroutineDispatcher      // на не-JVM таргетах маппится на default
}
```

Реализации: JVM/Android — реальные `Dispatchers`; iOS — `Main`/`Default`; тесты — `TestDispatcherProvider(StandardTestDispatcher(scheduler))`.

## Clock, UUID, Random

```kotlin
fun interface TimeSource { fun now(): Instant }
```

- Kotlin 2.1.20+: `kotlin.time.Clock`/`Instant` стабилизированы; kotlinx-datetime `Clock` — deprecated typealias. В новых стеках инжектируй `kotlin.time.Clock`.
- Прямые `Clock.System.now()`, `Random.nextInt()`, генерация UUID в domain делают логику невоспроизводимой в тестах — всегда порт+инъекция.

## Koin: модульная сборка

```kotlin
// commonMain
val orderModule = module {
    single<OrderRepository> { OrderRepositoryImpl(get(), get(), get()) }
    factory { GetOrders(get()) }
}

expect fun platformModule(): Module     // драйвер БД, engine Ktor, secure storage

fun initKoin(extra: Module = module {}) = startKoin {
    modules(orderModule, /* …фичи… */, platformModule(), extra)
}
```

```kotlin
// androidMain
actual fun platformModule() = module {
    single<SqlDriver> { AndroidSqliteDriver(Schema, get(), "app.db") }
    single<HttpClientEngine> { OkHttp.create() }
}
```

- iOS вызывает `initKoin()` из своего entry point (`MainViewController`/AppDelegate).
- Проводка графа — рантайм: обязательный smoke-тест `checkModules`/подъём Koin в `commonTest`.
- Тестовый override — параметр `extra`: `initKoin(module { single<OrderRepository> { FakeOrderRepository() } })`.

## expect/actual: когда всё же он

```kotlin
// commonMain
expect fun createHttpClientEngine(): HttpClientEngine

// androidMain
actual fun createHttpClientEngine(): HttpClientEngine = OkHttp.create()

// iosMain
actual fun createHttpClientEngine(): HttpClientEngine = Darwin.create()
```

Правила:

- `actual` — в том же пакете и с той же сигнатурой; отсутствие actual в любом таргете — ошибка компиляции.
- Функции/фабрики/свойства — стабильны; **классы** — Beta, избегай.
- Если то, что ты хочешь заэкспектить, можно описать интерфейсом — опиши интерфейсом и отдай в DI.

## iOS-интероп

- `suspend`/`Flow` наружу в Swift — только через SKIE или обёртки (иначе теряется отмена, коллбэки на неверных потоках).
- Держи публичную поверхность shared-фреймворка минимальной: меньше экспортов — быстрее компиляция и чище Swift-API.
