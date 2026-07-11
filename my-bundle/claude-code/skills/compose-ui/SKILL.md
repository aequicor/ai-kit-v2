---
name: compose-ui
description: How to design reusable Compose components, theming and custom drawing in ${bundle.input.projectName} — component API contract (state hoisting, modifier discipline, slots), design-system tokens via CompositionLocal, drawBehind/drawWithCache and phase-aware rendering. Use when creating or reviewing composables, building design-system components, theming (colors/typography/shapes, light/dark), or drawing custom graphics on Canvas.
---

# compose-ui

Проектирование UI-компонентов, темы и кастомного рисования в **${bundle.input.projectName}**. Плейбуки: [references/component-api.md](references/component-api.md), [references/theming.md](references/theming.md), [references/drawing.md](references/drawing.md).

## Контракт компонента

- **Stateless-ядро** + state hoisting: компонент принимает `value` и `onValueChange`, состояние держит вызывающий. Stateful-обёртка (`remember` внутри) — только как тонкое удобство поверх stateless-ядра, никогда как единственный API.
- **Порядок параметров:** обязательные → `modifier: Modifier = Modifier` (первый опциональный!) → опциональные/стили → слоты/колбэки. Опциональные лямбды по умолчанию `{}`, не `null`.
- **Один `modifier`** на компонент; применяется ровно один раз — к внешнему layout-узлу. Внутренняя кастомизация — слоты (`content: @Composable () -> Unit`, scoped `RowScope.() -> Unit`) и style-объекты, не `textModifier`/`iconModifier`.
- Именование: emitter'ы — PascalCase-существительные (`AvatarBadge`), колбэки — `onX`; composable возвращает `Unit`.
- Никаких side effects в composition; побочки — `LaunchedEffect`/`DisposableEffect`/колбэки. В превьюшных компонентах перед сетью/инициализацией — гейт `LocalInspectionMode.current`.

## Тема и токены

- Все цвета/размеры/шейпы/типографика — через дизайн-систему: `CompositionLocal` + `@Immutable` data-классы токенов. Хардкод-литералы в компонентах запрещены.
- Светлая/тёмная тема — на уровне провайдера темы, компоненты читают только токены.
- Детали (структура токенов, per-component style objects) — [references/theming.md](references/theming.md).

## Кастомное рисование — выбор инструмента

| Инструмент | Когда |
|---|---|
| `drawBehind {}` | простые фоны/фигуры без аллокаций |
| `drawWithCache {}` | кадр требует тяжёлых объектов: `Path`, `Brush`, `Shader`, измеренный текст |
| `drawWithContent {}` | нужно рисовать вокруг `drawContent()` |

- **Не аллоцируй** `Path`/`Brush`/`TextMeasurer.measure` в per-frame лямбде — строй в `drawWithCache` (инвалидация по size/state) или `remember`.
- Высокочастотный state читай в фазе draw/layout: `drawBehind { drawRect(colorProvider()) }`, `graphicsLayer {}`, `offset { IntOffset(…) }`; state передавай как `() -> T`.
- Внутри `DrawScope`: origin — левый верх, dp конвертируй `.toPx()`, трансформации — `withTransform { rotate(); scale() }`.

## Что НЕ делать

- `Modifier.composed {}` — устарел; кастомные модификаторы — через `Modifier.Node` + `ModifierNodeElement`.
- Читать часто меняющийся state в блоке кэша `drawWithCache` — это сбрасывает кэш каждый кадр; читай его в `onDrawBehind`.
- `BlendMode.Clear`/offscreen-эффекты без `graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }` — рендер будет неверным.
- Верстать под конкретный размер экрана — используй интринсики/веса/`BoxWithConstraints`.
