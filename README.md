# AI-Kit v2

Это новая итерация кита с целью создать детерменированного помошника работающего по понятным правилам.

Старая версия кита была нацелена на универсальной начинке — что билось с суровой реальностью: невозможно охватить всех ИИ-агентов, особенно если ты в них не работаешь.

Новая версия будет чётко разделять core часть — которая отвечает за парсинг. Отдельно за генерацию файлов, отдельно валидация, отдельно CLI — это всё будут отдельные gradle-модули, написанные по принципам SOLID и Clean Architecture, соблюдая все правила Kotlin разработки. Это будет гарантироваться внимательным просмотром кода человеком.

Также новый кит не должен быть завязан исключительно на локальных шаблонах. AI-Kit, грубо говоря, разархивирует шаблоны. Это могут быть как внутренний bundle, так и внешний.

## Манифест бандла (`bundle.json`)

Каждый бандл описывается файлом `bundle.json` в корне бандла:

```json
{
  "schemaVersion": 1,
  "name": "my-bundle",
  "version": "1.0.0",
  "description": "Описание бандла",
  "author": "Имя Автора <email@example.com>",
  "license": "MIT",
  "targets": ["claude-code"],
  "inputs": []
}
```

Каждый поддерживаемый агент имеет собственный каталог `<bundle>/<agent>/` с файлом `config.json`, описывающим настройки, MCP-серверы, скилы, хуки и права инструментов для этого агента. Подробнее — в [`kit-setup/BUNDLE_JSON.md`](kit-setup/BUNDLE_JSON.md) и [`kit-setup/CONFIG_JSON.md`](kit-setup/CONFIG_JSON.md).

## CLI: ручной запуск

Готовые бинарники под Windows / Linux / macOS публикуются в [GitHub Releases](https://github.com/aequicor/ai-kit-v2/releases). Скачайте подходящий, сделайте исполняемым (`chmod +x kit-setup`) и положите в `PATH`. Локальная сборка: `cd kit-setup && ./gradlew :modules:cli:linkReleaseExecutableMacosArm64` (под нужный таргет) — бинарь окажется в `kit-setup/modules/cli/build/bin/<target>/releaseExecutable/cli.kexe`.

Все команды читают/пишут относительно текущей рабочей директории.

### `kit-setup schema manifest`

Печатает JSON-схему файла `.aikit/manifest.json` — общая структура (`aikitVersion`, `applications[]`, `targets`).

```bash
kit-setup schema manifest > .aikit/manifest.schema.json
```

Удобно подцепить как `$schema` в IDE — будут работать автодополнение и валидация полей проектного манифеста.

### `kit-setup schema bundle <REF>`

Печатает JSON-схему для блока `inputs` конкретного бандла. Используйте, когда подключаете сторонний (или свой) бандл и нужно понять, какие параметры он принимает, какие значения допустимы и какие обязательны.

`<REF>` — та же форма ссылки, что и в проектном манифесте:

- путь к каталогу бандла: `./bundles/simple_kit-0_0_1`
- путь к zip-архиву: `./my-bundle.zip` или `zip:./my-bundle.zip`
- встроенный бандл: `embedded:<name>`

Опции:

- `--base-dir <DIR>` — база для относительных путей в `REF` (по умолчанию `.`).
- `--list` — вместо схемы вывести список встроенных бандлов.

Пример:

```bash
kit-setup schema bundle ./bundles/simple_kit-0_0_1 > .aikit/simple-kit.inputs.schema.json
```

Полученная схема (draft 2020-12) пригодна для валидации блока `targets.<name>.inputs` в `.aikit/manifest.json` любым стандартным валидатором или IDE.

### `kit-setup verify <MANIFEST>`

Валидирует проектный манифест и все бандлы, на которые он ссылается. Никаких файлов не пишет, выходит с ненулевым кодом при ошибках.

```bash
kit-setup verify .aikit/manifest.json
```

### `kit-setup generate <MANIFEST>`

Полный пайплайн: читает манифест, разрешает бандлы, рендерит шаблоны и пишет итоговые конфиги агентов в проект.

```bash
kit-setup generate .aikit/manifest.json
```

### `kit-setup --version` / `kit-setup --help`

Версия CLI и встроенная справка. Справка по конкретной подкоманде — `kit-setup <cmd> --help` (например, `kit-setup schema bundle --help`).
