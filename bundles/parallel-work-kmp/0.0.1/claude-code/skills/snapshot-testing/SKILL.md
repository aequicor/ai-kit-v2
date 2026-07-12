---
name: snapshot-testing
description: How to verify UI in ${bundle.input.projectName} with a parallel-friendly layer first — JVM screenshot/snapshot tests that need no emulator and run concurrently across worktrees (each has its own build/), falling back to a real device only for final E2E, one device per session. Use when asked to test the UI, check a visual change, add a snapshot, investigate a screenshot diff, or verify a Compose screen.
---

# snapshot-testing

Слой UI-проверки для **${bundle.input.projectName}**, который не дерётся за эмулятор в параллельных
сессиях. Принцип: основную массу регрессий ловим JVM-снапшотами (без устройства), на реальном
устройстве — только финальное E2E одной фичи.

## Почему снапшоты первыми

JVM-снапшоты рендерят Compose на хосте, без эмулятора, и пишут в `build/` своего worktree. Поскольку
`build/` у каждой сессии свой, несколько сессий гоняют снапшоты **параллельно** без коллизий — в
отличие от эмулятора, который общий. Поэтому при параллельной работе снапшоты — слой по умолчанию.
<!-- when: ${bundle.input.snapshotTool} == 'roborazzi' -->

## Roborazzi (выбран в этом проекте)

JVM-рендер Compose через Robolectric Native Graphics (`@GraphicsMode(GraphicsMode.Mode.NATIVE)`),
эмулятор не нужен. Кросс-платформенный (поддерживает Compose Multiplatform — desktop, iOS).

| Действие | Команда |
|---|---|
| Записать/обновить эталоны | `./gradlew recordRoborazzi<Variant>` (напр. `recordRoborazziDebug`) |
| Проверить против эталонов | `./gradlew verifyRoborazzi<Variant>` (напр. `verifyRoborazziDebug`) |
| Сравнить и собрать диффы | `./gradlew compareRoborazzi<Variant>` |

Вывод и диффы — в `<module>/build/outputs/roborazzi/` (картинка `golden | actual | diff`).

Точные имена тасок и поддержку конкретного KMP-плагина (включая
`com.android.kotlin.multiplatform.library`) сверь с версией Roborazzi, закреплённой в проекте, через
`./gradlew tasks --all` — не считай по памяти.
<!-- end -->
<!-- when: ${bundle.input.snapshotTool} == 'compose-preview' -->

## Compose Preview Screenshot Testing (выбран в этом проекте)

Официальный инструмент Google на основе `@Preview`/`@PreviewTest`. Требует AGP 8.5+, плагин
`com.android.compose.screenshot` и source set `screenshotTest`. Статус — экспериментальный.

Точные имена update/validate-тасок зависят от версии плагина — найди их через `./gradlew tasks --all`
(обычно `update<Variant>ScreenshotTest` для записи эталонов и `validate<Variant>ScreenshotTest` для
проверки). Не угадывай по памяти.
<!-- end -->

## Разбор диффа

При расхождении прочитай PNG-дифф тулом `Read` — встроенное зрение позволяет классифицировать тип
регрессии (сдвиг элемента, смена цвета, исчезновение текста) и решить: это баг (чинить код) или
намеренное изменение (перезаписать эталон record/update-таской). Эталоны коммить вместе с фичей.

## E2E на устройстве — только финал, по устройству на сессию

Когда снапшотов мало (жесты, реальная клавиатура, системные диалоги) — переходи на устройство, строго
своего лейна (см. скил **parallel-sessions**).
<!-- when: ${bundle.input.mobileMcp} -->

- **claude-in-mobile MCP:** перед действиями выбери устройство своего лейна (`$ANDROID_SERIAL` /
  `$IOS_SIM_UDID`), не «первое в списке». Запусти приложение, обойди экраны, проверь элементы и
  доступность.
<!-- end -->
<!-- when: ${bundle.input.maestroMcp} -->
- **Maestro MCP:** декларативные флоу в `.maestro/*.yaml`. Гоняй с явным устройством:
  `maestro --device "$ANDROID_SERIAL" test .maestro/<flow>.yaml`. Флоу — часть кодовой базы, коммить
  с фичей.
<!-- end -->

Никогда не запускай установку/прогон на устройстве без серийника/UDID своего лейна — попадёшь в чужую
сессию.
