# Утечки в Compose и Decompose

## Compose: эффекты и подписки

```kotlin
// УТЕЧКА: подписка в composition, отписки нет
@Composable
fun LocationLabel(tracker: LocationTracker) {
    tracker.addListener(listener)          // side effect в composition!
    …
}

// ФИКС: DisposableEffect с onDispose
@Composable
fun LocationLabel(tracker: LocationTracker) {
    DisposableEffect(tracker) {
        tracker.addListener(listener)
        onDispose { tracker.removeListener(listener) }
    }
}
```

Чек-лист Compose:

- Любой register/subscribe в composable — только внутри `DisposableEffect(keys)` с парной отпиской в `onDispose`.
- `LaunchedEffect(key)`: корутина отменяется при выходе из composition/смене ключа — но `LaunchedEffect(Unit)` с циклом на экране-долгожителе живёт, пока жив экран; проверь, тот ли это lifecycle.
- `rememberCoroutineScope()` отменяется с composition — не передавай этот scope наружу в долгожителей.
- `remember { heavyObject }` держит объект, пока composable в композиции; объект с ссылкой на `Context`/`View` в long-lived host — путь удержания.
- Сбор Flow в UI — `collectAsStateWithLifecycle()` (Android) — иначе сбор продолжается в фоне.

## Decompose: retention и back-stack

```kotlin
// УТЕЧКА: inner-класс держит компонент (и всё под ним) через поворот
private inner class Retained : InstanceKeeper.Instance { … }

// ФИКС: топ-левел/nested класс без захвата, зависимости через конструктор
private class Retained(private val repo: Repo) : InstanceKeeper.Instance {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    override fun onDestroy() { scope.cancel() }
}
```

Чек-лист Decompose:

- В `InstanceKeeper.Instance` — никаких `Context`/`Activity`/`View` и никаких `inner`-классов: retained переживает config change, утечка «прилипает».
- Каждый retained с ресурсами реализует `onDestroy()` и там всё освобождает (`scope.cancel()`, `close()`).
- **Back-stack = STOPPED, не DESTROYED**: подписка, запущенная в `init`/конструкторе, продолжает работать у неактивного экрана. Стартуй в `doOnStart`, останавливай в `doOnStop`.
- Scope компонента (Essenty `coroutineScope(...)`) отменится сам на destroy — но пересоздаётся на поворот: работа, которая должна пережить поворот, — в scope retained instance.
- Обсерверы `Value.subscribe { }` вне UI — храни `Cancellation` и вызывай `cancel()`.

## Android-специфика

- `Activity`/`Fragment`/`View` в companion/static/singleton — классика; передавай `applicationContext`, если нужен Context долгожителю.
- `Handler().postDelayed` с анонимным Runnable держит Activity — `removeCallbacks` в `onDestroy` или lifecycle-aware альтернативы.
- BroadcastReceiver/sensor/location listeners — unregister симметрично lifecycle-методу регистрации.
- Проверка: LeakCanary в debug-сборке; каждый репорт — путь удержания до GC root, чини верхнее звено.
