'use client'

import { useState, type InputHTMLAttributes, type ReactNode } from 'react'
import { Check, Eye, EyeOff, Info, X, AlertTriangle } from 'lucide-react'
import type { DepositStatus, WithdrawalStatus } from '@/lib/types'
import { useStore } from '@/lib/store'

/* ---------------- Toast host ---------------- */
export function ToastHost() {
  const { toasts, dismissToast } = useStore()
  return (
    <div className="pointer-events-none absolute inset-x-0 top-0 z-50 flex flex-col items-center gap-2 px-4 pt-4">
      {toasts.map((t) => {
        const styles =
          t.variant === 'success'
            ? 'bg-emerald text-white'
            : t.variant === 'error'
              ? 'bg-coral text-white'
              : 'bg-primary-dark text-white'
        const Icon =
          t.variant === 'success' ? Check : t.variant === 'error' ? AlertTriangle : Info
        return (
          <div
            key={t.id}
            className={`animate-toast-in pointer-events-auto flex w-full max-w-sm items-start gap-2 rounded-xl px-4 py-3 shadow-lg ${styles}`}
            role="status"
          >
            <Icon className="mt-0.5 size-4 shrink-0" />
            <p className="flex-1 text-sm font-medium leading-snug">{t.message}</p>
            <button
              onClick={() => dismissToast(t.id)}
              className="opacity-70 transition hover:opacity-100"
              aria-label="Tutup notifikasi"
            >
              <X className="size-4" />
            </button>
          </div>
        )
      })}
    </div>
  )
}

/* ---------------- Status badge ---------------- */
export function DepositBadge({ status }: { status: DepositStatus }) {
  const map: Record<DepositStatus, { label: string; cls: string }> = {
    APPROVED: { label: 'VALID', cls: 'bg-emerald/15 text-emerald-dark' },
    PENDING: { label: 'PENDING', cls: 'bg-amber/15 text-amber' },
    REJECTED: { label: 'DITOLAK', cls: 'bg-coral/15 text-coral-dark' },
  }
  const { label, cls } = map[status]
  return (
    <span className={`rounded-md px-2 py-0.5 text-[10px] font-bold tracking-wide ${cls}`}>
      {label}
    </span>
  )
}

export function WithdrawalBadge({ status }: { status: WithdrawalStatus }) {
  const map: Record<WithdrawalStatus, { label: string; cls: string }> = {
    SUCCESS: { label: 'SUKSES', cls: 'bg-emerald/15 text-emerald-dark' },
    PROCESSED: { label: 'DIPROSES', cls: 'bg-primary/15 text-primary' },
    REJECTED: { label: 'DITOLAK', cls: 'bg-coral/15 text-coral-dark' },
  }
  const { label, cls } = map[status]
  return (
    <span className={`rounded-md px-2 py-0.5 text-[10px] font-bold tracking-wide ${cls}`}>
      {label}
    </span>
  )
}

/* ---------------- Text field ---------------- */
interface FieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string
  icon?: ReactNode
  password?: boolean
  hint?: string
}

export function Field({ label, icon, password, hint, ...props }: FieldProps) {
  const [show, setShow] = useState(false)
  const type = password ? (show ? 'text' : 'password') : props.type ?? 'text'
  return (
    <label className="block">
      <span className="mb-1.5 block text-xs font-semibold text-ink-secondary">{label}</span>
      <div className="flex items-center gap-2 rounded-xl border border-card-border bg-surface px-3 focus-within:border-primary focus-within:ring-2 focus-within:ring-primary/15">
        {icon ? <span className="text-ink-tertiary">{icon}</span> : null}
        <input
          {...props}
          type={type}
          className="min-w-0 flex-1 bg-transparent py-3 text-sm text-ink outline-none placeholder:text-ink-tertiary"
        />
        {password ? (
          <button
            type="button"
            onClick={() => setShow((s) => !s)}
            className="text-ink-tertiary transition hover:text-ink-secondary"
            aria-label={show ? 'Sembunyikan password' : 'Tampilkan password'}
          >
            {show ? <EyeOff className="size-4" /> : <Eye className="size-4" />}
          </button>
        ) : null}
      </div>
      {hint ? <span className="mt-1 block text-[11px] text-ink-tertiary">{hint}</span> : null}
    </label>
  )
}

/* ---------------- Section title ---------------- */
export function SectionTitle({
  children,
  action,
}: {
  children: ReactNode
  action?: ReactNode
}) {
  return (
    <div className="flex items-center justify-between">
      <h2 className="text-sm font-bold text-ink">{children}</h2>
      {action}
    </div>
  )
}
