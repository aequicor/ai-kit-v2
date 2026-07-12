# Ktor: hardening периметра

## TLS / транспорт

```kotlin
install(HttpsRedirect) { sslPort = 443; permanentRedirect = true }
install(HSTS) { includeSubDomains = true; maxAgeInSeconds = 31_536_000 }
```

- TLS терминируется на прокси — ок, но `HttpsRedirect`/`HSTS` оставляй; между прокси и приложением — приватная сеть.
- Отключение проверки сертификатов у HTTP-клиента (`trustManager` = accept-all) не коммитится никогда — даже «для стейджа».

## CORS

```kotlin
install(CORS) {
    allowHost("app.example.com", schemes = listOf("https"))   // scheme указывать явно!
    allowHeader(HttpHeaders.Authorization)
    allowCredentials = true
    allowMethod(HttpMethod.Put); allowMethod(HttpMethod.Delete)
}
```

- `anyHost()` — только локальная отладка; с `allowCredentials = true` браузеры его вообще отвергают.
- `allowHost` без `schemes` по умолчанию не включает https-вариант с нестандартной схемой записи — задавай явно.

## Rate limiting

```kotlin
install(RateLimit) {
    register(RateLimitName("auth")) {
        rateLimiter(limit = 10, refillPeriod = 60.seconds)
        requestKey { call -> call.request.origin.remoteAddress }   // или principal
    }
}
routing {
    rateLimit(RateLimitName("auth")) {
        post("/login") { … }
        post("/token") { … }
    }
}
```

- Ключ per-IP валиден только если IP настоящий: `XForwardedHeaders`/`ForwardedHeaders` подключай **только** за доверенным прокси; иначе заголовок подделывается и лимит обходится.
- 429 наружу — без деталей внутреннего состояния.

## Заголовки и ошибки

```kotlin
install(DefaultHeaders) { header("X-Content-Type-Options", "nosniff") }
install(StatusPages) {
    exception<Throwable> { call, cause ->
        call.application.log.error("Unhandled", cause)             // подробности — в лог
        call.respond(HttpStatusCode.InternalServerError, ErrorBody("internal_error"))  // наружу — нейтрально
    }
}
```

Стектрейс/сообщение исключения в теле ответа — утечка внутренностей; всегда нейтральное тело + лог.

## Логирование

```kotlin
install(CallLogging) {
    level = Level.INFO
    filter { call -> !call.request.path().startsWith("/health") }
    // НЕ логировать: Authorization, Cookie, тела запросов/ответов
}
```

- `LogLevel.ALL`/`logging body` у клиента и сервера — только локальная отладка, не коммитить.
- Провал аутентификации логируй фактом («invalid api key for client X»), не значением секрета.
- PII в логах — по минимуму и осознанно (регуляторика).

## SSRF и исходящие запросы

- URL от пользователя не фетчить без allowlist хостов/схем.
- Резолв редиректов у клиента ограничивай; `followRedirects = false`, если не нужен.

## Сериализация

- kotlinx.serialization: polymorphic-десериализация недоверенного ввода — только с закрытым `SerializersModule` (никакой регистрации «всего»).
- Ограничивай размер тела (`install(RequestValidation)` / лимиты движка) — защита от OOM-баллонов.
