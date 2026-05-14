# Режимы работы: несколько манифестов

AI-Kit поддерживает несколько конфигураций агента в одном проекте — например, интерактивный, автономный и «спящий» режимы. Каждая конфигурация живёт в отдельном файле манифеста; переключение выполняется одной строкой.

## Рекомендуемая структура

```
<project root>/
└── .aikit/
    ├── manifest.interactive.json   # интерактивный режим
    ├── manifest.autonomous.json    # автономный режим
    ├── manifest.sleepy.json        # минималистичный «спящий» режим
    ├── manifest.lock.json          # управляется CLI — не редактировать
    └── local.properties            # per-developer конфиг — НЕ коммитить
```

Имена режимов произвольны: `qa`, `demo`, `ci` — любые строки без пробелов. `interactive`, `autonomous`, `sleepy` — только рекомендация.

## `.aikit/local.properties`

Per-developer конфигурационный файл — **не коммитить в git** (аналог `local.properties` в Gradle). Указывает активный манифест для конкретного разработчика:

```properties
# AI-Kit local config — per-developer, do not commit
manifest=.aikit/manifest.autonomous.json
```

Путь в значении `manifest` интерпретируется относительно корня проекта (директории, содержащей `.aikit/`). Абсолютные пути тоже поддерживаются.

Добавьте в `.gitignore`:

```
.aikit/local.properties
```

CLI печатает подсказку, если `local.properties` существует, но не защищён `.gitignore`.

## Приоритеты резолва манифеста

CLI определяет активный манифест, просматривая источники в порядке убывания приоритета:

| Приоритет | Источник | Пример |
|-----------|----------|--------|
| 1 | Позиционный аргумент или `--manifest <path>` | `kit-setup generate .aikit/manifest.autonomous.json` |
| 2 | `--mode <id>` (shortcut → `.aikit/manifest.<id>.json`) | `kit-setup generate --mode autonomous` |
| 3 | Переменная окружения `AIKIT_MANIFEST` | `AIKIT_MANIFEST=.aikit/manifest.autonomous.json kit-setup generate` |
| 4 | Переменная окружения `AIKIT_MODE` | `AIKIT_MODE=autonomous kit-setup generate` |
| 5 | Поле `manifest=` в `.aikit/local.properties` | *(автоматически)* |
| 6 | Файл `.aikit/manifest.json` (legacy-fallback) | *(автоматически, обратная совместимость)* |
| — | Ни один источник не задан | Ошибка `NoManifestConfigured` |

Если источник задан, но файл не существует — ошибка `ManifestNotFound`. Тихого перехода к следующему источнику нет: опечатка видна явно.

Конфликт явного пути и `--mode` → ошибка `ConflictingArgs`.

## Audit-trail

При каждом вызове команды CLI печатает строку, показывающую, какой манифест активен и откуда он взят:

```
Manifest: .aikit/manifest.autonomous.json (from .aikit/local.properties)
```

Для legacy-fallback (`.aikit/manifest.json` без явного источника) строка не печатается.

## Переключение с очисткой: план → wipe → apply

Когда активный манифест отличается от того, под которым был сделан последний `generate`, CLI автоматически выполняет **plan-first → wipe → apply**:

1. **Plan-фаза.** Полная загрузка, валидация и рендер нового манифеста в памяти. Файлы на диске не трогаются.
2. **Wipe.** Если plan успешен — удалить файлы предыдущей установки (то же, что `kit-setup remove --keep-manifest`). Drift-protection действует по умолчанию: изменённые файлы сохраняются, если не передан `--force`.
3. **Apply.** Записать результаты plan-фазы, обновить lock с новым `manifestRef`.

**Гарантия безопасности:** если новый манифест содержит ошибку (неверные inputs, отсутствующий бандл), plan-фаза завершается с ошибкой — wipe **не выполняется**, текущая установка остаётся нетронутой. Битый манифест не уничтожит рабочее состояние `.claude/`.

Принудительная очистка без смены манифеста: `kit-setup generate --clean`.

## Пример сценария

```bash
# 1. Установить несколько манифестов
mkdir .aikit
cat > .aikit/manifest.interactive.json << 'EOF'
{ "aikitVersion": "0.1.0", "applications": [ ... ] }
EOF
cat > .aikit/manifest.autonomous.json << 'EOF'
{ "aikitVersion": "0.1.0", "applications": [ ... ] }
EOF

# 2. Выбрать интерактивный режим
echo "manifest=.aikit/manifest.interactive.json" > .aikit/local.properties

# 3. Первая генерация
kit-setup generate
# → Manifest: .aikit/manifest.interactive.json (from .aikit/local.properties)
# → Created (8): ...

# 4. Переключиться в автономный режим
echo "manifest=.aikit/manifest.autonomous.json" > .aikit/local.properties
kit-setup generate
# → Manifest: .aikit/manifest.autonomous.json (from .aikit/local.properties)
# → Switched manifest (was .aikit/manifest.interactive.json) — previous installation wiped.
# → Created (5): ...

# 5. Переопределить через env (разово)
AIKIT_MANIFEST=.aikit/manifest.interactive.json kit-setup generate
```

## CI-интеграция

В CI используйте переменную окружения — `local.properties` нет в репозитории:

```yaml
# GitHub Actions
env:
  AIKIT_MANIFEST: .aikit/manifest.autonomous.json

steps:
  - run: kit-setup verify --all   # валидировать все манифесты
  - run: kit-setup generate       # применить целевой манифест
```

`verify --all` проверяет все файлы вида `manifest.json` и `manifest.*.json` в `.aikit/`, что позволяет CI убедиться в корректности всех режимов одной командой.

## Troubleshooting

**`Manifest not found: '.aikit/manifest.autonomous.json'`**
— Файл не существует. Проверьте имя файла и рабочий каталог.

**`No manifest configured`**
— Ни один источник не указал манифест. Создайте `.aikit/manifest.json` или заполните `local.properties`.

**`cannot combine an explicit manifest path ... with --mode`**
— `MANIFEST`/`--manifest` и `--mode` взаимоисключают друг друга.

**Wipe не выполнился при смене манифеста**
— Wipe происходит только если в старом lock есть поле `manifestRef` (добавлено в v0.1.0). Для locks, созданных до v0.1.0, run `kit-setup generate --clean` один раз.

**Дрейфующие файлы при wipe**
— CLI сохраняет изменённые вами файлы. Используйте `--force`, чтобы удалить их принудительно.
