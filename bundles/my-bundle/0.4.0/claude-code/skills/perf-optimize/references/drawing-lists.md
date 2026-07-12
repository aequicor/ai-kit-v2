# Perf: рисование и ленивые списки

## Ленивые списки: полный чек-лист

```kotlin
LazyColumn {
    items(
        items = feed,
        key = { it.id },                 // стабильный ДОМЕННЫЙ ключ
        contentType = { it.cardType },   // переиспользование слотов между типами
    ) { item -> FeedCard(item) }
}
```

- **key**: индекс = дефолт и антипаттерн — при вставке/удалении всё ниже пересоздаётся, анимации и scroll position ломаются. Ключ — сохраняемый тип (Long/String): участвует в state restoration.
- **contentType**: без него слоты не переиспользуются между разными шаблонами item'ов — заметно на смешанных лентах.
- Item'ы с внутренним state/эффектами вне `items` DSL (цикл + композиция вручную) — оборачивай `key(id) { … }`.
- Тяжёлый item: выноси вычисления из composable item'а (готовь данные заранее); картинки — с плейсхолдерами и правильным размером запроса.
- Не вкладывай скролл в скролл одного направления; для смешанных лент — один LazyColumn с типами item'ов.
- Прыжки скролла — `rememberLazyListState()` хойстится и переживает навигацию.

## Анимации без рекомпозиций

```kotlin
val offset by animateFloatAsState(target)          // state меняется каждый кадр

// ПЛОХО: чтение в composition
Box(Modifier.offset(x = offset.dp))

// ХОРОШО: чтение в layout-фазе
Box(Modifier.offset { IntOffset(offset.roundToPx(), 0) })
```

- Аналогично `graphicsLayer { alpha/scale/rotation }` для draw-фазы.
- Цвет фона от state — `drawBehind { drawRect(color()) }`, где `color: () -> Color`.
- Бесконечные анимации (`rememberInfiniteTransition`) — убедись, что читаются только в draw/layout.

## Рисование: аллокации

- Per-frame лямбды (`drawBehind`, `onDrawBehind`) — **ноль** аллокаций: `Path`/`Brush`/`Paint`/measure — в `drawWithCache`/`remember`.
- Кэш-блок `drawWithCache` не должен читать частый state (сбрасывает кэш каждый кадр) — частое читай в `onDrawBehind`.
- Текст: `rememberTextMeasurer()` + measure в кэше.
- Слои: `graphicsLayer` создаёт отдельный слой — дёшево для transform/alpha, но не плоди сотнями; `CompositingStrategy.Offscreen` — только когда нужен (BlendMode-эффекты).

## Горячие пути вне UI

- Коллекции: одна итерация вместо цепочки `filter{}.map{}` (или `asSequence()`); заранее заданный `initialCapacity` у билдеров.
- Примитивы: `IntArray`/`FloatArray` вместо `List<Int>` в математике; избегай боксинга в лямбдах горячего цикла.
- Строки: `StringBuilder`/`buildString`; конкатенация в цикле — квадратичная.
- I/O и парсинг — никогда на Main: `withContext(injected.io)`; батчинг мелких запросов.
- Логи в горячем цикле — под уровнем и лениво.

## Замер

| Инструмент | Что даёт |
|---|---|
| Layout Inspector | счётчики рекомпозиций живьём |
| Compose compiler reports | skippable/stable по composable |
| Macrobenchmark + Baseline Profiles (Android) | старт, скролл-джанк в release |
| `measureTime {}` вокруг подозреваемого | грубая локализация не-UI горячих мест |

Всегда: release/R8, реальное устройство (не эмулятор) для UI-метрик, фиксируй «до/после» в отчёте.
