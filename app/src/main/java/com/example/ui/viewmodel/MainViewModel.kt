package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.AppRepository
import com.example.data.firebase.MarketplaceRepository
import com.example.data.firebase.unwrapFirestoreRootError
import com.example.data.model.ActivationPayment
import com.example.data.model.AppNotification
import com.example.data.model.AppSettings
import com.example.data.model.DataPackage
import com.example.data.model.DataSellRequest
import com.example.data.model.GlobalAnnouncement
import com.example.data.model.ReferralCodeLookup
import com.example.data.model.ReferralRecord
import com.example.data.model.SupportTicket
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.model.WalletTransaction
import com.example.data.model.WithdrawalRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val appRepo: AppRepository,
    private val marketRepo: MarketplaceRepository,
    val currentUserId: String
) : ViewModel() {

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    val userProfileState: StateFlow<UiState<UserProfile?>> = appRepo.observeUserProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapFirestoreRootError(error)
            Log.w(TAG, "Error observing profile", root)
            emit(UiState.Error(root.localizedMessage ?: "Failed to load profile"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val appSettings: StateFlow<AppSettings> = appRepo.observeAppSettings()
        .catch { error ->
            val root = unwrapFirestoreRootError(error)
            Log.w(TAG, "Error observing settings", root)
            emit(AppSettings())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), AppSettings())

    val isAdmin: StateFlow<Boolean> = appRepo.observeAdminStatus(currentUserId)
        .catch { error ->
            unwrapFirestoreRootError(error)
            emit(false)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), false)

    val activationPaymentsState: StateFlow<UiState<List<ActivationPayment>>> = appRepo.observeMyActivationPayments(currentUserId)
        .map<List<ActivationPayment>, UiState<List<ActivationPayment>>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapFirestoreRootError(error)
            emit(UiState.Error(root.localizedMessage ?: "Failed to load activation payments"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val dataPackagesState: StateFlow<UiState<List<DataPackage>>> = marketRepo.observeDataPackages()
        .map<List<DataPackage>, UiState<List<DataPackage>>> { list ->
            UiState.Success(list.filter { it.active })
        }
        .catch { error ->
            val root = unwrapFirestoreRootError(error)
            emit(UiState.Error(root.localizedMessage ?: "Failed to load data packages"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val sellRequestsState: StateFlow<UiState<List<DataSellRequest>>> = marketRepo.observeMySellRequests(currentUserId)
        .map<List<DataSellRequest>, UiState<List<DataSellRequest>>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapFirestoreRootError(error)
            emit(UiState.Error(root.localizedMessage ?: "Failed to load sell requests"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val withdrawalsState: StateFlow<UiState<List<WithdrawalRequest>>> = marketRepo.observeMyWithdrawals(currentUserId)
        .map<List<WithdrawalRequest>, UiState<List<WithdrawalRequest>>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapFirestoreRootError(error)
            emit(UiState.Error(root.localizedMessage ?: "Failed to load withdrawals"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val transactionsState: StateFlow<UiState<List<WalletTransaction>>> = marketRepo.observeMyTransactions(currentUserId)
        .map<List<WalletTransaction>, UiState<List<WalletTransaction>>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapFirestoreRootError(error)
            emit(UiState.Error(root.localizedMessage ?: "Failed to load transactions"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val referralsState: StateFlow<UiState<List<ReferralRecord>>> = appRepo.observeMyReferrals(currentUserId)
        .map<List<ReferralRecord>, UiState<List<ReferralRecord>>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapFirestoreRootError(error)
            emit(UiState.Error(root.localizedMessage ?: "Failed to load referrals"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val notificationsState: StateFlow<UiState<List<AppNotification>>> = appRepo.observeMyNotifications(currentUserId)
        .map<List<AppNotification>, UiState<List<AppNotification>>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapFirestoreRootError(error)
            emit(UiState.Error(root.localizedMessage ?: "Failed to load notifications"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val announcements: StateFlow<List<GlobalAnnouncement>> = appRepo.observeAnnouncements()
        .catch { error ->
            unwrapFirestoreRootError(error)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    val supportTicketsState: StateFlow<UiState<List<SupportTicket>>> = appRepo.observeMySupportTickets(currentUserId)
        .map<List<SupportTicket>, UiState<List<SupportTicket>>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapFirestoreRootError(error)
            emit(UiState.Error(root.localizedMessage ?: "Failed to load support tickets"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    fun showMessage(msg: String) {
        _feedbackMessage.value = msg
    }

    fun createProfile(
        fullName: String,
        mobileNumber: String,
        email: String,
        photoUri: String?,
        referralCode: String?,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = appRepo.createUserProfile(fullName, mobileNumber, email, photoUri, referralCode)
            _isSubmitting.value = false
            result.fold(
                onSuccess = {
                    _feedbackMessage.value = "Account created! Complete ₹5 activation to unlock."
                    onSuccess()
                },
                onFailure = { e ->
                    _feedbackMessage.value = e.localizedMessage ?: "Registration failed"
                }
            )
        }
    }

    fun validateReferralCode(code: String, onResult: (ReferralCodeLookup?) -> Unit) {
        viewModelScope.launch {
            val result = appRepo.lookupReferralCode(code)
            result.fold(
                onSuccess = { lookup ->
                    if (lookup == null) _feedbackMessage.value = "Referral code not found."
                    onResult(lookup)
                },
                onFailure = { e ->
                    _feedbackMessage.value = e.localizedMessage ?: "Invalid referral code"
                    onResult(null)
                }
            )
        }
    }

    fun updateProfile(fullName: String, mobileNumber: String, photoUri: String?) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val res = appRepo.updateUserProfile(fullName, mobileNumber, photoUri)
            _isSubmitting.value = false
            res.fold(
                onSuccess = { _feedbackMessage.value = "Profile updated successfully." },
                onFailure = { _feedbackMessage.value = it.localizedMessage ?: "Update failed" }
            )
        }
    }

    fun submitActivationPayment(profile: UserProfile, utr: String, settings: AppSettings) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val res = appRepo.submitActivationPayment(
                profile = profile,
                utrRaw = utr,
                upiId = settings.activationUpiId,
                amountInr = settings.activationFeeInr
            )
            _isSubmitting.value = false
            res.fold(
                onSuccess = {
                    _feedbackMessage.value = "UTR submitted for backend verification."
                },
                onFailure = { e ->
                    _feedbackMessage.value = e.localizedMessage ?: "Payment submission failed"
                }
            )
        }
    }

    fun submitSellRequest(
        profile: UserProfile,
        packageType: String,
        packageName: String,
        packageId: String,
        quantityMb: Double,
        ratePer100MbInr: Double,
        minSellDataMb: Double,
        provider: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val res = marketRepo.submitDataSellRequest(
                profile = profile,
                packageType = packageType,
                packageName = packageName,
                packageId = packageId,
                quantityMb = quantityMb,
                ratePer100MbInr = ratePer100MbInr,
                minSellDataMb = minSellDataMb,
                networkProvider = provider
            )
            _isSubmitting.value = false
            res.fold(
                onSuccess = {
                    _feedbackMessage.value = "Sell request for ${quantityMb.toInt()} MB submitted for review!"
                    onSuccess()
                },
                onFailure = { e ->
                    _feedbackMessage.value = e.localizedMessage ?: "Submission failed"
                }
            )
        }
    }

    fun requestWithdrawal(profile: UserProfile, amount: Double, upiId: String, holderName: String, minInr: Double, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val res = marketRepo.requestWithdrawal(profile, amount, upiId, holderName, minInr)
            _isSubmitting.value = false
            res.fold(
                onSuccess = {
                    _feedbackMessage.value = "Withdrawal request submitted!"
                    onSuccess()
                },
                onFailure = { e ->
                    _feedbackMessage.value = e.localizedMessage ?: "Withdrawal request failed"
                }
            )
        }
    }

    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch {
            appRepo.markNotificationRead(currentUserId, notificationId)
        }
    }

    fun submitSupportTicket(profile: UserProfile?, category: String, subject: String, description: String, utr: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val res = appRepo.submitSupportTicket(profile, category, subject, description, utr)
            _isSubmitting.value = false
            res.fold(
                onSuccess = {
                    _feedbackMessage.value = "Support ticket submitted! We will reply shortly."
                    onSuccess()
                },
                onFailure = { e ->
                    _feedbackMessage.value = e.localizedMessage ?: "Failed to submit ticket"
                }
            )
        }
    }

    fun claimAdminAccess() {
        viewModelScope.launch {
            _isSubmitting.value = true
            val res = appRepo.claimBootstrappedAdmin()
            _isSubmitting.value = false
            res.fold(
                onSuccess = { _feedbackMessage.value = "Admin privileges verified." },
                onFailure = { _feedbackMessage.value = "This account is not authorized as an admin." }
            )
        }
    }

    private companion object {
        const val TAG = "MainVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
