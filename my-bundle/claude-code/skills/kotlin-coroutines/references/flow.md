# Flow: cold vs hot, операторы, шаринг

## Cold Flow — по умолчанию

Холодный `Flow` выполняет builder заново на каждый `collect`. Это правильный тип для data/domain-слоя: вызывающий владеет временем жизни.

```kotlin
class NewsRepository(
    private val api: NewsApi,
    private val ioDispatcher: CoroutineDispatcher,
) {
    fun latestNews(): Flow<List<Article>> = flow {
        while (true) {
            emit(api.fetchLatest())
            delay(REFRESH_MS)
        }
    }.flowOn(ioDispatcher) // upstream (flow{} и всё выше) — на io
}
```

- `flowOn` влияет **только на upstream**; ставить его надо сразу над тяжёлой частью цепочки.
- Никогда не `withContext { emit(...) }` внутри `flow {}` — нарушение context preservation, будет исключение.

## Hot: StateFlow / SharedFlow

| Тип | Семантика | Когда |
|---|---|---|
| `StateFlow<T>` | всегда есть значение, конфляция, replay=1 | UI-состояние |
| `SharedFlow<T>` | настраиваемый replay/buffer | события, broadcast |

- Приватная мутабельная часть + read-only наружу:

```kotlin
private val _state = MutableStateFlow(UiState.Loading)
val state: StateFlow<UiState> = _state.asStateFlow()
```

- Одноразовые события через `SharedFlow(replay = 0)` теряются без подписчика — для UI-событий предпочитай моделирование состоянием.

## stateIn / shareIn

```kotlin
val user: StateFlow<User?> = repository.userFlow()
    .stateIn(
        scope = injectedScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )
```

- `WhileSubscribed(5000)` — переживает ресайз/поворот без рестарта upstream; без таймаута upstream перезапускается на каждый unsubscribe.
- **Хойсти** результат `stateIn`/`shareIn` в свойство класса. Вызов внутри функции, возвращающей flow, создаёт новый hot-поток на каждый вызов — утечка и лишняя работа.
- `Lazily` — для необязательных вычислений; `Eagerly` — почти никогда (работает даже без подписчиков).

## Операторы: типовые ошибки

- `conflate()` — когда важно только последнее значение (прогресс, позиция).
- `buffer()` — когда producer быстрее consumer'а и терять нельзя.
- `distinctUntilChanged()` уже встроен в `StateFlow` — не дублируй.
- `collectLatest {}` — отменяет обработку предыдущего значения при новом; для тяжёлой обработки последнего состояния.
- В Compose/Android собирай с учётом lifecycle: `collectAsStateWithLifecycle()` / `repeatOnLifecycle`.

## Callback → Flow

`callbackFlow {}` + `awaitClose { unsubscribe() }`. Отсутствие `awaitClose` = утечка подписки и крэш при закрытии канала.
