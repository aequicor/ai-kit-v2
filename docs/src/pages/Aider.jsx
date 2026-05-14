import { useApp } from '../App'
import { PageHero, SectionHeader, FilesTable, DiffCard } from '../ui'

export default function Aider() {
  const { t } = useApp()
  const p = t.aider

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

      {/* Limitations */}
      <section className="py-16 px-6 bg-gradient-to-b from-transparent via-black/[0.02] dark:via-white/[0.02] to-transparent">
        <div className="max-w-3xl mx-auto">
          <SectionHeader eyebrow={p.limits.eyebrow} title={p.limits.title} />
          <div className="space-y-4">
            {p.limits.items.map((item) => (
              <DiffCard key={item.title} title={item.title} body={item.body} />
            ))}
          </div>
        </div>
      </section>
    </>
  )
}
