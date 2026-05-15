import { useApp } from '../App'
import { PageHero, SectionHeader } from '../ui'

export default function Cli() {
  const { t } = useApp()
  const c = t.cli
  return (
    <>
      <PageHero eyebrow={c.hero.eyebrow} title={c.hero.title} lead={c.hero.lead} />

      <section className="py-12 px-6">
        <div className="max-w-4xl mx-auto">
          <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-3">{c.resolution.title}</h2>
          <p className="text-sm text-gray-500 dark:text-gray-400 mb-4 leading-relaxed">{c.resolution.lead}</p>
          <ol className="list-decimal pl-6 space-y-1.5 text-sm text-gray-600 dark:text-gray-300">
            {c.resolution.steps.map((s) => (
              <li key={s}><code className="font-mono text-violet-600 dark:text-violet-400">{s}</code></li>
            ))}
          </ol>
        </div>
      </section>

      <section className="py-8 px-6">
        <div className="max-w-4xl mx-auto space-y-5">
          {c.commands.map((cmd) => (
            <div key={cmd.name} className="card-glass p-6">
              <code className="block text-cyan-500 dark:text-cyan-400 font-mono text-base font-semibold mb-2">{cmd.name}</code>
              <p className="text-sm text-gray-500 dark:text-gray-400 leading-relaxed mb-4">{cmd.summary}</p>
              {cmd.flags.length > 0 && (
                <div className="overflow-x-auto rounded-lg border border-gray-200 dark:border-white/10">
                  <table className="w-full text-xs">
                    <tbody>
                      {cmd.flags.map(([flag, desc]) => (
                        <tr key={flag} className="border-b border-gray-100 dark:border-white/5 last:border-0">
                          <td className="px-4 py-2.5 w-[40%] align-top">
                            <code className="text-violet-600 dark:text-violet-400 font-mono">{flag}</code>
                          </td>
                          <td className="px-4 py-2.5 text-gray-500 dark:text-gray-400 leading-relaxed align-top">{desc}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          ))}
        </div>
      </section>

      <section className="py-12 px-6">
        <div className="max-w-4xl mx-auto">
          <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">{c.exitTitle}</h2>
          <div className="overflow-x-auto rounded-xl border border-gray-200 dark:border-white/10">
            <table className="w-full text-sm">
              <tbody>
                {c.exit.map((row) => (
                  <tr key={row.code} className="border-b border-gray-100 dark:border-white/5 last:border-0">
                    <td className="px-5 py-3 w-16 align-top">
                      <code className="text-cyan-500 dark:text-cyan-400 font-mono font-bold">{row.code}</code>
                    </td>
                    <td className="px-5 py-3 text-gray-500 dark:text-gray-400 leading-relaxed align-top">{row.body}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </section>
    </>
  )
}
