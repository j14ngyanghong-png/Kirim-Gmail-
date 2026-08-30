import type {
  Announcement,
  AppConfig,
  GmailDeposit,
  User,
  Withdrawal,
} from './types'

const HOUR = 1000 * 60 * 60
const MIN = 1000 * 60

export function seedUsers(now: number): User[] {
  return [
    {
      id: 1,
      username: 'admin_master',
      email: 'admin@rewards.id',
      phone: '081299887766',
      passwordHash: 'admin123',
      balance: 1_500_000,
      totalDeposited: 0,
      validCount: 0,
      rejectedCount: 0,
      referralCode: 'ADMINVIP',
      role: 'ADMIN',
      isSoundEnabled: true,
      isVibrationEnabled: true,
      joinedAt: now - HOUR * 24 * 120,
    },
    {
      id: 2,
      username: 'budi_santoso',
      email: 'budi@gmail.com',
      phone: '085712345678',
      passwordHash: 'budi123',
      balance: 47_500,
      totalDeposited: 24,
      validCount: 19,
      rejectedCount: 2,
      referralCode: 'BUDI2024',
      role: 'USER',
      isSoundEnabled: true,
      isVibrationEnabled: true,
      joinedAt: now - HOUR * 24 * 45,
    },
  ]
}

export function seedConfig(now: number): AppConfig {
  return {
    isMaintenanceMode: false,
    maintenanceMessage:
      'Server sedang dalam pemeliharaan berkala untuk peningkatan kecepatan verifikasi akun. Silakan coba kembali dalam beberapa saat.',
    currentRatePerAccount: 2500,
    bonusRateTier: 500,
    minWithdrawalAmount: 25_000,
    activeBroadcastBanner:
      '⚡ Rate Spesial Hari Ini: Rp 2.500 / Akun + Bonus Rp 500 / Akun untuk setor minimal 10 akun!',
    lastUpdated: now,
  }
}

export function seedAnnouncements(now: number): Announcement[] {
  return [
    {
      id: 1,
      title: 'Update Rate Reward Akun Gmail Terbaru!',
      content:
        'Halo Mitra Setor Gmail! Mulai tanggal 28 Agustus 2026, rate akun Gmail aktif dan terverifikasi naik menjadi Rp 2.500 per akun. Untuk setoran massal 10 akun atau lebih, Anda akan mendapatkan bonus tambahan Rp 500 per akun!',
      category: 'RATE_UPDATE',
      isImportant: true,
      author: 'Admin Official',
      dateFormatted: '28 Agu 2026, 08:00',
      createdAt: now - HOUR * 20,
    },
    {
      id: 2,
      title: 'Event Bonus Mingguan: Top 5 Mitra Terbanyak',
      content:
        'Dapatkan saldo reward ekstra total Rp 500.000 untuk 5 mitra dengan setoran akun valid terbanyak setiap hari Minggu pukul 23:59 WIB. Pantau terus rekap pendapatan harian Anda!',
      category: 'PROMO_BONUS',
      isImportant: true,
      author: 'Marketing Team',
      dateFormatted: '27 Agu 2026, 14:30',
      createdAt: now - HOUR * 40,
    },
    {
      id: 3,
      title: 'Tips Lolos Verifikasi 100% Cepat & Tanpa Revisi',
      content:
        'Pastikan akun Gmail:\n1. Tidak mengaktifkan verifikasi 2 langkah (2FA)\n2. Memiliki email pemulihan yang aktif\n3. Password minimal 8 karakter kombinasi huruf & angka\n4. Menggunakan format "email|password|recovery" untuk setor massal.',
      category: 'TIPS',
      isImportant: false,
      author: 'Tim Verifikasi',
      dateFormatted: '26 Agu 2026, 10:15',
      createdAt: now - HOUR * 60,
    },
    {
      id: 4,
      title: 'Penarikan Saldo Instan via E-Wallet DANA, OVO & GoPay',
      content:
        'Proses penarikan saldo reward kini diproses maksimal 5-15 menit pada jam kerja (08:00 - 22:00 WIB). Minimal penarikan saldo Rp 25.000 bebas biaya admin.',
      category: 'GENERAL',
      isImportant: false,
      author: 'Finance Official',
      dateFormatted: '25 Agu 2026, 09:00',
      createdAt: now - HOUR * 80,
    },
  ]
}

export function seedDeposits(now: number): GmailDeposit[] {
  return [
    {
      id: 101,
      userId: 2,
      username: 'budi_santoso',
      email: 'budisantoso.acc99@gmail.com',
      password: 'Password123#',
      recoveryInfo: 'recovery99@outlook.com',
      accountYear: '2023',
      status: 'APPROVED',
      rewardAmount: 2500,
      note: 'Akun tahun 2023 aktif',
      rejectReason: '',
      submittedAt: now - HOUR * 3,
      reviewedAt: now - HOUR * 2,
    },
    {
      id: 102,
      userId: 2,
      username: 'budi_santoso',
      email: 'budiwork.trade12@gmail.com',
      password: 'SecurePass2024!',
      recoveryInfo: 'budi.rec@yahoo.com',
      accountYear: '2024',
      status: 'APPROVED',
      rewardAmount: 2500,
      note: 'Setoran tunggal',
      rejectReason: '',
      submittedAt: now - HOUR * 5,
      reviewedAt: now - HOUR * 4,
    },
    {
      id: 103,
      userId: 2,
      username: 'budi_santoso',
      email: 'budigaming77@gmail.com',
      password: 'BudiGaming123!',
      recoveryInfo: 'recovery77@gmail.com',
      accountYear: '2024',
      status: 'PENDING',
      rewardAmount: 2500,
      note: 'Menunggu pengecekan sistem',
      rejectReason: '',
      submittedAt: now - MIN * 25,
      reviewedAt: null,
    },
    {
      id: 104,
      userId: 2,
      username: 'budi_santoso',
      email: 'budialfa88@gmail.com',
      password: 'BudiAlfa#2024',
      recoveryInfo: 'alfa88@outlook.com',
      accountYear: '2024',
      status: 'PENDING',
      rewardAmount: 2500,
      note: 'Setoran massal batch 1',
      rejectReason: '',
      submittedAt: now - MIN * 15,
      reviewedAt: null,
    },
    {
      id: 105,
      userId: 2,
      username: 'budi_santoso',
      email: 'budierror00@gmail.com',
      password: 'WrongPass123',
      recoveryInfo: '',
      accountYear: '2024',
      status: 'REJECTED',
      rewardAmount: 0,
      note: 'Perlu perbaikan',
      rejectReason: 'Password salah / Verifikasi 2FA terdeteksi aktif.',
      submittedAt: now - HOUR * 24,
      reviewedAt: now - HOUR * 20,
    },
  ]
}

export function seedWithdrawals(now: number): Withdrawal[] {
  return [
    {
      id: 201,
      userId: 2,
      username: 'budi_santoso',
      method: 'DANA',
      accountNumber: '085712345678',
      accountHolderName: 'Budi Santoso',
      amount: 50_000,
      status: 'SUCCESS',
      adminNote: 'Transfer Berhasil via DANA Instant',
      requestedAt: now - HOUR * 48,
      completedAt: now - HOUR * 47,
    },
    {
      id: 202,
      userId: 2,
      username: 'budi_santoso',
      method: 'GoPay',
      accountNumber: '085712345678',
      accountHolderName: 'Budi Santoso',
      amount: 25_000,
      status: 'SUCCESS',
      adminNote: 'Transfer Berhasil via GoPay',
      requestedAt: now - HOUR * 96,
      completedAt: now - HOUR * 95,
    },
  ]
}
