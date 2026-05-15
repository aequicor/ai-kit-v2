import { useState, useEffect, createContext, useContext } from 'react'
import { HashRouter, Routes, Route, Link, useLocation } from 'react-router-dom'
import { T } from './i18n'
import { LogoIcon, SunIcon, MoonIcon, GitHubIcon } from './ui'
import { useLatestRelease } from './hooks/useLatestRelease'
import Home from './pages/Home'
import Start from './pages/Start'
import Features from './pages/Features'
import Cli from './pages/Cli'
import Bundles from './pages/Bundles'
import Claude from './pages/Claude'
import Architecture from './pages/Architecture'

// ---------------------------------------------------------------------------
// Context
// ---------------------------------------------------------------------------
export const AppContext = createContext()
export function useApp() { return useContext(AppContext) }

// ---------------------------------------------------------------------------
// Scroll to top on route change
// ---------------------------------------------------------------------------
function ScrollToTop() {
  const { pathname } = useLocation()
  useEffect(() => { window.scrollTo(0, 0) }, [pathname])
  return null
}

// ---------------------------------------------------------------------------
// Nav
// ---------------------------------------------------------------------------
const GITHUB = 'https://github.com/aequicor/ai-kit-v2'

function NavLink({ to, children }) {
  const { pathname } = useLocation()
  const active = pathname === to
  return (
    <Link
      to={to}
      className={`transition-colors whitespace-nowrap ${
        active
          ? 'text-gray-900 dark:text-white font-medium'
          : 'text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white'
      }`}
    >
      {children}
    </Link>
  )
}

function Nav() {
  const { t, lang, setLang, isDark, setIsDark } = useApp()
  const release = useLatestRelease()

  return (
    <nav className="fixed top-0 left-0 right-0 z-50 border-b border-black/5 dark:border-white/5 bg-white/80 dark:bg-[#0a0f1e]/80 backdrop-blur-md transition-colors duration-300">
      <div className="max-w-7xl mx-auto px-6 h-14 flex items-center justify-between gap-4">
        <div className="flex items-center gap-2 shrink-0">
          <Link to="/" className="flex items-center gap-2">
            <LogoIcon className="w-6 h-6" />
            <span className="font-semibold text-gray-900 dark:text-white text-sm">AI Kit</span>
          </Link>
          <a
            href={release.url}
            target="_blank"
            rel="noopener noreferrer"
            className="text-xs px-2 py-0.5 rounded-full bg-cyan-500/10 border border-cyan-500/20 text-cyan-600 dark:text-cyan-400 font-mono hidden sm:inline hover:border-cyan-500/40 transition-colors"
          >
            {release.tag}
          </a>
        </div>

        <div className="flex items-center gap-5 text-sm overflow-x-auto scrollbar-hide flex-1 justify-center">
          <NavLink to="/">{t.nav.home}</NavLink>
          <NavLink to="/start">{t.nav.start}</NavLink>
          <NavLink to="/features">{t.nav.features}</NavLink>
          <NavLink to="/cli">{t.nav.cli}</NavLink>
          <NavLink to="/bundles">{t.nav.bundles}</NavLink>
          <NavLink to="/claude">{t.nav.claude}</NavLink>
          <NavLink to="/architecture">{t.nav.architecture}</NavLink>
        </div>

        <div className="flex items-center gap-2 shrink-0">
          <a
            href={GITHUB}
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-1.5 text-sm text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white transition-colors"
          >
            <GitHubIcon />
            <span className="hidden lg:inline">GitHub</span>
          </a>

          <div className="flex items-center rounded-lg border border-gray-200 dark:border-white/10 overflow-hidden text-xs font-semibold">
            <button
              onClick={() => setLang('en')}
              className={`px-2.5 py-1 transition-colors ${
                lang === 'en' ? 'bg-cyan-500 text-white' : 'text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white'
              }`}
            >
              EN
            </button>
            <button
              onClick={() => setLang('ru')}
              className={`px-2.5 py-1 transition-colors ${
                lang === 'ru' ? 'bg-cyan-500 text-white' : 'text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white'
              }`}
            >
              RU
            </button>
          </div>

          <button
            onClick={() => setIsDark((d) => !d)}
            className="w-8 h-8 flex items-center justify-center rounded-lg border border-gray-200 dark:border-white/10 text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white hover:border-gray-300 dark:hover:border-white/20 transition-colors"
            aria-label="Toggle theme"
          >
            {isDark ? <SunIcon /> : <MoonIcon />}
          </button>
        </div>
      </div>
    </nav>
  )
}

// ---------------------------------------------------------------------------
// Footer
// ---------------------------------------------------------------------------
function Footer() {
  const { t } = useApp()
  const release = useLatestRelease()
  return (
    <footer className="border-t border-black/5 dark:border-white/5 py-8 px-6 transition-colors duration-300">
      <div className="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-4 text-sm text-gray-400 dark:text-gray-600">
        <div className="flex items-center gap-2">
          <LogoIcon className="w-5 h-5" />
          <span className="text-gray-500 dark:text-gray-600">AI Kit</span>
          <span className="text-gray-300 dark:text-gray-700">·</span>
          <a
            href={release.url}
            target="_blank"
            rel="noopener noreferrer"
            className="text-gray-500 dark:text-gray-600 hover:text-gray-700 dark:hover:text-gray-400 transition-colors"
          >
            {release.tag}
          </a>
          <span className="text-gray-300 dark:text-gray-700">·</span>
          <a
            href={`${GITHUB}/blob/main/LICENSE`}
            target="_blank"
            rel="noopener noreferrer"
            className="hover:text-gray-700 dark:hover:text-gray-400 transition-colors"
          >
            {t.footer.license}
          </a>
        </div>
        <div className="flex items-center gap-6">
          <a href={GITHUB} target="_blank" rel="noopener noreferrer" className="hover:text-gray-700 dark:hover:text-gray-400 transition-colors">
            {t.footer.repo}
          </a>
          <a href={`${GITHUB}/releases`} target="_blank" rel="noopener noreferrer" className="hover:text-gray-700 dark:hover:text-gray-400 transition-colors">
            {t.footer.releases}
          </a>
        </div>
      </div>
    </footer>
  )
}

// ---------------------------------------------------------------------------
// Background grid decoration
// ---------------------------------------------------------------------------
function BgGrid() {
  return (
    <div
      className="fixed inset-0 opacity-[0.03] pointer-events-none hidden dark:block"
      style={{
        backgroundImage:
          'linear-gradient(rgba(255,255,255,0.1) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.1) 1px, transparent 1px)',
        backgroundSize: '60px 60px',
      }}
    />
  )
}

// ---------------------------------------------------------------------------
// Root
// ---------------------------------------------------------------------------
export default function App() {
  const [isDark, setIsDark] = useState(() => {
    if (typeof window !== 'undefined') {
      const stored = localStorage.getItem('theme')
      if (stored) return stored === 'dark'
    }
    return true
  })
  const [lang, setLang] = useState(() => {
    if (typeof window !== 'undefined') return localStorage.getItem('lang') || 'en'
    return 'en'
  })

  useEffect(() => {
    document.documentElement.classList.toggle('dark', isDark)
    localStorage.setItem('theme', isDark ? 'dark' : 'light')
  }, [isDark])

  useEffect(() => {
    localStorage.setItem('lang', lang)
  }, [lang])

  const t = T[lang]

  return (
    <AppContext.Provider value={{ t, lang, setLang, isDark, setIsDark }}>
      <HashRouter>
        <div className="min-h-screen bg-[#f8fafc] dark:bg-[#0a0f1e] text-gray-900 dark:text-gray-100 transition-colors duration-300">
          <BgGrid />
          <Nav />
          <main>
            <ScrollToTop />
            <Routes>
              <Route path="/" element={<Home />} />
              <Route path="/start" element={<Start />} />
              <Route path="/features" element={<Features />} />
              <Route path="/cli" element={<Cli />} />
              <Route path="/bundles" element={<Bundles />} />
              <Route path="/claude" element={<Claude />} />
              <Route path="/architecture" element={<Architecture />} />
            </Routes>
          </main>
          <Footer />
        </div>
      </HashRouter>
    </AppContext.Provider>
  )
}
