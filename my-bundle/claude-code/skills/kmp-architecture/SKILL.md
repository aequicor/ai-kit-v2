---
name: kmp-architecture
description: How to structure Kotlin Multiplatform code in ${bundle.input.projectName} — module topology (feature slices, api/impl), Clean Architecture layers with the dependency rule, commonMain vs platform sourcesets, expect/actual vs interface+DI, injecting dispatchers/clock. Use when creating modules or features, deciding where code belongs (domain/data/presentation, common/platform), wiring DI, or reviewing architecture boundaries.
---

# kmp-architecture

Архитектура KMP-кода в **${bundle.input.projectName}**. Плейбуки: [references/layers.md](references/layers.md) (слои и правило зависимостей), [references/platform-di.md](references/platform-di.md) (платформенное через DI).

## Правило зависимостей

```
presentation → domain ← data
```

- `domain` — чистый Kotlin в `commonMain`: модели, интерфейсы репозиториев (порты), use case'ы. **Ноль** импортов Ktor/androidx/БД/`android.*`/`platform.*`.
- `data` — реализации портов: Ktor-клиент, DTO (`@Serializable`), БД, маппинг DTO↔domain. Ничего из этого не поднимается выше data.
- `presentation` — ViewModel/Decompose-компоненты: собирают use case'ы, маппят в UI-state. Зависит от domain, никогда от data напрямую.

## Топология модулей

- **Фиче-слайсы**: каждая фича несёт свои domain/data/presentation. Общий `core` — только контракты и утилиты, не свалка конкретных реализаций.
- Фичи друг от друга не зависят. Нужен вызов фичи из фичи → раздели её на `:feature:api` (контракты+модели) и `:feature:impl`; зависимость — только на `:api`, и это закреплено в Gradle, не дисциплиной.
- `androidMain`/`iosMain`/`jvmMain` — тонкие: `actual`-реализации и нативный клей. Оркестрация и логика — в `commonMain`.

## expect/actual vs интерфейс+DI

| Нужно | Инструмент |
|---|---|
| Подменяемое поведение (репозиторий, источник времени, диспетчеры) | интерфейс в domain + DI (Koin) |
| Прямой нативный API (драйвер БД, `Context`, secure storage, движок Ktor) | `expect/actual` (функции/фабрики) |

`expect/actual` **классы** — Beta (`-Xexpect-actual-classes`); ограничивают одной реализацией на платформу и плохо фейкаются — предпочитай интерфейсы.

## Ошибки через границы

Репозиторий возвращает типизированный sealed-результат (`Result`-иерархию домена), а не бросает `IOException` сквозь слои; конвертация исключений — внутри data.

## DI (Koin)

- Один модуль на фичу + `expect fun platformModule(): Module`; сборка — `initKoin { modules(featureModules + platformModule()) }`.
- Отсутствие platformModule — **рантайм**-ошибка «no definition found»: держи smoke-тест на подъём графа.

## Что НЕ делать

- Не выставляй Swift'у голые `suspend`/`Flow` — SKIE или обёртки, иначе ломаются отмена/потоки.
- Не пиши платформенные `if` в common-коде — это признак пропущенного порта.
- Не превращай shared-модуль в god-module с конкретикой — контракты в core, реализации в фичах.
- `Clock.System.now()`/UUID/random напрямую в domain — инжектируй (тестируемость).
