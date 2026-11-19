package com.example.data.firebase

import android.content.Context
import com.example.R
import com.example.data.model.ActivationPayment
import com.example.data.model.AppSettings
import com.example.data.model.DataPackage
import com.example.data.model.DataSellRequest
import com.example.data.model.ReferralRecord
import com.example.data.model.SupportTicket
import com.example.data.model.UserProfile
import com.example.data.model.WalletTransaction
import com.example.data.model.WithdrawalRequest
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AdminRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Admin must be signed in.")
    }

    private fun nextTxnId(): String {
        val datePart = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val rand = UUID.randomUUID().toString().replace("-", "").uppercase().take(6)
        return "TXN-$datePart-$rand"
    }

    fun observeAllUsers(): Flow<List<UserProfile>> =
        db.collection("users").snapshots()
            .map { snap ->
                snap.toObjects(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catchFirestoreErrors(OperationType.LIST, "users")

    fun observeAllActivationPayments(): Flow<List<ActivationPayment>> =
        db.collection("activation_payments").snapshots()
            .map { snap ->
                snap.toObjects(ActivationPayment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catchFirestoreErrors(OperationType.LIST, "activation_payments")

    fun observeAllSellRequests(): Flow<List<DataSellRequest>> =
        db.collection("data_sell_requests").snapshots()
            .map { snap ->
                snap.toObjects(DataSellRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catchFirestoreErrors(OperationType.LIST, "data_sell_requests")

    fun observeAllWithdrawals(): Flow<List<WithdrawalRequest>> =
        db.collection("withdrawals").snapshots()
            .map { snap ->
                snap.toObjects(WithdrawalRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catchFirestoreErrors(OperationType.LIST, "withdrawals")

    fun observeAllTransactions(): Flow<List<WalletTransaction>> =
        db.collection("transactions").snapshots()
            .map { snap ->
                snap.toObjects(WalletTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catchFirestoreErrors(OperationType.LIST, "transactions")

    fun observeAllReferrals(): Flow<List<ReferralRecord>> =
        db.collection("referrals").snapshots()
            .map { snap ->
                snap.toObjects(ReferralRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catchFirestoreErrors(OperationType.LIST, "referrals")

    fun observeAllSupportTickets(): Flow<List<SupportTicket>> =
        db.collection("support_tickets").snapshots()
            .map { snap ->
                snap.toObjects(SupportTicket::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catchFirestoreErrors(OperationType.LIST, "support_tickets")

    suspend fun seedDefaultCatalogAndSettings(): Result<Unit> {
        return try {
            requireUserId()
            val batch = db.batch()
            val settingsRef = db.collection("app_settings").document("global")
            val settingsSnap = settingsRef.get().await()
            if (!settingsSnap.exists()) {
                batch.set(
                    settingsRef,
                    mapOf(
                        "activationFeeInr" to 5.0,
                        "activationUpiId" to "janara2@ptyes",
                        "minWithdrawalInr" to 50.0,
                        "referralRewardInr" to 10.0,
                        "referralsEnabled" to true,
                        "supportEmail" to "support@dilsatdatasell.com",
                        "announcementBanner" to "Earn ₹2 per 100 MB! Minimum 100 MB per data sell request.",
                        "ratePer100MbInr" to 2.0,
                        "minSellDataMb" to 100.0,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            val pkgSnap = db.collection("data_packages").get().await()
            if (pkgSnap.isEmpty) {
                val defaults = listOf(
                    "pkg_4g_surplus" to mapOf(
                        "name" to "4G Daily Surplus",
                        "packageType" to "4G",
                        "pricePerGb" to 12.0,
                        "minGb" to 1.0,
                        "maxGb" to 50.0,
                        "description" to "Monetize leftover daily 4G mobile data quota.",
                        "active" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    "pkg_5g_ultra" to mapOf(
                        "name" to "5G Ultra Pool",
                        "packageType" to "5G",
                        "pricePerGb" to 18.0,
                        "minGb" to 2.0,
                        "maxGb" to 200.0,
                        "description" to "Premium rate for high-throughput 5G surplus bandwidth.",
                        "active" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    "pkg_night_rollover" to mapOf(
                        "name" to "Weekend & Rollover",
                        "packageType" to "Rollover",
                        "pricePerGb" to 8.5,
                        "minGb" to 1.0,
                        "maxGb" to 100.0,
                        "description" to "Convert expiring weekend rollover data into cash.",
                        "active" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    "pkg_fiber_share" to mapOf(
                        "name" to "Fiber Broadband Share",
                        "packageType" to "Broadband",
                        "pricePerGb" to 15.0,
                        "minGb" to 5.0,
                        "maxGb" to 500.0,
                        "description" to "High-volume home fiber broadband data exchange.",
                        "active" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                defaults.forEach { (id, payload) ->
                    batch.set(db.collection("data_packages").document(id), payload)
                }
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "app_settings/global")
            Result.failure(e)
        }
    }

    suspend fun verifyActivationPayment(
        payment: ActivationPayment,
        approve: Boolean,
        note: String
    ): Result<Unit> {
        return try {
            requireUserId()
            val batch = db.batch()
            val payRef = db.collection("activation_payments").document(payment.id)
            val userRef = db.collection("users").document(payment.userId)
            val notifRef = userRef.collection("notifications").document("notif_${UUID.randomUUID().toString().take(10)}")

            val cleanNote = note.trim().take(200)
            val payUpdate = mutableMapOf<String, Any>(
                "status" to if (approve) "verified" else "rejected",
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (cleanNote.isNotEmpty()) payUpdate["adminNote"] = cleanNote
            batch.update(payRef, payUpdate)

            if (approve) {
                batch.update(
                    userRef,
                    mapOf(
                        "activationStatus" to "activated",
                        "totalTransactions" to FieldValue.increment(1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                val txnId = nextTxnId()
                batch.set(
                    db.collection("transactions").document(txnId),
                    mapOf(
                        "userId" to payment.userId,
                        "referenceCode" to txnId,
                        "type" to "activation_fee",
                        "direction" to "fee",
                        "amount" to payment.amount,
                        "status" to "completed",
                        "description" to "Account Activation Fee (UTR: ${payment.utrReference})",
                        "relatedId" to payment.id,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.set(
                    notifRef,
                    mapOf(
                        "title" to "Payment Successful",
                        "message" to "Your ₹${payment.amount.toInt()} activation payment (${payment.utrReference}) is verified. Welcome to Dilsat Data Sell!",
                        "category" to "payment",
                        "read" to false,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            } else {
                val reason = cleanNote.ifEmpty { "UTR could not be matched on ${payment.upiId}." }
                batch.update(
                    userRef,
                    mapOf(
                        "activationStatus" to "rejected",
                        "activationFailureReason" to reason,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.set(
                    notifRef,
                    mapOf(
                        "title" to "Activation Payment Failed",
                        "message" to "Payment verification rejected: $reason",
                        "category" to "payment",
                        "read" to false,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "activation_payments/${payment.id}")
            Result.failure(e)
        }
    }

    suspend fun processDataSellRequest(
        req: DataSellRequest,
        newStatus: String,
        note: String
    ): Result<Unit> {
        return try {
            requireUserId()
            val batch = db.batch()
            val reqRef = db.collection("data_sell_requests").document(req.id)
            val userRef = db.collection("users").document(req.userId)
            val notifRef = userRef.collection("notifications").document("notif_${UUID.randomUUID().toString().take(10)}")
            val cleanNote = note.trim().take(200)

            val reqUpdate = mutableMapOf<String, Any>(
                "status" to newStatus,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (cleanNote.isNotEmpty()) reqUpdate["adminNote"] = cleanNote
            batch.update(reqRef, reqUpdate)

            when (newStatus) {
                "approved", "completed" -> {
                    batch.update(
                        userRef,
                        mapOf(
                            "walletBalance" to FieldValue.increment(req.estimatedPayout),
                            "totalEarnings" to FieldValue.increment(req.estimatedPayout),
                            "todayEarnings" to FieldValue.increment(req.estimatedPayout),
                            "totalDataSoldGb" to FieldValue.increment(req.quantityGb),
                            "totalTransactions" to FieldValue.increment(1L),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )

                    val txnId = nextTxnId()
                    val mbLabel = "${req.displayMb.toInt()} MB"
                    batch.set(
                        db.collection("transactions").document(txnId),
                        mapOf(
                            "userId" to req.userId,
                            "referenceCode" to txnId,
                            "type" to "data_sale_credit",
                            "direction" to "credit",
                            "amount" to req.estimatedPayout,
                            "status" to "completed",
                            "description" to "Data Sale Approved: $mbLabel (${req.packageType} • ${req.networkProvider})",
                            "relatedId" to req.id,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )
                    batch.set(
                        notifRef,
                        mapOf(
                            "title" to "Data Sell Request Approved",
                            "message" to "$mbLabel verified! ₹${"%.2f".format(req.estimatedPayout)} has been added to your wallet.",
                            "category" to "data_sell",
                            "read" to false,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
                "rejected" -> {
                    val mbLabel = "${req.displayMb.toInt()} MB"
                    batch.set(
                        notifRef,
                        mapOf(
                            "title" to "Data Sell Request Rejected",
                            "message" to (cleanNote.ifEmpty { "Your $mbLabel request could not be verified." }),
                            "category" to "data_sell",
                            "read" to false,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "data_sell_requests/${req.id}")
            Result.failure(e)
        }
    }

    suspend fun processWithdrawal(
        wd: WithdrawalRequest,
        newStatus: String,
        utrNumber: String,
        note: String
    ): Result<Unit> {
        return try {
            requireUserId()
            val batch = db.batch()
            val wdRef = db.collection("withdrawals").document(wd.id)
            val userRef = db.collection("users").document(wd.userId)
            val notifRef = userRef.collection("notifications").document("notif_${UUID.randomUUID().toString().take(10)}")
            val cleanNote = note.trim().take(200)
            val cleanUtr = utrNumber.trim().take(40)

            val wdUpdates = mutableMapOf<String, Any>(
                "status" to newStatus,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (cleanNote.isNotEmpty()) wdUpdates["adminNote"] = cleanNote
            if (cleanUtr.isNotEmpty()) wdUpdates["utrNumber"] = cleanUtr
            batch.update(wdRef, wdUpdates)

            if (newStatus == "paid") {
                val userSnap = userRef.get().await()
                val curBalance = userSnap.getDouble("walletBalance") ?: 0.0
                if (curBalance < wd.amount) {
                    return Result.failure(IllegalStateException("User wallet balance (₹$curBalance) is lower than ₹${wd.amount}."))
                }
                batch.update(
                    userRef,
                    mapOf(
                        "walletBalance" to (curBalance - wd.amount),
                        "totalTransactions" to FieldValue.increment(1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                val txnId = nextTxnId()
                batch.set(
                    db.collection("transactions").document(txnId),
                    mapOf(
                        "userId" to wd.userId,
                        "referenceCode" to txnId,
                        "type" to "withdrawal_debit",
                        "direction" to "debit",
                        "amount" to wd.amount,
                        "status" to "completed",
                        "description" to "UPI Withdrawal to ${wd.upiId}" + if (cleanUtr.isNotEmpty()) " (UTR: $cleanUtr)" else "",
                        "relatedId" to wd.id,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.set(
                    notifRef,
                    mapOf(
                        "title" to "Withdrawal Successful",
                        "message" to "₹${"%.2f".format(wd.amount)} sent to ${wd.upiId}." + if (cleanUtr.isNotEmpty()) " Ref: $cleanUtr" else "",
                        "category" to "withdrawal",
                        "read" to false,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            } else if (newStatus == "rejected") {
                batch.set(
                    notifRef,
                    mapOf(
                        "title" to "Withdrawal Rejected",
                        "message" to (cleanNote.ifEmpty { "Withdrawal of ₹${"%.2f".format(wd.amount)} was rejected." }),
                        "category" to "withdrawal",
                        "read" to false,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "withdrawals/${wd.id}")
            Result.failure(e)
        }
    }

    suspend fun processReferral(ref: ReferralRecord, approve: Boolean): Result<Unit> {
        return try {
            requireUserId()
            val batch = db.batch()
            val refDoc = db.collection("referrals").document(ref.id)
            val referrerRef = db.collection("users").document(ref.referrerUid)
            val notifRef = referrerRef.collection("notifications").document("notif_${UUID.randomUUID().toString().take(10)}")

            batch.update(
                refDoc,
                mapOf(
                    "status" to if (approve) "credited" else "rejected",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            if (approve) {
                batch.update(
                    referrerRef,
                    mapOf(
                        "walletBalance" to FieldValue.increment(ref.rewardAmount),
                        "totalEarnings" to FieldValue.increment(ref.rewardAmount),
                        "todayEarnings" to FieldValue.increment(ref.rewardAmount),
                        "referralCount" to FieldValue.increment(1L),
                        "referralEarnings" to FieldValue.increment(ref.rewardAmount),
                        "totalTransactions" to FieldValue.increment(1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                val txnId = nextTxnId()
                batch.set(
                    db.collection("transactions").document(txnId),
                    mapOf(
                        "userId" to ref.referrerUid,
                        "referenceCode" to txnId,
                        "type" to "referral_bonus",
                        "direction" to "credit",
                        "amount" to ref.rewardAmount,
                        "status" to "completed",
                        "description" to "Referral Bonus for inviting ${ref.referredName}",
                        "relatedId" to ref.id,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.set(
                    notifRef,
                    mapOf(
                        "title" to "Referral Reward Received!",
                        "message" to "You earned ₹${"%.2f".format(ref.rewardAmount)} for referring ${ref.referredName}.",
                        "category" to "referral",
                        "read" to false,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "referrals/${ref.id}")
            Result.failure(e)
        }
    }

    suspend fun toggleUserBlocked(user: UserProfile, blocked: Boolean): Result<Unit> {
        return try {
            requireUserId()
            db.collection("users").document(user.userId)
                .update(
                    mapOf(
                        "blocked" to blocked,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/${user.userId}")
            Result.failure(e)
        }
    }

    suspend fun upsertDataPackage(pkg: DataPackage): Result<Unit> {
        val docId = pkg.id.ifBlank { "pkg_${UUID.randomUUID().toString().replace("-", "").take(10)}" }
        return try {
            requireUserId()
            val docRef = db.collection("data_packages").document(docId)
            val exists = pkg.id.isNotBlank() && pkg.createdAt != null
            val payload = mapOf(
                "name" to pkg.name.trim(),
                "packageType" to pkg.packageType,
                "pricePerGb" to pkg.pricePerGb,
                "minGb" to pkg.minGb,
                "maxGb" to pkg.maxGb,
                "description" to pkg.description.trim(),
                "active" to pkg.active,
                "createdAt" to (pkg.createdAt ?: FieldValue.serverTimestamp()),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (exists) {
                docRef.update(payload.filterKeys { it != "createdAt" }).await()
            } else {
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "data_packages/$docId")
            Result.failure(e)
        }
    }

    suspend fun deleteDataPackage(packageId: String): Result<Unit> {
        return try {
            requireUserId()
            db.collection("data_packages").document(packageId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "data_packages/$packageId")
            Result.failure(e)
        }
    }

    suspend fun updateAppSettings(settings: AppSettings): Result<Unit> {
        return try {
            requireUserId()
            val payload = mapOf(
                "activationFeeInr" to settings.activationFeeInr,
                "activationUpiId" to settings.activationUpiId.trim(),
                "minWithdrawalInr" to settings.minWithdrawalInr,
                "referralRewardInr" to settings.referralRewardInr,
                "referralsEnabled" to settings.referralsEnabled,
                "supportEmail" to settings.supportEmail.trim(),
                "announcementBanner" to settings.announcementBanner.trim(),
                "ratePer100MbInr" to settings.ratePer100MbInr,
                "minSellDataMb" to settings.minSellDataMb,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("app_settings").document("global").set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "app_settings/global")
            Result.failure(e)
        }
    }

    suspend fun broadcastAnnouncement(title: String, message: String, priority: String): Result<Unit> {
        val annId = "ann_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        return try {
            requireUserId()
            db.collection("announcements").document(annId)
                .set(
                    mapOf(
                        "title" to title.trim(),
                        "message" to message.trim(),
                        "priority" to priority,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "announcements/$annId")
            Result.failure(e)
        }
    }

    suspend fun sendDirectNotification(targetUserId: String, title: String, message: String, category: String): Result<Unit> {
        val notifId = "notif_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        return try {
            requireUserId()
            db.collection("users").document(targetUserId)
                .collection("notifications").document(notifId)
                .set(
                    mapOf(
                        "title" to title.trim(),
                        "message" to message.trim(),
                        "category" to category,
                        "read" to false,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "users/$targetUserId/notifications/$notifId")
            Result.failure(e)
        }
    }

    suspend fun replySupportTicket(ticket: SupportTicket, newStatus: String, reply: String): Result<Unit> {
        return try {
            requireUserId()
            val cleanReply = reply.trim().take(500)
            val updates = mutableMapOf<String, Any>(
                "status" to newStatus,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (cleanReply.isNotEmpty()) updates["adminReply"] = cleanReply
            db.collection("support_tickets").document(ticket.id).update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "support_tickets/${ticket.id}")
            Result.failure(e)
        }
    }

    fun buildTransactionCsvReport(transactions: List<WalletTransaction>): String {
        val sb = StringBuilder()
        sb.appendLine("TransactionID,UserID,Type,Direction,AmountINR,Status,Description,Timestamp")
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        transactions.forEach { tx ->
            val dateStr = tx.createdAt?.toDate()?.let { fmt.format(it) } ?: ""
            val safeDesc = tx.description.replace("\"", "\"\"")
            sb.appendLine("\"${tx.referenceCode}\",\"${tx.userId}\",\"${tx.type}\",\"${tx.direction}\",${tx.amount},\"${tx.status}\",\"$safeDesc\",\"$dateStr\"")
        }
        return sb.toString()
    }
}
