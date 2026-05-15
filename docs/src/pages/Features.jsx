import { useApp } from '../App'
import { PageHero, FeatureCard } from '../ui'

export default function Features() {
  const { t } = useApp()
  const f = t.features
  return (
    <>
      <PageHero eyebrow={f.hero.eyebrow} title={f.hero.title} lead={f.hero.lead} />
      <section className="py-12 px-6">
        <div className="max-w-5xl mx-auto space-y-14">
          {f.sections.map((section) => (
            <div key={section.group}>
              <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-6">{section.group}</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {section.items.map((item) => (
                  <FeatureCard key={item.title} title={item.title} body={item.body} />
                ))}
              </div>
            </div>
          ))}
        </div>
      </section>
    </>
  )
}
