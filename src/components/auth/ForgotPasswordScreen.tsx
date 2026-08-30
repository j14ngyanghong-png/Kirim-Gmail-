'use client'

import { useState } from 'react'
import { ArrowLeft, AtSign, KeyRound, Lock } from 'lucide-react'
import { useStore } from '@/lib/store'
import { Field } from '../ui'

export function ForgotPasswordScreen() {
  const { resetPassword, setAuthState } = useStore()
  const [emailOrPhone, setEmailOrPhone] = useState('')
  const [newPass, setNewPass] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState<string | null>(null)

  function submit(e: React.FormEvent) {
    e.preventDefault()
    if (newPass.length < 6) {
      setError('Password baru minimal 6 karakter.')
      return
    }
    if (newPass !== confirm) {
      setError('Konfirmasi password tidak cocok.')
      return
    }
    setError(null)
    resetPassword(emailOrPhone, newPass)
  }

  return (
    <div className="min-h-full">
      <div className="relative overflow-hidden bg-gradient-to-br from-primary-dark to-primary px-6 pb-12 pt-10 text-white">
        <div className="absolute -right-12 -top-8 size-40 rounded-full bg-white/10" />
        <button
          onClick={() => setAuthState('LOGIN_SCREEN')}
          className="relative mb-4 flex size-9 items-center justify-center rounded-full bg-white/15"
          aria-label="Kembali"
        >
          <ArrowLeft className="size-5" />
        </button>
        <span className="relative flex size-12 items-center justify-center rounded-2xl bg-white/15">
          <KeyRound className="size-6 text-gold-light" />
        </span>
        <h1 className="relative mt-3 text-2xl font-extrabold">Reset Password</h1>
        <p className="relative mt-1 text-sm text-white/80">
          Masukkan email/nomor terdaftar dan buat password baru Anda.
        </p>
      </div>

      <div className="relative -mt-6 rounded-t-3xl bg-background px-6 pb-10 pt-6">
        <form onSubmit={submit} className="flex flex-col gap-3.5">
          <Field
            label="Email / Nomor WhatsApp Terdaftar"
            icon={<AtSign className="size-4" />}
            value={emailOrPhone}
            onChange={(e) => setEmailOrPhone(e.target.value)}
            placeholder="budi@gmail.com"
          />
          <Field
            label="Password Baru"
            icon={<Lock className="size-4" />}
            password
            value={newPass}
            onChange={(e) => setNewPass(e.target.value)}
            placeholder="Minimal 6 karakter"
          />
          <Field
            label="Konfirmasi Password Baru"
            icon={<Lock className="size-4" />}
            password
            value={confirm}
            onChange={(e) => setConfirm(e.target.value)}
            placeholder="Ulangi password baru"
          />
          {error ? <p className="text-xs font-medium text-coral">{error}</p> : null}

          <button
            type="submit"
            className="mt-2 h-12 rounded-xl bg-gradient-to-r from-primary to-primary-light text-sm font-bold text-white shadow-lg shadow-primary/25 transition active:scale-[0.99]"
          >
            Simpan Password Baru
          </button>
        </form>

        <p className="mt-6 text-center text-xs text-ink-secondary">
          Ingat password Anda?{' '}
          <button
            onClick={() => setAuthState('LOGIN_SCREEN')}
            className="font-bold text-primary"
          >
            Kembali ke Login
          </button>
        </p>
      </div>
    </div>
  )
}
