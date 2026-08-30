export type Role = 'USER' | 'ADMIN'
export type DepositStatus = 'PENDING' | 'APPROVED' | 'REJECTED'
export type WithdrawalStatus = 'PROCESSED' | 'SUCCESS' | 'REJECTED'
export type AnnouncementCategory =
  | 'RATE_UPDATE'
  | 'MAINTENANCE'
  | 'PROMO_BONUS'
  | 'TIPS'
  | 'GENERAL'

export interface User {
  id: number
  username: string
  email: string
  phone: string
  passwordHash: string
  balance: number
  totalDeposited: number
  validCount: number
  rejectedCount: number
  referralCode: string
  role: Role
  isSoundEnabled: boolean
  isVibrationEnabled: boolean
  joinedAt: number
}

export interface GmailDeposit {
  id: number
  userId: number
  username: string
  email: string
  password: string
  recoveryInfo: string
  accountYear: string
  status: DepositStatus
  rewardAmount: number
  note: string
  rejectReason: string
  submittedAt: number
  reviewedAt: number | null
}

export interface Withdrawal {
  id: number
  userId: number
  username: string
  method: string
  accountNumber: string
  accountHolderName: string
  amount: number
  status: WithdrawalStatus
  adminNote: string
  requestedAt: number
  completedAt: number | null
}

export interface Announcement {
  id: number
  title: string
  content: string
  category: AnnouncementCategory
  isImportant: boolean
  author: string
  dateFormatted: string
  createdAt: number
}

export interface AppConfig {
  isMaintenanceMode: boolean
  maintenanceMessage: string
  currentRatePerAccount: number
  bonusRateTier: number
  minWithdrawalAmount: number
  activeBroadcastBanner: string
  lastUpdated: number
}

export type ScreenTab =
  | 'HOME'
  | 'DEPOSIT'
  | 'WITHDRAW'
  | 'ANNOUNCEMENTS'
  | 'PROFILE'
  | 'ADMIN'

export type AuthState =
  | 'LOGGED_IN'
  | 'LOGIN_SCREEN'
  | 'REGISTER_SCREEN'
  | 'FORGOT_PASSWORD_SCREEN'
