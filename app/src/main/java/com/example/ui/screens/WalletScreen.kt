package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.AppSettings
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.model.WithdrawalRequest
import com.example.ui.components.AnimatedCurrencyText
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FintechCard
import com.example.ui.components.NeonGradientButton
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatDateTime
import com.example.ui.components.formatInr
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalDilsatColors
import com.example.ui.viewmodel.MainViewModel

@Composable
fun WalletScreen(
    profile: UserProfile,
    settings: AppSettings,
    withdrawalsState: UiState<List<WithdrawalRequest>>,
    isSubmitting: Boolean,
    viewModel: MainViewModel
) {
    val ext = LocalDilsatColors.current
    val withdrawals = (withdrawalsState as? UiState.Success)?.data.orEmpty()

    var amountInput by remember { mutableStateOf("") }
    var upiInput by remember { mutableStateOf("") }
    var holderInput by remember { mutableStateOf(profile.fullName) }
    var formError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Digital Wallet & Payouts",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Balance Overview
            item {
                FintechCard(
                    accentBorder = ext.neonGreen.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "AVAILABLE BALANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = ext.electricCyan
                    )
                    Spacer(Modifier.height(4.dp))
                    AnimatedCurrencyText(
                        amount = profile.walletBalance,
                        style = MaterialTheme.typography.displayLarge,
                        color = ext.neonGreen
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Pending Balance",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatInr(profile.pendingBalance),
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = ext.statusPending
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Total Earnings",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatInr(profile.totalEarnings),
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = ext.electricCyan
                            )
                        }
                    }
                }
            }

            // Withdrawal Form
            item {
                FintechCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Payments,
                            contentDescription = null,
                            tint = ext.vividPurple
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Request UPI Withdrawal",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Minimum withdrawal: ${formatInr(settings.minWithdrawalInr)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ext.electricCyan
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it; formError = null },
                        label = { Text("Amount (₹)") },
                        leadingIcon = { Icon(Icons.Filled.CurrencyRupee, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_amount_input")
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = upiInput,
                        onValueChange = { upiInput = it; formError = null },
                        label = { Text("Recipient UPI ID") },
                        placeholder = { Text("yourname@okaxis") },
                        leadingIcon = { Icon(Icons.Filled.AlternateEmail, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_upi_input")
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = holderInput,
                        onValueChange = { holderInput = it; formError = null },
                        label = { Text("Account Holder Name") },
                        leadingIcon = { Icon(Icons.Filled.AccountCircle, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_holder_input")
                    )

                    if (formError != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = formError ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ext.statusError
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                    NeonGradientButton(
                        text = "REQUEST WITHDRAWAL",
                        usePurpleAccent = true,
                        isLoading = isSubmitting,
                        testTag = "submit_withdrawal_button",
                        onClick = {
                            val amt = amountInput.toDoubleOrNull()
                            when {
                                amt == null || amt <= 0 -> formError = "Enter a valid amount."
                                amt < settings.minWithdrawalInr -> formError = "Minimum withdrawal is ${formatInr(settings.minWithdrawalInr)}."
                                amt > profile.walletBalance -> formError = "Amount exceeds available wallet balance."
                                !upiInput.contains("@") || upiInput.trim().length < 5 -> formError = "Enter a valid UPI ID."
                                holderInput.trim().length < 2 -> formError = "Enter account holder name."
                                else -> {
                                    viewModel.requestWithdrawal(
                                        profile = profile,
                                        amount = amt,
                                        upiId = upiInput,
                                        holderName = holderInput,
                                        minInr = settings.minWithdrawalInr,
                                        onSuccess = {
                                            amountInput = ""
                                        }
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Withdrawal History
            item {
                Text(
                    text = "Withdrawal History",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (withdrawals.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Filled.AccountBalanceWallet,
                        title = "No withdrawals yet",
                        subtitle = "Your UPI payout requests and status updates appear here."
                    )
                }
            } else {
                items(withdrawals, key = { it.id }) { wd ->
                    FintechCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = wd.upiId,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${wd.accountHolderName} • ${formatDateTime(wd.createdAt)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (!wd.utrNumber.isNullOrBlank()) {
                                    Text(
                                        text = "Payout UTR: ${wd.utrNumber}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ext.neonGreen
                                    )
                                }
                                if (!wd.adminNote.isNullOrBlank()) {
                                    Text(
                                        text = wd.adminNote,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatInr(wd.amount),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(4.dp))
                                StatusBadge(status = wd.status)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}
