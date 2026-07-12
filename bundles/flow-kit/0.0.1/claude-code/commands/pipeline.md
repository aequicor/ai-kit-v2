---
description: Autonomous dev cycle — analyze → develop → security → interface test → commit
argument-hint: <task description>
---

# /pipeline

Автономный цикл разработки для **${bundle.input.projectName}**.

Задача: **$ARGUMENTS**

Принцип каждой стадии: **контекст → действие → проверка**. Сквозной гейт — «не можешь проверить, не коммить». Знание разрешай по скилу **docs-on-demand** (код → исходники зависимости → офиц. доки → память), не угадывай.

## Стадия 0 — Контекст

Прочитай `CLAUDE.md`, `git status`, `git diff`. Определи затронутую область и тип проекта.

## Стадия 1 — Аналитика
<!-- when: 'analyst' in ${bundle.input.subagents} -->
Делегируй субагенту **analyst**: пусть прочитает код и историю, при нехватке — исходники зависимостей и доки, и вернёт план (Goal / Plan / Risks / Verification).
<!-- end -->
<!-- when: !('analyst' in ${bundle.input.subagents}) -->
Сам: прочитай связанные файлы целиком, сверься с `CLAUDE.md`, сформируй план (Goal / Plan / Risks / Verification). Определи **минимальное** изменение под задачу.
<!-- end -->
<!-- when: ${bundle.input.autonomyLevel} == 'guided' -->
**СТОП (guided):** покажи план пользователю и дождись подтверждения, прежде чем писать код.
<!-- end -->
<!-- when: ${bundle.input.autonomyLevel} == 'auto' -->
**auto:** если требование однозначно — продолжай. Неоднозначно или есть Open questions — спроси и остановись.
<!-- end -->

## Стадия 2 — Разработка

Реализуй минимальное изменение по плану. Не рефактори вокруг, не вводи абстракции на будущее.
<!-- when: ('kotlin-specialist' in ${bundle.input.subagents}) || ('ktor-specialist' in ${bundle.input.subagents}) -->
Делегируй нетривиальные части:
<!-- when: 'kotlin-specialist' in ${bundle.input.subagents} -->
- KMP / корутины / Compose / sealed-модели → **kotlin-specialist**.
<!-- end -->
<!-- when: 'ktor-specialist' in ${bundle.input.subagents} -->
- Ktor: роутинг / плагины / auth / сериализация / `testApplication` → **ktor-specialist**.
<!-- end -->
<!-- end -->
<!-- when: 'ktlint' in ${bundle.input.qualityTools} -->
ktlint форматит код сам (PostToolUse-хук) — вручную не вызывай.
<!-- end -->

## Стадия 3 — Безопасность
<!-- when: 'security-reviewer' in ${bundle.input.subagents} -->
Делегируй субагенту **security-reviewer** (свежий контекст, diff-aware).
<!-- end -->
<!-- when: !('security-reviewer' in ${bundle.input.subagents}) && ('security-review' in ${bundle.input.skills}) -->
Активируй скил **security-review** и пройди diff по таблице угроз.
<!-- end -->
<!-- when: !('security-reviewer' in ${bundle.input.subagents}) && !('security-review' in ${bundle.input.skills}) -->
Пройди `git diff` на injection / auth / secrets / serialization / Ktor TLS-CORS / гонки / утечки в логи.
<!-- end -->
**Гейт:** Critical/High → возврат к Стадии 2 (до 2 итераций), затем эскалация пользователю.

## Стадия 4 — Тестирование интерфейсов
<!-- when: 'interface-tester' in ${bundle.input.subagents} -->
Делегируй субагенту **interface-tester**.
<!-- end -->
<!-- when: !('interface-tester' in ${bundle.input.subagents}) && ('interface-testing' in ${bundle.input.skills}) -->
Активируй скил **interface-testing** и прогони интерфейс по плейбуку под тип проекта.
<!-- end -->
<!-- when: !('interface-tester' in ${bundle.input.subagents}) && !('interface-testing' in ${bundle.input.skills}) -->
<!-- when: ${bundle.input.projectType} in ['compose-app', 'kmp-fullstack'] -->Прогони UI (MCP `mobile`, если подключён): подними приложение, пройди сценарий, проверь наблюдаемый результат.<!-- end --><!-- when: ${bundle.input.projectType} == 'ktor-server' -->Прогони эндпоинты через `testApplication` (позитивные и негативные кейсы).<!-- end --><!-- when: ${bundle.input.projectType} == 'kmp-library' -->Прогони `./gradlew allTests`, проверь публичный API на граничных значениях.<!-- end -->
<!-- end -->
Параллельно прогони тесты (`./gradlew allTests` / соответствующий task). **Гейт:** красные тесты или провал сценария → возврат к Стадии 2.

## Стадия 5 — Коммит

Только если security чистый **и** тесты зелёные.
<!-- when: ${bundle.input.autonomyLevel} == 'guided' -->
**СТОП (guided):** покажи `git diff --staged` и черновик сообщения, дождись подтверждения.
<!-- end -->
- Сообщение — по стилю репозитория (`git log`). Описывай **почему**, не пересказывай код.
- Коммить только относящиеся к задаче файлы (по именам, не `git add -A`).
- **Push — никогда без явной просьбы пользователя.**
<!-- when: ${bundle.input.strict} -->
- Хук `guard-commit` — последний рубеж: блокирует коммит при секретах / `println`-`TODO`-`FIXME` в коде / `!!`. Сработал — чини причину, не обходи.
<!-- end -->

## Самокоррекция

Циклы ограничены: максимум 2 возврата на одну стадию. После — остановись и доложи пользователю, что застряло и почему. Не «чини тест ради зелёного» — чини причину.
