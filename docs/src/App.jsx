import { useState, useEffect } from 'react'

// ---------------------------------------------------------------------------
// Translations
// ---------------------------------------------------------------------------
const T = {
  en: {
    nav: {
      docs: 'Docs',
      manifest: 'Manifest',
      howItWorks: 'How it works',
    },
    heroBadge: 'Early access — v0.0.1',
    heroTagline: 'Deterministic template bundles for AI agents.',
    heroSubtext:
      'Pack, ship, and apply CLAUDE.md configurations with a single CLI command. Reproducible across every machine, every time.',
    ctaDownload: 'Download v0.0.1',
    ctaSeeHow: 'See how it works →',
    overviewLabel: 'Overview',
    overviewHeading: 'What is AI-Kit?',
    overviewBody:
      'AI-Kit is a lightweight Kotlin CLI tool that unpacks and applies template bundles for AI agents like Claude. It solves a real problem: keeping agent configuration files (like CLAUDE.md) consistent, versioned, and reproducible across projects and machines.',
    features: [
      {
        title: 'Parsing',
        desc: 'Reads and validates bundle manifests. Strict schema enforcement means no silent failures.',
      },
      {
        title: 'Generation',
        desc: 'Renders templates from the bundle into your project directory exactly as defined.',
      },
      {
        title: 'Validation',
        desc: 'Verifies the bundle structure and manifest integrity before applying anything.',
      },
      {
        title: 'CLI',
        desc: 'Single binary, zero runtime dependencies. Works on Windows, Linux, and macOS.',
      },
    ],
    manifestLabel: 'Manifest',
    manifestHeading: 'Bundle manifest format',
    manifestBody:
      'Every bundle ships with a bundle.json manifest that declares the bundle identity, schema version, and the exact files to be placed.',
    manifestFields: [
      { field: 'bundle', desc: '— unique bundle identifier' },
      { field: 'version', desc: '— schema version, drives compatibility checks' },
      { field: 'claude', desc: '— map of destination → source template paths' },
    ],
    workflowLabel: 'Workflow',
    workflowHeading: 'How it works',
    workflowSubtext: 'Three steps from zero to a fully configured AI agent workspace.',
    steps: [
      {
        title: 'Download',
        desc: 'Grab the single binary for your platform from GitHub Releases. No runtime, no JVM required on the target machine.',
      },
      {
        title: 'Run',
        desc: 'Point ai-kit at your bundle zip. It validates the manifest, then unpacks templates into your project directory.',
      },
      {
        title: 'Done',
        desc: 'Your CLAUDE.md and other agent config files are in place. Start your AI agent — it picks up the config automatically.',
      },
    ],
    archLabel: 'Architecture',
    archHeading: 'Clean Gradle modules',
    archSubtext:
      'Built as a Kotlin multi-module Gradle project, each concern is isolated into its own module.',
    modules: [
      { name: ':parsing', desc: 'Reads and deserializes bundle.json manifests' },
      { name: ':generation', desc: 'Renders template files into target directories' },
      { name: ':validation', desc: 'Schema and integrity checks before apply' },
      { name: ':cli', desc: 'Entry-point binary, wires all modules together' },
    ],
    ctaHeading: 'Ready to get started?',
    ctaBody: 'Download AI-Kit v0.0.1 and start shipping deterministic agent configurations today.',
    ctaBtn: 'Download v0.0.1 from GitHub',
    footerIssues: 'Issues',
  },
  ru: {
    nav: {
      docs: 'Документация',
      manifest: 'Манифест',
      howItWorks: 'Как работает',
    },
    heroBadge: 'Ранний доступ — v0.0.1',
    heroTagline: 'Детерминированные шаблонные бандлы для ИИ-агентов.',
    heroSubtext:
      'Упакуй, доставь и примени конфигурацию CLAUDE.md одной CLI-командой. Воспроизводимо на любом устройстве.',
    ctaDownload: 'Скачать v0.0.1',
    ctaSeeHow: 'Как работает →',
    overviewLabel: 'Обзор',
    overviewHeading: 'Что такое AI-Kit?',
    overviewBody:
      'AI-Kit — лёгкий Kotlin CLI-инструмент, который распаковывает и применяет шаблонные бандлы для ИИ-агентов вроде Claude. Он решает реальную задачу: поддерживает конфигурационные файлы агентов (например, CLAUDE.md) согласованными, версионированными и воспроизводимыми на разных проектах и машинах.',
    features: [
      {
        title: 'Парсинг',
        desc: 'Читает и валидирует манифесты бандлов. Строгая схема — никаких скрытых сбоев.',
      },
      {
        title: 'Генерация',
        desc: 'Рендерит шаблоны из бандла в директорию проекта точно так, как задано.',
      },
      {
        title: 'Валидация',
        desc: 'Проверяет структуру бандла и целостность манифеста перед применением.',
      },
      {
        title: 'CLI',
        desc: 'Единый бинарник, никаких зависимостей. Работает на Windows, Linux и macOS.',
      },
    ],
    manifestLabel: 'Манифест',
    manifestHeading: 'Формат манифеста бандла',
    manifestBody:
      'Каждый бандл поставляется с манифестом bundle.json, который объявляет идентификатор бандла, версию схемы и точный список размещаемых файлов.',
    manifestFields: [
      { field: 'bundle', desc: '— уникальный идентификатор бандла' },
      { field: 'version', desc: '— версия схемы, управляет проверками совместимости' },
      { field: 'claude', desc: '— карта путей destination → source для шаблонов' },
    ],
    workflowLabel: 'Процесс',
    workflowHeading: 'Как это работает',
    workflowSubtext: 'Три шага от нуля до полностью настроенного рабочего пространства ИИ-агента.',
    steps: [
      {
        title: 'Скачать',
        desc: 'Загрузи единый бинарник для своей платформы из GitHub Releases. Рантайм и JVM на целевой машине не нужны.',
      },
      {
        title: 'Запустить',
        desc: 'Укажи ai-kit путь к zip-архиву бандла. Инструмент проверит манифест и распакует шаблоны в директорию проекта.',
      },
      {
        title: 'Готово',
        desc: 'CLAUDE.md и другие конфигурационные файлы агента на месте. Запускай ИИ-агента — он подхватит конфиг автоматически.',
      },
    ],
    archLabel: 'Архитектура',
    archHeading: 'Чистые Gradle-модули',
    archSubtext:
      'Проект построен как Kotlin multi-module Gradle проект, каждая ответственность изолирована в отдельный модуль.',
    modules: [
      { name: ':parsing', desc: 'Читает и десериализует манифесты bundle.json' },
      { name: ':generation', desc: 'Рендерит шаблонные файлы в целевые директории' },
      { name: ':validation', desc: 'Проверки схемы и целостности перед применением' },
      { name: ':cli', desc: 'Точка входа бинарника, связывает все модули' },
    ],
    ctaHeading: 'Готов начать?',
    ctaBody: 'Скачай AI-Kit v0.0.1 и начни поставлять детерминированные конфигурации агентов уже сегодня.',
    ctaBtn: 'Скачать v0.0.1 с GitHub',
    footerIssues: 'Задачи',
  },
}

// ---------------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------------
const MANIFEST_JSON = `{
  "bundle": "internal",
  "version": "v1-simple",
  "claude": {
    "CLAUDE.md": "CLAUDE.md"
  }
}`

const CLI_COMMANDS = [
  { cmd: '# 1. Download the binary for your platform', comment: true },
  { cmd: 'curl -L https://github.com/your-org/ai-kit-v2/releases/download/v0.0.1/ai-kit-linux -o ai-kit', comment: false },
  { cmd: '', comment: false },
  { cmd: '# 2. Make it executable and run', comment: true },
  { cmd: 'chmod +x ai-kit && ./ai-kit apply --bundle ./my-bundle.zip', comment: false },
]

// ---------------------------------------------------------------------------
// SVG Logo
// ---------------------------------------------------------------------------
function LogoIcon({ className = 'w-7 h-7' }) {
  return (
    <svg className={className} viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <linearGradient id="logoGrad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#22d3ee" />
          <stop offset="100%" stopColor="#7c3aed" />
        </linearGradient>
      </defs>
      <polygon
        points="16,2 28,9 28,23 16,30 4,23 4,9"
        fill="url(#logoGrad)"
        opacity="0.2"
      />
      <polygon
        points="16,2 28,9 28,23 16,30 4,23 4,9"
        fill="none"
        stroke="url(#logoGrad)"
        strokeWidth="1.5"
      />
      <line x1="16" y1="7" x2="10" y2="22" stroke="url(#logoGrad)" strokeWidth="2" strokeLinecap="round" />
      <line x1="16" y1="7" x2="22" y2="22" stroke="url(#logoGrad)" strokeWidth="2" strokeLinecap="round" />
      <line x1="12" y1="17" x2="20" y2="17" stroke="url(#logoGrad)" strokeWidth="2" strokeLinecap="round" />
    </svg>
  )
}

// ---------------------------------------------------------------------------
// Code / Terminal blocks
// ---------------------------------------------------------------------------
function CodeBlock({ code, language = 'json' }) {
  return (
    <div className="relative group">
      <div className="absolute inset-0 bg-gradient-to-r from-cyan-500/10 to-violet-500/10 rounded-xl blur-sm opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
      <pre className="relative bg-[#0d1530] dark:bg-[#0d1530] light:bg-gray-100 border border-white/10 dark:border-white/10 light:border-gray-200 rounded-xl p-5 overflow-x-auto text-sm leading-relaxed font-mono text-gray-300 dark:text-gray-300">
        <span className="absolute top-3 right-4 text-xs text-gray-600 uppercase tracking-widest select-none">
          {language}
        </span>
        <code>{code}</code>
      </pre>
    </div>
  )
}

function TerminalBlock({ lines }) {
  return (
    <div className="relative group">
      <div className="absolute inset-0 bg-gradient-to-r from-cyan-500/10 to-violet-500/10 rounded-xl blur-sm opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
      <div className="relative bg-[#0d1530] dark:bg-[#0d1530] border border-white/10 rounded-xl overflow-hidden">
        <div className="flex items-center gap-2 px-4 py-3 border-b border-white/10 bg-white/[0.03]">
          <span className="w-3 h-3 rounded-full bg-red-500/70" />
          <span className="w-3 h-3 rounded-full bg-yellow-500/70" />
          <span className="w-3 h-3 rounded-full bg-green-500/70" />
          <span className="ml-2 text-xs text-gray-500 font-mono">terminal</span>
        </div>
        <pre className="p-5 overflow-x-auto text-sm leading-relaxed font-mono">
          {lines.map((line, i) =>
            line.comment ? (
              <div key={i} className="text-gray-500">{line.cmd}</div>
            ) : line.cmd === '' ? (
              <div key={i} className="h-3" />
            ) : (
              <div key={i} className="text-gray-300">
                <span className="text-cyan-400 select-none">$ </span>
                {line.cmd}
              </div>
            )
          )}
        </pre>
      </div>
    </div>
  )
}

// ---------------------------------------------------------------------------
// Cards
// ---------------------------------------------------------------------------
function StepCard({ number, title, description, icon }) {
  return (
    <div className="card-glass p-6 flex flex-col gap-4 hover:border-cyan-500/30 transition-colors duration-300">
      <div className="flex items-center gap-4">
        <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-cyan-500/20 to-violet-500/20 border border-cyan-500/20 flex items-center justify-center text-xl">
          {icon}
        </div>
        <div className="w-7 h-7 rounded-full bg-cyan-500/10 border border-cyan-500/30 flex items-center justify-center">
          <span className="text-cyan-400 text-xs font-bold">{number}</span>
        </div>
      </div>
      <div>
        <h3 className="font-semibold text-lg mb-1 text-gray-900 dark:text-white">{title}</h3>
        <p className="text-sm leading-relaxed text-gray-500 dark:text-gray-400">{description}</p>
      </div>
    </div>
  )
}

function FeatureCard({ title, description, icon }) {
  return (
    <div className="card-glass p-6 hover:border-violet-500/30 transition-colors duration-300">
      <div className="text-2xl mb-3">{icon}</div>
      <h3 className="font-semibold mb-2 text-gray-900 dark:text-white">{title}</h3>
      <p className="text-sm leading-relaxed text-gray-500 dark:text-gray-400">{description}</p>
    </div>
  )
}

// ---------------------------------------------------------------------------
// Icons
// ---------------------------------------------------------------------------
function SunIcon() {
  return (
    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
      <circle cx="12" cy="12" r="5" />
      <path strokeLinecap="round" d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
    </svg>
  )
}

function MoonIcon() {
  return (
    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z" />
    </svg>
  )
}

function GitHubIcon() {
  return (
    <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
      <path d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z" />
    </svg>
  )
}

// ---------------------------------------------------------------------------
// App
// ---------------------------------------------------------------------------
export default function App() {
  // Theme
  const [isDark, setIsDark] = useState(() => {
    if (typeof window !== 'undefined') {
      const stored = localStorage.getItem('theme')
      if (stored) return stored === 'dark'
    }
    return true
  })

  // Language
  const [lang, setLang] = useState(() => {
    if (typeof window !== 'undefined') {
      return localStorage.getItem('lang') || 'en'
    }
    return 'en'
  })

  const t = T[lang]

  useEffect(() => {
    document.documentElement.classList.toggle('dark', isDark)
    localStorage.setItem('theme', isDark ? 'dark' : 'light')
  }, [isDark])

  useEffect(() => {
    localStorage.setItem('lang', lang)
  }, [lang])

  const featureIcons = ['📦', '⚙️', '✅', '🖥️']
  const stepIcons = ['⬇️', '▶️', '🚀']

  return (
    <div className="min-h-screen bg-[#f8fafc] dark:bg-[#0a0f1e] text-gray-900 dark:text-gray-100 transition-colors duration-300">
      {/* Background grid — dark only */}
      <div
        className="fixed inset-0 opacity-[0.03] pointer-events-none hidden dark:block"
        style={{
          backgroundImage:
            'linear-gradient(rgba(255,255,255,0.1) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.1) 1px, transparent 1px)',
          backgroundSize: '60px 60px',
        }}
      />

      {/* Nav */}
      <nav className="fixed top-0 left-0 right-0 z-50 border-b border-black/5 dark:border-white/5 bg-white/80 dark:bg-[#0a0f1e]/80 backdrop-blur-md transition-colors duration-300">
        <div className="max-w-6xl mx-auto px-6 h-14 flex items-center justify-between">
          {/* Logo */}
          <div className="flex items-center gap-2">
            <LogoIcon className="w-7 h-7" />
            <span className="font-semibold text-gray-900 dark:text-white">AI-Kit</span>
            <span className="text-xs px-2 py-0.5 rounded-full bg-cyan-500/10 border border-cyan-500/20 text-cyan-600 dark:text-cyan-400 font-mono">
              v0.0.1
            </span>
          </div>

          {/* Nav links + controls */}
          <div className="flex items-center gap-4 text-sm text-gray-500 dark:text-gray-400">
            <a href="#what-is" className="hover:text-gray-900 dark:hover:text-white transition-colors hidden sm:block">{t.nav.docs}</a>
            <a href="#manifest" className="hover:text-gray-900 dark:hover:text-white transition-colors hidden sm:block">{t.nav.manifest}</a>
            <a href="#how-it-works" className="hover:text-gray-900 dark:hover:text-white transition-colors hidden sm:block">{t.nav.howItWorks}</a>
            <a
              href="https://github.com/your-org/ai-kit-v2"
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center gap-1.5 hover:text-gray-900 dark:hover:text-white transition-colors"
            >
              <GitHubIcon />
              <span className="hidden sm:inline">GitHub</span>
            </a>

            {/* Language toggle */}
            <div className="flex items-center rounded-lg border border-gray-200 dark:border-white/10 overflow-hidden text-xs font-semibold">
              <button
                onClick={() => setLang('en')}
                className={`px-2.5 py-1 transition-colors ${
                  lang === 'en'
                    ? 'bg-cyan-500 text-white'
                    : 'text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white'
                }`}
              >
                EN
              </button>
              <button
                onClick={() => setLang('ru')}
                className={`px-2.5 py-1 transition-colors ${
                  lang === 'ru'
                    ? 'bg-cyan-500 text-white'
                    : 'text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white'
                }`}
              >
                RU
              </button>
            </div>

            {/* Theme toggle */}
            <button
              onClick={() => setIsDark((d) => {
                const next = !d
                document.documentElement.classList.toggle('dark', next)
                localStorage.setItem('theme', next ? 'dark' : 'light')
                return next
              })}
              className="w-8 h-8 flex items-center justify-center rounded-lg border border-gray-200 dark:border-white/10 text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white hover:border-gray-300 dark:hover:border-white/20 transition-colors"
              aria-label="Toggle theme"
            >
              {isDark ? <SunIcon /> : <MoonIcon />}
            </button>
          </div>
        </div>
      </nav>

      {/* Hero */}
      <section className="relative pt-32 pb-24 px-6 flex flex-col items-center text-center overflow-hidden">
        <div className="absolute top-20 left-1/4 w-96 h-96 bg-cyan-500/5 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute top-20 right-1/4 w-96 h-96 bg-violet-500/5 rounded-full blur-3xl pointer-events-none" />

        <div className="relative max-w-4xl mx-auto">
          <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full border border-cyan-500/20 bg-cyan-500/5 text-cyan-600 dark:text-cyan-400 text-xs font-medium mb-8">
            <span className="w-1.5 h-1.5 rounded-full bg-cyan-400 animate-pulse" />
            {t.heroBadge}
          </div>

          <h1 className="text-5xl md:text-7xl font-extrabold tracking-tight mb-6 leading-none">
            <span className="text-gray-900 dark:text-white">AI-Kit</span>{' '}
            <span className="bg-clip-text text-transparent bg-gradient-to-r from-cyan-400 via-blue-400 to-violet-400">
              v2
            </span>
          </h1>

          <p className="text-xl md:text-2xl text-gray-500 dark:text-gray-400 font-light mb-4 max-w-2xl mx-auto leading-relaxed">
            {t.heroTagline}
          </p>
          <p className="text-base text-gray-400 dark:text-gray-500 mb-10 max-w-xl mx-auto">
            {t.heroSubtext}
          </p>

          <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
            <a
              href="https://github.com/your-org/ai-kit-v2/releases/tag/v0.0.1"
              target="_blank"
              rel="noopener noreferrer"
              className="px-8 py-3.5 rounded-xl font-semibold text-sm bg-gradient-to-r from-cyan-500 to-blue-600 text-white hover:from-cyan-400 hover:to-blue-500 transition-all duration-200 shadow-lg shadow-cyan-500/20 hover:shadow-cyan-500/30"
            >
              {t.ctaDownload}
            </a>
            <a
              href="#how-it-works"
              className="px-8 py-3.5 rounded-xl font-semibold text-sm border border-gray-200 dark:border-white/10 text-gray-600 dark:text-gray-300 hover:border-gray-300 dark:hover:border-white/20 hover:text-gray-900 dark:hover:text-white transition-all duration-200"
            >
              {t.ctaSeeHow}
            </a>
          </div>
        </div>
      </section>

      {/* What is AI-Kit */}
      <section id="what-is" className="py-20 px-6">
        <div className="max-w-6xl mx-auto">
          <div className="mb-12">
            <p className="text-cyan-600 dark:text-cyan-400 text-sm font-semibold uppercase tracking-widest mb-3">
              {t.overviewLabel}
            </p>
            <h2 className="text-3xl md:text-4xl font-bold text-gray-900 dark:text-white mb-4">
              {t.overviewHeading}
            </h2>
            <p className="text-gray-500 dark:text-gray-400 max-w-2xl leading-relaxed">
              {t.overviewBody.split('CLAUDE.md').map((part, i, arr) =>
                i < arr.length - 1 ? (
                  <span key={i}>
                    {part}
                    <code className="text-cyan-600 dark:text-cyan-400 bg-cyan-500/10 px-1.5 py-0.5 rounded text-sm font-mono">
                      CLAUDE.md
                    </code>
                  </span>
                ) : (
                  <span key={i}>{part}</span>
                )
              )}
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            {t.features.map((f, i) => (
              <FeatureCard key={i} icon={featureIcons[i]} title={f.title} description={f.desc} />
            ))}
          </div>
        </div>
      </section>

      {/* Manifest format */}
      <section
        id="manifest"
        className="py-20 px-6 bg-gradient-to-b from-transparent via-black/[0.02] dark:via-white/[0.02] to-transparent"
      >
        <div className="max-w-6xl mx-auto">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
            <div>
              <p className="text-cyan-600 dark:text-cyan-400 text-sm font-semibold uppercase tracking-widest mb-3">
                {t.manifestLabel}
              </p>
              <h2 className="text-3xl md:text-4xl font-bold text-gray-900 dark:text-white mb-4">
                {t.manifestHeading}
              </h2>
              <p className="text-gray-500 dark:text-gray-400 leading-relaxed mb-6">
                {t.manifestBody.split('bundle.json').map((part, i, arr) =>
                  i < arr.length - 1 ? (
                    <span key={i}>
                      {part}
                      <code className="text-cyan-600 dark:text-cyan-400 bg-cyan-500/10 px-1.5 py-0.5 rounded text-sm font-mono">
                        bundle.json
                      </code>
                    </span>
                  ) : (
                    <span key={i}>{part}</span>
                  )
                )}
              </p>
              <ul className="space-y-3 text-sm text-gray-500 dark:text-gray-400">
                {t.manifestFields.map((mf, i) => (
                  <li key={i} className="flex gap-3">
                    <span className="text-cyan-500 mt-0.5 shrink-0">▸</span>
                    <span>
                      <code className="text-gray-700 dark:text-gray-200 font-mono">{mf.field}</code>{' '}
                      {mf.desc}
                    </span>
                  </li>
                ))}
              </ul>
            </div>
            <div>
              <CodeBlock code={MANIFEST_JSON} language="json" />
            </div>
          </div>
        </div>
      </section>

      {/* How it works */}
      <section id="how-it-works" className="py-20 px-6">
        <div className="max-w-6xl mx-auto">
          <div className="mb-12">
            <p className="text-cyan-600 dark:text-cyan-400 text-sm font-semibold uppercase tracking-widest mb-3">
              {t.workflowLabel}
            </p>
            <h2 className="text-3xl md:text-4xl font-bold text-gray-900 dark:text-white mb-4">
              {t.workflowHeading}
            </h2>
            <p className="text-gray-500 dark:text-gray-400 max-w-xl leading-relaxed">
              {t.workflowSubtext}
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
            {t.steps.map((step, i) => (
              <StepCard
                key={i}
                number={String(i + 1)}
                icon={stepIcons[i]}
                title={step.title}
                description={step.desc}
              />
            ))}
          </div>

          <TerminalBlock lines={CLI_COMMANDS} />
        </div>
      </section>

      {/* Modules / Architecture */}
      <section className="py-20 px-6 bg-gradient-to-b from-transparent via-black/[0.02] dark:via-white/[0.02] to-transparent">
        <div className="max-w-6xl mx-auto">
          <div className="mb-12">
            <p className="text-cyan-600 dark:text-cyan-400 text-sm font-semibold uppercase tracking-widest mb-3">
              {t.archLabel}
            </p>
            <h2 className="text-3xl md:text-4xl font-bold text-gray-900 dark:text-white mb-4">
              {t.archHeading}
            </h2>
            <p className="text-gray-500 dark:text-gray-400 max-w-xl leading-relaxed">
              {t.archSubtext}
            </p>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            {t.modules.map((mod) => (
              <div
                key={mod.name}
                className="card-glass p-5 hover:border-violet-500/30 transition-colors duration-300"
              >
                <code className="text-violet-500 dark:text-violet-400 font-mono text-sm font-medium">
                  {mod.name}
                </code>
                <p className="text-gray-500 dark:text-gray-400 text-sm mt-2 leading-relaxed">{mod.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="py-24 px-6">
        <div className="max-w-2xl mx-auto text-center">
          <div className="card-glass p-12 relative overflow-hidden">
            <div className="absolute inset-0 bg-gradient-to-br from-cyan-500/5 to-violet-500/5 pointer-events-none" />
            <div className="relative">
              <h2 className="text-3xl font-bold text-gray-900 dark:text-white mb-4">{t.ctaHeading}</h2>
              <p className="text-gray-500 dark:text-gray-400 mb-8 leading-relaxed">{t.ctaBody}</p>
              <a
                href="https://github.com/your-org/ai-kit-v2/releases/tag/v0.0.1"
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-2 px-8 py-3.5 rounded-xl font-semibold text-sm bg-gradient-to-r from-cyan-500 to-blue-600 text-white hover:from-cyan-400 hover:to-blue-500 transition-all duration-200 shadow-lg shadow-cyan-500/20"
              >
                <GitHubIcon />
                {t.ctaBtn}
              </a>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-black/5 dark:border-white/5 py-8 px-6 transition-colors duration-300">
        <div className="max-w-6xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-4 text-sm text-gray-400 dark:text-gray-600">
          <div className="flex items-center gap-2">
            <LogoIcon className="w-5 h-5" />
            <span className="text-gray-500 dark:text-gray-600">AI-Kit v2</span>
            <span className="text-gray-300 dark:text-gray-700">·</span>
            <span className="text-gray-500 dark:text-gray-600">v0.0.1</span>
          </div>
          <div className="flex items-center gap-6">
            <a
              href="https://github.com/your-org/ai-kit-v2"
              target="_blank"
              rel="noopener noreferrer"
              className="hover:text-gray-700 dark:hover:text-gray-400 transition-colors"
            >
              GitHub
            </a>
            <a
              href="https://github.com/your-org/ai-kit-v2/releases"
              target="_blank"
              rel="noopener noreferrer"
              className="hover:text-gray-700 dark:hover:text-gray-400 transition-colors"
            >
              Releases
            </a>
            <a
              href="https://github.com/your-org/ai-kit-v2/issues"
              target="_blank"
              rel="noopener noreferrer"
              className="hover:text-gray-700 dark:hover:text-gray-400 transition-colors"
            >
              {t.footerIssues}
            </a>
          </div>
        </div>
      </footer>
    </div>
  )
}
