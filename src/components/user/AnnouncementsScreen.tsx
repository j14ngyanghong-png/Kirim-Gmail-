"use client"

import { useMemo, useState } from "react"
import { useApp } from "@/lib/store"
import type { Announcement } from "@/lib/types"
import { EmptyState } from "@/components/ui"

const CATEGORIES = [
  { id: "ALL", label: "Semua Info" },
  { id: "IMPORTANT", label: "Penting" },
  { id: "RATE_UPDATE", label: "Update Rate" },
  { id: "PROMO_BONUS", label: "Promo Bonus" },
  { id: "TIPS", label: "Tips & Trik" },
  { id: "GENERAL", label: "Umum" },
]

export function AnnouncementsScreen() {
  const { state } = useApp()
  const [query, setQuery] = useState("")
  const [category, setCategory] = useState("ALL")

  const filtered = useMemo(() => {
    return state.announcements.filter((a) => {
      const matchesCategory =
        category === "ALL" ? true : category === "IMPORTANT" ? a.isImportant : a.category === category
      const q = query.toLowerCase()
      const matchesSearch = a.title.toLowerCase().includes(q) || a.content.toLowerCase().includes(q)
      return matchesCategory && matchesSearch
    })
  }, [state.announcements, query, category])

  return (
    <div className="flex flex-col gap-3 px-4 pt-3 pb-6">
      <div>
        <h1 className="text-xl font-bold text-foreground text-balance">Pusat Pengumuman & Info</h1>
        <p className="text-xs text-muted-foreground text-pretty">
          Informasi rate terbaru, event promo bonus, dan panduan mitra.
        </p>
      </div>

      <div className="relative">
        <span className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground">
          <SearchIcon />
        </span>
        <input
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Cari informasi atau pengumuman..."
          className="h-11 w-full rounded-2xl border border-border bg-card pl-10 pr-3 text-sm text-foreground outline-none transition-colors placeholder:text-muted-foreground/60 focus:border-primary"
        />
      </div>

      <div className="-mx-4 flex gap-1.5 overflow-x-auto px-4 pb-1">
        {CATEGORIES.map((c) => (
          <button
            key={c.id}
            onClick={() => setCategory(c.id)}
            className={`shrink-0 rounded-full border px-3 py-1.5 text-[11px] font-bold transition-colors ${
              category === c.id ? "border-primary bg-primary text-primary-foreground" : "border-border bg-card text-foreground"
            }`}
          >
            {c.label}
          </button>
        ))}
      </div>

      {filtered.length === 0 ? (
        <EmptyState
          title="Tidak Ada Pengumuman"
          description="Tidak ditemukan pengumuman yang sesuai dengan filter pencarian Anda."
        />
      ) : (
        <div className="flex flex-col gap-3">
          {filtered.map((a) => (
            <AnnouncementCard key={a.id} announcement={a} />
          ))}
        </div>
      )}
    </div>
  )
}

function AnnouncementCard({ announcement }: { announcement: Announcement }) {
  const [expanded, setExpanded] = useState(announcement.isImportant)

  const meta: Record<string, { color: string; label: string }> = {
    RATE_UPDATE: { color: "var(--primary)", label: "Update Rate" },
    PROMO_BONUS: { color: "var(--gold-dark)", label: "Event Promo" },
    TIPS: { color: "var(--emerald)", label: "Tips Mitra" },
    GENERAL: { color: "#64748B", label: "Informasi" },
    IMPORTANT: { color: "#64748B", label: "Informasi" },
  }
  const m = meta[announcement.category] ?? meta.GENERAL

  return (
    <button
      onClick={() => setExpanded((v) => !v)}
      className={`w-full rounded-2xl border bg-card p-4 text-left transition-shadow ${
        announcement.isImportant ? "border-[var(--gold)] shadow-sm" : "border-border"
      }`}
    >
      <div className="flex items-center justify-between gap-2">
        <div className="flex items-center gap-1.5">
          {announcement.isImportant && (
            <span className="flex items-center gap-1 rounded-md bg-[var(--gold)]/15 px-1.5 py-0.5 text-[10px] font-black text-[var(--gold-dark)]">
              <PinIcon /> PENTING
            </span>
          )}
          <span
            className="rounded-md px-1.5 py-0.5 text-[10px] font-bold"
            style={{ backgroundColor: `color-mix(in oklch, ${m.color} 12%, transparent)`, color: m.color }}
          >
            {m.label}
          </span>
        </div>
        <span className="text-[11px] text-muted-foreground">{announcement.dateFormatted || "Hari Ini"}</span>
      </div>

      <h3 className="mt-2 text-sm font-bold text-foreground">{announcement.title}</h3>
      <p className={`mt-1 text-sm leading-relaxed text-muted-foreground ${expanded ? "" : "line-clamp-2"}`}>
        {announcement.content}
      </p>

      <div className="mt-2 flex items-center justify-between">
        <span className="text-[11px] text-muted-foreground">Oleh: {announcement.author}</span>
        <span className="text-muted-foreground">{expanded ? <ChevronUp /> : <ChevronDown />}</span>
      </div>
    </button>
  )
}

function SearchIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="11" cy="11" r="8" />
      <path d="m21 21-4.3-4.3" />
    </svg>
  )
}
function PinIcon() {
  return (
    <svg width="11" height="11" viewBox="0 0 24 24" fill="currentColor">
      <path d="M12 2a2 2 0 0 1 2 2v5l3 3v2h-4v6l-1 2-1-2v-6H7v-2l3-3V4a2 2 0 0 1 2-2Z" />
    </svg>
  )
}
function ChevronDown() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="m6 9 6 6 6-6" />
    </svg>
  )
}
function ChevronUp() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="m18 15-6-6-6 6" />
    </svg>
  )
}
