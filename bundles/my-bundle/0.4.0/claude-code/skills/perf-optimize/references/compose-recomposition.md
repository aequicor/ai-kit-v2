# Compose: рекомпозиции и стабильность в деталях

## Модель скипа

Composable скипается, если Compose уверен, что входы не изменились. До strong skipping это требовало **стабильных** типов; со strong skipping (по умолчанию с Kotlin 2.0.20 / Compose 1.7) скипаются и нестабильные — но сравнение **по ссылке** (`===`).

Следствия:

- `list.add(item)` (мутация на месте) — ссылка та же → скип → **UI молча не обновился**. Худший класс багов.
- `list.map { … }` на каждом кадре — новая ссылка → рекомпозиция всегда.
- Поэтому иммутабельные коллекции (`kotlinx.collections.immutable`) и `@Immutable`-модели нужны по-прежнему: они дают structural equality и предсказуемость.

## Стабильность типов

| Тип | Статус |
|---|---|
| primitives, String, лямбды | стабильны |
| `List`/`Set`/`Map` (даже read-only) | **нестабильны всегда** |
| `MutableState`, `SnapshotStateList/Map` | стабильны |
| data class со всеми `val` стабильных типов | стабилен (в модуле с Compose-компилятором) |
| тип из модуля без Compose-компилятора | **нестабилен всегда** |

Лечение нестабильного:

1. `ImmutableList`/`PersistentList` в сигнатуре параметра.
2. UI-модель-обёртка в модуле с Compose.
3. Stability configuration file (`compose.stabilityConfigurationFile`) для чужих типов.
4. `@Immutable` (никогда не меняется) / `@Stable` (меняется только через State) — это **обещание**; нарушишь — устаревший UI.

## Диагностика

- Layout Inspector → Recomposition counts: ищи счётчики, растущие при действии, которое «не должно» трогать этот узел.
- Compose compiler metrics/reports (`-P …metricsDestination/reportsDestination`): колонки skippable/stable по каждому composable — точный список виновников.
- `@DontMemoize` на лямбде — отключить авто-мемоизацию точечно (редко нужно).

## derivedStateOf: когда да / когда нет

```kotlin
// ДА: скролл (шумный) → булево (редкое)
val showFab by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

// НЕТ: выход меняется с той же частотой, derivedStateOf — лишний слой
val label = "Count: $count"
```

Всегда оборачивай в `remember { derivedStateOf { … } }` — без remember пересоздаётся каждый раз.

## Эффекты и порядок

- Composable может выполняться в любом порядке и параллельно: не аккумулируй в захваченные `var` во время composition (`items++` в цикле — баг).
- `remember` без ключей переживает рекомпозицию, но не конфиг-чендж/process death — `rememberSaveable` для UI-state.
- Высокочастотное значение через широкий `CompositionLocal` рекомпозирует всех читателей — не гоняй через него скролл/анимацию.

## CompositionLocal и темы

- `staticCompositionLocalOf` для темы: чтение бесплатно, смена пересобирает поддерево (тема меняется редко — ок).
- Точечные скины/стили — параметрами/style-объектами, не новым CompositionLocal на каждый чих.

## Чек-лист ревью на рекомпозиции

- [ ] Параметры-коллекции — Immutable*?
- [ ] Модели — `@Immutable`/`@Stable` и честно ли?
- [ ] Дорогое — в `remember`/фоне?
- [ ] Шумный state → `derivedStateOf`/поздняя фаза?
- [ ] Нет записи в state во время composition?
- [ ] Лямбды не пересоздаются с захватом нестабильного? (strong skipping мемоизирует сам, но захват мутабельного — риск)
