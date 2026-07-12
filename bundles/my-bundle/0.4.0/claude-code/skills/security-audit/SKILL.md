---
name: security-audit
description: Diff-aware security review methodology for ${bundle.input.projectName} — threat checklist (injection, auth/IDOR, crypto, secrets, serialization, network/TLS/CORS, concurrency, logging) with a severity rubric; report only findings with a concrete exploitation path. Use when asked for a "security review", "audit", "check for vulnerabilities", before merging code touching auth, crypto, IO, serialization or secrets, or when a leak of credentials is suspected.
---

# security-audit

Diff-aware ревью безопасности изменений в **${bundle.input.projectName}**. Анализируй **изменения**, а не весь репозиторий. Расширенная таблица угроз с примерами — [references/threats.md](references/threats.md).

## Шаги

1. `git diff main...HEAD` — что изменилось (плюс `git log main..HEAD` для намерения).
2. Для каждого изменения пройди таблицу угроз (ниже и в references).
3. Сомнительное поведение библиотеки проверяй по её исходникам или официальной документации/CVE через `WebFetch` — не по памяти.
4. Отсеки ложные срабатывания: репортуй только то, для чего есть **конкретный путь эксплуатации** (кто, каким запросом/вводом, что получает).
5. Отчёт по severity; Critical/High — блокеры мержа.

## Таблица угроз (ядро)

| Категория | На что смотреть |
|---|---|
| Injection | Конкатенация ввода в SQL/command/path; `ProcessBuilder`/`Runtime.exec` со склейкой |
| AuthN/AuthZ | Маршрут без `authenticate`; IDOR — объект по id без проверки владельца; hardcoded creds; слабый хэш паролей |
| Crypto | Своя крипта; статичные IV/соли; `Random` вместо `SecureRandom`; ECB; `==` для секретов (timing) |
| Secrets | Ключи/токены в коде; `.env`/`local.properties`/keystore/`google-services.json` в git; секреты в `BuildConfig`/логах |
| Serialization | kotlinx polymorphic на недоверенном вводе без закрытого модуля; Jackson default typing; `ObjectInputStream` |
| Network/Ktor | Отключённый TLS у клиента; `anyHost()` CORS; cleartext; SSRF по пользовательскому URL без allowlist; отсутствие rate-limit на auth |
| Конкуренция | TOCTOU; гонки корутин на общем мутабельном состоянии/файле/соединении |
| Logging | PII/токены/`Authorization`/тела запросов в логах прода |
<!-- when: 'android' in ${bundle.input.technologies} -->
| Android | `exported=true` без permission; `WebView`+JS+внешний контент; `allowBackup=true` для приватных данных; секреты в ресурсах/`BuildConfig` |
<!-- end -->

## Severity

- **Critical** — RCE, утечка секретов прода, обход аутентификации.
- **High** — IDOR/чужие данные, инъекция с реальным вводом, отключённый TLS.
- **Medium** — слабая крипта/хэш, недостаточная валидация на доверенном пути.
- **Low/Info** — глубина защиты, потенциальная утечка в debug-логах.

## Формат отчёта

По каждой находке: файл:строка → категория → severity → путь эксплуатации → рекомендация. В конце — вердикт: `clean | needs-fixes | block`.

## Что НЕ делать

- Не выдумывать уязвимости ради объёма; нет пути эксплуатации — нет находки (максимум Info).
- Не анализировать неизменённый код, кроме как для контекста изменений.
- Не «чинить» самому в рамках аудита — отчёт отдаётся разработке.
