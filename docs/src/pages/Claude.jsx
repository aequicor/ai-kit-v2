import { useApp } from '../App'
import { PageHero, SectionHeader, FilesTable, DiffCard } from '../ui'

export default function Claude() {
  const { t } = useApp()
  const p = t.claude

  return (
    <>
      <PageHero eyebrow={p.hero.eyebrow} title={p.hero.title} lead={p.hero.lead} />

      {/* Artifacts */}
      <section className="py-16 px-6">
        <div className="max-w-3xl mx-auto">
          <SectionHeader eyebrow={p.files.eyebrow} title={p.files.title} lead={p.files.lead} />
          <FilesTable rows={p.files.rows} />
        </div>
      </section>

      {/* Features */}
      <section className="py-16 px-6 bg-gradient-to-b from-transparent via-black/[0.02] dark:via-white/[0.02] to-transparent">
        <div className="max-w-3xl mx-auto">
          <SectionHeader eyebrow={p.features.eyebrow} title={p.features.title} />
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {p.features.items.map((item) => (
              <DiffCard key={item.title} title={item.title} body={item.body} />
            ))}
          </div>
        </div>
      </section>

      {/* Dialect */}
      <section className="py-16 px-6">
        <div className="max-w-3xl mx-auto">
          <p className="text-cyan-600 dark:text-cyan-400 text-xs font-semibold uppercase tracking-widest mb-3">
            {p.dialect.eyebrow}
          </p>
          <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-4">{p.dialect.title}</h2>
          <p className="text-gray-500 dark:text-gray-400 leading-relaxed">{p.dialect.lead}</p>
        </div>
      </section>
    </>
  )
}
