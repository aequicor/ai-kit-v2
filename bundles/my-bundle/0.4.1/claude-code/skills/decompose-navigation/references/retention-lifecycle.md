# Decompose: retention, state и lifecycle

## Два механизма сохранения

| Механизм | Что переживает | Для чего |
|---|---|---|
| `InstanceKeeper` / `retainedInstance {}` | config change (Android) | тяжёлые/долгоживущие объекты: репозитории с кэшем, работающие корутины |
| `StateKeeper` | process death + config change | лёгкий сериализуемый snapshot состояния |

## InstanceKeeper

```kotlin
class DefaultFeedComponent(ctx: ComponentContext) : FeedComponent, ComponentContext by ctx {

    private val retained = retainedInstance { FeedRetained() }   // Decompose 3.2+

    private class FeedRetained : InstanceKeeper.Instance {
        val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
        override fun onDestroy() { scope.cancel() }              // освобождать здесь
    }
}
```

- Внутри retained instance — **никаких** `Context`/`Activity`/`View` и никаких `inner`-классов (захват внешнего компонента = утечка, переживающая поворот).
- До 3.2 — `instanceKeeper.getOrCreate(KEY) { … }`.
- Работа, которая должна пережить поворот, живёт в scope retained instance, не в компоненте: scope компонента отменяется и пересоздаётся на каждый config change.

## StateKeeper

```kotlin
@Serializable
private data class State(val query: String = "")

private var state: State =
    stateKeeper.consume(KEY_STATE, State.serializer()) ?: State()

init {
    stateKeeper.register(KEY_STATE, State.serializer()) { state }
}
```

- Только сериализуемое и лёгкое (текст поля, выбранный таб); большие данные перезагружай.
- Без `serializer` в `childStack` навигация тоже не восстановится — это тот же механизм.

## Lifecycle

Состояния: `CREATED → STARTED → RESUMED`, назад через `STOPPED → DESTROYED`.

- Инициализация с побочками — не в конструкторе, а в `lifecycle.doOnCreate {}` / подписки — `doOnStart`/`doOnResume` с парными `doOnStop`.
- **Back-stack компоненты STOPPED, не DESTROYED** — их корутины/подписки продолжают жить. Гейт: запускать наблюдение в `doOnStart`, останавливать в `doOnStop` (или использовать `repeatOnLifecycle`-аналоги).

## Корутины компонента

```kotlin
// Essenty lifecycle-coroutines
private val scope = coroutineScope(Dispatchers.Main.immediate + SupervisorJob())
```

- Авто-отмена на `onDestroy` — вручную ничего чистить не надо.
- Desktop/JVM: `Dispatchers.Main.immediate` требует Main-диспетчера (Swing) — подключи `kotlinx-coroutines-swing` или передай явный контекст.
- Тяжёлая работа — через инжектированный `DispatcherProvider`, как везде (см. скил **kotlin-coroutines**).

## Частые утечки (сверься со скилом **leak-hunt**)

- `Activity`/`Context` в retained instance.
- `inner class … : InstanceKeeper.Instance`.
- Подписка в `init`/конструкторе без остановки на `doOnStop` — работает в back-стеке.
- Scope, созданный вручную без привязки к lifecycle и без `cancel()`.
