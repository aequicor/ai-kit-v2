import { useEffect, useState } from 'react'

const REPO = 'aequicor/ai-kit-v2'
const API = `https://api.github.com/repos/${REPO}/releases/latest`
const RELEASES_URL = `https://github.com/${REPO}/releases`
const STORAGE_KEY = 'aikit:latest-release'
const TTL_MS = 6 * 60 * 60 * 1000

const FALLBACK_TAG = `v${typeof __APP_VERSION__ !== 'undefined' ? __APP_VERSION__ : '0.0.0'}`

function readCache() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    const parsed = JSON.parse(raw)
    if (!parsed.tag || !parsed.fetchedAt) return null
    if (Date.now() - parsed.fetchedAt > TTL_MS) return null
    return parsed
  } catch {
    return null
  }
}

function writeCache(tag, url) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({ tag, url, fetchedAt: Date.now() }))
  } catch {
    /* ignore quota / private mode */
  }
}

export function useLatestRelease() {
  const cached = typeof window !== 'undefined' ? readCache() : null
  const [state, setState] = useState(
    cached
      ? { tag: cached.tag, url: cached.url, source: 'cache' }
      : { tag: FALLBACK_TAG, url: RELEASES_URL, source: 'fallback' }
  )

  useEffect(() => {
    if (cached) return
    let cancelled = false
    fetch(API, { headers: { Accept: 'application/vnd.github+json' } })
      .then((r) => (r.ok ? r.json() : null))
      .then((data) => {
        if (cancelled || !data || !data.tag_name) return
        writeCache(data.tag_name, data.html_url)
        setState({ tag: data.tag_name, url: data.html_url, source: 'live' })
      })
      .catch(() => {})
    return () => {
      cancelled = true
    }
  }, [])

  return state
}
