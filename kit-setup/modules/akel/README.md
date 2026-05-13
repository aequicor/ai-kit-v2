# modules/akel

**AKEL** (AI-Kit Expression Language) — минималистичный строго типизированный язык булевых выражений. Используется в поле `when` манифеста бандла (`config.json`) для условного включения/исключения секций конфигурации перед передачей генератору агента.

Модуль — самостоятельный, не зависит от `:modules:core`. Kotlin Multiplatform, таргеты: `linuxX64`, `macosArm64`, `mingwX64`.

Полная спецификация языка — в [`kit-setup/CONFIG_JSON.md`](../../CONFIG_JSON.md). Этот README — краткое описание API модуля.

## Что умеет

| Категория | Примеры |
|---|---|
| Литералы | `true`, `42`, `-7`, `0.7`, `'full'`, `['ci', 'full']` |
| Ссылки | `${bundle.input.profile}`, `${bundle.meta.version}`, `${project.name}` |
| Логика | `&&`, `\|\|`, `!` |
| Равенство | `==`, `!=` |
| Сравнение | `<`, `<=`, `>`, `>=` (int, double, string) |
| Членство | `<value> in <list>` |
| Группировка | `(...)` |

Приоритет (от высшего к низшему): `!` → сравнения → `==`/`!=` → `in` → `&&` → `||`.

Чего намеренно нет: арифметики, функций, тернарного оператора, доступа к ФС/env.

## Ссылки

Ссылка имеет вид `${<dotted.path>}`. **AKEL не интерпретирует путь** — это произвольная дотированная строка, которую `AkelContext` интегрирующего слоя резолвит как ему удобно. Какие namespace-ы поддерживать (`bundle.input.*`, `bundle.meta.*`, `project.*` и т. п.) — решает host-приложение, не язык.

Сегменты пути — идентификаторы: `letter|_ { letter|digit|_|- }`. Минимум один сегмент.

## API

Пакет: `io.aequicor.aikit.akel`.

| Тип | Назначение |
|---|---|
| `Akel` | Фасад: `parse(source)`, `evaluate(source, ctx)` |
| `AkelExpression` | Распарсенное выражение, переиспользуемое: `evaluate(ctx)`, `evaluateAsBoolean(ctx)` |
| `AkelContext` | `fun interface` — `lookup(path: String): AkelValue?`. Готовые: `EMPTY`, `of(Map<String, AkelValue>)` |
| `AkelValue` | Sealed: `Bool`, `Int`, `Dbl`, `Str`, `Lst` |
| `AkelType` | Тег типа для сообщений об ошибках |
| `AkelError` | Sealed: `Syntax(position)`, `Type`, `UnknownRef(path)` |

Все операции возвращают `Result<T>` — исключения не выбрасываются за пределы `Result`.

## Использование

### Одношаговое вычисление

```kotlin
import io.aequicor.aikit.akel.Akel
import io.aequicor.aikit.akel.AkelContext
import io.aequicor.aikit.akel.AkelValue

val ctx = AkelContext.of(
    mapOf(
        "bundle.input.profile" to AkelValue.Str("full"),
        "bundle.input.skills"  to AkelValue.Lst(listOf(AkelValue.Str("review"))),
    ),
)

val active: Boolean = Akel
    .evaluate("\${bundle.input.profile} == 'full' && 'review' in \${bundle.input.skills}", ctx)
    .getOrThrow()
```

### Кастомный контекст с несколькими namespace-ами

```kotlin
val ctx = AkelContext { path ->
    when {
        path.startsWith("bundle.input.")  -> bundleInputs[path.removePrefix("bundle.input.")]
        path.startsWith("bundle.meta.")   -> bundleMeta[path.removePrefix("bundle.meta.")]
        path.startsWith("project.")       -> project[path.removePrefix("project.")]
        else -> null
    }
}
```

### Кэширование разобранного выражения

```kotlin
val expr = Akel.parse("\${bundle.input.flag}").getOrThrow()

val a = expr.evaluateAsBoolean(AkelContext.of(mapOf("bundle.input.flag" to AkelValue.Bool(true))))
val b = expr.evaluateAsBoolean(AkelContext.of(mapOf("bundle.input.flag" to AkelValue.Bool(false))))
```

### Обработка ошибок

```kotlin
when (val ex = Akel.parse(source).exceptionOrNull()) {
    is AkelError.Syntax     -> println("syntax error at ${ex.position}: ${ex.message}")
    is AkelError.Type       -> println("type error: ${ex.message}")
    is AkelError.UnknownRef -> println("unknown reference: ${ex.path}")
    null                    -> { /* ok */ }
    else                    -> throw ex
}
```

## Семантика

- **Жёсткая типизация.** `${bundle.input.count} == '5'` при `count: int` → `AkelError.Type`.
- **Boolean как `when`.** `"${bundle.input.flag}"` эквивалентно `"${bundle.input.flag} == true"`.
- **Короткое замыкание.** `false && X` и `true || X` не вычисляют правую часть — неразрешимые ссылки в отрезанной ветке не приводят к ошибке.
- **`in`** — левая часть должна быть совместима по типу с элементами правой (списка).

## Команды

Из корня `kit-setup/`:

```bash
# Тесты модуля
./gradlew :modules:akel:allTests

# Конкретный тестовый класс
./gradlew :modules:akel:allTests --tests "io.aequicor.aikit.akel.AkelTest"

# Статический анализ
./gradlew :modules:akel:detekt

# Полная сборка
./gradlew :modules:akel:build
```

## Структура

```
modules/akel/
├── build.gradle.kts
└── src/
    ├── commonMain/kotlin/io/aequicor/aikit/akel/
    │   ├── Akel.kt              # фасад
    │   ├── AkelExpression.kt    # распарсенное выражение
    │   ├── AkelContext.kt       # разрешение ссылок
    │   ├── AkelValue.kt         # типизированные значения
    │   ├── AkelType.kt          # теги типов
    │   ├── AkelError.kt         # ошибки парсинга/вычисления
    │   └── internal/            # лексер, парсер, AST, эвалюатор
    └── commonTest/kotlin/io/aequicor/aikit/akel/
        └── AkelTest.kt
```

`internal/` — детали реализации, не входят в публичный API.
