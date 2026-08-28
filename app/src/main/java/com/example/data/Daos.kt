package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :emailOrPhone OR phone = :emailOrPhone LIMIT 1")
    suspend fun getUserByEmailOrPhone(emailOrPhone: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserFlow(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users ORDER BY joinedAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET balance = balance + :amount, totalDeposited = totalDeposited + 1, validCount = validCount + 1 WHERE id = :userId")
    suspend fun addRewardToUser(userId: Long, amount: Long)

    @Query("UPDATE users SET balance = balance - :amount WHERE id = :userId")
    suspend fun deductBalance(userId: Long, amount: Long)

    @Query("UPDATE users SET balance = :newBalance WHERE id = :userId")
    suspend fun updateBalance(userId: Long, newBalance: Long)

    @Query("UPDATE users SET isSoundEnabled = :enabled WHERE id = :userId")
    suspend fun updateSoundSetting(userId: Long, enabled: Boolean)

    @Query("UPDATE users SET isVibrationEnabled = :enabled WHERE id = :userId")
    suspend fun updateVibrationSetting(userId: Long, enabled: Boolean)
}

@Dao
interface GmailDepositDao {
    @Query("SELECT * FROM gmail_deposits WHERE userId = :userId ORDER BY submittedAt DESC")
    fun getDepositsForUser(userId: Long): Flow<List<GmailDepositEntity>>

    @Query("SELECT * FROM gmail_deposits ORDER BY submittedAt DESC")
    fun getAllDeposits(): Flow<List<GmailDepositEntity>>

    @Query("SELECT * FROM gmail_deposits WHERE status = :status ORDER BY submittedAt DESC")
    fun getDepositsByStatus(status: String): Flow<List<GmailDepositEntity>>

    @Query("SELECT * FROM gmail_deposits WHERE id = :id LIMIT 1")
    suspend fun getDepositById(id: Long): GmailDepositEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeposit(deposit: GmailDepositEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(deposits: List<GmailDepositEntity>)

    @Update
    suspend fun updateDeposit(deposit: GmailDepositEntity)

    @Query("UPDATE gmail_deposits SET status = :status, reviewedAt = :reviewedAt, rejectReason = :rejectReason WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, reviewedAt: Long, rejectReason: String = "")

    @Query("SELECT COUNT(*) FROM gmail_deposits WHERE status = 'PENDING'")
    fun getPendingCountFlow(): Flow<Int>
}

@Dao
interface WithdrawalDao {
    @Query("SELECT * FROM withdrawals WHERE userId = :userId ORDER BY requestedAt DESC")
    fun getWithdrawalsForUser(userId: Long): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals ORDER BY requestedAt DESC")
    fun getAllWithdrawals(): Flow<List<WithdrawalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity): Long

    @Update
    suspend fun updateWithdrawal(withdrawal: WithdrawalEntity)

    @Query("UPDATE withdrawals SET status = :status, completedAt = :completedAt, adminNote = :note WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, completedAt: Long, note: String = "")
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY isImportant DESC, createdAt DESC")
    fun getAllAnnouncements(): Flow<List<AnnouncementEntity>>

    @Query("SELECT * FROM announcements WHERE category = :category ORDER BY isImportant DESC, createdAt DESC")
    fun getAnnouncementsByCategory(category: String): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity): Long

    @Query("DELETE FROM announcements WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface AppConfigDao {
    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    fun getConfigFlow(): Flow<AppConfigEntity?>

    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    suspend fun getConfig(): AppConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: AppConfigEntity)

    @Update
    suspend fun updateConfig(config: AppConfigEntity)

    @Query("UPDATE app_config SET isMaintenanceMode = :isMaintenance WHERE id = 1")
    suspend fun setMaintenanceMode(isMaintenance: Boolean)

    @Query("UPDATE app_config SET currentRatePerAccount = :rate WHERE id = 1")
    suspend fun setRate(rate: Long)
}
