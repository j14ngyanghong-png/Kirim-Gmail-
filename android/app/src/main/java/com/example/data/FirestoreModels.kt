package com.example.data

import android.util.Log
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.SetOptions

/**
 * Firestore data model to store user account records, roles, and profile metadata.
 */
@IgnoreExtraProperties
data class FirestoreUser(
    @DocumentId
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "USER", // "USER" or "ADMIN"
    val referralCode: String = "",
    val referredBy: String = "",
    val rewardBalance: Long = 0L,
    val totalDeposited: Int = 0,
    val validCount: Int = 0,
    val rejectedCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis()
)

/**
 * Firestore data model representing user account submission records (Gmail accounts deposited).
 */
@IgnoreExtraProperties
data class FirestoreDepositRecord(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val username: String = "",
    @get:PropertyName("accountEmail")
    @set:PropertyName("accountEmail")
    var accountEmail: String = "",
    @get:PropertyName("accountPassword")
    @set:PropertyName("accountPassword")
    var accountPassword: String = "",
    val recoveryInfo: String = "",
    val accountYear: String = "2024",
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val rewardAmount: Long = 2500L,
    val note: String = "",
    val rejectReason: String = "",
    val submittedAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null,
    val reviewerUid: String? = null
)

/**
 * Firestore data model to store user reward balances, lifetime earnings, and payout status.
 */
@IgnoreExtraProperties
data class FirestoreRewardBalance(
    @DocumentId
    val userId: String = "",
    val userEmail: String = "",
    val currentBalance: Long = 0L,
    val totalEarnedLifetime: Long = 0L,
    val totalPendingRewards: Long = 0L,
    val totalWithdrawn: Long = 0L,
    val lastRewardAdded: Long = 0L,
    val lastWithdrawalAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Firestore data model for tracking user withdrawal/payout requests.
 */
@IgnoreExtraProperties
data class FirestoreWithdrawalRecord(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val userEmail: String = "",
    val paymentMethod: String = "DANA", // DANA, OVO, GOPAY, SHOPEEPAY, BCA, BRI, MANDIRI, QRIS
    val accountNumber: String = "",
    val accountHolderName: String = "",
    val amount: Long = 0L,
    val status: String = "PROCESSED", // "PROCESSED", "SUCCESS", "REJECTED"
    val adminNote: String = "",
    val requestedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

/**
 * Constants for Firestore collection names across the application.
 */
object FirestoreCollections {
    const val USERS = "users"
    const val GMAIL_DEPOSITS = "gmail_deposits"
    const val REWARD_BALANCES = "reward_balances"
    const val WITHDRAWALS = "withdrawals"
    const val ANNOUNCEMENTS = "announcements"
    const val APP_CONFIG = "app_config"
}

/**
 * Extension mapper functions to convert Room entities into Firestore models.
 */
fun GmailDepositEntity.toFirestoreModel(userIdString: String = userId.toString()): FirestoreDepositRecord {
    return FirestoreDepositRecord(
        id = if (id > 0) id.toString() else "",
        userId = userIdString,
        username = username,
        accountEmail = email,
        accountPassword = password,
        recoveryInfo = recoveryInfo,
        accountYear = accountYear,
        status = status,
        rewardAmount = rewardAmount,
        note = note,
        rejectReason = rejectReason,
        submittedAt = submittedAt,
        reviewedAt = reviewedAt
    )
}

fun UserEntity.toFirestoreUser(uidString: String = id.toString()): FirestoreUser {
    return FirestoreUser(
        uid = uidString,
        username = username,
        email = email,
        phone = phone,
        role = role,
        referralCode = referralCode,
        rewardBalance = balance,
        totalDeposited = totalDeposited,
        validCount = validCount,
        rejectedCount = rejectedCount,
        createdAt = joinedAt,
        lastActiveAt = System.currentTimeMillis()
    )
}

fun UserEntity.toFirestoreRewardBalance(uidString: String = id.toString()): FirestoreRewardBalance {
    return FirestoreRewardBalance(
        userId = uidString,
        userEmail = email,
        currentBalance = balance,
        totalEarnedLifetime = balance,
        totalPendingRewards = 0L,
        totalWithdrawn = 0L,
        updatedAt = System.currentTimeMillis()
    )
}

/**
 * Service to synchronize application events and records directly to Firebase Firestore
 * using the native Android Firebase configuration (google-services.json).
 */
object FirestoreSyncService {
    private const val TAG = "FirestoreSyncService"

    /**
     * Sync user record to Firestore 'users' and 'reward_balances' collections.
     */
    fun syncUserToFirestore(user: UserEntity) {
        try {
            val db = FirebaseFirestore.getInstance()
            val userModel = user.toFirestoreUser()
            val rewardModel = user.toFirestoreRewardBalance()

            db.collection(FirestoreCollections.USERS)
                .document(user.id.toString())
                .set(userModel, SetOptions.merge())

            db.collection(FirestoreCollections.REWARD_BALANCES)
                .document(user.id.toString())
                .set(rewardModel, SetOptions.merge())

            Log.d(TAG, "Successfully synced user ${user.username} to Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing user to Firestore: ${e.message}")
        }
    }

    /**
     * Sync a single Gmail deposit submission to Firestore.
     */
    fun syncDepositToFirestore(deposit: GmailDepositEntity) {
        try {
            val db = FirebaseFirestore.getInstance()
            val depositModel = deposit.toFirestoreModel()
            val docRef = if (deposit.id > 0) {
                db.collection(FirestoreCollections.GMAIL_DEPOSITS).document(deposit.id.toString())
            } else {
                db.collection(FirestoreCollections.GMAIL_DEPOSITS).document()
            }
            docRef.set(depositModel, SetOptions.merge())
            Log.d(TAG, "Successfully synced deposit record for ${deposit.email} to Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing deposit to Firestore: ${e.message}")
        }
    }

    /**
     * Sync bulk Gmail submissions to Firestore.
     */
    fun syncBulkDepositsToFirestore(deposits: List<GmailDepositEntity>) {
        try {
            val db = FirebaseFirestore.getInstance()
            val batch = db.batch()
            deposits.forEach { dep ->
                val docRef = if (dep.id > 0) {
                    db.collection(FirestoreCollections.GMAIL_DEPOSITS).document(dep.id.toString())
                } else {
                    db.collection(FirestoreCollections.GMAIL_DEPOSITS).document()
                }
                batch.set(docRef, dep.toFirestoreModel(), SetOptions.merge())
            }
            batch.commit()
            Log.d(TAG, "Successfully synced ${deposits.size} bulk deposits to Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing bulk deposits to Firestore: ${e.message}")
        }
    }

    /**
     * Sync withdrawal payout requests to Firestore.
     */
    fun syncWithdrawalToFirestore(withdrawal: WithdrawalEntity, username: String = "", email: String = "") {
        try {
            val db = FirebaseFirestore.getInstance()
            val record = FirestoreWithdrawalRecord(
                id = withdrawal.id.toString(),
                userId = withdrawal.userId.toString(),
                username = username,
                userEmail = email,
                paymentMethod = withdrawal.method,
                accountNumber = withdrawal.accountNumber,
                accountHolderName = withdrawal.accountHolderName,
                amount = withdrawal.amount,
                status = withdrawal.status,
                adminNote = withdrawal.adminNote,
                requestedAt = withdrawal.requestedAt,
                completedAt = withdrawal.completedAt
            )
            val docRef = if (withdrawal.id > 0) {
                db.collection(FirestoreCollections.WITHDRAWALS).document(withdrawal.id.toString())
            } else {
                db.collection(FirestoreCollections.WITHDRAWALS).document()
            }
            docRef.set(record, SetOptions.merge())
            Log.d(TAG, "Successfully synced withdrawal request ID: ${withdrawal.id} to Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing withdrawal to Firestore: ${e.message}")
        }
    }
}
