package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.AdminRepository
import com.example.data.firebase.MarketplaceRepository
import com.example.data.firebase.unwrapFirestoreRootError
import com.example.data.model.ActivationPayment
import com.example.data.model.AppSettings
import com.example.data.model.DataPackage
import com.example.data.model.DataSellRequest
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

class AdminViewModel(
    private val adminRepo: AdminRepository,
    private val marketRepo: MarketplaceRepository
) : ViewModel() {

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _adminFeedback = MutableStateFlow<String?>(null)
    val adminFeedback: StateFlow<String?> = _adminFeedback.asStateFlow()

    init {
        viewModelScope.launch {
            adminRepo.seedDefaultCatalogAndSettings()
        }
    }

    val allUsersState: StateFlow<UiState<List<UserProfile>>> = adminRepo.observeAllUsers()
        .map<List<UserProfile>, UiState<List<UserProfile>>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapFirestoreRootError(error)
            Log.w(TAG, "observeAllUsers failed", root)
            emit(UiState.Error(root.localizedMessage ?: "Failed to load users"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val allPaymentsState: StateFlow<UiState<List<ActivationPayment>>> = adminRepo.observeAllActivationPayments()
        .map<List<ActivationPayment>, UiState<List<ActivationPayment>>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapFirestoreRootError(error)
            emit(UiState.Error(root.localizedMessage ?: "Failed to load payments"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val allRequestsState: StateFlow<UiState<List<DataSellRequest>>> = adminRepo.observeAllSellRequests()
        .map<List<DataSellRequest>, UiState<List<DataSellRequest>>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapFirestoreRootError(error)
            emit(UiState.Error(root.localizedMessage ?: "Failed to load requests"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val allWithdrawalsState: StateFlow<UiState<List<WithdrawalRequest>>> = adminRepo.observeAllWithdrawals()
        .map<List<WithdrawalRequest>, UiState<List<WithdrawalRequest>>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapFirestoreRootError(error)
            emit(UiState.Error(root.localizedMessage ?: "Failed to load withdrawals"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val allTransactionsState: StateFlow<UiState<List<WalletTransaction>>> = adminRepo.observeAllTransactions()
        .map<List<WalletTransaction>, UiState<List<WalletTransaction>>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapFirestoreRootError(error)
            emit(UiState.Error(root.localizedMessage ?: "Failed to load transactions"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val allPackagesState: StateFlow<UiState<List<DataPackage>>> = marketRepo.observeDataPackages()
        .map<List<DataPackage>, UiState<List<DataPackage>>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapFirestoreRootError(error)
            emit(UiState.Error(root.localizedMessage ?: "Failed to load packages"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val allReferralsState: StateFlow<UiState<List<ReferralRecord>>> = adminRepo.observeAllReferrals()
        .map<List<ReferralRecord>, UiState<List<ReferralRecord>>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapFirestoreRootError(error)
            emit(UiState.Error(root.localizedMessage ?: "Failed to load referrals"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val allTicketsState: StateFlow<UiState<List<SupportTicket>>> = adminRepo.observeAllSupportTickets()
        .map<List<SupportTicket>, UiState<List<SupportTicket>>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapFirestoreRootError(error)
            emit(UiState.Error(root.localizedMessage ?: "Failed to load support tickets"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    fun clearFeedback() {
        _adminFeedback.value = null
    }

    fun verifyPayment(payment: ActivationPayment, approve: Boolean, note: String = "") {
        runAdminTask(if (approve) "Activation approved!" else "Activation rejected.") {
            adminRepo.verifyActivationPayment(payment, approve, note)
        }
    }

    fun processSellRequest(req: DataSellRequest, status: String, note: String = "") {
        runAdminTask("Request updated to $status.") {
            adminRepo.processDataSellRequest(req, status, note)
        }
    }

    fun processWithdrawal(wd: WithdrawalRequest, status: String, utr: String = "", note: String = "") {
        runAdminTask("Withdrawal marked as $status.") {
            adminRepo.processWithdrawal(wd, status, utr, note)
        }
    }

    fun processReferral(ref: ReferralRecord, approve: Boolean) {
        runAdminTask(if (approve) "Referral credited!" else "Referral rejected.") {
            adminRepo.processReferral(ref, approve)
        }
    }

    fun toggleUserBlocked(user: UserProfile) {
        val next = !user.blocked
        runAdminTask(if (next) "User blocked." else "User unblocked.") {
            adminRepo.toggleUserBlocked(user, next)
        }
    }

    fun saveDataPackage(pkg: DataPackage) {
        runAdminTask("Package saved.") {
            adminRepo.upsertDataPackage(pkg)
        }
    }

    fun deleteDataPackage(packageId: String) {
        runAdminTask("Package deleted.") {
            adminRepo.deleteDataPackage(packageId)
        }
    }

    fun updateSettings(settings: AppSettings) {
        runAdminTask("App settings saved.") {
            adminRepo.updateAppSettings(settings)
        }
    }

    fun broadcastAnnouncement(title: String, message: String, priority: String) {
        runAdminTask("Announcement broadcasted!") {
            adminRepo.broadcastAnnouncement(title, message, priority)
        }
    }

    fun sendNotification(targetUid: String, title: String, message: String, category: String) {
        runAdminTask("Notification sent!") {
            adminRepo.sendDirectNotification(targetUid, title, message, category)
        }
    }

    fun replySupportTicket(ticket: SupportTicket, status: String, reply: String) {
        runAdminTask("Ticket updated.") {
            adminRepo.replySupportTicket(ticket, status, reply)
        }
    }

    fun generateCsvReport(transactions: List<WalletTransaction>): String {
        return adminRepo.buildTransactionCsvReport(transactions)
    }

    private fun runAdminTask(successMsg: String, block: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = block()
            _isBusy.value = false
            res.fold(
                onSuccess = { _adminFeedback.value = successMsg },
                onFailure = { _adminFeedback.value = it.localizedMessage ?: "Operation failed" }
            )
        }
    }

    private companion object {
        const val TAG = "AdminVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
