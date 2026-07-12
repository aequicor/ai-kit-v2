# Тестирование защищённых маршрутов Ktor

## testApplication — база

```kotlin
@Test
fun `orders requires auth`() = testApplication {
    application { module() }                       // РЕАЛЬНЫЙ module(), не пересборка руками

    val response = client.get("/orders")

    assertEquals(HttpStatusCode.Unauthorized, response.status)
}
```

- Тестируй сконфигурированный `module()` — иначе проверяешь не то, что в проде.
- `withTestApplication` / `handleRequest` удалены в Ktor 3 — только `testApplication {}` + `client`.

## Конфиг и секреты в тестах

```kotlin
testApplication {
    environment {
        config = MapApplicationConfig(
            "jwt.secret" to "test-secret",
            "jwt.issuer" to "test",
            "jwt.audience" to "test",
        )
    }
    application { module() }
    …
}
```

Или `config = ApplicationConfig("application-test.yaml")`. Боевые секреты в тестах не используются.

## Авторизованные запросы

```kotlin
private fun makeToken(sub: String = "user-1") =
    JWT.create().withIssuer("test").withAudience("test")
        .withClaim("sub", sub)
        .withExpiresAt(Date(System.currentTimeMillis() + 60_000))
        .sign(Algorithm.HMAC256("test-secret"))

@Test
fun `orders returns own orders`() = testApplication {
    environment { config = testConfig }
    application { module() }

    val response = client.get("/orders") {
        header(HttpHeaders.Authorization, "Bearer ${makeToken()}")
    }

    assertEquals(HttpStatusCode.OK, response.status)
    assertTrue(response.bodyAsText().contains("\"orders\""))
}
```

## JSON-клиент и куки

```kotlin
val jsonClient = createClient {
    install(ContentNegotiation) { json() }   // как на сервере
    install(HttpCookies)                     // сессии между запросами
}
```

## Обязательная негативная матрица

| Кейс | Ожидание |
|---|---|
| Без токена | 401 |
| Битый/протухший токен | 401 |
| Валидный токен, чужой ресурс | 403 или 404 (IDOR!) |
| Плохой ввод | 400/422, без стектрейса в теле |
| Несуществующий id | 404 |
| Повтор POST | 409 или идемпотентный 200 — по контракту |

## Внешние identity-провайдеры

`externalServices { hosts("https://auth.example.com") { routing { … } } }` — мок стороннего OAuth/JWKS без сети.

## Дисциплина

- Ассерты — на статус **и** тело, не на «запрос не упал».
- Красный негативный кейс — это находка безопасности, не «шумный тест»: чини код.
