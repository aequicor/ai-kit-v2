import { useApp } from '../App'
import { PageHero, SectionHeader, CopyBlock, StepCard, FilesTable, FeatureCard, CodeBlock } from '../ui'

const SETUP_PROMPT_PREVIEW = `Read this prompt carefully, then follow each step in order.

You are setting up AI Kit for this project.

Step 1 — ask the user which language to use for generated files: English or Russian.

Step 2 — scan the repository and ask the user:
  a) which agent they use (claude-code / opencode / qwen-code / cursor / aider)
  b) which model family they want (e.g. claude-4, gpt-4o, qwen2.5-coder, ...)

[... download and run kit-setup ...]`

const SETUP_PROMPT_FULL = `Read this prompt carefully, then follow each step in order.

You are setting up AI Kit for this project.

Step 1 — ask the user which language to use for generated files: English or Russian.

Step 2 — scan the repository and ask the user:
  a) which agent they use (claude-code / opencode / qwen-code / cursor / aider)
  b) which model family they want (e.g. claude-4, gpt-4o, qwen2.5-coder, ...)

Step 3 — based on your scan, propose a draft .aikit/manifest.yaml. Show it to the user and wait for approval before doing anything else.

Step 4 — determine the OS. Download the kit-setup binary for this platform from:
  https://github.com/aequicor/ai-kit-v2/releases/latest
  Filename pattern: kit-setup-<os>  (linux / macos / windows.exe)
  Save to .aikit/bin/kit-setup. Mark it executable on Linux/macOS.

Step 5 — run: .aikit/bin/kit-setup verify
  If errors appear, fix the manifest and re-run until it passes.

Step 6 — run: .aikit/bin/kit-setup generate
  Show the list of generated files to the user.

Step 7 — commit everything:
  git add .aikit CLAUDE.md AGENTS.md CONVENTIONS.md .claude .opencode .qwen .cursor .aider opencode.json .mcp.json 2>/dev/null; git commit -m "chore: add AI Kit"

Done. Tell the user which files were generated and what to do next.`

export default function Start() {
  const { t } = useApp()
  const s = t.start
  const c = t.common

  return (
    <>
      <PageHero eyebrow={s.hero.eyebrow} title={s.hero.title} lead={s.hero.lead} />

      {/* Setup prompt */}
      <section className="py-16 px-6">
        <div className="max-w-3xl mx-auto">
          <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">{s.setup.title}</h2>
          <p className="text-gray-500 dark:text-gray-400 mb-6 text-sm">{s.setup.subtitle}</p>

          <CopyBlock
            preview={SETUP_PROMPT_PREVIEW}
            fullPrompt={SETUP_PROMPT_FULL}
            label={s.setup.previewLabel}
            copyLabel={c.copyFull}
            copiedLabel={c.copied}
          />

          <div className="mt-8">
            <p className="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-4">{s.setup.stepsTitle}</p>
            <ol className="space-y-3">
              {s.setup.steps.map((step, i) => (
                <li key={i} className="flex gap-3 text-sm text-gray-500 dark:text-gray-400">
                  <span className="shrink-0 w-5 h-5 rounded-full bg-cyan-500/10 border border-cyan-500/30 flex items-center justify-center text-cyan-400 text-xs font-bold">
                    {i + 1}
                  </span>
                  <span className="leading-relaxed">{step}</span>
                </li>
              ))}
            </ol>
          </div>
        </div>
      </section>

      {/* Result files */}
      <section className="py-16 px-6 bg-gradient-to-b from-transparent via-black/[0.02] dark:via-white/[0.02] to-transparent">
        <div className="max-w-3xl mx-auto">
          <SectionHeader eyebrow={s.files.eyebrow} title={s.files.title} lead={s.files.lead} />
          <div className="overflow-x-auto rounded-xl border border-gray-200 dark:border-white/10">
            <table className="w-full text-sm">
              <tbody>
                {s.files.tree.map((row) => (
                  <tr key={row.path} className="border-b border-gray-100 dark:border-white/5 last:border-0">
                    <td className="px-5 py-3.5 w-[52%] align-top">
                      <code className="text-violet-600 dark:text-violet-400 font-mono text-xs bg-violet-500/5 px-1.5 py-0.5 rounded">
                        {row.path}
                      </code>
                    </td>
                    <td className="px-5 py-3.5 text-gray-500 dark:text-gray-400 leading-relaxed align-top text-xs">
                      {row.note}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </section>

      {/* Three commands */}
      <section className="py-16 px-6">
        <div className="max-w-3xl mx-auto">
          <SectionHeader eyebrow={s.commands.eyebrow} title={s.commands.title} lead={s.commands.lead} />
          <div className="space-y-4">
            {s.commands.items.map((cmd) => (
              <div key={cmd.name} className="card-glass p-6 hover:border-cyan-500/30 transition-colors duration-300">
                <div className="flex items-baseline gap-3 mb-2">
                  <code className="text-cyan-400 dark:text-cyan-400 font-mono text-base font-semibold">{cmd.name}</code>
                  <span className="text-xs text-gray-400 dark:text-gray-500 uppercase tracking-widest font-semibold">{cmd.subtitle}</span>
                </div>
                <p className="text-sm leading-relaxed text-gray-500 dark:text-gray-400">{cmd.body}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Scenarios */}
      <section className="py-16 px-6 bg-gradient-to-b from-transparent via-black/[0.02] dark:via-white/[0.02] to-transparent">
        <div className="max-w-3xl mx-auto">
          <SectionHeader eyebrow={s.scenarios.eyebrow} title={s.scenarios.title} />
          <div className="space-y-3">
            {s.scenarios.items.map((item) => (
              <div key={item.title} className="card-glass p-5 hover:border-violet-500/30 transition-colors duration-300">
                <p className="font-semibold text-sm text-gray-900 dark:text-white mb-1">{item.title}</p>
                <p className="text-sm leading-relaxed text-gray-500 dark:text-gray-400">{item.body}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CLI reference */}
      <section className="py-16 px-6">
        <div className="max-w-3xl mx-auto">
          <SectionHeader eyebrow={s.cli.eyebrow} title={s.cli.title} lead={s.cli.lead} />

          <div className="space-y-4 mb-10">
            {s.cli.subcommands.map((sub) => (
              <div key={sub.cmd} className="card-glass p-5">
                <code className="block text-violet-400 font-mono text-sm mb-2">{sub.cmd}</code>
                <p className="text-sm leading-relaxed text-gray-500 dark:text-gray-400">{sub.body}</p>
              </div>
            ))}
          </div>

          <p className="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-4">{s.cli.exitTitle}</p>
          <div className="overflow-x-auto rounded-xl border border-gray-200 dark:border-white/10 mb-8">
            <table className="w-full text-sm">
              <tbody>
                {s.cli.exit.map((row) => (
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

          <a
            href="https://github.com/aequicor/ai-kit-v2/releases/latest"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 px-6 py-3 rounded-xl font-semibold text-sm bg-gradient-to-r from-cyan-500 to-blue-600 text-white hover:from-cyan-400 hover:to-blue-500 transition-all duration-200 shadow-lg shadow-cyan-500/20"
          >
            {s.cli.releasesLink}
          </a>
        </div>
      </section>
    </>
  )
}
