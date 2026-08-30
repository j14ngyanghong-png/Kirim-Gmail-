'use client'

import { useMemo, useState } from 'react'
import Image from 'next/image'
import {
  ArrowUpRight,
  BadgeCheck,
  Clock,
  Copy,
  Gauge,
  PlusCircle,
  ShieldCheck,
  Volume2,
  VolumeX,
  Wallet,
  XCircle,
} from 'lucide-react'
import { useStore } from '@/lib/store'
import { formatRelative, formatRupiah, maskEmail } from '@/lib/format'
import type { DepositStatus, GmailDeposit } from '@/lib/types'
import { DepositBadge } from '../ui'

const FILTERS: { key: string; label: string }[] = [
  { key: 'SEMUA', label: 'Semua' },
  { key: 'VALID', label: 'Valid' },
  { key: 'PENDING', label: 'Pending' },
  { key: 'DITOLAK', label: 'Ditolak' },
]

export function DashboardScreen() {
  const {
    state,
    currentUser,
    setTab,
    toggleSound,
    switchRoleToAdmin,
  } = useStore()
  const [filter, setFilter] = useState('SEMUA')

  const deposits = useMemo(
    () =>
      state.deposits
        .filter((d) => d.userId === currentUser?.id)
        .sort((a, b) => b.submittedAt - a.submittedAt),
    [state.deposits, currentUser?.id]
  )

  const total = deposits.length
  const valid = deposits.filter((d) => d.status === 'APPROVED').length
  const pending = deposits.filter((d) => d.status === 'PENDING').length
  const rejected = deposits.filter((d) => d.status === 'REJECTED').length
  const accuracy = total > 0 ? Math.round((valid / total) * 100) : 100

  const filtered = deposits.filter((d) => {
    if (filter === 'VALID') return d.status === 'APPROVED'
    if (filter === 'PENDING') return d.status === 'PENDING'
    if (filter === 'DITOLAK') return d.status === 'REJECTED'
    return true
  })

  if (!currentUser) return null

  function copyReferral() {
    navigator.clipboard?.writeText(currentUser!.referralCode)
  }

  return (
    <div className="flex flex-col gap-3.5 px-4 pb-6 pt-3">
      {/* Header */}
      <div className="flex items-center justify-between">
        <button
          onClick={() => setTab('PROFILE')}
          className="flex items-center gap-2.5 rounded-xl p-1 text-left"
        >
          <span className="flex size-11 items-center justify-center rounded-full bg-primary/15">
            <span className="text-base font-bold text-primary">
              {currentUser.username.charAt(0).toUpperCase()}
            </span>
          </span>
          <span>
            <span className="block text-[15px] font-bold text-ink">
              Halo, {currentUser.username}
            </span>
            <span className="flex items-center gap-1.5">
              <span className="rounded-md bg-emerald/15 px-1.5 py-0.5 text-[10px] font-bold text-emerald-dark">
                MITRA AKTIF
              </span>
              <span className="text-[11px] text-ink-secondary">Akurasi {accuracy}%</span>
            </span>
          </span>
        </button>

        <div className="flex items-center gap-1">
          <button
            onClick={toggleSound}
            className="flex size-9 items-center justify-center rounded-full text-ink-secondary transition hover:bg-surface-variant"
            aria-label="Toggle suara"
          >
            {currentUser.isSoundEnabled ? (
              <Volume2 className="size-5 text-primary" />
            ) : (
              <VolumeX className="size-5" />
            )}
          </button>
          {currentUser.role === 'ADMIN' ? (
            <button
              onClick={switchRoleToAdmin}
              className="flex items-center gap-1 rounded-lg bg-gold/20 px-2.5 py-1.5 text-[11px] font-bold text-gold-dark"
            >
              <ShieldCheck className="size-4" /> Panel Admin
            </button>
          ) : null}
        </div>
      </div>

      {/* Broadcast marquee */}
      <BroadcastBanner text={state.config.activeBroadcastBanner} />

      {/* Hero */}
      <div className="relative h-32 overflow-hidden rounded-2xl">
        <Image
          src="/images/rewards-banner.jpg"
          alt="Promo rate tinggi setor Gmail"
          fill
          className="object-cover"
          priority
        />
        <div className="absolute inset-0 bg-gradient-to-r from-primary-dark/90 via-primary-dark/60 to-transparent" />
        <div className="absolute inset-0 flex flex-col justify-center gap-1 p-4">
          <span className="w-fit rounded bg-gold px-1.5 py-0.5 text-[9px] font-black text-black">
            RATE TINGGI 2026
          </span>
          <p className="max-w-[70%] text-[15px] font-bold leading-tight text-white text-balance">
            Setor Akun Gmail &amp; Dapatkan Cuan Harian!
          </p>
          <p className="text-[11px] text-[#d6e2ff]">
            Proses verifikasi kilat &amp; auto reward ke e-wallet
          </p>
        </div>
      </div>

      {/* Balance card */}
      <div className="overflow-hidden rounded-3xl bg-gradient-to-br from-primary to-primary-dark p-5 text-white shadow-lg shadow-primary/25">
        <div className="flex items-start justify-between">
          <div>
            <p className="text-xs text-white/80">Saldo Reward Anda</p>
            <p className="mt-1 text-3xl font-extrabold tracking-tight">
              {formatRupiah(currentUser.balance)}
            </p>
          </div>
          <span className="flex size-11 items-center justify-center rounded-full bg-white/15">
            <Wallet className="size-6 text-gold-light" />
          </span>
        </div>

        <div className="mt-4 flex items-center justify-between rounded-xl bg-black/20 px-3 py-2">
          <span className="flex items-center gap-1.5 text-xs font-semibold">
            <Gauge className="size-4 text-emerald-light" />
            Rate per Akun: {formatRupiah(state.config.currentRatePerAccount)}
          </span>
          <span className="text-[11px] text-white/70">
            Min. Tarik: {formatRupiah(state.config.minWithdrawalAmount)}
          </span>
        </div>

        <div className="mt-4 flex gap-2.5">
          <button
            onClick={() => setTab('DEPOSIT')}
            className="flex h-11 flex-1 items-center justify-center gap-1.5 rounded-xl bg-gold text-sm font-bold text-black transition active:scale-[0.98]"
          >
            <PlusCircle className="size-[18px]" /> Setor Akun
          </button>
          <button
            onClick={() => setTab('WITHDRAW')}
            className="flex h-11 flex-1 items-center justify-center gap-1.5 rounded-xl bg-white/15 text-sm font-bold text-white transition active:scale-[0.98]"
          >
            <ArrowUpRight className="size-[18px]" /> Tarik Saldo
          </button>
        </div>
      </div>

      {/* Stats row */}
      <div className="grid grid-cols-3 gap-2.5">
        <StatCard icon={<BadgeCheck className="size-4 text-emerald" />} label="Valid" value={valid} tone="emerald" />
        <StatCard icon={<Clock className="size-4 text-amber" />} label="Pending" value={pending} tone="amber" />
        <StatCard icon={<XCircle className="size-4 text-coral" />} label="Ditolak" value={rejected} tone="coral" />
      </div>

      {/* Referral */}
      <button
        onClick={copyReferral}
        className="flex items-center justify-between rounded-2xl border border-dashed border-primary/40 bg-primary/5 px-4 py-3 text-left"
      >
        <div>
          <p className="text-[11px] text-ink-secondary">Kode Referral Anda</p>
          <p className="text-base font-extrabold tracking-wide text-primary">
            {currentUser.referralCode}
          </p>
        </div>
        <span className="flex items-center gap-1 text-xs font-semibold text-primary">
          <Copy className="size-4" /> Salin
        </span>
      </button>

      {/* History */}
      <div className="mt-1 flex items-center justify-between">
        <h2 className="text-sm font-bold text-ink">Riwayat Setoran</h2>
        <span className="text-[11px] text-ink-secondary">{total} akun</span>
      </div>

      <div className="no-scrollbar -mx-4 flex gap-2 overflow-x-auto px-4">
        {FILTERS.map((f) => (
          <button
            key={f.key}
            onClick={() => setFilter(f.key)}
            className={`shrink-0 rounded-full px-3.5 py-1.5 text-xs font-semibold transition ${
              filter === f.key
                ? 'bg-primary text-white'
                : 'bg-surface text-ink-secondary'
            }`}
          >
            {f.label}
          </button>
        ))}
      </div>

      <div className="flex flex-col gap-2.5">
        {filtered.length === 0 ? (
          <EmptyState onSetor={() => setTab('DEPOSIT')} />
        ) : (
          filtered.map((d) => <DepositRow key={d.id} deposit={d} />)
        )}
      </div>
    </div>
  )
}

function BroadcastBanner({ text }: { text: string }) {
  return (
    <div className="flex items-center gap-2 overflow-hidden rounded-xl border border-gold/40 bg-gold/10 px-3 py-2">
      <span className="flex size-5 shrink-0 items-center justify-center rounded-full bg-gold text-black">
        <ArrowUpRight className="size-3" />
      </span>
      <div className="relative flex-1 overflow-hidden">
        <div className="animate-marquee flex w-max gap-16 whitespace-nowrap text-xs font-semibold text-gold-dark">
          <span>{text}</span>
          <span aria-hidden>{text}</span>
        </div>
      </div>
    </div>
  )
}

function StatCard({
  icon,
  label,
  value,
  tone,
}: {
  icon: React.ReactNode
  label: string
  value: number
  tone: 'emerald' | 'amber' | 'coral'
}) {
  const ring =
    tone === 'emerald'
      ? 'bg-emerald/10'
      : tone === 'amber'
        ? 'bg-amber/10'
        : 'bg-coral/10'
  return (
    <div className="flex flex-col items-center gap-1 rounded-2xl border border-card-border bg-surface py-3">
      <span className={`flex size-8 items-center justify-center rounded-full ${ring}`}>
        {icon}
      </span>
      <span className="text-lg font-extrabold text-ink">{value}</span>
      <span className="text-[11px] text-ink-secondary">{label}</span>
    </div>
  )
}

function DepositRow({ deposit }: { deposit: GmailDeposit }) {
  const iconByStatus: Record<DepositStatus, React.ReactNode> = {
    APPROVED: <BadgeCheck className="size-5 text-emerald" />,
    PENDING: <Clock className="size-5 text-amber" />,
    REJECTED: <XCircle className="size-5 text-coral" />,
  }
  const bgByStatus: Record<DepositStatus, string> = {
    APPROVED: 'bg-emerald/10',
    PENDING: 'bg-amber/10',
    REJECTED: 'bg-coral/10',
  }
  return (
    <div className="flex items-center gap-3 rounded-2xl border border-card-border bg-surface p-3">
      <span
        className={`flex size-10 shrink-0 items-center justify-center rounded-xl ${bgByStatus[deposit.status]}`}
      >
        {iconByStatus[deposit.status]}
      </span>
      <div className="min-w-0 flex-1">
        <p className="truncate text-sm font-semibold text-ink">
          {maskEmail(deposit.email)}
        </p>
        <p className="truncate text-[11px] text-ink-secondary">
          {deposit.status === 'REJECTED' && deposit.rejectReason
            ? deposit.rejectReason
            : `Akun ${deposit.accountYear} · ${formatRelative(deposit.submittedAt)}`}
        </p>
      </div>
      <div className="flex flex-col items-end gap-1">
        <DepositBadge status={deposit.status} />
        <span
          className={`text-xs font-bold ${
            deposit.status === 'APPROVED' ? 'text-emerald-dark' : 'text-ink-tertiary'
          }`}
        >
          {deposit.status === 'APPROVED'
            ? `+${formatRupiah(deposit.rewardAmount)}`
            : formatRupiah(deposit.rewardAmount)}
        </span>
      </div>
    </div>
  )
}

function EmptyState({ onSetor }: { onSetor: () => void }) {
  return (
    <div className="flex flex-col items-center gap-2 rounded-2xl border border-dashed border-card-border bg-surface py-8 text-center">
      <span className="flex size-12 items-center justify-center rounded-full bg-surface-variant">
        <PlusCircle className="size-6 text-primary" />
      </span>
      <p className="text-sm font-semibold text-ink">Belum ada setoran</p>
      <p className="max-w-[220px] text-xs text-ink-secondary">
        Mulai setor akun Gmail terverifikasi untuk mendapatkan reward.
      </p>
      <button
        onClick={onSetor}
        className="mt-1 rounded-lg bg-primary px-4 py-2 text-xs font-bold text-white"
      >
        Setor Sekarang
      </button>
    </div>
  )
}
