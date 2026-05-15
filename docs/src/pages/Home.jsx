import { Link } from 'react-router-dom'
import { useApp } from '../App'
import { GitHubIcon, FeatureCard } from '../ui'
import { useLatestRelease } from '../hooks/useLatestRelease'

const GITHUB = 'https://github.com/aequicor/ai-kit-v2'

export default function Home() {
  const { t } = useApp()
  const h = t.home
  const release = useLatestRelease()

  return (
    <>
      {/* Hero */}
      <section className="relative pt-32 pb-24 px-6 flex flex-col items-center text-center overflow-hidden">
        <div className="absolute top-20 left-1/4 w-96 h-96 bg-cyan-500/5 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute top-20 right-1/4 w-96 h-96 bg-violet-500/5 rounded-full blur-3xl pointer-events-none" />

        <div className="relative max-w-3xl mx-auto">
          <a
            href={release.url}
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full border border-cyan-500/20 bg-cyan-500/5 text-cyan-600 dark:text-cyan-400 text-xs font-medium mb-8 hover:border-cyan-500/40 transition-colors"
          >
            <span className="w-1.5 h-1.5 rounded-full bg-cyan-400 animate-pulse" />
            {release.tag}{h.hero.suffix}
          </a>

          <h1 className="text-5xl md:text-7xl font-extrabold tracking-tight mb-6 leading-none">
            <span className="text-gray-900 dark:text-white">AI</span>{' '}
            <span className="bg-clip-text text-transparent bg-gradient-to-r from-cyan-400 via-blue-400 to-violet-400">
              Kit
            </span>
          </h1>

          <p className="text-lg md:text-xl text-gray-500 dark:text-gray-400 mb-10 max-w-2xl mx-auto leading-relaxed">
            {h.hero.lead}
          </p>

          <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
            <Link
              to="/start"
              className="px-8 py-3.5 rounded-xl font-semibold text-sm bg-gradient-to-r from-cyan-500 to-blue-600 text-white hover:from-cyan-400 hover:to-blue-500 transition-all duration-200 shadow-lg shadow-cyan-500/20 hover:shadow-cyan-500/30"
            >
              {h.hero.ctaStart}
            </Link>
            <a
              href={GITHUB}
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center gap-2 px-8 py-3.5 rounded-xl font-semibold text-sm border border-gray-200 dark:border-white/10 text-gray-600 dark:text-gray-300 hover:border-gray-300 dark:hover:border-white/20 hover:text-gray-900 dark:hover:text-white transition-all duration-200"
            >
              <GitHubIcon />
              {h.hero.ctaGithub}
            </a>
          </div>
        </div>
      </section>

      {/* Problems */}
      <section className="py-20 px-6 bg-gradient-to-b from-transparent via-black/[0.02] dark:via-white/[0.02] to-transparent">
        <div className="max-w-6xl mx-auto">
          <div className="mb-12">
            <p className="text-cyan-600 dark:text-cyan-400 text-xs font-semibold uppercase tracking-widest mb-3">{h.problem.eyebrow}</p>
            <h2 className="text-3xl md:text-4xl font-bold text-gray-900 dark:text-white">{h.problem.title}</h2>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            {h.problem.items.map((item) => (
              <FeatureCard key={item.title} icon={item.icon} title={item.title} body={item.body} />
            ))}
          </div>
        </div>
      </section>

      {/* Solution */}
      <section className="py-20 px-6">
        <div className="max-w-6xl mx-auto">
          <div className="mb-12">
            <p className="text-cyan-600 dark:text-cyan-400 text-xs font-semibold uppercase tracking-widest mb-3">{h.solution.eyebrow}</p>
            <h2 className="text-3xl md:text-4xl font-bold text-gray-900 dark:text-white mb-4">{h.solution.title}</h2>
            <p className="text-gray-500 dark:text-gray-400 max-w-xl leading-relaxed">{h.solution.lead}</p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {h.solution.items.map((item) => (
              <FeatureCard key={item.title} icon={item.icon} title={item.title} body={item.body} />
            ))}
          </div>
        </div>
      </section>

      {/* Why better than hand-rolling */}
      <section className="py-20 px-6 bg-gradient-to-b from-transparent via-black/[0.02] dark:via-white/[0.02] to-transparent">
        <div className="max-w-6xl mx-auto">
          <div className="mb-12">
            <p className="text-cyan-600 dark:text-cyan-400 text-xs font-semibold uppercase tracking-widest mb-3">{h.why.eyebrow}</p>
            <h2 className="text-3xl md:text-4xl font-bold text-gray-900 dark:text-white">{h.why.title}</h2>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {h.why.items.map((item) => (
              <FeatureCard key={item.title} title={item.title} body={item.body} />
            ))}
          </div>
          <div className="mt-8">
            <Link to="/features" className="inline-flex items-center gap-2 text-sm font-semibold text-cyan-500 dark:text-cyan-400 hover:text-cyan-400">
              All features →
            </Link>
          </div>
        </div>
      </section>

      {/* Supported agents */}
      <section className="py-20 px-6">
        <div className="max-w-6xl mx-auto">
          <div className="mb-12">
            <p className="text-cyan-600 dark:text-cyan-400 text-xs font-semibold uppercase tracking-widest mb-3">{h.runners.eyebrow}</p>
            <h2 className="text-3xl md:text-4xl font-bold text-gray-900 dark:text-white mb-4">{h.runners.title}</h2>
            <p className="text-gray-500 dark:text-gray-400 max-w-xl leading-relaxed">{h.runners.lead}</p>
          </div>
          <div className="flex flex-wrap gap-3">
            {h.runners.items.map((item) => (
              <div key={item.id} className="card-glass px-5 py-3.5 flex items-center gap-3">
                <span className="font-medium text-gray-900 dark:text-white text-sm">{item.name}</span>
                <span className="text-xs text-gray-400 dark:text-gray-500 border-l border-gray-200 dark:border-white/10 pl-3">{item.note}</span>
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
              <h2 className="text-3xl font-bold text-gray-900 dark:text-white mb-4">{h.cta.title}</h2>
              <p className="text-gray-500 dark:text-gray-400 mb-8 leading-relaxed">{h.cta.body}</p>
              <Link
                to="/start"
                className="inline-flex items-center gap-2 px-8 py-3.5 rounded-xl font-semibold text-sm bg-gradient-to-r from-cyan-500 to-blue-600 text-white hover:from-cyan-400 hover:to-blue-500 transition-all duration-200 shadow-lg shadow-cyan-500/20"
              >
                {h.cta.button}
              </Link>
            </div>
          </div>
        </div>
      </section>
    </>
  )
}
