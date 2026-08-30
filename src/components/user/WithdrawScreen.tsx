"use client"

import { useState } from "react"
import { useApp } from "@/lib/store"
import { formatRupiah, formatDateTime } from "@/lib/format"
import { SectionHeader, EmptyState, StatusBadge } from "@/components/ui"

type Method = { name: string; category: string; color: string }

const METHODS: Method[] = [
  { name: "DANA", category: "E-Wallet", color: "#108EE9" },
  { name: "OVO", category: "E-Wallet", color: "#4C2A86" },
  { name: "GoPay", category: "E-Wallet", color: "#00AED6" },
  { name: "ShopeePay", category: "E-Wallet", color: "#EE4D2D" },
  { name: "Bank BCA", category: "Transfer Bank", color: "#005EAA" },
  { name: "Bank BRI", category: "Transfer Bank", color: "#00529C" },
  { name: "Bank Mandiri", category: "Transfer Bank", color: "#003087" },
  { name: "QRIS Instant", category: "Scan Pay", color: "#E11938" },
]

export function WithdrawScreen() {
  const { state, requestWithdrawal } = useApp()
  const user = state.currentUser
  const balance = user?.balance ?? 0
  const minWithdrawal = state.config.minWithdrawalAmount

  const [selectedMethod, setSelectedMethod] = useState("DANA")
  const [accountNumber, setAccountNumber] = useState(user?.phone ?? "")
  const [accountName, setAccountName] = useState(user?.username ?? "")
  const [amountInput, setAmountInput] = useState("")
  const [error, setError] = useState<string | null>(null)

  const withdrawals = state.withdrawals.filter((w) => w.userId === user?.id)
  const progress = Math.min(1, Math.max(0, balance / minWithdrawal))

  function submit() {
    const amount = Number.parseInt(amountInput || "0", 10)
    if (!accountNumber.trim() || !accountName.trim() || amount <= 0) {
      setError("Harap isi semua data penarikan dengan benar!")
    } else if (amount < minWithdrawal) {
      setError(`Minimal penarikan adalah ${formatRupiah(minWithdrawal)}`)
    } else if (amount > balance) {
      setError("Saldo tidak cukup untuk penarikan ini!")
    } else {
      setError(null)
      requestWithdrawal(selectedMethod, accountNumber, accountName, amount)
      setAmountInput("")
    }
  }

  return (
    <div className="flex flex-col gap-4 px-4 pt-2 pb-6">
      <div>
        <h1 className="text-xl font-bold text-foreground text-balance">Tarik Saldo Reward</h1>
        <p className="text-xs text-muted-foreground text-pretty">
          Cairkan saldo reward Anda langsung ke E-Wallet atau Rekening Bank bebas biaya admin.
        </p>
      </div>

      {/* Balance & progress */}
      <div
        className="rounded-3xl p-5 text-white shadow-lg"
        style={{ background: "linear-gradient(135deg, var(--emerald-dark), #0A3B1B)" }}
      >
        <div className="flex items-center justify-between">
          <div>
            <p className="text-xs text-white/80">Saldo Tersedia Ditarik</p>
            <p className="mt-1 text-2xl font-extrabold">{formatRupiah(balance)}</p>
          </div>
          <div className="flex h-11 w-11 items-center justify-center rounded-full bg-white/15">
            <PaymentsIcon />
          </div>
        </div>
        <div className="mt-4">
          <div className="flex items-center justify-between text-[11px]">
            <span className="text-white/80">Syarat Min. Penarikan</span>
            <span className="font-bold">
              {formatRupiah(balance)} / {formatRupiah(minWithdrawal)}
            </span>
          </div>
          <div className="mt-1.5 h-2 w-full overflow-hidden rounded-full bg-white/20">
            <div className="h-full rounded-full" style={{ width: `${progress * 100}%`, background: "var(--gold)" }} />
          </div>
        </div>
      </div>

      {/* Payment methods */}
      <div>
        <SectionHeader title="Pilih Metode Pembayaran" />
        <div className="-mx-4 flex gap-2 overflow-x-auto px-4 pb-1">
          {METHODS.map((m) => {
            const selected = selectedMethod === m.name
            return (
              <button
                key={m.name}
                onClick={() => setSelectedMethod(m.name)}
                className={`flex shrink-0 items-center gap-2 rounded-2xl border px-3.5 py-2.5 text-left transition-colors ${
                  selected ? "border-primary bg-primary/10" : "border-border bg-card"
                }`}
              >
                <span
                  className="flex h-7 w-7 items-center justify-center rounded-full"
                  style={{ backgroundColor: `${m.color}26`, color: m.color }}
                >
                  <DeviceIcon />
                </span>
                <span>
                  <span className="block text-[13px] font-bold text-foreground">{m.name}</span>
                  <span className="block text-[10px] text-muted-foreground">{m.category}</span>
                </span>
              </button>
            )
          })}
        </div>
      </div>

      {/* Form */}
      <div className="rounded-3xl border border-border bg-card p-5">
        <h2 className="text-sm font-bold text-foreground">Detail Rekening Tujuan ({selectedMethod})</h2>
        <div className="mt-3 flex flex-col gap-2.5">
          <Field
            label="Nomor HP / Nomor Rekening"
            placeholder="cth: 085712345678"
            value={accountNumber}
            onChange={(v) => setAccountNumber(v.replace(/\D/g, ""))}
            inputMode="numeric"
          />
          <Field
            label="Nama Pemilik Rekening / Akun"
            placeholder="Nama sesuai KTP / Akun E-Wallet"
            value={accountName}
            onChange={setAccountName}
          />
          <Field
            label="Nominal Penarikan (Rp)"
            placeholder="Min. 25000"
            value={amountInput}
            onChange={(v) => setAmountInput(v.replace(/\D/g, ""))}
            inputMode="numeric"
          />
        </div>

        <p className="mt-3 text-[11px] text-muted-foreground">Pilih Nominal Cepat:</p>
        <div className="mt-1.5 flex flex-wrap gap-1.5">
          {[25000, 50000, 100000].map((c) => (
            <Chip key={c} active={amountInput === String(c)} onClick={() => setAmountInput(String(c))}>
              {formatRupiah(c)}
            </Chip>
          ))}
          {balance >= minWithdrawal && (
            <Chip active={amountInput === String(balance)} onClick={() => setAmountInput(String(balance))} emerald>
              Tarik Semua
            </Chip>
          )}
        </div>

        {error && <p className="mt-2 text-xs text-destructive">{error}</p>}

        <button
          onClick={submit}
          disabled={balance < minWithdrawal}
          className="mt-4 flex h-12 w-full items-center justify-center gap-2 rounded-xl bg-[var(--emerald)] text-[15px] font-bold text-white transition-opacity disabled:opacity-50"
        >
          <SendIcon />
          Ajukan Penarikan Saldo
        </button>
      </div>

      {/* History */}
      <SectionHeader
        title="Riwayat Penarikan Saldo"
        subtitle="Daftar pencairan dana ke e-wallet & rekening"
      />
      {withdrawals.length === 0 ? (
        <EmptyState
          title="Belum Ada Riwayat Penarikan"
          description="Setor akun Gmail untuk mengumpulkan saldo, lalu tarik ke rekening Anda di sini."
        />
      ) : (
        <div className="flex flex-col gap-2.5">
          {withdrawals.map((w) => (
            <div key={w.id} className="rounded-2xl border border-border bg-card p-4">
              <div className="flex items-start justify-between">
                <div>
                  <p className="text-sm font-bold text-foreground">{formatRupiah(w.amount)}</p>
                  <p className="text-xs text-muted-foreground">
                    {w.method} - {w.accountName}
                  </p>
                  <p className="text-[11px] text-muted-foreground">{w.accountNumber}</p>
                </div>
                <StatusBadge status={w.status} />
              </div>
              <p className="mt-2 text-[11px] text-muted-foreground">{formatDateTime(w.requestedAt)}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

function Field({
  label,
  placeholder,
  value,
  onChange,
  inputMode,
}: {
  label: string
  placeholder: string
  value: string
  onChange: (v: string) => void
  inputMode?: "numeric" | "text"
}) {
  return (
    <label className="flex flex-col gap-1">
      <span className="text-[11px] font-medium text-muted-foreground">{label}</span>
      <input
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        inputMode={inputMode}
        className="h-11 rounded-xl border border-border bg-background px-3 text-sm text-foreground outline-none transition-colors placeholder:text-muted-foreground/60 focus:border-primary"
      />
    </label>
  )
}

function Chip({
  children,
  active,
  onClick,
  emerald,
}: {
  children: React.ReactNode
  active: boolean
  onClick: () => void
  emerald?: boolean
}) {
  const base = "rounded-full border px-3 py-1.5 text-[11px] font-semibold transition-colors"
  if (active) {
    return (
      <button
        onClick={onClick}
        className={`${base} ${emerald ? "border-[var(--emerald)] bg-[var(--emerald)] text-white" : "border-primary bg-primary text-primary-foreground"}`}
      >
        {children}
      </button>
    )
  }
  return (
    <button onClick={onClick} className={`${base} border-border bg-card text-foreground`}>
      {children}
    </button>
  )
}

function PaymentsIcon() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--emerald-light)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <rect x="2" y="6" width="20" height="12" rx="2" />
      <circle cx="12" cy="12" r="2" />
      <path d="M6 12h.01M18 12h.01" />
    </svg>
  )
}

function DeviceIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <rect x="5" y="2" width="14" height="20" rx="2" />
      <path d="M12 18h.01" />
    </svg>
  )
}

function SendIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="m22 2-7 20-4-9-9-4Z" />
      <path d="M22 2 11 13" />
    </svg>
  )
}
