package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class AppSettings(
    @DocumentId val id: String = "",
    val activationFeeInr: Double = 5.0,
    val activationUpiId: String = "janara2@ptyes",
    val minWithdrawalInr: Double = 50.0,
    val referralRewardInr: Double = 10.0,
    val referralsEnabled: Boolean = true,
    val supportEmail: String = "support@dilsatdatasell.com",
    val announcementBanner: String = "",
    val ratePer100MbInr: Double = 2.0,
    val minSellDataMb: Double = 100.0,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val mobileNumber: String = "",
    val email: String = "",
    val photoUri: String? = null,
    val role: String = "user",
    val activationStatus: String = "unactivated",
    val activationFailureReason: String? = null,
    val walletBalance: Double = 0.0,
    val pendingBalance: Double = 0.0,
    val totalEarnings: Double = 0.0,
    val todayEarnings: Double = 0.0,
    val totalDataSoldGb: Double = 0.0,
    val pendingDataGb: Double = 0.0,
    val totalTransactions: Long = 0L,
    val referralCode: String = "",
    val referredByCode: String? = null,
    val referredByUid: String? = null,
    val referralCount: Long = 0L,
    val referralEarnings: Double = 0.0,
    val blocked: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val isActivated: Boolean get() = activationStatus == "activated"
    val isPendingVerification: Boolean get() = activationStatus == "pending_verification"
    val isRejected: Boolean get() = activationStatus == "rejected"
}

@Keep
@IgnoreExtraProperties
data class ReferralCodeLookup(
    @DocumentId val id: String = "",
    val code: String = "",
    val ownerUid: String = "",
    val ownerName: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ReferralRecord(
    @DocumentId val id: String = "",
    val referrerUid: String = "",
    val referredUid: String = "",
    val referredName: String = "",
    val referralCode: String = "",
    val rewardAmount: Double = 0.0,
    val status: String = "pending",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ActivationPayment(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val amount: Double = 5.0,
    val upiId: String = "janara2@ptyes",
    val utrReference: String = "",
    val status: String = "pending_verification",
    val adminNote: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class DataPackage(
    @DocumentId val id: String = "",
    val name: String = "",
    val packageType: String = "4G",
    val pricePerGb: Double = 0.0,
    val minGb: Double = 1.0,
    val maxGb: Double = 100.0,
    val description: String = "",
    val active: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class DataSellRequest(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val packageId: String = "",
    val packageName: String = "",
    val packageType: String = "4G",
    val quantityGb: Double = 0.0,
    val quantityMb: Double = 0.0,
    val pricePerGb: Double = 0.0,
    val estimatedPayout: Double = 0.0,
    val networkProvider: String = "",
    val status: String = "pending",
    val adminNote: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val displayMb: Double get() = if (quantityMb > 0.0) quantityMb else quantityGb * 1000.0
}

@Keep
@IgnoreExtraProperties
data class WithdrawalRequest(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val amount: Double = 0.0,
    val upiId: String = "",
    val accountHolderName: String = "",
    val status: String = "pending",
    val adminNote: String? = null,
    val utrNumber: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class WalletTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val referenceCode: String = "",
    val type: String = "data_sale_credit",
    val direction: String = "credit",
    val amount: Double = 0.0,
    val status: String = "completed",
    val description: String = "",
    val relatedId: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AppNotification(
    @DocumentId val id: String = "",
    val title: String = "",
    val message: String = "",
    val category: String = "announcement",
    val read: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class GlobalAnnouncement(
    @DocumentId val id: String = "",
    val title: String = "",
    val message: String = "",
    val priority: String = "normal",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SupportTicket(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val category: String = "general",
    val subject: String = "",
    val description: String = "",
    val utrReference: String? = null,
    val status: String = "open",
    val adminReply: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
