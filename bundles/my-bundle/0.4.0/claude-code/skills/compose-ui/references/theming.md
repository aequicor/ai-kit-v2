# Дизайн-система: тема и токены

## Токены как @Immutable-классы

```kotlin
@Immutable
data class AppColors(
    val background: Color,
    val surface: Color,
    val textPrimary: Color,
    val accent: Color,
)

@Immutable
data class AppTypography(
    val headline: TextStyle,
    val body: TextStyle,
    val caption: TextStyle,
)

@Immutable
data class AppShapes(
    val small: Shape,
    val medium: Shape,
    val large: Shape,
)
```

`@Immutable` обязателен: без него каждый read токена тащит рекомпозиции.

## CompositionLocal + провайдер темы

```kotlin
val LocalAppColors = staticCompositionLocalOf<AppColors> { error("No AppColors provided") }
val LocalAppTypography = staticCompositionLocalOf<AppTypography> { error("No AppTypography provided") }

object AppTheme {
    val colors: AppColors @Composable get() = LocalAppColors.current
    val typography: AppTypography @Composable get() = LocalAppTypography.current
}

@Composable
fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors
    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalAppTypography provides Typography,
        content = content,
    )
}
```

- `staticCompositionLocalOf` — для значений, меняющихся редко (тема целиком); смена пересобирает всё поддерево, зато чтение бесплатное.
- `compositionLocalOf` — если значение меняется часто и надо рекомпозировать только читателей.
- Высокочастотные значения через широкий CompositionLocal не гонять.

## Правила использования

- Компоненты читают **только** `AppTheme.colors.…` / `AppTheme.typography.…` — никаких `Color(0xFF…)` в теле компонента.
- Светлая/тёмная тема — два набора токенов в провайдере; компоненты про режим не знают.
- Новый цвет/стиль — сначала добавь токен, потом используй; «одноразовый» литерал — почти всегда ошибка дизайна.

## Per-component style objects

Для сложных компонентов — объект стиля с дефолтами из темы (паттерн Material `ButtonDefaults`):

```kotlin
@Immutable
data class BadgeStyle(val container: Color, val content: Color, val shape: Shape)

object BadgeDefaults {
    @Composable
    fun style(
        container: Color = AppTheme.colors.accent,
        content: Color = AppTheme.colors.textPrimary,
        shape: Shape = AppTheme.shapes.small,
    ) = BadgeStyle(container, content, shape)
}

@Composable
fun Badge(text: String, modifier: Modifier = Modifier, style: BadgeStyle = BadgeDefaults.style())
```

Это единственное место, где допустима «настройка внешности» параметрами — через типизированный style-объект, не через десяток отдельных Color-параметров.

## Compose Multiplatform

- Токены и тема живут в `commonMain` — они не платформенны.
- Платформенные различия (динамические цвета Android 12+, системная типографика) — инжектируются в провайдер темы из платформенного кода, компоненты не меняются.
