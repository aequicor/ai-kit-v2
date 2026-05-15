import { useState } from 'react'
import { useApp } from '../App'
import { PageHero, SectionHeader } from '../ui'

function CodeCard({ code, language }) {
  const [copied, setCopied] = useState(false)
  const handleCopy = () => {
    navigator.clipboard.writeText(code).then(() => {
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    })
  }
  return (
    <div className="relative">
      <pre className="bg-[#0d1530] border border-white/10 rounded-xl p-5 overflow-x-auto text-sm leading-relaxed font-mono text-gray-300">
        {language && (
          <span className="absolute top-3 right-14 text-xs text-gray-600 uppercase tracking-widest select-none">{language}</span>
        )}
        <code>{code}</code>
      </pre>
      <button
        onClick={handleCopy}
        className="absolute top-3 right-3 px-2 py-1 rounded-md bg-cyan-500/10 border border-cyan-500/20 text-cyan-400 text-xs font-medium hover:bg-cyan-500/20 transition-colors"
      >
        {copied ? '✓' : 'copy'}
      </button>
    </div>
  )
}

function CommandList({ items }) {
  return (
    <div className="space-y-3">
      {items.map((it) => (
        <div key={it.cmd} className="card-glass p-4">
          <code className="block text-cyan-500 dark:text-cyan-400 font-mono text-sm font-semibold mb-1">{it.cmd}</code>
          <p className="text-sm text-gray-500 dark:text-gray-400 leading-relaxed">{it.body}</p>
        </div>
      ))}
    </div>
  )
}

export default function Start() {
  const { t } = useApp()
  const s = t.start

  return (
    <>
      <PageHero eyebrow={s.hero.eyebrow} title={s.hero.title} lead={s.hero.lead} />

      {/* Easiest path: ask your agent */}
      <section className="py-12 px-6">
        <div className="max-w-3xl mx-auto">
          <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">{s.prompt.title}</h2>
          <p className="text-gray-500 dark:text-gray-400 mb-5 text-sm">{s.prompt.subtitle}</p>
          <CodeCard code={s.prompt.body} language="prompt" />
        </div>
      </section>

      {/* Install */}
      <section className="py-12 px-6 bg-gradient-to-b from-transparent via-black/[0.02] dark:via-white/[0.02] to-transparent">
        <div className="max-w-3xl mx-auto">
          <SectionHeader eyebrow={s.install.eyebrow} title={s.install.title} lead={s.install.lead} />
          <p className="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2">{s.install.manifestLabel}</p>
          <CodeCard code={s.install.manifest} language="json" />
          <p className="text-sm font-semibold text-gray-700 dark:text-gray-300 mt-6 mb-2">{s.install.runLabel}</p>
          <CodeCard code={s.install.run} language="bash" />
          <ul className="mt-6 space-y-2 text-sm text-gray-500 dark:text-gray-400 list-disc pl-5">
            {s.install.notes.map((n) => (
              <li key={n} className="leading-relaxed">{n}</li>
            ))}
          </ul>
        </div>
      </section>

      {/* Update */}
      <section className="py-12 px-6">
        <div className="max-w-3xl mx-auto">
          <SectionHeader eyebrow={s.update.eyebrow} title={s.update.title} lead={s.update.lead} />
          <CommandList items={s.update.sub} />
        </div>
      </section>

      {/* Remove */}
      <section className="py-12 px-6 bg-gradient-to-b from-transparent via-black/[0.02] dark:via-white/[0.02] to-transparent">
        <div className="max-w-3xl mx-auto">
          <SectionHeader eyebrow={s.remove.eyebrow} title={s.remove.title} lead={s.remove.lead} />
          <CommandList items={s.remove.sub} />
        </div>
      </section>

      {/* Binary */}
      <section className="py-12 px-6">
        <div className="max-w-3xl mx-auto">
          <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-3">{s.install_binary.title}</h2>
          <p className="text-sm text-gray-500 dark:text-gray-400 mb-6 leading-relaxed">{s.install_binary.body}</p>
          <a
            href="https://github.com/aequicor/ai-kit-v2/releases/latest"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 px-6 py-3 rounded-xl font-semibold text-sm bg-gradient-to-r from-cyan-500 to-blue-600 text-white hover:from-cyan-400 hover:to-blue-500 transition-all duration-200 shadow-lg shadow-cyan-500/20"
          >
            {s.install_binary.link}
          </a>
        </div>
      </section>
    </>
  )
}
