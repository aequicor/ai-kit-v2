---
name: security-review
description: Security-focused review of pending changes in Kotlin project ${bundle.input.projectName}. Use when the user asks for "security review", "audit", "check for vulnerabilities", or before merging code that touches auth, crypto, input parsing, or external I/O.
---

# /security-review

Security-ревью текущих изменений в **${bundle.input.projectName}**.

## Что искать

| Категория | На что смотреть |
|---|---|
| Injection | SQL/command/LDAP/XPath конкатенация с пользовательским вводом; `Runtime.exec` / `ProcessBuilder` со склеенной строкой |
| AuthN/AuthZ | Пропущенные проверки доступа, hardcoded credentials, weak hashing (MD5/SHA1 для паролей) |
| Crypto | Свои реализации крипто, статичные IV/соли, `Random` вместо `SecureRandom` |
| Secrets | Ключи в коде/коммитах/логах, `local.properties`/`.env`/`google-services.json` в git, BuildConfig с секретами |
| Deserialization | `ObjectInputStream`/Jackson `enableDefaultTyping`/kotlinx.serialization с непроверенным polymorphic input |
| SSRF/XXE | Запросы по пользовательским URL без allowlist; `DocumentBuilderFactory` без `disallow-doctype-decl` |
| Race conditions | TOCTOU, неатомарные операции с ФС/БД, гонки между корутинами на общем `MutableState` |
| Logging | Утечка PII/токенов в логи; Timber/SLF4J с пользовательским вводом без маскирования |
| Android | `exported=true` без permission; `WebView` с `setJavaScriptEnabled(true)` + загрузкой внешнего контента; `allowBackup=true` для приложений с приватными данными |
| Network | OkHttp/Ktor с отключённой проверкой сертификата; `cleartextTrafficPermitted` |

## Шаги

1. `git diff main...HEAD` — что изменилось.
2. Для каждого изменения — пройдись по таблице выше.
3. Сформируй отчёт по уровням: **Critical / High / Medium / Low / Info**.

## Что НЕ делать

- Не ищи уязвимости, которых нет, ради списка.
- Не разворачивай теоретические сценарии без конкретного пути эксплуатации.
- Не анализируй код, который не менялся, если это не контекст для понимания изменений.
