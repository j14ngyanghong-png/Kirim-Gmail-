'use client'

import { useMemo, useState } from 'react'
import {
  AtSign,
  Info,
  KeyRound,
  LifeBuoy,
  Lock,
  Send,
  Layers,
  FileText,
} from 'lucide-react'
import { useStore } from '@/lib/store'
import { formatRupiah } from '@/lib/format'
import { Field } from '../ui'

const YEARS = ['2025', '2024', '2023', '2022', '2021 & sebelumnya']

export function DepositScreen() {
  const { state } = useStore()
  const [mode, setMode] = useState<'single' | 'bulk'>('single')
  const rate = state.config.currentRatePerAccount

  return (
    <div className="min-h-full">
      <div className="relative overflow-hidden bg-gradient-to-br from-primary to-primary-dark px-5 pb-8 pt-5 text-white">
        <div className="absolute -right-10 -top-10 size-36 rounded-full bg-white/10" />
        <h1 className="relative text-xl font-extrabold">Setor Akun Gmail</h1>
        <p className="relative mt-1 text-sm text-white/80">
          Reward {formatRupiah(rate)} / akun valid. Bonus {formatRupiah(state.config.bonusRateTier)} / akun untuk 10+ akun.
        </p>

        <div className="relative mt-4 flex rounded-xl bg-black/20 p-1">
          <button
            onClick={() => setMode('single')}
            className={`flex flex-1 items-center justify-center gap-1.5 rounded-lg py-2 text-xs font-bold transition ${
              mode === 'single' ? 'bg-white text-primary-dark' : 'text-white/80'
            }`}
          >
            <FileText className="size-4" /> Setor Tunggal
          </button>
          <button
            onClick={() => setMode('bulk')}
            className={`flex flex-1 items-center justify-center gap-1.5 rounded-lg py-2 text-xs font-bold transition ${
              mode === 'bulk' ? 'bg-white text-primary-dark' : 'text-white/80'
            }`}
          >
            <Layers className="size-4" /> Setor Massal
          </button>
        </div>
      </div>

      <div className="relative -mt-4 rounded-t-3xl bg-background px-5 pb-8 pt-5">
        {mode === 'single' ? <SingleForm /> : <BulkForm />}
      </div>
    </div>
  )
}

function SingleForm() {
  const { submitSingleDeposit } = useStore()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [recovery, setRecovery] = useState('')
  const [year, setYear] = useState('2024')
  const [note, setNote] = useState('')
  const [error, setError] = useState<string | null>(null)

  function submit(e: React.FormEvent) {
    e.preventDefault()
    if (!email.includes('@gmail.com')) {
      setError('Email harus berformat @gmail.com')
      return
    }
    if (password.length < 4) {
      setError('Password akun wajib diisi.')
      return
    }
    setError(null)
    submitSingleDeposit({ email, password, recovery, year, note })
    setEmail('')
    setPassword('')
    setRecovery('')
    setNote('')
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-3.5">
      <Field
        label="Email Gmail"
        icon={<AtSign className="size-4" />}
        value={email}
        onChange={(e) => setEmail(e.target.value.toLowerCase().trim())}
        placeholder="akun.anda@gmail.com"
      />
      <Field
        label="Password Akun"
        icon={<Lock className="size-4" />}
        password
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder="Password akun Gmail"
      />
      <Field
        label="Email / Nomor Pemulihan (Recovery)"
        icon={<LifeBuoy className="size-4" />}
        value={recovery}
        onChange={(e) => setRecovery(e.target.value)}
        placeholder="recovery@outlook.com"
      />

      <div>
        <span className="mb-1.5 block text-xs font-semibold text-ink-secondary">
          Tahun Pembuatan Akun
        </span>
        <div className="flex flex-wrap gap-2">
          {YEARS.map((y) => (
            <button
              key={y}
              type="button"
              onClick={() => setYear(y)}
              className={`rounded-lg border px-3 py-1.5 text-xs font-semibold transition ${
                year === y
                  ? 'border-primary bg-primary/10 text-primary'
                  : 'border-card-border bg-surface text-ink-secondary'
              }`}
            >
              {y}
            </button>
          ))}
        </div>
      </div>

      <Field
        label="Catatan (Opsional)"
        icon={<FileText className="size-4" />}
        value={note}
        onChange={(e) => setNote(e.target.value)}
        placeholder="Contoh: akun jarang dipakai"
      />

      {error ? <p className="text-xs font-medium text-coral">{error}</p> : null}

      <SafetyNote />

      <button
        type="submit"
        className="mt-1 flex h-12 items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-emerald to-emerald-dark text-sm font-bold text-white shadow-lg shadow-emerald/25 transition active:scale-[0.99]"
      >
        <Send className="size-[18px]" /> Setor Akun Sekarang
      </button>
    </form>
  )
}

function BulkForm() {
  const { submitBulkDeposits, state } = useStore()
  const [raw, setRaw] = useState('')
  const [error, setError] = useState<string | null>(null)

  const parsed = useMemo(() => {
    return raw
      .split('\n')
      .map((line) => line.trim())
      .filter(Boolean)
      .map((line) => {
        const [email = '', password = '', recovery = ''] = line
          .split(/[|,;\t]/)
          .map((s) => s.trim())
        return { email, password, recovery }
      })
      .filter((e) => e.email && e.password)
  }, [raw])

  const rate = state.config.currentRatePerAccount
  const bonus = parsed.length >= 10 ? state.config.bonusRateTier : 0
  const estimate = parsed.length * (rate + bonus)

  function submit(e: React.FormEvent) {
    e.preventDefault()
    if (parsed.length === 0) {
      setError('Tidak ada akun valid. Gunakan format email|password|recovery')
      return
    }
    setError(null)
    submitBulkDeposits(parsed)
    setRaw('')
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-3.5">
      <div className="rounded-xl border border-primary/30 bg-primary/5 p-3">
        <p className="flex items-center gap-1.5 text-[11px] font-bold text-primary">
          <Info className="size-3.5" /> Format satu akun per baris
        </p>
        <code className="mt-1 block rounded-md bg-surface px-2 py-1 text-[11px] text-ink-secondary">
          email@gmail.com|password|recovery@mail.com
        </code>
      </div>

      <label className="block">
        <span className="mb-1.5 block text-xs font-semibold text-ink-secondary">
          Daftar Akun (Massal)
        </span>
        <textarea
          value={raw}
          onChange={(e) => setRaw(e.target.value)}
          rows={8}
          placeholder={
            'akun1@gmail.com|Pass123!|rec1@mail.com\nakun2@gmail.com|Pass456!|rec2@mail.com'
          }
          className="w-full resize-none rounded-xl border border-card-border bg-surface px-3 py-3 font-mono text-[13px] text-ink outline-none focus:border-primary focus:ring-2 focus:ring-primary/15"
        />
      </label>

      <div className="flex items-center justify-between rounded-xl bg-surface-variant px-4 py-3">
        <div>
          <p className="text-[11px] text-ink-secondary">Akun terdeteksi</p>
          <p className="text-lg font-extrabold text-ink">{parsed.length} akun</p>
        </div>
        <div className="text-right">
          <p className="text-[11px] text-ink-secondary">
            Estimasi reward {bonus > 0 ? '(termasuk bonus)' : ''}
          </p>
          <p className="text-lg font-extrabold text-emerald-dark">
            {formatRupiah(estimate)}
          </p>
        </div>
      </div>

      {error ? <p className="text-xs font-medium text-coral">{error}</p> : null}

      <SafetyNote />

      <button
        type="submit"
        className="mt-1 flex h-12 items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-emerald to-emerald-dark text-sm font-bold text-white shadow-lg shadow-emerald/25 transition active:scale-[0.99]"
      >
        <Layers className="size-[18px] " /> Setor {parsed.length || ''} Akun Sekaligus
      </button>
    </form>
  )
}

function SafetyNote() {
  return (
    <div className="flex items-start gap-2 rounded-xl bg-emerald/10 px-3 py-2.5">
      <KeyRound className="mt-0.5 size-4 shrink-0 text-emerald-dark" />
      <p className="text-[11px] leading-relaxed text-emerald-dark">
        Pastikan akun <strong>tidak mengaktifkan 2FA</strong> dan memiliki email pemulihan
        aktif agar cepat lolos verifikasi tanpa revisi.
      </p>
    </div>
  )
}
