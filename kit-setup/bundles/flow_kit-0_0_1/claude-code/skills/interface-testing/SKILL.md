---
name: interface-testing
description: How to exercise the interface of ${bundle.input.projectName} and assert real outcomes — driving the claude-in-mobile MCP for Compose / Android / iOS UI, or HTTP / testApplication for a Ktor server. Use during the interface-testing stage of the pipeline, or when asked to "test the UI", "click through the app", or "check the endpoints". Detailed playbooks live in references/.
---

# interface-testing

Как прогнать интерфейс **${bundle.input.projectName}** и проверить **реальный** результат, а не «зелёную галочку».

## Главное правило

Утверждай, что фича работает, только если **наблюдал** поведение: текст на экране, смену состояния/URL, HTTP-статус и тело. Хрупкие ассерты (фиксированные координаты, `sleep`) — запрещены: привязывайся к семантике/тексту.

## Какой плейбук использовать

<!-- when: ${bundle.input.projectType} in ['compose-app', 'kmp-fullstack'] -->
- **UI (Compose / Android / iOS / desktop)** — через MCP `mobile`. Подробный плейбук: [`references/compose.md`](references/compose.md) — запуск приложения, дамп дерева, навигация, ассерты, блок-лист деструктивных действий, базовая доступность.
<!-- end -->
<!-- when: ${bundle.input.projectType} in ['ktor-server', 'kmp-fullstack'] -->
- **HTTP API (Ktor)** — `testApplication` или живой сервер. Подробный плейбук: [`references/ktor-api.md`](references/ktor-api.md) — позитивные и негативные кейсы, коды ошибок, защищённые маршруты.
<!-- end -->
<!-- when: ${bundle.input.projectType} == 'kmp-library' -->
- **Публичный API библиотеки** — у библиотеки нет UI/HTTP. «Интерфейс» = публичный API: прогоняй `./gradlew allTests`, проверяй контракт на граничных значениях.
<!-- end -->

## Гейт пайплайна

Стадия проходит, только если наблюдаемый результат совпал с ожидаемым. Падение → возврат к стадии разработки с конкретным шагом воспроизведения.
