'use client'

import { useStore } from '@/lib/store'
import { PhoneShell } from './PhoneShell'
import { LoginScreen } from './auth/LoginScreen'
import { RegisterScreen } from './auth/RegisterScreen'
import { ForgotPasswordScreen } from './auth/ForgotPasswordScreen'
import { DashboardScreen } from './user/DashboardScreen'
import { DepositScreen } from './user/DepositScreen'
import { WithdrawScreen } from './user/WithdrawScreen'
import { AnnouncementsScreen } from './user/AnnouncementsScreen'
import { ProfileScreen } from './user/ProfileScreen'
import { AdminDashboard } from './admin/AdminDashboard'
import { MaintenanceScreen } from './MaintenanceScreen'

export function AppRoot() {
  const { state, currentUser } = useStore()

  // Auth flow (no shell / nav)
  if (state.authState !== 'LOGGED_IN' || !currentUser) {
    if (state.authState === 'REGISTER_SCREEN') {
      return (
        <PhoneShell showNav={false}>
          <RegisterScreen />
        </PhoneShell>
      )
    }
    if (state.authState === 'FORGOT_PASSWORD_SCREEN') {
      return (
        <PhoneShell showNav={false}>
          <ForgotPasswordScreen />
        </PhoneShell>
      )
    }
    return (
      <PhoneShell showNav={false}>
        <LoginScreen />
      </PhoneShell>
    )
  }

  // Maintenance mode blocks regular users
  if (state.config.isMaintenanceMode && currentUser.role !== 'ADMIN') {
    return (
      <PhoneShell showNav={false}>
        <MaintenanceScreen />
      </PhoneShell>
    )
  }

  // Admin panel has its own layout
  if (state.activeTab === 'ADMIN' && currentUser.role === 'ADMIN') {
    return (
      <PhoneShell showNav={false}>
        <AdminDashboard />
      </PhoneShell>
    )
  }

  return (
    <PhoneShell>
      {state.activeTab === 'HOME' && <DashboardScreen />}
      {state.activeTab === 'DEPOSIT' && <DepositScreen />}
      {state.activeTab === 'WITHDRAW' && <WithdrawScreen />}
      {state.activeTab === 'ANNOUNCEMENTS' && <AnnouncementsScreen />}
      {state.activeTab === 'PROFILE' && <ProfileScreen />}
    </PhoneShell>
  )
}
