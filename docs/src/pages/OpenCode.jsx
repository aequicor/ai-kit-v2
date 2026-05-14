import { useApp } from '../App'
import { PageHero, SectionHeader, FilesTable, DiffCard } from '../ui'

export default function OpenCode() {
  const { t } = useApp()
  const p = t.opencode

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

      {/* Differences */}
      <section className="py-16 px-6 bg-gradient-to-b from-transparent via-black/[0.02] dark:via-white/[0.02] to-transparent">
        <div className="max-w-3xl mx-auto">
          <SectionHeader eyebrow={p.diff.eyebrow} title={p.diff.title} />
          <div className="space-y-4">
            {p.diff.items.map((item) => (
              <DiffCard key={item.title} title={item.title} body={item.body} />
            ))}
          </div>
        </div>
      </section>
    </>
  )
}
