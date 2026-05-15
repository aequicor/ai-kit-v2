import { useApp } from '../App'
import { PageHero, SectionHeader, FeatureCard } from '../ui'

export default function Bundles() {
  const { t } = useApp()
  const b = t.bundles
  return (
    <>
      <PageHero eyebrow={b.hero.eyebrow} title={b.hero.title} lead={b.hero.lead} />

      <section className="py-12 px-6">
        <div className="max-w-4xl mx-auto">
          <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-3">{b.manifest.title}</h2>
          <p className="text-sm text-gray-500 dark:text-gray-400 mb-5 leading-relaxed">{b.manifest.lead}</p>
          <div className="overflow-x-auto rounded-xl border border-gray-200 dark:border-white/10">
            <table className="w-full text-sm">
              <tbody>
                {b.manifest.fields.map(([name, type, desc]) => (
                  <tr key={name} className="border-b border-gray-100 dark:border-white/5 last:border-0">
                    <td className="px-5 py-3 align-top whitespace-nowrap"><code className="text-violet-600 dark:text-violet-400 font-mono text-xs">{name}</code></td>
                    <td className="px-5 py-3 align-top whitespace-nowrap"><code className="text-cyan-500 dark:text-cyan-400 font-mono text-xs">{type}</code></td>
                    <td className="px-5 py-3 text-gray-500 dark:text-gray-400 leading-relaxed align-top">{desc}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <section className="py-8 px-6">
        <div className="max-w-4xl mx-auto">
          <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">{b.inputs.title}</h2>
          <div className="overflow-x-auto rounded-xl border border-gray-200 dark:border-white/10">
            <table className="w-full text-sm">
              <tbody>
                {b.inputs.rows.map(([type, desc]) => (
                  <tr key={type} className="border-b border-gray-100 dark:border-white/5 last:border-0">
                    <td className="px-5 py-3 w-32 align-top"><code className="text-violet-600 dark:text-violet-400 font-mono text-xs">{type}</code></td>
                    <td className="px-5 py-3 text-gray-500 dark:text-gray-400 leading-relaxed align-top">{desc}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <section className="py-8 px-6">
        <div className="max-w-4xl mx-auto">
          <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">{b.layout.title}</h2>
          <pre className="bg-[#0d1530] border border-white/10 rounded-xl p-5 overflow-x-auto text-xs leading-relaxed font-mono text-gray-300">
            {b.layout.body}
          </pre>
        </div>
      </section>

      <section className="py-8 px-6">
        <div className="max-w-4xl mx-auto">
          <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">{b.sources.title}</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {b.sources.items.map((item) => (
              <FeatureCard key={item.title} title={item.title} body={item.body} />
            ))}
          </div>
        </div>
      </section>

      <section className="py-8 px-6">
        <div className="max-w-4xl mx-auto">
          <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">{b.shipped.title}</h2>
          <div className="space-y-4">
            {b.shipped.items.map((item) => (
              <div key={item.name} className="card-glass p-6">
                <div className="flex items-baseline gap-3 mb-2 flex-wrap">
                  <code className="text-cyan-500 dark:text-cyan-400 font-mono text-base font-semibold">{item.name}</code>
                  <span className="text-xs text-gray-400 dark:text-gray-500 font-mono">targets: {item.targets}</span>
                </div>
                <p className="text-sm text-gray-500 dark:text-gray-400 leading-relaxed mb-2">{item.desc}</p>
                <p className="text-xs text-gray-500 dark:text-gray-500 font-mono">inputs: {item.inputs}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="py-12 px-6">
        <div className="max-w-4xl mx-auto">
          <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">{b.custom.title}</h2>
          <ol className="list-decimal pl-6 space-y-2 text-sm text-gray-600 dark:text-gray-300 leading-relaxed">
            {b.custom.steps.map((step) => (
              <li key={step}>{step}</li>
            ))}
          </ol>
        </div>
      </section>
    </>
  )
}
