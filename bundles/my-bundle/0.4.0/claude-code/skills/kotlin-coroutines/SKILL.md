---
name: kotlin-coroutines
description: How to write correct coroutine and Flow code in ${bundle.input.projectName} — structured concurrency, dispatcher injection, main-safety, cancellation, cold Flow vs hot StateFlow/SharedFlow, stateIn/shareIn. Use when writing or reviewing suspend functions, launching coroutines, choosing a scope or dispatcher, designing Flow pipelines, or debugging cancellation/exception behaviour.
---

# kotlin-coroutines

Дисциплина корутин и Flow в **${bundle.input.projectName}**. Полные плейбуки: [references/flow.md](references/flow.md) (cold/hot, операторы), [references/cancellation-exceptions.md](references/cancellation-exceptions.md) (отмена и исключения).

## Три жёстких правила

1. **Инжектируй диспетчер.** `CoroutineDispatcher` — параметр конструктора (`Dispatchers.Default` допустим только как default-значение). Прямые `Dispatchers.IO/Main/Default` в теле кода запрещены — их не подменить в тестах, а в KMP `Dispatchers.IO` нет на JS/Wasm.
2. **Никакого `GlobalScope`.** Работа в рамках вызова — `coroutineScope {}`; работа, переживающая вызов, — инжектированный `CoroutineScope` (с `SupervisorJob`, если дети независимы).
3. **Main-safety.** Suspend-функция сама уводит блокирующую/CPU-работу в `withContext(injectedDispatcher)`; вызывающий вправе звать её с Main.

## Выбор scope

| Ситуация | Scope |
|---|---|
| Результат нужен вызывающему | `coroutineScope { async {} … }` |
| Дети падают независимо | `supervisorScope {}` |
| Привязка к экрану/компоненту | `viewModelScope` / scope Decompose-компонента |
| Переживает вызов (кэш, аналитика) | инжектированный долгоживущий scope |

## Отмена (кратко)

- Длинный CPU-цикл — вызывай `ensureActive()`/`yield()`.
- `CancellationException` **не глотать** — перебрасывать; ловить конкретные типы, не `Exception`.
- `CoroutineExceptionHandler` работает только на корневых `launch`; у `async` исключение всплывает в `await()`.

## Flow (кратко)

- Данные/домен наружу отдают `Flow<T>` и `suspend`-функции — lifecycle контролирует вызывающий.
- `MutableStateFlow`/`MutableSharedFlow` — приватные; наружу `asStateFlow()`/`asSharedFlow()`.
- `flowOn(dispatcher)` ставится сразу над тяжёлыми операторами (действует на upstream); `emit` из-под `withContext` внутри `flow {}` — запрещено.
- Cold→hot: `stateIn`/`shareIn` с `SharingStarted.WhileSubscribed(5000)` на инжектированном scope; хойсти результат в свойство — hot-flow на каждый вызов функции = утечка.
- `flowOn`/`buffer`/`cancellable` на `StateFlow`/`SharedFlow` не действуют — они уже hot.

## Что НЕ делать

- `runBlocking` вне `main()` и тестов — поднимай `suspend` по цепочке.
- Свои пулы потоков — используй `dispatcher.limitedParallelism(n)`.
- `Job()` в scope, где дети должны быть независимы, — там `SupervisorJob()`.
- Хранить/передавать scope, которым не владеешь.
