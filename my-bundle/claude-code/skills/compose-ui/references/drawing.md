# Кастомное рисование: drawBehind, drawWithCache, DrawScope

## Дерево выбора

1. Простой фон/фигура, ничего не аллоцируется → `Modifier.drawBehind {}`.
2. Нужны тяжёлые объекты (`Path`, `Brush`, `Shader`, измеренный текст) → `Modifier.drawWithCache {}`.
3. Надо рисовать **вокруг** контента (под и над) → `Modifier.drawWithContent {}` + `drawContent()`.
4. Компонент, который **и есть** рисунок (график, индикатор) → `Canvas(modifier) {}` (это `Spacer.drawBehind`).

## drawWithCache: правила кэша

```kotlin
Modifier.drawWithCache {
    // Блок кэша: выполняется при изменении size или прочитанного здесь state
    val path = buildWavePath(size)                    // тяжёлое — здесь
    val brush = Brush.linearGradient(colors)
    onDrawBehind {
        // per-frame: только команды рисования
        drawPath(path, brush)
        drawCircle(colorProvider())                   // частый state читать ЗДЕСЬ
    }
}
```

- Кэш инвалидируется при смене `size` **или любого state, прочитанного в блоке кэша**. Частый state в блоке кэша = кэш бесполезен.
- Тривиальному рисованию `drawWithCache` только вредит (лишние аллокации лямбд) — там `drawBehind`.

## Текст на Canvas

```kotlin
val textMeasurer = rememberTextMeasurer()
Modifier.drawWithCache {
    val layout = textMeasurer.measure(text, style)    // мерить в кэше
    onDrawBehind { drawText(layout, topLeft = …) }
}
```

`textMeasurer.measure()` в per-frame лямбде — дорого; только в кэш-блоке/`remember`.

## Фаза draw: «рисуй чаще, рекомпозируй реже»

Высокочастотный state (анимация, скролл, drag) не должен перезапускать composition:

| Вместо (composition) | Используй (layout/draw) |
|---|---|
| `Modifier.offset(x, y)` | `Modifier.offset { IntOffset(x.roundToPx(), 0) }` |
| `Modifier.background(color)` | `Modifier.drawBehind { drawRect(color()) }` |
| `Modifier.graphicsLayer(alpha = a)` | `Modifier.graphicsLayer { alpha = a() }` |

State передавай лямбдой `() -> T`, чтобы чтение произошло в поздней фазе.

## DrawScope: механика

- Координаты: origin — левый верхний угол, y вниз; всё в px — `4.dp.toPx()`.
- Трансформации группируй: `withTransform { rotate(45f); scale(2f) } { drawRect(…) }` — вместо вложенных `rotate { scale { … } }`.
- Клиппинг: `clipRect`/`clipPath { … }`.
- `BlendMode.Clear` и другие offscreen-эффекты требуют `Modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }`.
- Побег к нативному канвасу — `drawIntoCanvas { it.nativeCanvas … }` (платформенно; в KMP — только в платформенных sourcesets).

## Чек-лист перед коммитом рисующего кода

- [ ] Ни одной аллокации (`Path()`, `Paint()`, `Brush.…`, `measure`) в per-frame лямбде.
- [ ] Частый state читается в draw-фазе (лямбды), не в composition.
- [ ] Кэш-блок `drawWithCache` не читает частый state.
- [ ] dp→px через `toPx()`, никаких магических чисел-пикселей.
