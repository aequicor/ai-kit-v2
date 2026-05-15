import { useApp } from '../App'
import { PageHero, FeatureCard } from '../ui'

export default function Architecture() {
  const { t } = useApp()
  const a = t.architecture
  return (
    <>
      <PageHero eyebrow={a.hero.eyebrow} title={a.hero.title} lead={a.hero.lead} />

      <section className="py-12 px-6">
        <div className="max-w-4xl mx-auto">
          <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">{a.modules.title}</h2>
          <div className="overflow-x-auto rounded-xl border border-gray-200 dark:border-white/10">
            <table className="w-full text-sm">
              <tbody>
                {a.modules.rows.map(([name, desc]) => (
                  <tr key={name} className="border-b border-gray-100 dark:border-white/5 last:border-0">
                    <td className="px-5 py-3.5 w-32 align-top">
                      <code className="text-violet-600 dark:text-violet-400 font-mono text-xs bg-violet-500/5 px-1.5 py-0.5 rounded">:modules:{name}</code>
                    </td>
                    <td className="px-5 py-3.5 text-gray-500 dark:text-gray-400 leading-relaxed align-top">{desc}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <section className="py-8 px-6">
        <div className="max-w-4xl mx-auto">
          <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">{a.flow.title}</h2>
          <ol className="list-decimal pl-6 space-y-2 text-sm text-gray-600 dark:text-gray-300 leading-relaxed">
            {a.flow.steps.map((step) => (
              <li key={step}>{step}</li>
            ))}
          </ol>
        </div>
      </section>

      <section className="py-8 px-6">
        <div className="max-w-4xl mx-auto">
          <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">{a.safety.title}</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {a.safety.items.map((item) => (
              <FeatureCard key={item.title} title={item.title} body={item.body} />
            ))}
          </div>
        </div>
      </section>

      <section className="py-12 px-6">
        <div className="max-w-4xl mx-auto">
          <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">{a.docs.title}</h2>
          <p className="text-sm text-gray-500 dark:text-gray-400 leading-relaxed">{a.docs.body}</p>
        </div>
      </section>
    </>
  )
}
