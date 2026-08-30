'use client'

import { useState } from 'react'
import { Plus, ShieldCheck, X } from 'lucide-react'

const GOOGLE_LOGO =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48"><path fill="#FFC107" d="M43.6 20.5H42V20H24v8h11.3c-1.6 4.7-6.1 8-11.3 8-6.6 0-12-5.4-12-12s5.4-12 12-12c3.1 0 5.9 1.2 8 3.1l5.7-5.7C34.5 6.1 29.6 4 24 4 12.9 4 4 12.9 4 24s8.9 20 20 20 20-8.9 20-20c0-1.3-.1-2.3-.4-3.5z"/><path fill="#FF3D00" d="M6.3 14.7l6.6 4.8C14.7 15.1 19 12 24 12c3.1 0 5.9 1.2 8 3.1l5.7-5.7C34.5 6.1 29.6 4 24 4 16.3 4 9.7 8.3 6.3 14.7z"/><path fill="#4CAF50" d="M24 44c5.5 0 10.3-2.1 14-5.5l-6.5-5.5C29.6 34.5 26.9 36 24 36c-5.2 0-9.6-3.3-11.3-7.9l-6.5 5C9.6 39.6 16.2 44 24 44z"/><path fill="#1976D2" d="M43.6 20.5H42V20H24v8h11.3c-.8 2.3-2.3 4.3-4.3 5.5l6.5 5.5C41.9 36.3 44 30.7 44 24c0-1.3-.1-2.3-.4-3.5z"/></svg>`
  )

interface GoogleAccount {
  name: string
  email: string
  avatarBg: string
}

const PREDEFINED: GoogleAccount[] = [
  { name: 'Jiang Yanghong (Admin)', email: 'j14ngyanghong@gmail.com', avatarBg: '#1E88E5' },
  { name: 'Administrator Master', email: 'admin@setorgmail.com', avatarBg: '#E65100' },
  { name: 'Budi Santoso', email: 'budi.santoso@gmail.com', avatarBg: '#43A047' },
  { name: 'Mitra Rewards', email: 'mitra.rewards.official@gmail.com', avatarBg: '#E53935' },
]

export function GoogleDialog({
  onClose,
  onSelect,
}: {
  onClose: () => void
  onSelect: (email: string, displayName: string) => void
}) {
  const [custom, setCustom] = useState(false)
  const [email, setEmail] = useState('')
  const [name, setName] = useState('')
  const [error, setError] = useState<string | null>(null)

  return (
    <div className="absolute inset-0 z-40 flex items-center justify-center bg-black/50 p-4">
      <div className="animate-pop-in w-full max-w-sm rounded-3xl bg-surface p-6 shadow-2xl">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img src={GOOGLE_LOGO} alt="Google" className="size-6" />
            <h3 className="text-base font-bold text-ink">Masuk dengan Google</h3>
          </div>
          <button onClick={onClose} aria-label="Tutup" className="text-ink-tertiary">
            <X className="size-5" />
          </button>
        </div>

        <p className="mt-1.5 text-xs leading-relaxed text-ink-secondary">
          Pilih akun Google Anda untuk melanjutkan ke Setor Gmail Rewards
        </p>

        <div className="my-4 h-px bg-card-border" />

        {!custom ? (
          <div className="flex flex-col">
            {PREDEFINED.map((acc) => (
              <button
                key={acc.email}
                onClick={() => onSelect(acc.email, acc.name)}
                className="flex items-center gap-3 rounded-xl px-1.5 py-2.5 text-left transition hover:bg-surface-variant"
              >
                <span
                  className="flex size-10 items-center justify-center rounded-full text-base font-bold text-white"
                  style={{ backgroundColor: acc.avatarBg }}
                >
                  {acc.name.charAt(0)}
                </span>
                <span className="min-w-0 flex-1">
                  <span className="block truncate text-sm font-semibold text-ink">
                    {acc.name}
                  </span>
                  <span className="block truncate text-xs text-ink-secondary">
                    {acc.email}
                  </span>
                </span>
              </button>
            ))}

            <div className="my-2 h-px bg-card-border" />

            <button
              onClick={() => setCustom(true)}
              className="flex items-center gap-3 rounded-xl px-1.5 py-2.5 text-left transition hover:bg-surface-variant"
            >
              <span className="flex size-9 items-center justify-center rounded-full bg-surface-variant">
                <Plus className="size-5 text-primary" />
              </span>
              <span className="text-sm font-bold text-primary">
                Gunakan Akun Google Lain
              </span>
            </button>
          </div>
        ) : (
          <div className="flex flex-col gap-3">
            <p className="text-xs font-bold text-ink">Masukkan Akun Google Baru:</p>
            <input
              value={email}
              onChange={(e) => setEmail(e.target.value.toLowerCase().trim())}
              placeholder="nama.anda@gmail.com"
              className="w-full rounded-xl border border-card-border bg-surface px-3 py-2.5 text-sm outline-none focus:border-primary"
            />
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Nama Lengkap / Panggilan"
              className="w-full rounded-xl border border-card-border bg-surface px-3 py-2.5 text-sm outline-none focus:border-primary"
            />
            {error ? <p className="text-xs text-coral">{error}</p> : null}
            <div className="flex gap-2">
              <button
                onClick={() => setCustom(false)}
                className="flex-1 rounded-xl border border-card-border py-2.5 text-sm font-semibold text-ink-secondary"
              >
                Kembali
              </button>
              <button
                onClick={() => {
                  if (!email.includes('@gmail.com')) {
                    setError('Email harus berformat @gmail.com')
                    return
                  }
                  onSelect(email, name || email.split('@')[0].replace(/\./g, ' '))
                }}
                className="flex-1 rounded-xl bg-primary py-2.5 text-sm font-bold text-white"
              >
                Lanjutkan
              </button>
            </div>
          </div>
        )}

        <div className="mt-4 flex items-center justify-center gap-1.5 text-[11px] text-ink-secondary">
          <ShieldCheck className="size-3.5 text-emerald" />
          Google Sign-In Terverifikasi &amp; Aman
        </div>
      </div>
    </div>
  )
}

export { GOOGLE_LOGO }
