---
name: leak-hunt
description: How to hunt resource and memory leaks in ${bundle.input.projectName} — unscoped/uncancelled coroutines and GlobalScope, unclosed Closeable/HttpClient/sockets, runaway Flow collectors and hot flows, Compose remember/effect leaks, Decompose retained-instance and lifecycle leaks, Android Context leaks. Use when memory grows, work keeps running after leaving a screen, sockets/files stay open, or when asked to "find leaks" / "почему течёт память".
---

# leak-hunt

Охота на утечки в **${bundle.input.projectName}**: систематический проход по источникам. Детальные чек-листы: [references/coroutines-flow.md](references/coroutines-flow.md), [references/compose-lifecycle.md](references/compose-lifecycle.md).

## Метод

1. Сузь симптом: память растёт / работа продолжается после ухода / дескрипторы копятся.
2. Пройди grep-паттерны ниже по подозреваемой области (или всему модулю).
3. Для каждого совпадения ответь: **кто владеет временем жизни и кто отменяет/закрывает?** Нет ответа — находка.
4. Подтверди: логом в `onDestroy`/`finally`, счётчиком подписок, heap dump'ом (Android Studio Profiler / LeakCanary).

## Grep-паттерны первого прохода

| Паттерн | Подозрение |
|---|---|
| `GlobalScope` | неотменяемая работа — почти всегда баг |
| `CoroutineScope(` | scope создан вручную — где `cancel()`? |
| `.launchIn(` | на каком scope? привязан ли к lifecycle? |
| `stateIn(`/`shareIn(` | внутри функции = hot-flow на каждый вызов |
| `HttpClient(` | Ktor-клиент на запрос без `close()`/переиспользования |
| `openStream`/`FileInputStream`/`Socket(` | нет `.use {}` |
| `callbackFlow` | есть ли `awaitClose { unsubscribe }`? |
| `object .*Listener`/`addListener`/`register` | где парный remove/unregister? |
| `Timer(`/`scheduleAtFixedRate` | кто отменяет? |
| `companion object.*Context`/`static.*Context` | Context в долгожителе |

## Топ-источники по стеку

- **Корутины**: scope без владельца; `CancellationException` проглочен — отмена «не доходит»; `async` без `await` копит исключения. → references/coroutines-flow.md
- **Flow**: коллектор на внешнем scope переживает экран; `SharingStarted.Eagerly` работает вечно; `WhileSubscribed()` без таймаута перезапускает upstream штормом.
- **Ресурсы**: всё `Closeable` — через `.use {}` или явный `close()` в `finally`/`onDestroy`; один переиспользуемый Ktor `HttpClient` на приложение.
<!-- when: 'compose' in ${bundle.input.technologies} -->
- **Compose**: подписка в composition вместо `DisposableEffect(key) { onDispose { … } }`; `LaunchedEffect(Unit)` с бесконечным циклом на неверном ключе; ссылки на `Context`/`View` в объектах, «запомненных» надолго. → references/compose-lifecycle.md
<!-- end -->
<!-- when: 'decompose' in ${bundle.input.technologies} -->
- **Decompose**: `Context`/`Activity`/`inner`-класс внутри `InstanceKeeper.Instance` — утечка переживает поворот; подписки, запущенные в `init` без остановки на `doOnStop`, — работают в back-стеке (компоненты STOPPED, не DESTROYED). → references/compose-lifecycle.md
<!-- end -->
<!-- when: 'android' in ${bundle.input.technologies} -->
- **Android**: `Activity`/`View`/`Fragment` в singleton/companion/статике; анонимный inner-класс Handler'а/Listener'а держит Activity; неотписанный BroadcastReceiver/sensor listener.
<!-- end -->

## Формат отчёта

Файл:строка → тип утечки → кто держит/что не закрыто → как воспроизвести/подтвердить → фикс (владелец lifecycle + точка отмены/закрытия).

## Что НЕ делать

- Не объявлять утечкой всё подряд: долгоживущий scope с владельцем и `cancel()` — норма.
- Не «чинить» добавлением `System.gc()`/nulling полей — чини владение временем жизни.
