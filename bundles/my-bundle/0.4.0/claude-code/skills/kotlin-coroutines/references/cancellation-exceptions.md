# Отмена и исключения в корутинах

## Кооперативная отмена

Отмена — кооперативная: корутина должна давать точки отмены.

- Все suspend-функции `kotlinx.coroutines` (delay, withContext, emit…) проверяют отмену сами.
- Чистый CPU-цикл точек не имеет — добавляй:

```kotlin
suspend fun crunch(data: List<Item>) = withContext(defaultDispatcher) {
    data.forEach { item ->
        ensureActive()          // или yield()
        process(item)
    }
}
```

## CancellationException — не исключение, а сигнал

```kotlin
try {
    doWork()
} catch (e: CancellationException) {
    throw e                     // ВСЕГДА перебрасывать
} catch (e: IOException) {      // ловить КОНКРЕТНЫЕ типы
    emitError(e)
}
```

- Поймал `Exception`/`Throwable` без переброса `CancellationException` → корутина «переживает» отмену: утечки, зависшие тесты, двойная работа.
- Код очистки после отмены — `withContext(NonCancellable) { … }` (только короткая очистка, не новая работа).

## Иерархия ошибок

- Падение ребёнка с обычным `Job` отменяет родителя и всех сиблингов.
- `SupervisorJob` / `supervisorScope` — дети падают независимо; **каждому** ребёнку нужна своя обработка.
- `CoroutineExceptionHandler`:
  - срабатывает только на **корневых** `launch` данного scope;
  - игнорируется `async` — там исключение хранится в `Deferred` и всплывает при `await()`;
  - не «ловит» — только логирует/репортит; предотвращать падение надо `try/catch` внутри.

```kotlin
val scope = CoroutineScope(SupervisorJob() + dispatcher + CoroutineExceptionHandler { _, e ->
    log.error("uncaught", e)
})
```

## Таймауты

- `withTimeout(ms)` бросает `TimeoutCancellationException` (подтип `CancellationException` — см. правило переброса!).
- Нужен результат-или-null — `withTimeoutOrNull(ms)`.

## Частые баги

| Симптом | Причина |
|---|---|
| Корутина работает после ухода с экрана | scope не привязан к lifecycle / `GlobalScope` |
| Отмена «не работает» | CPU-цикл без `ensureActive`; проглоченный `CancellationException` |
| Падает весь экран из-за одной загрузки | обычный `Job` вместо `SupervisorJob` |
| `async` «молча» падает | никто не вызвал `await()` |
