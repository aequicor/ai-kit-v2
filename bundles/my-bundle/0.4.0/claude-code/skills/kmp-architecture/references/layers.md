# Слои Clean Architecture в KMP

## Раскладка фичи

```
feature/orders/
├── domain/          # commonMain, чистый Kotlin
│   ├── model/       #   Order, OrderId, OrderStatus (data/sealed/value class)
│   ├── repository/  #   interface OrderRepository (порт)
│   └── usecase/     #   class GetOrders(private val repo: OrderRepository) { suspend operator fun invoke(...) }
├── data/            # commonMain + платформенные actual при необходимости
│   ├── remote/      #   OrderApi (Ktor), OrderDto (@Serializable)
│   ├── local/       #   таблицы/DAO БД
│   ├── OrderRepositoryImpl.kt
│   └── mapper/      #   OrderDto.toDomain(), Order.toEntity()
└── presentation/    # ViewModel или Decompose-компонент
    └── OrdersComponent.kt / OrdersViewModel.kt
```

## Домен: критерии чистоты

- Импорты — только Kotlin stdlib, kotlinx.coroutines (Flow/suspend — допустимы как язык асинхронности), свои модели.
- Ни одного упоминания Ktor, sqldelight/room, androidx, `@Serializable`.
- Use case — один класс, одна операция, `operator fun invoke`; композиция use case'ов — тоже use case.

## Data: где что живёт

- DTO повторяет форму API/БД и **никогда** не используется как доменная модель — даже если поля совпадают сегодня.
- Маппинг DTO↔domain — рядом с реализацией репозитория (extension-функции `toDomain()`/`toDto()`).
- Ошибки: `catch` сетевых/БД-исключений здесь, наружу — доменный sealed-результат:

```kotlin
sealed interface OrdersResult {
    data class Success(val orders: List<Order>) : OrdersResult
    data class Failure(val error: OrdersError) : OrdersResult
}
sealed interface OrdersError { data object Network : OrdersError; data object Unauthorized : OrdersError }
```

## Presentation

- Держатель состояния — один на проект: androidx `ViewModel` (lifecycle 2.8+, доступен в commonMain) **или** Decompose-компонент — не смешивать оба подхода на одном экране.
- Наружу — иммутабельный state (`StateFlow`/`Value`); UI (Compose) — stateless.
- Presentation не знает про DTO и Ktor; только use case'ы и доменные модели.

## Контроль границ

- Границы закрепляй зависимостями Gradle: у `:feature:orders:domain` в dependencies нет ktor/sqldelight — тогда нарушение не соберётся.
- В ревью первым делом смотри импорты изменённых файлов: импорт `io.ktor.*` в domain/presentation — стоп-сигнал.
- Общий `core`: контракты (`DispatcherProvider`, `Clock`-порт, Result-типы), дизайн-токены, утилиты. Не конкретные репозитории.

## Sourcesets

- `commonMain` — почти всё: домены, данные, presentation-логика.
- `androidMain`/`iosMain` — реализации портов: драйвер БД, secure storage, платёжные SDK.
- `commonTest` — юнит-тесты на фейках; прогон — `./gradlew allTests` (все таргеты) или `:module:jvmTest` для скорости.
