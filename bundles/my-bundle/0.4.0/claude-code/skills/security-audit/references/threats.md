# Таблица угроз: примеры и признаки в коде

## Injection

```kotlin
// ПЛОХО: конкатенация в SQL
db.rawQuery("SELECT * FROM users WHERE name = '$name'")
// ХОРОШО: параметризация
db.query("SELECT * FROM users WHERE name = ?", arrayOf(name))
```

- Командная строка: `Runtime.exec("convert $userFile …")` — ввод в аргументы только списком, не строкой.
- Path traversal: `File(baseDir, userPath)` без нормализации — проверяй `canonicalPath.startsWith(baseDir.canonicalPath)`.

## AuthN / AuthZ

- Новый маршрут в Ktor вне `authenticate {}` — осознанно ли публичен?
- **IDOR** — самый частый High: `get("/orders/{id}")` возвращает заказ по id без проверки `order.userId == principal.userId`.
- Пароли — только медленный хэш (bcrypt/argon2); SHA-256 без соли/итераций — Medium+.
- Сравнение секретов `==`/`equals` — timing attack; `MessageDigest.isEqual`.

## Crypto

- `java.util.Random` для токенов/ключей → `SecureRandom`.
- Статичный IV у AES-CBC / режим ECB / ключ в константе — High.
- «Своя» крипта (XOR, самодельный шифр) — всегда находка.

## Secrets

Признаки в diff:

- Строки вида `api_key = "…"`, `password = "…"`, PEM-заголовки `-----BEGIN … PRIVATE KEY-----`, AWS `AKIA…`.
- Новые файлы: `.env`, `local.properties`, `*.jks`, `*.keystore`, `google-services.json`, `application.conf` с кредами.
- `BuildConfig.API_SECRET`, логирование конфига целиком.

Секрет уже закоммичен → считается скомпрометированным: ротация, не просто удаление из HEAD.

## Serialization

- kotlinx.serialization polymorphic на **недоверенном** вводе: открытая регистрация подтипов = создание неожиданных объектов; только закрытый `SerializersModule`.
- Jackson `enableDefaultTyping`/`@JsonTypeInfo` c пользовательским вводом — классический RCE-вектор.
- `ObjectInputStream.readObject` на внешних данных — запрет.

## Network

- Ktor client: `trustManager` accept-all, `HostnameVerifier { _, _ -> true }` — High даже «для стейджа».
- CORS `anyHost()` (+`allowCredentials`) — High на проде.
- SSRF: `client.get(userProvidedUrl)` — allowlist хостов/схем, запрет резолва во внутренние сети.
- Cleartext HTTP к API — Medium+; на Android проверь `usesCleartextTraffic`.
- Login/token без rate-limit — Medium.

## Конкуренция

- TOCTOU: `if (file.exists()) file.read()` на разделяемом ресурсе.
- Незащищённый общий мутабельный state между корутинами (`var`/`MutableList` без синхронизации/`Mutex`) — гонка; данные и повреждение инвариантов.
- Двойное списание/идемпотентность: повтор запроса без idempotency key на денежных операциях.

## Logging

- `log.info("token=$token")`, логирование `Authorization`, `Set-Cookie`, тел запросов в проде.
- Стектрейсы наружу в HTTP-ответе (см. StatusPages в скиле **ktor-security**).
- PII (email, телефон) в логах без необходимости.

## Как проверять библиотеку, а не память

1. Открой исходник зависимости (Gradle-кэш / GitHub по версии из `libs.versions.toml`).
2. CVE: поиск «<библиотека> <версия> CVE» через WebFetch/WebSearch.
3. В отчёте ссылайся на конкретное поведение версии, не «кажется, эта библиотека…».
