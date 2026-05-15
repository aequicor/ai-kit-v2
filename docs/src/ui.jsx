// Shared UI components — imported by pages and App

import { useState } from 'react'

// ---------------------------------------------------------------------------
// Logo
// ---------------------------------------------------------------------------
export function LogoIcon({ className = 'w-7 h-7' }) {
  return (
    <svg className={className} viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect x="2" y="2" width="28" height="28" rx="7" fill="#6366f1" />
      <g stroke="#ffffff" strokeLinecap="round" fill="none">
        <line x1="16" y1="10.4" x2="10.4" y2="21.6" strokeWidth="1.4" />
        <line x1="16" y1="10.4" x2="21.6" y2="21.6" strokeWidth="1.4" />
        <line x1="10.4" y1="21.6" x2="21.6" y2="21.6" strokeWidth="1.4" />
      </g>
      <circle cx="16"   cy="10.4" r="2.6" fill="#ffffff" />
      <circle cx="10.4" cy="21.6" r="2.6" fill="#ffffff" />
      <circle cx="21.6" cy="21.6" r="2.6" fill="#ffffff" />
    </svg>
  )
}

// ---------------------------------------------------------------------------
// Icons
// ---------------------------------------------------------------------------
export function SunIcon() {
  return (
    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
      <circle cx="12" cy="12" r="5" />
      <path strokeLinecap="round" d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
    </svg>
  )
}

export function MoonIcon() {
  return (
    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z" />
    </svg>
  )
}

export function GitHubIcon({ className = 'w-4 h-4' }) {
  return (
    <svg className={className} fill="currentColor" viewBox="0 0 24 24">
      <path d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z" />
    </svg>
  )
}

export function ChevronRightIcon() {
  return (
    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
      <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
    </svg>
  )
}

// ---------------------------------------------------------------------------
// Code block (JSON / inline snippet)
// ---------------------------------------------------------------------------
export function CodeBlock({ code, language = 'json' }) {
  return (
    <div className="relative group">
      <div className="absolute inset-0 bg-gradient-to-r from-cyan-500/10 to-violet-500/10 rounded-xl blur-sm opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
      <pre className="relative bg-[#0d1530] border border-white/10 rounded-xl p-5 overflow-x-auto text-sm leading-relaxed font-mono text-gray-300">
        <span className="absolute top-3 right-4 text-xs text-gray-600 uppercase tracking-widest select-none">
          {language}
        </span>
        <code>{code}</code>
      </pre>
    </div>
  )
}

// ---------------------------------------------------------------------------
// Copyable prompt block
// ---------------------------------------------------------------------------
export function CopyBlock({ preview, fullPrompt, label, copyLabel, copiedLabel }) {
  const [copied, setCopied] = useState(false)

  function handleCopy() {
    navigator.clipboard.writeText(fullPrompt).then(() => {
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    })
  }

  return (
    <div className="rounded-2xl border border-white/10 bg-[#0d1530] overflow-hidden">
      {label && (
        <div className="px-5 py-3 border-b border-white/10 text-xs text-gray-500 font-mono">{label}</div>
      )}
      <pre className="p-5 text-sm leading-relaxed font-mono text-gray-400 overflow-x-auto whitespace-pre-wrap">
        {preview}
      </pre>
      <div className="px-5 pb-4">
        <button
          onClick={handleCopy}
          className="px-4 py-2 rounded-lg bg-cyan-500/10 border border-cyan-500/20 text-cyan-400 text-sm font-medium hover:bg-cyan-500/20 transition-colors"
        >
          {copied ? copiedLabel : copyLabel}
        </button>
      </div>
    </div>
  )
}

// ---------------------------------------------------------------------------
// Feature card
// ---------------------------------------------------------------------------
export function FeatureCard({ icon, title, body }) {
  return (
    <div className="card-glass p-6 hover:border-violet-500/30 transition-colors duration-300">
      <div className="text-2xl mb-3">{icon}</div>
      <h3 className="font-semibold mb-2 text-gray-900 dark:text-white">{title}</h3>
      <p className="text-sm leading-relaxed text-gray-500 dark:text-gray-400">{body}</p>
    </div>
  )
}

// ---------------------------------------------------------------------------
// Numbered step card
// ---------------------------------------------------------------------------
export function StepCard({ num, title, body }) {
  return (
    <div className="card-glass p-6 flex flex-col gap-4 hover:border-cyan-500/30 transition-colors duration-300">
      <div className="w-8 h-8 rounded-full bg-cyan-500/10 border border-cyan-500/30 flex items-center justify-center shrink-0">
        <span className="text-cyan-400 text-sm font-bold">{num}</span>
      </div>
      <div>
        <h3 className="font-semibold text-lg mb-1 text-gray-900 dark:text-white">{title}</h3>
        <p className="text-sm leading-relaxed text-gray-500 dark:text-gray-400">{body}</p>
      </div>
    </div>
  )
}

// ---------------------------------------------------------------------------
// Files table
// ---------------------------------------------------------------------------
export function FilesTable({ rows }) {
  return (
    <div className="overflow-x-auto rounded-xl border border-gray-200 dark:border-white/10">
      <table className="w-full text-sm">
        <tbody>
          {rows.map((row) => (
            <tr key={row.path} className="border-b border-gray-100 dark:border-white/5 last:border-0">
              <td className="px-5 py-3.5 w-[38%] align-top">
                <code className="text-violet-600 dark:text-violet-400 font-mono text-xs bg-violet-500/5 px-1.5 py-0.5 rounded">
                  {row.path}
                </code>
              </td>
              <td className="px-5 py-3.5 text-gray-500 dark:text-gray-400 leading-relaxed align-top">
                {row.purpose}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

// ---------------------------------------------------------------------------
// Section header (eyebrow + h2 + optional lead)
// ---------------------------------------------------------------------------
export function SectionHeader({ eyebrow, title, lead, center = false }) {
  return (
    <div className={`mb-10 ${center ? 'text-center' : ''}`}>
      {eyebrow && (
        <p className="text-cyan-600 dark:text-cyan-400 text-xs font-semibold uppercase tracking-widest mb-3">
          {eyebrow}
        </p>
      )}
      <h2 className="text-3xl md:text-4xl font-bold text-gray-900 dark:text-white mb-4">{title}</h2>
      {lead && (
        <p className={`text-gray-500 dark:text-gray-400 leading-relaxed ${center ? 'max-w-2xl mx-auto' : 'max-w-2xl'}`}>
          {lead}
        </p>
      )}
    </div>
  )
}

// ---------------------------------------------------------------------------
// Page hero banner
// ---------------------------------------------------------------------------
export function PageHero({ eyebrow, title, lead }) {
  return (
    <section className="relative pt-32 pb-16 px-6 overflow-hidden">
      <div className="absolute top-20 left-1/4 w-96 h-96 bg-cyan-500/5 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute top-20 right-1/4 w-96 h-96 bg-violet-500/5 rounded-full blur-3xl pointer-events-none" />
      <div className="relative max-w-3xl mx-auto">
        {eyebrow && (
          <span className="inline-block text-cyan-600 dark:text-cyan-400 text-xs font-semibold uppercase tracking-widest mb-4">
            {eyebrow}
          </span>
        )}
        <h1 className="text-4xl md:text-5xl font-extrabold tracking-tight text-gray-900 dark:text-white mb-5 leading-tight">
          {title}
        </h1>
        <p className="text-lg text-gray-500 dark:text-gray-400 leading-relaxed max-w-2xl">{lead}</p>
      </div>
    </section>
  )
}

// ---------------------------------------------------------------------------
// Diff card (differences section)
// ---------------------------------------------------------------------------
export function DiffCard({ title, body }) {
  return (
    <div className="card-glass p-6 hover:border-cyan-500/30 transition-colors duration-300">
      <h3 className="font-semibold mb-2 text-gray-900 dark:text-white">{title}</h3>
      <p className="text-sm leading-relaxed text-gray-500 dark:text-gray-400">{body}</p>
    </div>
  )
}
