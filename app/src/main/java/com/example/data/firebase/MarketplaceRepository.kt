package com.example.data.firebase

import android.content.Context
import com.example.R
import com.example.data.model.DataPackage
import com.example.data.model.DataSellRequest
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
import java.util.UUID
import kotlin.math.roundToInt

class MarketplaceRepository(
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

    fun observeDataPackages(): Flow<List<DataPackage>> =
        db.collection("data_packages")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(DataPackage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.pricePerGb }
            }
            .catchFirestoreErrors(OperationType.LIST, "data_packages")

    fun observeMySellRequests(userId: String): Flow<List<DataSellRequest>> =
        db.collection("data_sell_requests")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(DataSellRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catchFirestoreErrors(OperationType.LIST, "data_sell_requests", userId)

    suspend fun submitDataSellRequest(
        profile: UserProfile,
        packageType: String,
        packageName: String,
        packageId: String,
        quantityMb: Double,
        ratePer100MbInr: Double,
        minSellDataMb: Double,
        networkProvider: String
    ): Result<Unit> {
        val requestId = "sell_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        return try {
            val validUid = requireUserId()
            check(validUid == profile.userId)
            require(profile.isActivated && !profile.blocked) {
                "Account must be activated and active to sell data."
            }
            require(quantityMb >= minSellDataMb) {
                "Minimum data sell quantity is ${minSellDataMb.toInt()} MB."
            }
            require(quantityMb <= 10_000_000.0) {
                "Maximum single request is 10,000,000 MB."
            }
            val cleanProvider = networkProvider.trim()
            require(cleanProvider.length in 2..40) { "Select a valid network provider." }

            val estimatedPayout = (((quantityMb / 100.0) * ratePer100MbInr) * 100.0).roundToInt() / 100.0
            val quantityGb = (quantityMb / 1000.0).coerceAtLeast(0.001)
            val effectivePricePerGb = ((ratePer100MbInr * 10.0) * 100.0).roundToInt() / 100.0
            val payload = mapOf(
                "userId" to validUid,
                "userName" to profile.fullName,
                "packageId" to packageId,
                "packageName" to packageName,
                "packageType" to packageType,
                "quantityGb" to quantityGb,
                "quantityMb" to quantityMb,
                "pricePerGb" to effectivePricePerGb,
                "estimatedPayout" to estimatedPayout,
                "networkProvider" to cleanProvider,
                "status" to "pending",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("data_sell_requests").document(requestId).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "data_sell_requests/$requestId")
            Result.failure(e)
        }
    }

    fun observeMyWithdrawals(userId: String): Flow<List<WithdrawalRequest>> =
        db.collection("withdrawals")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(WithdrawalRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catchFirestoreErrors(OperationType.LIST, "withdrawals", userId)

    suspend fun requestWithdrawal(
        profile: UserProfile,
        amount: Double,
        upiId: String,
        accountHolderName: String,
        minWithdrawalInr: Double
    ): Result<Unit> {
        val withdrawalId = "wd_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        return try {
            val validUid = requireUserId()
            check(validUid == profile.userId)
            require(profile.isActivated && !profile.blocked) {
                "Account must be activated to request a withdrawal."
            }
            require(amount >= minWithdrawalInr) {
                "Minimum withdrawal amount is ₹${minWithdrawalInr.toInt()}."
            }
            require(amount <= profile.walletBalance) {
                "Insufficient available wallet balance."
            }
            val cleanUpi = upiId.trim()
            val cleanHolder = accountHolderName.trim()
            require(cleanUpi.length in 5..100 && cleanUpi.contains("@")) {
                "Enter a valid UPI ID (e.g., name@bank)."
            }
            require(cleanHolder.length in 2..80) {
                "Account holder name must be 2 to 80 characters."
            }

            val payload = mapOf(
                "userId" to validUid,
                "userName" to profile.fullName,
                "amount" to amount,
                "upiId" to cleanUpi,
                "accountHolderName" to cleanHolder,
                "status" to "pending",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("withdrawals").document(withdrawalId).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "withdrawals/$withdrawalId")
            Result.failure(e)
        }
    }

    fun observeMyTransactions(userId: String): Flow<List<WalletTransaction>> =
        db.collection("transactions")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(WalletTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catchFirestoreErrors(OperationType.LIST, "transactions", userId)
}
