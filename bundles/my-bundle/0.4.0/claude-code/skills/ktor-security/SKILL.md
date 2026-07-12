---
name: ktor-security
description: How to implement authentication and server security in Ktor 3.x for ${bundle.input.projectName} — named auth providers, JWT/API-key validation, principals after the 3.x Principal deprecation, secrets via env config, TLS/HSTS/CORS/RateLimit hardening, testApplication for protected routes. Use when adding or reviewing auth, protecting endpoints, handling API keys or tokens, configuring CORS/TLS/logging, or testing secured routes.
---

# ktor-security

Аутентификация и безопасность Ktor-сервера (3.x) в **${bundle.input.projectName}**. Плейбуки: [references/auth.md](references/auth.md), [references/hardening.md](references/hardening.md), [references/testing.md](references/testing.md).

## Каркас

- `install(Authentication)` с **именованными** провайдерами: `jwt("auth-jwt")`, `basic("auth-basic")`, `bearer("auth-bearer")`. Безымянный допустим только один; опечатка в имени у `authenticate("…")` падает в **рантайме**.
- Маршруты защищай `authenticate("name") { … }`; личность — `call.principal<UserPrincipal>()`.
- **Ktor 3.x:** интерфейс `Principal` deprecated; `validate {}` возвращает `Any?` — возвращай свой `data class UserPrincipal(...)` (НЕ имплементи `Principal`), при провале — `null`.
- `challenge {}` отдаёт чистый 401 без объяснения причин; `authenticate(optional = true)` — только там, где аноним действительно допустим.

## Секреты

- Только из env: в HOCON — `jwt.secret = ${JWT_SECRET}` (фигурные скобки обязательны; в YAML — `$VAR` тоже работает), чтение — `environment.config.property("jwt.secret").getString()`.
- Никаких секретов в `application.conf`/`application.yaml` под git, в коде, в логах.

## Сравнение ключей

`==` для API-ключей/токенов — **timing attack**. Только константное время:

```kotlin
MessageDigest.isEqual(provided.toByteArray(), expectedHash)
```

Ключи храни хэшированными, ищи по хэшу; официальный доковый пример с `==` не копировать.

## JWT (минимум)

`verifier(JWT.require(alg).withIssuer(issuer).withAudience(audience).build())` + проверка кастомных claims в `validate {}` (безусловный не-null = любой подписанный токен проходит). `exp` проверяется автоматически; токены — короткоживущие.

## Периметр (кратко)

- HTTPS всегда: `HttpsRedirect` + `HSTS`; проверку сертификатов не отключать даже «на время».
- `RateLimit` (ktor-server-rate-limit) перед login/token/API-key маршрутами, ключ per-IP/per-principal.
- CORS: перечисление `allowHost("app.example.com", schemes = listOf("https"))`; **никогда** `anyHost()` — тем более с `allowCredentials = true`.
- `XForwardedHeaders` — только за доверенным прокси, иначе клиент подделает IP и обойдёт rate-limit.
- Логи: без `Authorization`, токенов, тел запросов; провал auth логируй без значения секрета.

## Тесты

`testApplication {}` (`ktor-server-test-host`), встроенный `client`, без реального порта; `withTestApplication`/`handleRequest` удалены в Ktor 3. Обязательные кейсы: без токена → 401; чужой ресурс → 403/404; протухший/битый токен → 401.

## Что НЕ делать

- Не коммитить отладочные послабления: `anyHost()`, отключённый TLS, `LogLevel.ALL` с телами.
- Не отдавать стектрейсы наружу — ставь `StatusPages` с нейтральным телом ошибки.
- Не изобретать свою крипту/сессии — плагины Ktor + проверенные библиотеки.
