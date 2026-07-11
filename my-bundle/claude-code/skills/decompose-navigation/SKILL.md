---
name: decompose-navigation
description: How to structure Decompose components and navigation in ${bundle.input.projectName} — ComponentContext anatomy, Child Stack/Slot/Pages, @Serializable configs, StateKeeper/InstanceKeeper retention, Essenty back handling and lifecycle-scoped coroutines. Use when creating screens/components, adding navigation flows, handling back, preserving state across config change or process death, or testing components.
---

# decompose-navigation

Компоненты и навигация Decompose (3.x, Essenty 2.x) в **${bundle.input.projectName}**. Плейбуки: [references/navigation.md](references/navigation.md), [references/retention-lifecycle.md](references/retention-lifecycle.md), [references/testing.md](references/testing.md).

## Анатомия компонента

```kotlin
interface ListComponent {
    val model: Value<Model>
    fun onItemClicked(id: ItemId)
}

class DefaultListComponent(
    componentContext: ComponentContext,
    private val onItemSelected: (ItemId) -> Unit,   // output к родителю
) : ListComponent, ComponentContext by componentContext { … }
```

- Интерфейс + `Default*`-реализация: UI/превью/тесты зависят от интерфейса.
- Вся не-UI логика — в компоненте; composable только рендерит `model` и зовёт колбэки. Компонент не знает про Compose/`Context`/`Activity`.
- Состояние наружу — read-only `Value<T>` (не `MutableValue`, не `StateFlow`); в Compose — `subscribeAsState()`.
- Ребёнок сообщает родителю через output-лямбды из `childFactory`; ссылку на родителя не держит.

## Корень

Создаётся **один раз, на main-потоке, вне composable**: `defaultComponentContext()` в Activity / `DefaultComponentContext(LifecycleRegistry())` на desktop/iOS. Пересоздание в composition — сломанный lifecycle и утечки.

## Навигация (кратко)

- Back-stack: `StackNavigation<Config>()` + `childStack(source, serializer = Config.serializer(), initialConfiguration = …, handleBackButton = true, childFactory = ::child)`.
- `Config` — `@Serializable sealed interface`; конфиги уникальны по equality; несколько навигаций в одном компоненте — уникальные `key`.
- Все вызовы `push/pop/replaceCurrent/bringToFront/activate/dismiss/select` — на Main.
- Выбор модели: **Stack** — история; **Slot** — один-или-ничего (диалог, sheet); **Pages** — табы/пейджер.
- `serializer = null` молча отключает восстановление стека — только осознанно.

## Retention и lifecycle (кратко)

- Пережить config change (Android): `retainedInstance { … }` / `instanceKeeper.getOrCreate {}` — внутри никаких `Context`/`Activity`/`inner`-классов.
- Лёгкий snapshot-state — `stateKeeper.register(key, serializer) { state }` / `consume`.
- Корутины — Essenty `coroutineScope(mainContext + SupervisorJob())`: авто-отмена на destroy. Работа, переживающая поворот, — scope внутри retained instance.
- Компоненты back-стека **STOPPED, не DESTROYED** — подписки/таймеры гейть на lifecycle-состояние.

## Back и UI

- Back — Essenty `BackHandler`/`BackCallback` (**не** `androidx.activity.compose.BackHandler`).
- Рендер стека: `Children(component.childStack, animation = stackAnimation(fade())) { child -> when (val c = child.instance) { … } }`; predictive back — `predictiveBackAnimation(…)` + `handleBackButton = true`.

## Что НЕ делать

- Side-effect в конструкторе компонента — переноси в `lifecycle.doOnCreate/doOnResume`.
- `Parcelable`/`@Parcelize` для конфигов — это Decompose 1.x/2.x; в 3.x — kotlinx-serialization.
- Дублирующиеся конфиги в одном стеке — бросает исключение.
