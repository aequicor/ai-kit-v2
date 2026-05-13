---
name: security-review
description: Security-focused review of pending changes in ${bundle.input.projectName}. Use when the user asks for "security review", "audit", "check for vulnerabilities", or before merging code that touches auth, crypto, input parsing, or external I/O.
---

# /security-review

Security-ревью текущих изменений в **${bundle.input.projectName}**.

## Что искать

| Категория | На что смотреть |
|---|---|
| Injection | SQL/command/LDAP/XPath конкатенация с пользовательским вводом |
| AuthN/AuthZ | Пропущенные проверки доступа, hardcoded credentials, weak hashing |
| Crypto | Свои реализации крипто, MD5/SHA1 для паролей, статичные IV/соли |
| Secrets | Ключи в коде/коммитах/логах, `.env` в git |
| Deserialization | `pickle`/`unserialize` пользовательского ввода |
| SSRF/XXE | Запросы по пользовательским URL без allowlist, парсинг XML с external entities |
| Race conditions | TOCTOU, неатомарные операции с ФС/БД |
| Logging | Утечка PII/токенов в логи |

## Шаги

1. `git diff main...HEAD` — что изменилось.
2. Для каждого изменения — пройдись по таблице выше.
3. Сформируй отчёт по уровням: **Critical / High / Medium / Low / Info**.

## Что НЕ делать

- Не ищи уязвимости, которых нет, ради списка.
- Не разворачивай теоретические сценарии без конкретного пути эксплуатации.
- Не анализируй код, который не менялся, если это не контекст для понимания изменений.
