package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val balance: Long = 0L,
    val totalDeposited: Int = 0,
    val validCount: Int = 0,
    val rejectedCount: Int = 0,
    val referralCode: String = "",
    val role: String = "USER", // "USER" or "ADMIN"
    val isSoundEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true,
    val joinedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "gmail_deposits")
data class GmailDepositEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val username: String,
    val email: String,
    val password: String,
    val recoveryInfo: String = "",
    val accountYear: String = "2024",
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val rewardAmount: Long = 2500L,
    val note: String = "",
    val rejectReason: String = "",
    val submittedAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null
)

@Entity(tableName = "withdrawals")
data class WithdrawalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val username: String,
    val method: String, // DANA, OVO, GOPAY, SHOPEEPAY, BCA, BRI, MANDIRI, QRIS
    val accountNumber: String,
    val accountHolderName: String,
    val amount: Long,
    val status: String = "PROCESSED", // "PROCESSED", "SUCCESS", "REJECTED"
    val adminNote: String = "",
    val requestedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String, // "RATE_UPDATE", "MAINTENANCE", "PROMO_BONUS", "TIPS", "GENERAL"
    val isImportant: Boolean = false,
    val author: String = "Admin Official",
    val dateFormatted: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_config")
data class AppConfigEntity(
    @PrimaryKey val id: Int = 1,
    val isMaintenanceMode: Boolean = false,
    val maintenanceMessage: String = "Server sedang dalam pemeliharaan rutin untuk peningkatan sistem verifikasi otomatis. Estimasi selesai 30 menit.",
    val currentRatePerAccount: Long = 2500L,
    val bonusRateTier: Long = 500L, // Bonus if bulk >= 10
    val minWithdrawalAmount: Long = 25000L,
    val activeBroadcastBanner: String = "🔥 UPDATE: Rate akun Gmail tahun 2023 ke bawah naik jadi Rp 3.500/akun!",
    val lastUpdated: Long = System.currentTimeMillis()
)
