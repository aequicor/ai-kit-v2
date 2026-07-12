# API Compose-компонента

## Эталонная сигнатура

```kotlin
@Composable
fun AvatarBadge(
    user: User,                                  // обязательные данные
    onClick: () -> Unit,                         // обязательные колбэки
    modifier: Modifier = Modifier,               // ПЕРВЫЙ опциональный
    size: AvatarSize = AvatarSize.Medium,        // опциональные стили
    badge: (@Composable () -> Unit)? = null,     // слот
) {
    Box(modifier = modifier.clickable(onClick = onClick)) { // modifier — один раз, к внешнему узлу
        …
    }
}
```

## State hoisting: stateless-ядро + stateful-обёртка

```kotlin
// Ядро — единственный обязательный API
@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
)

// Обёртка — тонкое удобство, НЕ замена ядра
@Composable
fun SearchField(modifier: Modifier = Modifier) {
    var query by rememberSaveable { mutableStateOf("") }
    SearchField(query, { query = it }, modifier)
}
```

Правило: у компонента с состоянием всегда должна оставаться hoisted-версия — иначе его нельзя контролировать снаружи и тестировать.

## Слоты вместо флагов и модификаторов

Плохо: `showIcon: Boolean`, `iconModifier: Modifier`. Хорошо:

```kotlin
@Composable
fun ListItem(
    headline: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
)
```

- Слот даёт вызывающему полный контроль контента; компонент отвечает только за раскладку.
- Для коллекций-слотов — scoped-лямбды (`RowScope.() -> Unit`, `LazyListScope.() -> Unit`).

## Модификатор: дисциплина

- Параметр называется `modifier`, тип `Modifier`, default `Modifier`.
- Компонент **не** решает за вызывающего размер/отступ снаружи — это дело переданного modifier'а; внутренние отступы — дело компонента.
- Порядок в цепочке значим: `padding` до/после `background`/`clip` — разные результаты; `drawBehind` использует границы узла в этой точке цепочки.
- Кастомный переиспользуемый модификатор — `Modifier.Node`:

```kotlin
private class GlowNode(var color: Color) : Modifier.Node(), DrawModifierNode {
    override fun ContentDrawScope.draw() { …; drawContent() }
}
private data class GlowElement(val color: Color) : ModifierNodeElement<GlowNode>() {
    override fun create() = GlowNode(color)
    override fun update(node: GlowNode) { node.color = color }
}
fun Modifier.glow(color: Color): Modifier = this then GlowElement(color)
```

`Modifier.composed {}` не использовать — не скипается и перезапускается на каждой рекомпозиции.

## Стабильность параметров

- Модели параметров — `@Immutable` (никогда не меняются) или `@Stable` (меняются только через Compose State). `@Immutable` на реально мутабельном типе = устаревший UI.
- Коллекции всегда нестабильны — `ImmutableList`/`persistentListOf` (kotlinx.collections.immutable) или обёртка-модель.
- Strong skipping (Kotlin 2.0.20+) сравнивает нестабильные параметры по ссылке: мутация на месте не обновит UI, пересоздание списка — лишняя рекомпозиция. Иммутабельные обёртки по-прежнему нужны.

## Превью

- Каждому компоненту — `@Preview` на stateless-ядро с фейковыми данными.
- Побочные эффекты гейти `if (!LocalInspectionMode.current) { … }`, иначе превью ломаются.
