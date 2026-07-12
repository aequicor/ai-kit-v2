# Ktor 3.x: аутентификация в деталях

## JWT-провайдер (HS256)

```kotlin
data class UserPrincipal(val userId: String, val roles: Set<String>)   // НЕ : Principal (deprecated в 3.x)

fun Application.configureAuth() {
    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "app"
            verifier(
                JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer(issuer)
                    .withAudience(audience)
                    .build()
            )
            validate { credential ->
                val userId = credential.payload.getClaim("sub").asString()
                if (userId.isNullOrBlank()) null            // null = 401
                else UserPrincipal(userId, credential.payload.rolesOrEmpty())
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized)   // без деталей причины
            }
        }
    }
}
```

- RS256/JWKS: `verifier(jwkProvider, issuer)` c `JwkProviderBuilder(url).cached(…).rateLimited(…)`.
- `validate` обязан проверять смысловые claims; «`return@validate JWTPrincipal(payload)` безусловно» — дыра: любой корректно подписанный токен проходит.

## Защита маршрутов

```kotlin
routing {
    authenticate("auth-jwt") {
        get("/orders") {
            val principal = call.principal<UserPrincipal>()!!
            val orders = orderService.forUser(principal.userId)   // авторизация: только СВОИ данные
            call.respond(orders)
        }
    }
}
```

- AuthN ≠ AuthZ: после аутентификации проверяй **владение ресурсом** (IDOR — топ-уязвимость: объект по id без проверки владельца).
- Вложенные `authenticate` комбинируются; `authenticate("a", "b")` — достаточно любого; `strategy = AuthenticationStrategy.Required` — все.

## API-key

```kotlin
bearer("auth-api-key") {                       // или кастомный header-провайдер
    authenticate { credential ->
        val hash = sha256(credential.token)
        apiKeyStore.findByHash(hash)           // ищем по ХЭШУ
            ?.takeIf { it.isActive && it.expiresAt > clock.now() }
            ?.let { ApiClientPrincipal(it.clientId, it.scopes) }
    }
}
```

Правила ключей:

- Хранить только хэш; показывать ключ один раз при создании.
- Срок жизни + ротация (два активных ключа на клиента для бесшовной смены).
- Сравнение/поиск — константное время (`MessageDigest.isEqual` при прямом сравнении).
- Community-провайдер `dev.forst:ktor-api-key` (`apiKey {}`) — проверь совместимость артефакта со своей версией Ktor, прежде чем использовать.

## Сессии (если нужны)

- `install(Sessions)` + `cookie<UserSession>("session") { cookie.httpOnly = true; cookie.secure = true; cookie.extensions["SameSite"] = "Strict" }`.
- Подписывай/шифруй: `SessionTransportTransformerMessageAuthentication(key)` / `…Encrypt(encKey, signKey)`; ключи — из env.

## Конфиг и env

| Формат | Синтаксис env | Default |
|---|---|---|
| HOCON | `${JWT_SECRET}` (скобки обязательны) | `${?JWT_SECRET}` — опциональный |
| YAML | `$JWT_SECRET` или `${JWT_SECRET}` | `${JWT_SECRET:default}` |

Смешение синтаксисов между форматами молча не работает — проверяй фактическое значение на старте (fail-fast, если секрет пуст).
