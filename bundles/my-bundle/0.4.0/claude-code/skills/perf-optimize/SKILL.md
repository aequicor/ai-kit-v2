---
name: perf-optimize
description: How to find and fix performance problems in ${bundle.input.projectName} — Compose recomposition/stability (immutable params, strong skipping), deferring state reads to layout/draw, Lazy list keys/contentType, phase-aware drawing, hot-path allocations and collection choices; measure in release builds only. Use when UI jank/slow scroll is reported, recomposition counts explode, frames drop during animation, or when asked to "optimize" / "почему тормозит".
---

# perf-optimize

Оптимизация производительности в **${bundle.input.projectName}**. Плейбуки: [references/compose-recomposition.md](references/compose-recomposition.md), [references/drawing-lists.md](references/drawing-lists.md).

## Порядок работы

1. **Сначала измерь**: release/R8-сборка (debug в разы медленнее и врёт), Layout Inspector (счётчики рекомпозиций), Macrobenchmark для старта/скролла. Оптимизация без замера — запрещена.
2. Найди класс проблемы: лишние рекомпозиции / работа в неверной фазе / аллокации в горячем цикле / медленный I-O на Main.
3. Примени точечный фикс из плейбука, перемерь, зафиксируй в отчёте «до/после».

## Compose: лишние рекомпозиции (кратко)

- Параметры — стабильные: `ImmutableList`/`persistentListOf` вместо `List`; модели — `@Immutable`/`@Stable`; типы из не-Compose модулей — обёртка или stability configuration file.
- Strong skipping (Kotlin 2.0.20+) сравнивает нестабильное **по ссылке**: мутация на месте — UI не обновится; пересоздание — лишняя рекомпозиция. Иммутабельность всё ещё нужна.
- `remember(keys)` для дорогих вычислений; тяжёлое (сортировка, парсинг) — вообще вне composition, в фоне.
- `remember { derivedStateOf { noisy > threshold } }` — когда шумный вход даёт редкий выход; если выход меняется так же часто — не нужен.
- Никаких backwards writes: не пиши в state, уже прочитанный в этом кадре; не заводи layout-результат (`onGloballyPositioned`) обратно во вход composition — многокадровые циклы.

## Фазы: composition → layout → draw

Высокочастотный state читай как можно позже:

| Вместо | Используй |
|---|---|
| `Modifier.offset(x)` | `Modifier.offset { IntOffset(…) }` |
| `Modifier.background(color)` | `drawBehind { drawRect(color()) }` |
| `graphicsLayer(alpha = a)` | `graphicsLayer { alpha = a() }` |

State в такие места передавай лямбдой `() -> T`. Рисование — `drawWithCache` для тяжёлых объектов (см. скил **compose-ui**).

## Списки

- `items(list, key = { it.id }, contentType = { it.type })` — стабильный доменный ключ (не индекс!) + contentType для гетерогенных списков.
- Фиксированные размеры item'ов где можно; вложенные скроллы одного направления — запрещены.

## Аллокации и коллекции на горячих путях

- В per-frame/пер-элементных циклах — без аллокаций: выноси `Path`/`Paint`/буферы наружу, переиспользуй.
- Цепочки `filter{}.map{}` на больших коллекциях в горячем цикле создают промежуточные списки — `asSequence()` или один проход.
- Автобоксинг (`Int` в дженериках/лямбдах горячего цикла) — примитивные массивы (`IntArray`), специализированные структуры.
- Строки в циклах — `StringBuilder`; логирование в горячем пути — лениво (`log.debug { … }`).

## Что НЕ делать

- Не оптимизировать без замера и не мерить в debug.
- Не лепить `@Immutable` на мутабельный тип «для скорости» — устаревший UI хуже медленного.
- Не кэшировать всё подряд: кэш без инвалидации — источник багов; `drawWithCache` на тривиальном рисовании — пессимизация.
- Baseline Profiles — Android-only: не переноси ожидания на desktop/iOS.
