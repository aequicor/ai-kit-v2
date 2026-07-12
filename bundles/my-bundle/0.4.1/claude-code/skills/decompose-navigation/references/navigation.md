# Навигация Decompose: Stack, Slot, Pages

## Child Stack — история экранов

```kotlin
class DefaultRootComponent(ctx: ComponentContext) : RootComponent, ComponentContext by ctx {

    @Serializable
    sealed interface Config {
        @Serializable data object List : Config
        @Serializable data class Details(val id: Long) : Config
    }

    private val navigation = StackNavigation<Config>()

    override val childStack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),      // включает восстановление стека
        initialConfiguration = Config.List,
        handleBackButton = true,               // pop по системному back
        childFactory = ::child,
    )

    private fun child(config: Config, ctx: ComponentContext): RootComponent.Child = when (config) {
        is Config.List -> RootComponent.Child.ListChild(
            DefaultListComponent(ctx, onItemSelected = { navigation.push(Config.Details(it)) })
        )
        is Config.Details -> RootComponent.Child.DetailsChild(
            DefaultDetailsComponent(ctx, id = config.id, onFinished = navigation::pop)
        )
    }
}
```

Операции: `push(cfg)`, `pop()`, `pop { result -> … }`, `replaceCurrent(cfg)`, `replaceAll(vararg cfg)`, `bringToFront(cfg)` (для bottom-nav: не плодит дубликаты). Все — на Main.

## Child Slot — один-или-ничего

Диалоги, bottom sheet'ы, side panel:

```kotlin
private val dialogNavigation = SlotNavigation<DialogConfig>()

val dialog: Value<ChildSlot<*, DialogComponent>> = childSlot(
    source = dialogNavigation,
    serializer = DialogConfig.serializer(),
    handleBackButton = true,          // dismiss по back
    childFactory = ::dialogChild,
)
// dialogNavigation.activate(DialogConfig(…)) / dialogNavigation.dismiss()
```

## Child Pages — табы/пейджер

```kotlin
val pages: Value<ChildPages<*, PageComponent>> = childPages(
    source = pagesNavigation,
    serializer = PageConfig.serializer(),
    initialPages = { Pages(items = tabs, selectedIndex = 0) },
    childFactory = ::pageChild,
)
// pagesNavigation.select(index)
```

Выбранная страница RESUMED, соседние — CREATED/STOPPED (настраивается `pageStatus`).

## Правила конфигов

- `@Serializable sealed interface` + `data object`/`data class` — equality обязательна.
- Дубликаты (равные по equality) в одном стеке/slot/pages — исключение в рантайме.
- Несколько навигационных моделей в одном компоненте — каждой уникальный `key = "…"`.
- В конфиге — только идентификаторы (id, тип), не тяжёлые объекты: конфиг сериализуется при сохранении состояния.

## Рендер в Compose

```kotlin
@Composable
fun RootContent(component: RootComponent, modifier: Modifier = Modifier) {
    Children(
        stack = component.childStack,
        modifier = modifier,
        animation = stackAnimation(fade() + scale()),
    ) { child ->
        when (val c = child.instance) {
            is RootComponent.Child.ListChild -> ListContent(c.component)
            is RootComponent.Child.DetailsChild -> DetailsContent(c.component)
        }
    }
}
```

Predictive back: `animation = predictiveBackAnimation(backHandler = component.backHandler, fallbackAnimation = stackAnimation(fade()), onBack = component::onBackClicked)` (API `@ExperimentalDecomposeApi`).

## Кастомный back

```kotlin
private val backCallback = BackCallback(isEnabled = true) { showConfirmExit() }
init { backHandler.register(backCallback) }
// backCallback.isEnabled = …  — динамически
```

Приоритет колбэков — параметр `priority`. В Compose-UI — Essenty `BackHandler`, не androidx.
