'use client'

import type { ReactNode } from 'react'
import {
  Home,
  Wallet,
  PlusCircle,
  Megaphone,
  User,
} from 'lucide-react'
import { useStore } from '@/lib/store'
import type { ScreenTab } from '@/lib/types'
import { ToastHost } from './ui'

const NAV: { tab: ScreenTab; label: string; icon: typeof Home }[] = [
  { tab: 'HOME', label: 'Beranda', icon: Home },
  { tab: 'WITHDRAW', label: 'Tarik', icon: Wallet },
  { tab: 'DEPOSIT', label: 'Setor', icon: PlusCircle },
  { tab: 'ANNOUNCEMENTS', label: 'Info', icon: Megaphone },
  { tab: 'PROFILE', label: 'Akun', icon: User },
]

export function PhoneShell({
  children,
  showNav = true,
}: {
  children: ReactNode
  showNav?: boolean
}) {
  return (
    <div className="flex min-h-dvh items-center justify-center bg-gradient-to-br from-[#dbe4ff] via-background to-[#e8fff2] p-0 sm:p-6">
      <div className="relative flex h-dvh w-full max-w-[440px] flex-col overflow-hidden bg-background shadow-2xl sm:h-[900px] sm:max-h-[92vh] sm:rounded-[2.25rem] sm:ring-8 sm:ring-black/80">
        <ToastHost />
        <div className="no-scrollbar flex-1 overflow-y-auto overscroll-contain">
          {children}
        </div>
        {showNav ? <BottomNav /> : null}
      </div>
    </div>
  )
}

function BottomNav() {
  const { state, setTab } = useStore()
  const active = state.activeTab

  return (
    <nav className="relative z-20 flex items-stretch justify-around border-t border-card-border bg-surface/95 px-2 pb-[env(safe-area-inset-bottom)] pt-1.5 backdrop-blur">
      {NAV.map(({ tab, label, icon: Icon }) => {
        const isActive = active === tab
        const isCenter = tab === 'DEPOSIT'
        if (isCenter) {
          return (
            <button
              key={tab}
              onClick={() => setTab(tab)}
              className="relative flex flex-1 flex-col items-center"
              aria-label={label}
            >
              <span
                className={`-mt-6 flex size-14 items-center justify-center rounded-full border-4 border-surface shadow-lg transition ${
                  isActive
                    ? 'bg-gradient-to-br from-gold to-gold-dark'
                    : 'bg-gradient-to-br from-primary to-primary-dark'
                }`}
              >
                <Icon className="size-7 text-white" />
              </span>
              <span
                className={`mt-0.5 text-[10px] font-semibold ${
                  isActive ? 'text-gold-dark' : 'text-primary'
                }`}
              >
                {label}
              </span>
            </button>
          )
        }
        return (
          <button
            key={tab}
            onClick={() => setTab(tab)}
            className="flex flex-1 flex-col items-center gap-0.5 py-1.5"
            aria-label={label}
            aria-current={isActive ? 'page' : undefined}
          >
            <Icon
              className={`size-5 transition ${
                isActive ? 'text-primary' : 'text-ink-tertiary'
              }`}
            />
            <span
              className={`text-[10px] font-medium transition ${
                isActive ? 'text-primary' : 'text-ink-tertiary'
              }`}
            >
              {label}
            </span>
          </button>
        )
      })}
    </nav>
  )
}
