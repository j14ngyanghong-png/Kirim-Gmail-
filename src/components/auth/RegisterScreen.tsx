'use client'

import { useState } from 'react'
import { ArrowLeft, AtSign, Gift, Lock, Phone, UserRound } from 'lucide-react'
import { useStore } from '@/lib/store'
import { Field } from '../ui'
import { GoogleDialog, GOOGLE_LOGO } from './GoogleDialog'

export function RegisterScreen() {
  const { register, loginWithGoogle, setAuthState } = useStore()
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [password, setPassword] = useState('')
  const [referral, setReferral] = useState('')
  const [showGoogle, setShowGoogle] = useState(false)

  function submit(e: React.FormEvent) {
    e.preventDefault()
    register({ username, email, phone, password, referralCode: referral })
  }

  return (
    <div className="relative min-h-full">
      {showGoogle ? (
        <GoogleDialog
          onClose={() => setShowGoogle(false)}
          onSelect={(em, name) => {
            setShowGoogle(false)
            loginWithGoogle(em, name)
          }}
        />
      ) : null}

      <div className="relative overflow-hidden bg-gradient-to-br from-emerald-dark to-emerald px-6 pb-12 pt-10 text-white">
        <div className="absolute -right-12 -top-8 size-40 rounded-full bg-white/10" />
        <button
          onClick={() => setAuthState('LOGIN_SCREEN')}
          className="relative mb-4 flex size-9 items-center justify-center rounded-full bg-white/15"
          aria-label="Kembali"
        >
          <ArrowLeft className="size-5" />
        </button>
        <h1 className="relative text-2xl font-extrabold">Daftar Akun Baru</h1>
        <p className="relative mt-1 text-sm text-white/80">
          Gabung jadi Mitra & dapatkan bonus Rp 500 dengan kode referral.
        </p>
      </div>

      <div className="relative -mt-6 rounded-t-3xl bg-background px-6 pb-10 pt-6">
        <form onSubmit={submit} className="flex flex-col gap-3.5">
          <Field
            label="Nama Lengkap / Panggilan"
            icon={<UserRound className="size-4" />}
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="Budi Santoso"
          />
          <Field
            label="Email"
            icon={<AtSign className="size-4" />}
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="budi@gmail.com"
          />
          <Field
            label="Nomor WhatsApp"
            icon={<Phone className="size-4" />}
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            placeholder="0857xxxxxxx"
            inputMode="numeric"
          />
          <Field
            label="Password"
            icon={<Lock className="size-4" />}
            password
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Minimal 6 karakter"
          />
          <Field
            label="Kode Referral (Opsional)"
            icon={<Gift className="size-4" />}
            value={referral}
            onChange={(e) => setReferral(e.target.value.toUpperCase())}
            placeholder="BUDI2024"
            hint="Masukkan kode referral untuk bonus saldo Rp 500."
          />

          <button
            type="submit"
            className="mt-2 h-12 rounded-xl bg-gradient-to-r from-emerald to-emerald-dark text-sm font-bold text-white shadow-lg shadow-emerald/25 transition active:scale-[0.99]"
          >
            Daftar Sekarang
          </button>
        </form>

        <div className="my-5 flex items-center gap-3">
          <span className="h-px flex-1 bg-card-border" />
          <span className="text-[11px] font-medium text-ink-tertiary">atau</span>
          <span className="h-px flex-1 bg-card-border" />
        </div>

        <button
          onClick={() => setShowGoogle(true)}
          className="flex h-12 w-full items-center justify-center gap-3 rounded-xl border border-card-border bg-surface text-sm font-bold text-ink shadow-sm"
        >
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img src={GOOGLE_LOGO} alt="" className="size-5" />
          Daftar cepat dengan Google
        </button>

        <p className="mt-6 text-center text-xs text-ink-secondary">
          Sudah punya akun?{' '}
          <button
            onClick={() => setAuthState('LOGIN_SCREEN')}
            className="font-bold text-primary"
          >
            Masuk di sini
          </button>
        </p>
      </div>
    </div>
  )
}
