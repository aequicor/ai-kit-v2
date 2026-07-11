# Утечки корутин, Flow и ресурсов

## Корутины: владение scope

```kotlin
// УТЕЧКА: никто не отменит
class SyncService {
    fun start() { GlobalScope.launch { syncForever() } }
}

// ФИКС: scope инжектирован, владелец отменяет
class SyncService(private val scope: CoroutineScope) {
    fun start() { scope.launch { syncForever() } }
}
```

Чек-лист:

- Каждый `CoroutineScope(...)` имеет владельца и точку `cancel()` (lifecycle-колбэк, `close()`, DI-скоуп).
- `launch`/`launchIn` — на scope, чей lifecycle совпадает с работой; работа для экрана — scope экрана/компонента.
- Проглоченный `CancellationException` (`catch (e: Exception)` без переброса) делает корутину неотменяемой — см. скил **kotlin-coroutines**.
- `async` без `await()`: исключение хранится в `Deferred`; при `SupervisorJob` — молча копится.

## Flow: подписки

```kotlin
// УТЕЧКА: hot-flow на каждый вызов
fun items(): Flow<List<Item>> = repo.observe().stateIn(appScope, Eagerly, emptyList())

// ФИКС: один hot-flow как свойство
val items: StateFlow<List<Item>> =
    repo.observe().stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())
```

- `stateIn`/`shareIn` — только в свойствах; scope — с владельцем.
- `SharingStarted.Eagerly` живёт, пока жив scope, даже без подписчиков — оправдан редко.
- Коллектор в UI: Android — `collectAsStateWithLifecycle`/`repeatOnLifecycle(STARTED)`; голый `launch { flow.collect{} }` на scope активности переживает уход с экрана.
- `callbackFlow` без `awaitClose { unregister() }` — утечка колбэка + крэш канала.

## Ресурсы: Closeable

```kotlin
// УТЕЧКА при исключении
val input = file.inputStream()
parse(input)
input.close()

// ФИКС
file.inputStream().use { parse(it) }
```

- Всё `Closeable`/`AutoCloseable` — `.use {}`; несколько ресурсов — вложенные `use` или `use` + `runCatching`.
- **Ktor `HttpClient`** — тяжёлый (пул соединений, engine): один инстанс на приложение через DI; создал локально — обязан `close()`. Симптом утечки — рост потоков `ktor-…`/`OkHttp Dispatcher`.
- БД: курсоры/стейтменты/транзакции закрываются в `finally`; connection — из пула, возврат гарантирован.
- Файловые watcher'ы, `Selector`, `ServerSocket` — явный `close()` в остановке сервиса.

## Слушатели и колбэки

- Каждому `addListener`/`register`/`subscribe` — парный `remove`/`unregister`/`dispose` в симметричной точке lifecycle.
- Анонимный listener, захвативший `this` долгожителем (static registry, singleton bus), держит весь объект.

## Как подтвердить

- Лог в `onDestroy`/`awaitClose`/`finally` — вызвался ли?
- Счётчик активных подписок/соединений в фейке при тесте: после `lifecycle.destroy()` должен быть 0.
- JVM: heap dump → доминаторы; Android: LeakCanary/Memory Profiler, смотри путь удержания до GC root.
