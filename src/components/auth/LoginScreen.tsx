'use client'

import { useState } from 'react'
import { AtSign, Lock, MailCheck, Shield, UserRound, Zap } from 'lucide-react'
import { useStore } from '@/lib/store'
import { Field } from '../ui'
import { GoogleDialog, GOOGLE_LOGO } from './GoogleDialog'

export function LoginScreen() {
  const { login, loginWithGoogle, setAuthState, quickLogin } = useStore()
  const [emailOrPhone, setEmailOrPhone] = useState('budi@gmail.com')
  const [password, setPassword] = useState('budi123')
  const [showGoogle, setShowGoogle] = useState(false)

  function submit(e: React.FormEvent) {
    e.preventDefault()
    login(emailOrPhone, password)
  }

  return (
    <div className="relative min-h-full">
      {showGoogle ? (
        <GoogleDialog
          onClose={() => setShowGoogle(false)}
          onSelect={(email, name) => {
            setShowGoogle(false)
            loginWithGoogle(email, name)
          }}
        />
      ) : null}

      {/* Header */}
      <div className="relative overflow-hidden bg-gradient-to-br from-primary to-primary-dark px-6 pb-14 pt-12 text-white">
        <div className="absolute -right-10 -top-10 size-40 rounded-full bg-white/10" />
        <div className="absolute -bottom-16 -left-8 size-44 rounded-full bg-white/5" />
        <div className="relative flex items-center gap-3">
          <span className="flex size-12 items-center justify-center rounded-2xl bg-white/15 backdrop-blur">
            <MailCheck className="size-7 text-gold-light" />
          </span>
          <div>
            <h1 className="text-xl font-extrabold leading-tight">Setor Gmail Rewards</h1>
            <p className="text-xs text-white/70">Setor Akun, Cairkan Cuan Harian</p>
          </div>
        </div>
        <p className="relative mt-5 max-w-xs text-sm leading-relaxed text-white/80">
          Masuk untuk mulai menyetor akun Gmail terverifikasi dan menarik saldo instan ke
          e-wallet favoritmu.
        </p>
      </div>

      {/* Card */}
      <div className="relative -mt-8 rounded-t-3xl bg-background px-6 pb-10 pt-6">
        <form onSubmit={submit} className="flex flex-col gap-4">
          <Field
            label="Email / Nomor WhatsApp"
            icon={<AtSign className="size-4" />}
            value={emailOrPhone}
            onChange={(e) => setEmailOrPhone(e.target.value)}
            placeholder="budi@gmail.com"
            autoComplete="username"
          />
          <Field
            label="Password"
            icon={<Lock className="size-4" />}
            password
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Masukkan password"
            autoComplete="current-password"
          />

          <div className="flex justify-end">
            <button
              type="button"
              onClick={() => setAuthState('FORGOT_PASSWORD_SCREEN')}
              className="text-xs font-semibold text-primary"
            >
              Lupa Password?
            </button>
          </div>

          <button
            type="submit"
            className="mt-1 h-12 rounded-xl bg-gradient-to-r from-primary to-primary-light text-sm font-bold text-white shadow-lg shadow-primary/25 transition active:scale-[0.99]"
          >
            Masuk Sekarang
          </button>
        </form>

        <div className="my-5 flex items-center gap-3">
          <span className="h-px flex-1 bg-card-border" />
          <span className="text-[11px] font-medium text-ink-tertiary">atau</span>
          <span className="h-px flex-1 bg-card-border" />
        </div>

        <button
          onClick={() => setShowGoogle(true)}
          className="flex h-12 w-full items-center justify-center gap-3 rounded-xl border border-card-border bg-surface text-sm font-bold text-ink shadow-sm transition active:scale-[0.99]"
        >
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img src={GOOGLE_LOGO} alt="" className="size-5" />
          Lanjutkan dengan Google
        </button>

        {/* Quick demo logins */}
        <div className="mt-5 rounded-2xl border border-dashed border-primary/30 bg-primary/5 p-3">
          <p className="mb-2 flex items-center gap-1.5 text-[11px] font-bold text-primary">
            <Zap className="size-3.5" /> Akses Cepat Demo
          </p>
          <div className="flex gap-2">
            <button
              onClick={() => quickLogin(false)}
              className="flex flex-1 items-center justify-center gap-1.5 rounded-lg bg-surface py-2 text-xs font-semibold text-ink shadow-sm"
            >
              <UserRound className="size-3.5 text-emerald" /> Login User
            </button>
            <button
              onClick={() => quickLogin(true)}
              className="flex flex-1 items-center justify-center gap-1.5 rounded-lg bg-surface py-2 text-xs font-semibold text-ink shadow-sm"
            >
              <Shield className="size-3.5 text-gold-dark" /> Login Admin
            </button>
          </div>
        </div>

        <p className="mt-6 text-center text-xs text-ink-secondary">
          Belum punya akun?{' '}
          <button
            onClick={() => setAuthState('REGISTER_SCREEN')}
            className="font-bold text-primary"
          >
            Daftar Gratis
          </button>
        </p>
      </div>
    </div>
  )
}
