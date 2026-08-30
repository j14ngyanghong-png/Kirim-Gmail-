package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        GmailDepositEntity::class,
        WithdrawalEntity::class,
        AnnouncementEntity::class,
        AppConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun gmailDepositDao(): GmailDepositDao
    abstract fun withdrawalDao(): WithdrawalDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun appConfigDao(): AppConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "setor_gmail_rewards.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate with realistic initial data
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                seedDatabase(database)
                            }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedDatabase(db: AppDatabase) {
            // Default Admin Account
            val adminUser = UserEntity(
                id = 1,
                username = "admin_master",
                email = "admin@rewards.id",
                phone = "081299887766",
                passwordHash = "admin123",
                balance = 1500000L,
                totalDeposited = 0,
                validCount = 0,
                rejectedCount = 0,
                referralCode = "ADMINVIP",
                role = "ADMIN"
            )
            db.userDao().insertUser(adminUser)

            // Demo User Account
            val demoUser = UserEntity(
                id = 2,
                username = "budi_santoso",
                email = "budi@gmail.com",
                phone = "085712345678",
                passwordHash = "budi123",
                balance = 47500L,
                totalDeposited = 24,
                validCount = 19,
                rejectedCount = 2,
                referralCode = "BUDI2024",
                role = "USER"
            )
            db.userDao().insertUser(demoUser)

            // System Config
            db.appConfigDao().insertConfig(
                AppConfigEntity(
                    id = 1,
                    isMaintenanceMode = false,
                    maintenanceMessage = "Server sedang dalam pemeliharaan berkala untuk peningkatan kecepatan verifikasi akun. Silakan coba kembali dalam beberapa saat.",
                    currentRatePerAccount = 2500L,
                    bonusRateTier = 500L,
                    minWithdrawalAmount = 25000L,
                    activeBroadcastBanner = "⚡ Rate Spesial Hari Ini: Rp 2.500 / Akun + Bonus Rp 500 / Akun untuk setor minimal 10 akun!"
                )
            )

            // Initial Announcements
            val announcements = listOf(
                AnnouncementEntity(
                    title = "📢 Update Rate Reward Akun Gmail Terbaru!",
                    content = "Halo Mitra Setor Gmail! Mulai tanggal 28 Agustus 2026, rate akun Gmail aktif dan terverifikasi naik menjadi Rp 2.500 per akun. Untuk setoran massal 10 akun atau lebih, Anda akan mendapatkan bonus tambahan Rp 500 per akun!",
                    category = "RATE_UPDATE",
                    isImportant = true,
                    author = "Admin Official",
                    dateFormatted = "28 Agu 2026, 08:00"
                ),
                AnnouncementEntity(
                    title = "🎁 Event Bonus Mingguan: Top 5 Mitra Terbanyak",
                    content = "Dapatkan saldo reward ekstra total Rp 500.000 untuk 5 mitra dengan setoran akun valid terbanyak setiap hari Minggu pukul 23:59 WIB. Pantau terus rekap pendapatan harian Anda!",
                    category = "PROMO_BONUS",
                    isImportant = true,
                    author = "Marketing Team",
                    dateFormatted = "27 Agu 2026, 14:30"
                ),
                AnnouncementEntity(
                    title = "💡 Tips Lolos Verifikasi 100% Cepat & Tanpa Revisi",
                    content = "Pastikan akun Gmail:\n1. Tidak mengaktifkan verifikasi 2 langkah (2FA)\n2. Memiliki email pemulihan yang aktif\n3. Password minimal 8 karakter kombinasi huruf & angka\n4. Menggunakan format 'email|password|recovery' untuk setor massal.",
                    category = "TIPS",
                    isImportant = false,
                    author = "Tim Verifikasi",
                    dateFormatted = "26 Agu 2026, 10:15"
                ),
                AnnouncementEntity(
                    title = "⚡ Penarikan Saldo Instan via E-Wallet DANA, OVO & GoPay",
                    content = "Proses penarikan saldo reward kini diproses maksimal 5-15 menit pada jam kerja (08:00 - 22:00 WIB). Minimal penarikan saldo Rp 25.000 bebas biaya admin.",
                    category = "GENERAL",
                    isImportant = false,
                    author = "Finance Official",
                    dateFormatted = "25 Agu 2026, 09:00"
                )
            )
            announcements.forEach { db.announcementDao().insertAnnouncement(it) }

            // Initial Deposits for Demo User
            val sampleDeposits = listOf(
                GmailDepositEntity(
                    userId = 2,
                    username = "budi_santoso",
                    email = "budisantoso.acc99@gmail.com",
                    password = "Password123#",
                    recoveryInfo = "recovery99@outlook.com",
                    accountYear = "2023",
                    status = "APPROVED",
                    rewardAmount = 2500L,
                    note = "Akun tahun 2023 aktif",
                    submittedAt = System.currentTimeMillis() - 1000 * 60 * 60 * 3,
                    reviewedAt = System.currentTimeMillis() - 1000 * 60 * 60 * 2
                ),
                GmailDepositEntity(
                    userId = 2,
                    username = "budi_santoso",
                    email = "budiwork.trade12@gmail.com",
                    password = "SecurePass2024!",
                    recoveryInfo = "budi.rec@yahoo.com",
                    accountYear = "2024",
                    status = "APPROVED",
                    rewardAmount = 2500L,
                    note = "Setoran tunggal",
                    submittedAt = System.currentTimeMillis() - 1000 * 60 * 60 * 5,
                    reviewedAt = System.currentTimeMillis() - 1000 * 60 * 60 * 4
                ),
                GmailDepositEntity(
                    userId = 2,
                    username = "budi_santoso",
                    email = "budigaming77@gmail.com",
                    password = "BudiGaming123!",
                    recoveryInfo = "recovery77@gmail.com",
                    accountYear = "2024",
                    status = "PENDING",
                    rewardAmount = 2500L,
                    note = "Menunggu pengecekan sistem",
                    submittedAt = System.currentTimeMillis() - 1000 * 60 * 25
                ),
                GmailDepositEntity(
                    userId = 2,
                    username = "budi_santoso",
                    email = "budialfa88@gmail.com",
                    password = "BudiAlfa#2024",
                    recoveryInfo = "alfa88@outlook.com",
                    accountYear = "2024",
                    status = "PENDING",
                    rewardAmount = 2500L,
                    note = "Setoran massal batch 1",
                    submittedAt = System.currentTimeMillis() - 1000 * 60 * 15
                ),
                GmailDepositEntity(
                    userId = 2,
                    username = "budi_santoso",
                    email = "budierror00@gmail.com",
                    password = "WrongPass123",
                    recoveryInfo = "",
                    accountYear = "2024",
                    status = "REJECTED",
                    rewardAmount = 0L,
                    note = "Perlu perbaikan",
                    rejectReason = "Password salah / Verifikasi 2FA terdeteksi aktif.",
                    submittedAt = System.currentTimeMillis() - 1000 * 60 * 60 * 24,
                    reviewedAt = System.currentTimeMillis() - 1000 * 60 * 60 * 20
                )
            )
            sampleDeposits.forEach { db.gmailDepositDao().insertDeposit(it) }

            // Initial Withdrawals
            val sampleWithdrawals = listOf(
                WithdrawalEntity(
                    userId = 2,
                    username = "budi_santoso",
                    method = "DANA",
                    accountNumber = "085712345678",
                    accountHolderName = "Budi Santoso",
                    amount = 50000L,
                    status = "SUCCESS",
                    adminNote = "Transfer Berhasil via DANA Instant",
                    requestedAt = System.currentTimeMillis() - 1000 * 60 * 60 * 48,
                    completedAt = System.currentTimeMillis() - 1000 * 60 * 60 * 47
                ),
                WithdrawalEntity(
                    userId = 2,
                    username = "budi_santoso",
                    method = "GoPay",
                    accountNumber = "085712345678",
                    accountHolderName = "Budi Santoso",
                    amount = 25000L,
                    status = "SUCCESS",
                    adminNote = "Transfer Berhasil via GoPay",
                    requestedAt = System.currentTimeMillis() - 1000 * 60 * 60 * 96,
                    completedAt = System.currentTimeMillis() - 1000 * 60 * 60 * 95
                )
            )
            sampleWithdrawals.forEach { db.withdrawalDao().insertWithdrawal(it) }
        }
    }
}
