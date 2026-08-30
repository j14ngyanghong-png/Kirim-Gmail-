package com.example.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AppRepository(private val db: AppDatabase) {
    private val syncScope = CoroutineScope(Dispatchers.IO)

    // User Operations
    fun getUserFlow(userId: Long): Flow<UserEntity?> = db.userDao().getUserFlow(userId)
    suspend fun getUserById(userId: Long): UserEntity? = db.userDao().getUserById(userId)
    suspend fun getUserByEmailOrPhone(query: String): UserEntity? = db.userDao().getUserByEmailOrPhone(query)
    fun getAllUsers(): Flow<List<UserEntity>> = db.userDao().getAllUsers()
    
    suspend fun registerUser(user: UserEntity): Long {
        val id = db.userDao().insertUser(user)
        val savedUser = user.copy(id = id)
        syncScope.launch {
            FirestoreSyncService.syncUserToFirestore(savedUser)
        }
        return id
    }

    suspend fun updateUser(user: UserEntity) {
        db.userDao().updateUser(user)
        syncScope.launch {
            FirestoreSyncService.syncUserToFirestore(user)
        }
    }

    suspend fun updateSoundSetting(userId: Long, enabled: Boolean) = db.userDao().updateSoundSetting(userId, enabled)
    suspend fun updateVibrationSetting(userId: Long, enabled: Boolean) = db.userDao().updateVibrationSetting(userId, enabled)
    
    suspend fun updateBalance(userId: Long, newBalance: Long) {
        db.userDao().updateBalance(userId, newBalance)
        syncScope.launch {
            val user = db.userDao().getUserById(userId)
            if (user != null) {
                FirestoreSyncService.syncUserToFirestore(user)
            }
        }
    }

    // Gmail Deposit Operations
    fun getDepositsForUser(userId: Long): Flow<List<GmailDepositEntity>> = db.gmailDepositDao().getDepositsForUser(userId)
    fun getAllDeposits(): Flow<List<GmailDepositEntity>> = db.gmailDepositDao().getAllDeposits()
    fun getPendingDepositsCount(): Flow<Int> = db.gmailDepositDao().getPendingCountFlow()
    
    suspend fun submitDeposit(deposit: GmailDepositEntity): Long {
        val id = db.gmailDepositDao().insertDeposit(deposit)
        val savedDeposit = deposit.copy(id = id)
        syncScope.launch {
            FirestoreSyncService.syncDepositToFirestore(savedDeposit)
        }
        return id
    }

    suspend fun submitBulkDeposits(deposits: List<GmailDepositEntity>) {
        db.gmailDepositDao().insertAll(deposits)
        syncScope.launch {
            FirestoreSyncService.syncBulkDepositsToFirestore(deposits)
        }
    }

    suspend fun approveDeposit(depositId: Long, rewardAmount: Long, userId: Long) {
        db.gmailDepositDao().updateStatus(depositId, "APPROVED", System.currentTimeMillis(), "")
        db.userDao().addRewardToUser(userId, rewardAmount)
        syncScope.launch {
            val deposit = db.gmailDepositDao().getDepositById(depositId)
            if (deposit != null) {
                FirestoreSyncService.syncDepositToFirestore(deposit)
            }
            val user = db.userDao().getUserById(userId)
            if (user != null) {
                FirestoreSyncService.syncUserToFirestore(user)
            }
        }
    }

    suspend fun rejectDeposit(depositId: Long, reason: String) {
        db.gmailDepositDao().updateStatus(depositId, "REJECTED", System.currentTimeMillis(), reason)
        syncScope.launch {
            val deposit = db.gmailDepositDao().getDepositById(depositId)
            if (deposit != null) {
                FirestoreSyncService.syncDepositToFirestore(deposit)
            }
        }
    }

    // Withdrawal Operations
    fun getWithdrawalsForUser(userId: Long): Flow<List<WithdrawalEntity>> = db.withdrawalDao().getWithdrawalsForUser(userId)
    fun getAllWithdrawals(): Flow<List<WithdrawalEntity>> = db.withdrawalDao().getAllWithdrawals()
    
    suspend fun requestWithdrawal(withdrawal: WithdrawalEntity): Long {
        val id = db.withdrawalDao().insertWithdrawal(withdrawal)
        db.userDao().deductBalance(withdrawal.userId, withdrawal.amount)
        val savedWithdrawal = withdrawal.copy(id = id)
        syncScope.launch {
            val user = db.userDao().getUserById(withdrawal.userId)
            FirestoreSyncService.syncWithdrawalToFirestore(
                savedWithdrawal,
                username = user?.username ?: "",
                email = user?.email ?: ""
            )
            if (user != null) {
                FirestoreSyncService.syncUserToFirestore(user)
            }
        }
        return id
    }

    suspend fun updateWithdrawalStatus(id: Long, status: String, note: String) {
        db.withdrawalDao().updateStatus(id, status, System.currentTimeMillis(), note)
    }

    // Announcements
    fun getAllAnnouncements(): Flow<List<AnnouncementEntity>> = db.announcementDao().getAllAnnouncements()
    fun getAnnouncementsByCategory(category: String): Flow<List<AnnouncementEntity>> = db.announcementDao().getAnnouncementsByCategory(category)
    suspend fun createAnnouncement(announcement: AnnouncementEntity): Long = db.announcementDao().insertAnnouncement(announcement)
    suspend fun deleteAnnouncement(id: Long) = db.announcementDao().deleteById(id)

    // System Config
    fun getConfigFlow(): Flow<AppConfigEntity?> = db.appConfigDao().getConfigFlow()
    suspend fun getConfig(): AppConfigEntity? = db.appConfigDao().getConfig()
    suspend fun setMaintenanceMode(isMaintenance: Boolean) = db.appConfigDao().setMaintenanceMode(isMaintenance)
    suspend fun setRate(rate: Long) = db.appConfigDao().setRate(rate)
    suspend fun updateConfig(config: AppConfigEntity) = db.appConfigDao().updateConfig(config)
}
