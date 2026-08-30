'use client'

import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useReducer,
  useState,
  type ReactNode,
} from 'react'
import {
  seedAnnouncements,
  seedConfig,
  seedDeposits,
  seedUsers,
  seedWithdrawals,
} from './seed'
import type {
  Announcement,
  AppConfig,
  AuthState,
  GmailDeposit,
  ScreenTab,
  User,
  Withdrawal,
} from './types'

interface State {
  users: User[]
  deposits: GmailDeposit[]
  withdrawals: Withdrawal[]
  announcements: Announcement[]
  config: AppConfig
  currentUserId: number | null
  authState: AuthState
  activeTab: ScreenTab
  nextId: number
}

const SEED_NOW = 1_756_500_000_000 // fixed epoch for deterministic SSR/CSR seed

function createInitialState(): State {
  return {
    users: seedUsers(SEED_NOW),
    deposits: seedDeposits(SEED_NOW),
    withdrawals: seedWithdrawals(SEED_NOW),
    announcements: seedAnnouncements(SEED_NOW),
    config: seedConfig(SEED_NOW),
    currentUserId: null,
    authState: 'LOGIN_SCREEN',
    activeTab: 'HOME',
    nextId: 1000,
  }
}

type Action =
  | { type: 'SET_TAB'; tab: ScreenTab }
  | { type: 'SET_AUTH'; auth: AuthState }
  | { type: 'LOGIN'; userId: number; tab: ScreenTab }
  | { type: 'LOGOUT' }
  | { type: 'ADD_USER'; user: User }
  | { type: 'UPDATE_USER'; user: User }
  | { type: 'ADD_DEPOSITS'; deposits: GmailDeposit[] }
  | { type: 'APPROVE_DEPOSIT'; depositId: number }
  | { type: 'REJECT_DEPOSIT'; depositId: number; reason: string }
  | { type: 'ADD_WITHDRAWAL'; withdrawal: Withdrawal }
  | {
      type: 'UPDATE_WITHDRAWAL'
      id: number
      status: Withdrawal['status']
      note: string
      refund: boolean
    }
  | { type: 'ADD_ANNOUNCEMENT'; announcement: Announcement }
  | { type: 'DELETE_ANNOUNCEMENT'; id: number }
  | { type: 'SET_MAINTENANCE'; enabled: boolean }
  | { type: 'SET_RATE'; rate: number }
  | { type: 'TOGGLE_SOUND'; userId: number; enabled: boolean }
  | { type: 'BUMP_ID'; by: number }

function reducer(state: State, action: Action): State {
  switch (action.type) {
    case 'SET_TAB':
      return { ...state, activeTab: action.tab }
    case 'SET_AUTH':
      return { ...state, authState: action.auth }
    case 'LOGIN':
      return {
        ...state,
        currentUserId: action.userId,
        authState: 'LOGGED_IN',
        activeTab: action.tab,
      }
    case 'LOGOUT':
      return {
        ...state,
        currentUserId: null,
        authState: 'LOGIN_SCREEN',
        activeTab: 'HOME',
      }
    case 'ADD_USER':
      return { ...state, users: [...state.users, action.user] }
    case 'UPDATE_USER':
      return {
        ...state,
        users: state.users.map((u) =>
          u.id === action.user.id ? action.user : u
        ),
      }
    case 'ADD_DEPOSITS':
      return { ...state, deposits: [...action.deposits, ...state.deposits] }
    case 'APPROVE_DEPOSIT': {
      const dep = state.deposits.find((d) => d.id === action.depositId)
      if (!dep) return state
      return {
        ...state,
        deposits: state.deposits.map((d) =>
          d.id === action.depositId
            ? { ...d, status: 'APPROVED', reviewedAt: Date.now(), rejectReason: '' }
            : d
        ),
        users: state.users.map((u) =>
          u.id === dep.userId
            ? {
                ...u,
                balance: u.balance + dep.rewardAmount,
                validCount: u.validCount + 1,
              }
            : u
        ),
      }
    }
    case 'REJECT_DEPOSIT': {
      const dep = state.deposits.find((d) => d.id === action.depositId)
      if (!dep) return state
      return {
        ...state,
        deposits: state.deposits.map((d) =>
          d.id === action.depositId
            ? {
                ...d,
                status: 'REJECTED',
                reviewedAt: Date.now(),
                rejectReason: action.reason,
                rewardAmount: 0,
              }
            : d
        ),
        users: state.users.map((u) =>
          u.id === dep.userId
            ? { ...u, rejectedCount: u.rejectedCount + 1 }
            : u
        ),
      }
    }
    case 'ADD_WITHDRAWAL':
      return {
        ...state,
        withdrawals: [action.withdrawal, ...state.withdrawals],
        users: state.users.map((u) =>
          u.id === action.withdrawal.userId
            ? { ...u, balance: u.balance - action.withdrawal.amount }
            : u
        ),
      }
    case 'UPDATE_WITHDRAWAL': {
      const wd = state.withdrawals.find((w) => w.id === action.id)
      if (!wd) return state
      return {
        ...state,
        withdrawals: state.withdrawals.map((w) =>
          w.id === action.id
            ? {
                ...w,
                status: action.status,
                adminNote: action.note,
                completedAt: Date.now(),
              }
            : w
        ),
        users: action.refund
          ? state.users.map((u) =>
              u.id === wd.userId
                ? { ...u, balance: u.balance + wd.amount }
                : u
            )
          : state.users,
      }
    }
    case 'ADD_ANNOUNCEMENT':
      return {
        ...state,
        announcements: [action.announcement, ...state.announcements],
      }
    case 'DELETE_ANNOUNCEMENT':
      return {
        ...state,
        announcements: state.announcements.filter((a) => a.id !== action.id),
      }
    case 'SET_MAINTENANCE':
      return {
        ...state,
        config: { ...state.config, isMaintenanceMode: action.enabled },
      }
    case 'SET_RATE':
      return {
        ...state,
        config: { ...state.config, currentRatePerAccount: action.rate },
      }
    case 'TOGGLE_SOUND':
      return {
        ...state,
        users: state.users.map((u) =>
          u.id === action.userId ? { ...u, isSoundEnabled: action.enabled } : u
        ),
      }
    case 'BUMP_ID':
      return { ...state, nextId: state.nextId + action.by }
    default:
      return state
  }
}

interface Toast {
  id: number
  message: string
  variant: 'success' | 'error' | 'info'
}

interface StoreValue {
  state: State
  currentUser: User | null
  toasts: Toast[]
  dismissToast: (id: number) => void
  // navigation
  setTab: (tab: ScreenTab) => void
  setAuthState: (auth: AuthState) => void
  // auth
  login: (emailOrPhone: string, password: string) => boolean
  loginWithGoogle: (email: string, displayName: string) => void
  register: (args: {
    username: string
    email: string
    phone: string
    password: string
    referralCode: string
  }) => void
  resetPassword: (emailOrPhone: string, newPass: string) => boolean
  quickLogin: (asAdmin: boolean) => void
  logout: () => void
  switchRoleToAdmin: () => void
  switchRoleToUser: () => void
  // submissions
  submitSingleDeposit: (args: {
    email: string
    password: string
    recovery: string
    year: string
    note: string
  }) => void
  submitBulkDeposits: (
    entries: { email: string; password: string; recovery: string }[]
  ) => void
  requestWithdrawal: (args: {
    method: string
    accountNumber: string
    accountName: string
    amount: number
  }) => boolean
  // admin
  approveDeposit: (deposit: GmailDeposit) => void
  rejectDeposit: (deposit: GmailDeposit, reason: string) => void
  completeWithdrawal: (withdrawal: Withdrawal, note?: string) => void
  rejectWithdrawal: (withdrawal: Withdrawal, reason: string) => void
  toggleMaintenanceMode: (enabled: boolean) => void
  updateRate: (rate: number) => void
  createAnnouncement: (args: {
    title: string
    content: string
    category: Announcement['category']
    isImportant: boolean
  }) => void
  deleteAnnouncement: (id: number) => void
  toggleSound: () => void
}

const StoreContext = createContext<StoreValue | null>(null)

export function StoreProvider({ children }: { children: ReactNode }) {
  const [state, dispatch] = useReducer(reducer, undefined, createInitialState)
  const [toasts, setToasts] = useState<Toast[]>([])
  const idRef = useMemo(() => ({ current: state.nextId }), [])

  const currentUser =
    state.users.find((u) => u.id === state.currentUserId) ?? null

  const notify = useCallback(
    (message: string, variant: Toast['variant'] = 'info') => {
      const id = Date.now() + Math.random()
      setToasts((prev) => [...prev, { id, message, variant }])
      setTimeout(() => {
        setToasts((prev) => prev.filter((t) => t.id !== id))
      }, 3600)
    },
    []
  )

  const dismissToast = useCallback((id: number) => {
    setToasts((prev) => prev.filter((t) => t.id !== id))
  }, [])

  const genId = useCallback(() => {
    idRef.current += 1
    return idRef.current
  }, [idRef])

  const setTab = useCallback((tab: ScreenTab) => dispatch({ type: 'SET_TAB', tab }), [])
  const setAuthState = useCallback(
    (auth: AuthState) => dispatch({ type: 'SET_AUTH', auth }),
    []
  )

  const findUser = useCallback(
    (query: string) => {
      const q = query.trim().toLowerCase()
      return state.users.find(
        (u) => u.email.toLowerCase() === q || u.phone === query.trim()
      )
    },
    [state.users]
  )

  const login = useCallback(
    (emailOrPhone: string, password: string): boolean => {
      const user = findUser(emailOrPhone)
      if (user && user.passwordHash === password.trim()) {
        dispatch({
          type: 'LOGIN',
          userId: user.id,
          tab: user.role === 'ADMIN' ? 'ADMIN' : 'HOME',
        })
        notify(`Selamat datang kembali, ${user.username}!`, 'success')
        return true
      }
      notify('Email / Nomor WhatsApp atau Password salah!', 'error')
      return false
    },
    [findUser, notify]
  )

  const loginWithGoogle = useCallback(
    (email: string, displayName: string) => {
      const cleanEmail = email.trim().toLowerCase()
      const isAdminEmail =
        cleanEmail.startsWith('admin') || cleanEmail === 'j14ngyanghong@gmail.com'
      const existing = findUser(cleanEmail)
      if (existing) {
        if (isAdminEmail && existing.role !== 'ADMIN') {
          dispatch({ type: 'UPDATE_USER', user: { ...existing, role: 'ADMIN' } })
        }
        dispatch({
          type: 'LOGIN',
          userId: existing.id,
          tab: existing.role === 'ADMIN' || isAdminEmail ? 'ADMIN' : 'HOME',
        })
        notify(`Berhasil masuk via Google (${existing.email})!`, 'success')
      } else {
        const name =
          displayName.trim() ||
          cleanEmail.split('@')[0].replace(/\./g, ' ')
        const id = genId()
        const newUser: User = {
          id,
          username: name,
          email: cleanEmail,
          phone: '',
          passwordHash: 'GOOGLE_AUTH',
          balance: 500,
          totalDeposited: 0,
          validCount: 0,
          rejectedCount: 0,
          referralCode: `GGL${Math.floor(1000 + Math.random() * 9000)}`,
          role: isAdminEmail ? 'ADMIN' : 'USER',
          isSoundEnabled: true,
          isVibrationEnabled: true,
          joinedAt: Date.now(),
        }
        dispatch({ type: 'ADD_USER', user: newUser })
        dispatch({
          type: 'LOGIN',
          userId: id,
          tab: newUser.role === 'ADMIN' ? 'ADMIN' : 'HOME',
        })
        notify(
          `Selamat datang ${name}! Akun Google terhubung (+ Bonus Rp 500)!`,
          'success'
        )
      }
    },
    [findUser, genId, notify]
  )

  const register = useCallback(
    (args: {
      username: string
      email: string
      phone: string
      password: string
      referralCode: string
    }) => {
      const existing = findUser(args.email) ?? findUser(args.phone)
      if (existing) {
        notify('Email atau Nomor Telepon sudah terdaftar!', 'error')
        return
      }
      const id = genId()
      const newUser: User = {
        id,
        username: args.username.trim() || `Mitra_${id}`,
        email: args.email.trim(),
        phone: args.phone.trim(),
        passwordHash: args.password.trim(),
        balance: args.referralCode.trim() ? 500 : 0,
        totalDeposited: 0,
        validCount: 0,
        rejectedCount: 0,
        referralCode: `REF${Math.floor(1000 + Math.random() * 9000)}`,
        role: 'USER',
        isSoundEnabled: true,
        isVibrationEnabled: true,
        joinedAt: Date.now(),
      }
      dispatch({ type: 'ADD_USER', user: newUser })
      dispatch({ type: 'LOGIN', userId: id, tab: 'HOME' })
      const bonus = args.referralCode.trim() ? ' + Bonus Referral Rp 500!' : ''
      notify(
        `Pendaftaran Berhasil! Selamat datang di Setor Gmail Rewards${bonus}`,
        'success'
      )
    },
    [findUser, genId, notify]
  )

  const resetPassword = useCallback(
    (emailOrPhone: string, newPass: string): boolean => {
      const user = findUser(emailOrPhone)
      if (user) {
        dispatch({
          type: 'UPDATE_USER',
          user: { ...user, passwordHash: newPass.trim() },
        })
        notify(
          'Password berhasil direset! Silakan login dengan password baru.',
          'success'
        )
        dispatch({ type: 'SET_AUTH', auth: 'LOGIN_SCREEN' })
        return true
      }
      notify('Akun dengan email/nomor tersebut tidak ditemukan.', 'error')
      return false
    },
    [findUser, notify]
  )

  const quickLogin = useCallback(
    (asAdmin: boolean) => {
      if (asAdmin) {
        dispatch({ type: 'LOGIN', userId: 1, tab: 'ADMIN' })
        notify('Masuk sebagai Administrator (admin_master)', 'info')
      } else {
        dispatch({ type: 'LOGIN', userId: 2, tab: 'HOME' })
        notify('Masuk sebagai Pengguna (budi_santoso)', 'info')
      }
    },
    [notify]
  )

  const logout = useCallback(() => {
    dispatch({ type: 'LOGOUT' })
    notify('Anda telah keluar dari akun.', 'info')
  }, [notify])

  const switchRoleToAdmin = useCallback(() => {
    dispatch({ type: 'LOGIN', userId: 1, tab: 'ADMIN' })
    notify('Beralih ke Panel Administrator', 'info')
  }, [notify])

  const switchRoleToUser = useCallback(() => {
    dispatch({ type: 'LOGIN', userId: 2, tab: 'HOME' })
    notify('Beralih ke Mode Pengguna', 'info')
  }, [notify])

  const submitSingleDeposit = useCallback(
    (args: {
      email: string
      password: string
      recovery: string
      year: string
      note: string
    }) => {
      if (!currentUser) return
      const deposit: GmailDeposit = {
        id: genId(),
        userId: currentUser.id,
        username: currentUser.username,
        email: args.email.trim(),
        password: args.password.trim(),
        recoveryInfo: args.recovery.trim(),
        accountYear: args.year,
        status: 'PENDING',
        rewardAmount: state.config.currentRatePerAccount,
        note: args.note.trim(),
        rejectReason: '',
        submittedAt: Date.now(),
        reviewedAt: null,
      }
      dispatch({ type: 'ADD_DEPOSITS', deposits: [deposit] })
      notify(
        `Akun Gmail ${args.email} berhasil disetor! Menunggu verifikasi tim.`,
        'success'
      )
      dispatch({ type: 'SET_TAB', tab: 'HOME' })
    },
    [currentUser, genId, notify, state.config.currentRatePerAccount]
  )

  const submitBulkDeposits = useCallback(
    (entries: { email: string; password: string; recovery: string }[]) => {
      if (!currentUser || entries.length === 0) return
      const deposits: GmailDeposit[] = entries.map((e) => ({
        id: genId(),
        userId: currentUser.id,
        username: currentUser.username,
        email: e.email.trim(),
        password: e.password.trim(),
        recoveryInfo: e.recovery.trim(),
        accountYear: '2024',
        status: 'PENDING',
        rewardAmount: state.config.currentRatePerAccount,
        note: 'Setoran massal',
        rejectReason: '',
        submittedAt: Date.now(),
        reviewedAt: null,
      }))
      dispatch({ type: 'ADD_DEPOSITS', deposits })
      notify(
        `${deposits.length} Akun Gmail berhasil disetor sekaligus!`,
        'success'
      )
      dispatch({ type: 'SET_TAB', tab: 'HOME' })
    },
    [currentUser, genId, notify, state.config.currentRatePerAccount]
  )

  const requestWithdrawal = useCallback(
    (args: {
      method: string
      accountNumber: string
      accountName: string
      amount: number
    }): boolean => {
      if (!currentUser) return false
      const min = state.config.minWithdrawalAmount
      if (args.amount < min) {
        notify(`Minimal penarikan saldo adalah Rp ${min.toLocaleString('id-ID')}`, 'error')
        return false
      }
      if (args.amount > currentUser.balance) {
        notify('Saldo tidak mencukupi untuk melakukan penarikan ini.', 'error')
        return false
      }
      const withdrawal: Withdrawal = {
        id: genId(),
        userId: currentUser.id,
        username: currentUser.username,
        method: args.method,
        accountNumber: args.accountNumber.trim(),
        accountHolderName: args.accountName.trim(),
        amount: args.amount,
        status: 'PROCESSED',
        adminNote: 'Permintaan penarikan sedang diproses sistem otomatis',
        requestedAt: Date.now(),
        completedAt: null,
      }
      dispatch({ type: 'ADD_WITHDRAWAL', withdrawal })
      notify(
        `Permintaan penarikan Rp ${args.amount.toLocaleString('id-ID')} ke ${args.method} berhasil diajukan!`,
        'success'
      )
      return true
    },
    [currentUser, notify, state.config.minWithdrawalAmount]
  )

  const approveDeposit = useCallback(
    (deposit: GmailDeposit) => {
      dispatch({ type: 'APPROVE_DEPOSIT', depositId: deposit.id })
      notify(
        `Setoran ${deposit.email} DISETUJUI! Reward Rp ${deposit.rewardAmount.toLocaleString('id-ID')} ditambahkan.`,
        'success'
      )
    },
    [notify]
  )

  const rejectDeposit = useCallback(
    (deposit: GmailDeposit, reason: string) => {
      const actual = reason.trim() || 'Password tidak cocok / 2FA aktif'
      dispatch({ type: 'REJECT_DEPOSIT', depositId: deposit.id, reason: actual })
      notify(`Setoran ${deposit.email} DITOLAK (${actual})`, 'error')
    },
    [notify]
  )

  const completeWithdrawal = useCallback(
    (withdrawal: Withdrawal, note = 'Transfer Berhasil') => {
      dispatch({
        type: 'UPDATE_WITHDRAWAL',
        id: withdrawal.id,
        status: 'SUCCESS',
        note,
        refund: false,
      })
      notify(
        `Penarikan Rp ${withdrawal.amount.toLocaleString('id-ID')} ke ${withdrawal.method} dinyatakan SUKSES.`,
        'success'
      )
    },
    [notify]
  )

  const rejectWithdrawal = useCallback(
    (withdrawal: Withdrawal, reason: string) => {
      dispatch({
        type: 'UPDATE_WITHDRAWAL',
        id: withdrawal.id,
        status: 'REJECTED',
        note: reason,
        refund: true,
      })
      notify(
        `Penarikan DITOLAK. Saldo Rp ${withdrawal.amount.toLocaleString('id-ID')} dikembalikan ke user.`,
        'error'
      )
    },
    [notify]
  )

  const toggleMaintenanceMode = useCallback(
    (enabled: boolean) => {
      dispatch({ type: 'SET_MAINTENANCE', enabled })
      notify(
        enabled
          ? 'Mode Maintenance Server DIAKTIFKAN'
          : 'Mode Maintenance Server DINONAKTIFKAN (Normal)',
        'info'
      )
    },
    [notify]
  )

  const updateRate = useCallback(
    (rate: number) => {
      dispatch({ type: 'SET_RATE', rate })
      notify(
        `Rate reward per akun diperbarui menjadi Rp ${rate.toLocaleString('id-ID')}`,
        'success'
      )
    },
    [notify]
  )

  const createAnnouncement = useCallback(
    (args: {
      title: string
      content: string
      category: Announcement['category']
      isImportant: boolean
    }) => {
      const now = Date.now()
      const announcement: Announcement = {
        id: genId(),
        title: args.title.trim(),
        content: args.content.trim(),
        category: args.category,
        isImportant: args.isImportant,
        author: 'Admin Official',
        dateFormatted: new Intl.DateTimeFormat('id-ID', {
          day: '2-digit',
          month: 'short',
          year: 'numeric',
          hour: '2-digit',
          minute: '2-digit',
        }).format(new Date(now)),
        createdAt: now,
      }
      dispatch({ type: 'ADD_ANNOUNCEMENT', announcement })
      notify('Pengumuman publik berhasil dipublikasikan!', 'success')
    },
    [genId, notify]
  )

  const deleteAnnouncement = useCallback(
    (id: number) => {
      dispatch({ type: 'DELETE_ANNOUNCEMENT', id })
      notify('Pengumuman berhasil dihapus.', 'info')
    },
    [notify]
  )

  const toggleSound = useCallback(() => {
    if (!currentUser) return
    const enabled = !currentUser.isSoundEnabled
    dispatch({ type: 'TOGGLE_SOUND', userId: currentUser.id, enabled })
    notify(
      enabled ? 'Suara notifikasi diaktifkan' : 'Suara notifikasi dinonaktifkan',
      'info'
    )
  }, [currentUser, notify])

  const value: StoreValue = {
    state,
    currentUser,
    toasts,
    dismissToast,
    setTab,
    setAuthState,
    login,
    loginWithGoogle,
    register,
    resetPassword,
    quickLogin,
    logout,
    switchRoleToAdmin,
    switchRoleToUser,
    submitSingleDeposit,
    submitBulkDeposits,
    requestWithdrawal,
    approveDeposit,
    rejectDeposit,
    completeWithdrawal,
    rejectWithdrawal,
    toggleMaintenanceMode,
    updateRate,
    createAnnouncement,
    deleteAnnouncement,
    toggleSound,
  }

  return <StoreContext.Provider value={value}>{children}</StoreContext.Provider>
}

export function useStore() {
  const ctx = useContext(StoreContext)
  if (!ctx) throw new Error('useStore must be used within StoreProvider')
  return ctx
}
