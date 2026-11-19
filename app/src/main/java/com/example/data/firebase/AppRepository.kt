package com.example.data.firebase

import android.content.Context
import com.example.R
import com.example.data.model.ActivationPayment
import com.example.data.model.AppNotification
import com.example.data.model.AppSettings
import com.example.data.model.GlobalAnnouncement
import com.example.data.model.ReferralCodeLookup
import com.example.data.model.ReferralRecord
import com.example.data.model.SupportTicket
import com.example.data.model.UserProfile
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AppRepository(
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
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeAppSettings(): Flow<AppSettings> =
        db.collection("app_settings").document("global")
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(AppSettings::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        ?: AppSettings()
                } else {
                    AppSettings()
                }
            }
            .catchFirestoreErrors(OperationType.GET, "app_settings/global")

    fun observeAdminStatus(userId: String): Flow<Boolean> =
        db.collection("admins").document(userId)
            .snapshots()
            .map { it.exists() }
            .catchFirestoreErrors(OperationType.GET, "admins/$userId", userId)

    suspend fun claimBootstrappedAdmin(): Result<Unit> {
        val uid = auth.currentUser?.uid
        val path = "admins/${uid ?: "unknown"}"
        return try {
            val validUid = requireUserId()
            val payload = mapOf(
                "uid" to validUid,
                "role" to "super_admin",
                "addedBy" to "bootstrap",
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("admins").document(validUid).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> =
        db.collection("users").document(userId)
            .snapshots()
            .map { snapshot ->
                if (!snapshot.exists()) null
                else snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catchFirestoreErrors(OperationType.GET, "users/$userId", userId)

    suspend fun lookupReferralCode(code: String): Result<ReferralCodeLookup?> {
        val normalized = code.trim().uppercase()
        if (normalized.length !in 6..16 || !normalized.matches(Regex("^[A-Z0-9]+$"))) {
            return Result.failure(IllegalArgumentException("Referral code must be 6-16 alphanumeric characters."))
        }
        return try {
            requireUserId()
            val snap = db.collection("referral_codes").document(normalized).get().await()
            val result = if (snap.exists()) {
                snap.toObject(ReferralCodeLookup::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else null
            Result.success(result)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, "referral_codes/$normalized")
            Result.failure(e)
        }
    }

    suspend fun createUserProfile(
        fullName: String,
        mobileNumber: String,
        email: String,
        photoUri: String?,
        appliedReferralCode: String?
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: "unauthenticated"
        val userPath = "users/$uid"
        return try {
            val validUid = requireUserId()
            val cleanName = fullName.trim()
            val cleanMobile = mobileNumber.trim()
            val cleanEmail = email.trim()
            require(cleanName.length in 2..80) { "Full name must be 2 to 80 characters." }
            require(cleanMobile.length in 10..15) { "Enter a valid 10-15 digit mobile number." }
            require(cleanEmail.length in 5..120) { "Valid email address required." }

            val myReferralCode = generateReferralCode(validUid)

            var referrerLookup: ReferralCodeLookup? = null
            var rewardAmount = 10.0
            val cleanRefCode = appliedReferralCode?.trim()?.uppercase()?.takeIf { it.isNotBlank() }
            if (cleanRefCode != null) {
                val refSnap = db.collection("referral_codes").document(cleanRefCode).get().await()
                if (!refSnap.exists()) {
                    return Result.failure(IllegalArgumentException("Referral code '$cleanRefCode' was not found."))
                }
                referrerLookup = refSnap.toObject(ReferralCodeLookup::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                if (referrerLookup == null || referrerLookup.ownerUid == validUid) {
                    return Result.failure(IllegalArgumentException("Invalid referral code."))
                }
                val settingsSnap = db.collection("app_settings").document("global").get().await()
                if (settingsSnap.exists()) {
                    rewardAmount = settingsSnap.getDouble("referralRewardInr") ?: 10.0
                }
            }

            val batch = db.batch()
            val userPayload = mapOf(
                "userId" to validUid,
                "fullName" to cleanName,
                "mobileNumber" to cleanMobile,
                "email" to cleanEmail,
                "photoUri" to photoUri?.takeIf { it.isNotBlank() && it.length <= 500 },
                "role" to "user",
                "activationStatus" to "unactivated",
                "walletBalance" to 0.0,
                "pendingBalance" to 0.0,
                "totalEarnings" to 0.0,
                "todayEarnings" to 0.0,
                "totalDataSoldGb" to 0.0,
                "pendingDataGb" to 0.0,
                "totalTransactions" to 0L,
                "referralCode" to myReferralCode,
                "referredByCode" to referrerLookup?.code,
                "referredByUid" to referrerLookup?.ownerUid,
                "referralCount" to 0L,
                "referralEarnings" to 0.0,
                "blocked" to false,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            batch.set(db.collection("users").document(validUid), userPayload)

            batch.set(
                db.collection("referral_codes").document(myReferralCode),
                mapOf(
                    "code" to myReferralCode,
                    "ownerUid" to validUid,
                    "ownerName" to cleanName,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )

            if (referrerLookup != null) {
                val referralId = "ref_${validUid.take(12)}_${System.currentTimeMillis()}"
                batch.set(
                    db.collection("referrals").document(referralId),
                    mapOf(
                        "referrerUid" to referrerLookup.ownerUid,
                        "referredUid" to validUid,
                        "referredName" to cleanName,
                        "referralCode" to referrerLookup.code,
                        "rewardAmount" to rewardAmount,
                        "status" to "pending",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, userPath)
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(
        fullName: String,
        mobileNumber: String,
        photoUri: String?
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: "unauthenticated"
        val docPath = "users/$uid"
        return try {
            val validUid = requireUserId()
            val cleanName = fullName.trim()
            val cleanMobile = mobileNumber.trim()
            require(cleanName.length in 2..80) { "Name must be 2 to 80 characters." }
            require(cleanMobile.length in 10..15) { "Mobile number must be 10 to 15 digits." }

            val updates = mutableMapOf<String, Any>(
                "fullName" to cleanName,
                "mobileNumber" to cleanMobile,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!photoUri.isNullOrBlank() && photoUri.length <= 500) {
                updates["photoUri"] = photoUri
            }
            db.collection("users").document(validUid).update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    fun observeMyActivationPayments(userId: String): Flow<List<ActivationPayment>> =
        db.collection("activation_payments")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ActivationPayment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catchFirestoreErrors(OperationType.LIST, "activation_payments", userId)

    suspend fun submitActivationPayment(
        profile: UserProfile,
        utrRaw: String,
        upiId: String,
        amountInr: Double = 5.0
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: "unauthenticated"
        val cleanUtr = utrRaw.trim().uppercase()
        val paymentId = "pay_${uid.take(8)}_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        return try {
            val validUid = requireUserId()
            check(validUid == profile.userId) { "Session mismatch." }
            require(cleanUtr.length in 8..30 && cleanUtr.matches(Regex("^[A-Z0-9]+$"))) {
                "Enter a valid 8-30 character alphanumeric UPI Transaction / UTR ID."
            }

            // Pre-check UTR uniqueness to give an immediate friendly message
            val existingClaim = db.collection("utr_claims").document(cleanUtr).get().await()
            if (existingClaim.exists()) {
                return Result.failure(IllegalStateException("This UTR reference has already been submitted."))
            }

            val batch = db.batch()
            batch.set(
                db.collection("utr_claims").document(cleanUtr),
                mapOf(
                    "utr" to cleanUtr,
                    "userId" to validUid,
                    "purpose" to "activation",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                db.collection("activation_payments").document(paymentId),
                mapOf(
                    "userId" to validUid,
                    "userName" to profile.fullName,
                    "userEmail" to profile.email,
                    "amount" to amountInr,
                    "upiId" to upiId,
                    "utrReference" to cleanUtr,
                    "status" to "pending_verification",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                db.collection("users").document(validUid),
                mapOf(
                    "activationStatus" to "pending_verification",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "activation_payments/$paymentId")
            Result.failure(e)
        }
    }

    fun observeMyReferrals(userId: String): Flow<List<ReferralRecord>> =
        db.collection("referrals")
            .whereEqualTo("referrerUid", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ReferralRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catchFirestoreErrors(OperationType.LIST, "referrals", userId)

    fun observeMyNotifications(userId: String): Flow<List<AppNotification>> =
        db.collection("users").document(userId).collection("notifications")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(AppNotification::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catchFirestoreErrors(OperationType.LIST, "users/$userId/notifications", userId)

    suspend fun markNotificationRead(userId: String, notificationId: String): Result<Unit> {
        val path = "users/$userId/notifications/$notificationId"
        return try {
            val validUid = requireUserId()
            check(validUid == userId)
            db.collection("users").document(userId)
                .collection("notifications").document(notificationId)
                .update(mapOf("read" to true))
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    fun observeAnnouncements(): Flow<List<GlobalAnnouncement>> =
        db.collection("announcements")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(GlobalAnnouncement::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catchFirestoreErrors(OperationType.LIST, "announcements")

    fun observeMySupportTickets(userId: String): Flow<List<SupportTicket>> =
        db.collection("support_tickets")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(SupportTicket::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catchFirestoreErrors(OperationType.LIST, "support_tickets", userId)

    suspend fun submitSupportTicket(
        profile: UserProfile?,
        category: String,
        subject: String,
        description: String,
        utrReference: String?
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: "unauthenticated"
        val ticketId = "tkt_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        return try {
            val validUid = requireUserId()
            val userEmail = profile?.email ?: auth.currentUser?.email ?: "unknown@user.com"
            val userName = profile?.fullName ?: auth.currentUser?.displayName ?: "Dilsat User"
            val cleanSubject = subject.trim()
            val cleanDesc = description.trim()
            require(cleanSubject.length in 3..120) { "Subject must be 3 to 120 characters." }
            require(cleanDesc.length in 5..1000) { "Description must be 5 to 1000 characters." }

            val payload = mapOf(
                "userId" to validUid,
                "userName" to userName.take(80),
                "userEmail" to userEmail.take(120),
                "category" to category,
                "subject" to cleanSubject,
                "description" to cleanDesc,
                "utrReference" to utrReference?.trim()?.uppercase()?.takeIf { it.isNotBlank() && it.length <= 40 },
                "status" to "open",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            db.collection("support_tickets").document(ticketId).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "support_tickets/$ticketId")
            Result.failure(e)
        }
    }

    private fun generateReferralCode(uid: String): String {
        val suffix = uid.filter { it.isLetterOrDigit() }.uppercase().take(4).padEnd(4, 'X')
        val rand = UUID.randomUUID().toString().replace("-", "").uppercase().take(4)
        return "DL$suffix$rand"
    }
}
