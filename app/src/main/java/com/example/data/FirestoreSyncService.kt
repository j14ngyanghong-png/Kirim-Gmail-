package com.example.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Service to synchronize application events and records directly to Firebase Firestore
 * using the native Android Firebase configuration (google-services.json).
 */
object FirestoreSyncService {
    private const val TAG = "FirestoreSyncService"

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFirestore initialization note: ${e.message}")
            null
        }
    }

    /**
     * Sync user record to Firestore 'users' and 'reward_balances' collections.
     */
    suspend fun syncUserToFirestore(user: UserEntity) {
        try {
            val db = firestore ?: return
            val userModel = user.toFirestoreUser()
            val rewardModel = user.toFirestoreRewardBalance()

            db.collection(FirestoreCollections.USERS)
                .document(user.id.toString())
                .set(userModel, SetOptions.merge())
                .await()

            db.collection(FirestoreCollections.REWARD_BALANCES)
                .document(user.id.toString())
                .set(rewardModel, SetOptions.merge())
                .await()

            Log.d(TAG, "Successfully synced user ${user.username} (ID: ${user.id}) to Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing user to Firestore: ${e.message}")
        }
    }

    /**
     * Sync a single Gmail deposit submission to Firestore.
     */
    suspend fun syncDepositToFirestore(deposit: GmailDepositEntity) {
        try {
            val db = firestore ?: return
            val depositModel = deposit.toFirestoreModel()
            val docRef = if (deposit.id > 0) {
                db.collection(FirestoreCollections.GMAIL_DEPOSITS).document(deposit.id.toString())
            } else {
                db.collection(FirestoreCollections.GMAIL_DEPOSITS).document()
            }
            docRef.set(depositModel, SetOptions.merge()).await()
            Log.d(TAG, "Successfully synced deposit record for ${deposit.email} to Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing deposit to Firestore: ${e.message}")
        }
    }

    /**
     * Sync bulk Gmail submissions to Firestore.
     */
    suspend fun syncBulkDepositsToFirestore(deposits: List<GmailDepositEntity>) {
        try {
            val db = firestore ?: return
            val batch = db.batch()
            deposits.forEach { dep ->
                val docRef = if (dep.id > 0) {
                    db.collection(FirestoreCollections.GMAIL_DEPOSITS).document(dep.id.toString())
                } else {
                    db.collection(FirestoreCollections.GMAIL_DEPOSITS).document()
                }
                batch.set(docRef, dep.toFirestoreModel(), SetOptions.merge())
            }
            batch.commit().await()
            Log.d(TAG, "Successfully synced ${deposits.size} bulk deposits to Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing bulk deposits to Firestore: ${e.message}")
        }
    }

    /**
     * Sync withdrawal payout requests to Firestore.
     */
    suspend fun syncWithdrawalToFirestore(withdrawal: WithdrawalEntity, username: String = "", email: String = "") {
        try {
            val db = firestore ?: return
            val record = FirestoreWithdrawalRecord(
                id = withdrawal.id.toString(),
                userId = withdrawal.userId.toString(),
                username = username,
                userEmail = email,
                paymentMethod = withdrawal.paymentMethod,
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
            docRef.set(record, SetOptions.merge()).await()
            Log.d(TAG, "Successfully synced withdrawal request ID: ${withdrawal.id} to Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing withdrawal to Firestore: ${e.message}")
        }
    }
}
