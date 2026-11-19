package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppSettings
import com.example.data.model.UiState
import com.example.data.model.WithdrawalRequest
import com.example.ui.components.FintechBackdrop
import com.example.ui.components.FintechCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatDateTime
import com.example.ui.components.formatInr
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalDilsatColors
import com.example.ui.viewmodel.AdminViewModel

@Composable
fun AdminPanelScreen(
    adminViewModel: AdminViewModel,
    settings: AppSettings,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val ext = LocalDilsatColors.current
    val snackbarHostState = remember { SnackbarHostState() }

    val usersState by adminViewModel.allUsersState.collectAsStateWithLifecycle()
    val paymentsState by adminViewModel.allPaymentsState.collectAsStateWithLifecycle()
    val requestsState by adminViewModel.allRequestsState.collectAsStateWithLifecycle()
    val withdrawalsState by adminViewModel.allWithdrawalsState.collectAsStateWithLifecycle()
    val transactionsState by adminViewModel.allTransactionsState.collectAsStateWithLifecycle()
    val packagesState by adminViewModel.allPackagesState.collectAsStateWithLifecycle()
    val referralsState by adminViewModel.allReferralsState.collectAsStateWithLifecycle()
    val ticketsState by adminViewModel.allTicketsState.collectAsStateWithLifecycle()
    val feedback by adminViewModel.adminFeedback.collectAsStateWithLifecycle()

    LaunchedEffect(feedback) {
        feedback?.let {
            snackbarHostState.showSnackbar(it)
            adminViewModel.clearFeedback()
        }
    }

    val users = (usersState as? UiState.Success)?.data.orEmpty()
    val payments = (paymentsState as? UiState.Success)?.data.orEmpty()
    val requests = (requestsState as? UiState.Success)?.data.orEmpty()
    val withdrawals = (withdrawalsState as? UiState.Success)?.data.orEmpty()
    val transactions = (transactionsState as? UiState.Success)?.data.orEmpty()
    val packages = (packagesState as? UiState.Success)?.data.orEmpty()
    val referrals = (referralsState as? UiState.Success)?.data.orEmpty()
    val tickets = (ticketsState as? UiState.Success)?.data.orEmpty()

    val activatedUsers = users.count { it.isActivated }
    val totalActivationPaid = payments.filter { it.status == "verified" }.sumOf { it.amount }
    val pendingSellRequests = requests.count { it.status == "pending" }
    val totalWithdrawalsPaid = withdrawals.filter { it.status == "paid" }.sumOf { it.amount }
    val totalDataSoldValue = requests.filter { it.status == "approved" || it.status == "completed" }.sumOf { it.estimatedPayout }
    val totalAppRevenue = totalActivationPaid + (totalDataSoldValue * 0.15)

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Payments", "Requests", "Withdrawals", "Users & Refs", "Catalog & Config")
    var payoutDialogWd by remember { mutableStateOf<WithdrawalRequest?>(null) }

    FintechBackdrop {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("admin_back_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                            Column {
                                Text(
                                    text = "Admin Command Center",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Verified RBAC Operations & Payouts",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ext.vividPurple
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                val csv = adminViewModel.generateCsvReport(transactions)
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_SUBJECT, "Dilsat Transaction Report")
                                    putExtra(Intent.EXTRA_TEXT, csv)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Export Transaction Report"))
                            },
                            modifier = Modifier.testTag("export_csv_button")
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = "Export CSV")
                            Spacer(Modifier.width(4.dp))
                            Text("Export CSV")
                        }
                    }
                }

                // KPI Overview Grid
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        AdminKpiCard("Users", "${users.size} (${activatedUsers} active)", ext.electricCyan, Modifier.weight(1f))
                        AdminKpiCard("Activation Paid", formatInr(totalActivationPaid), ext.neonGreen, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        AdminKpiCard("Sell Requests", "${requests.size} (${pendingSellRequests} pend.)", ext.statusPending, Modifier.weight(1f))
                        AdminKpiCard("Paid Withdrawals", formatInr(totalWithdrawalsPaid), ext.vividPurple, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    AdminKpiCard("Total App Revenue (Fees + Margin)", formatInr(totalAppRevenue), ext.neonGreen, Modifier.fillMaxWidth())
                }

                // Tab Bar
                item {
                    ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 0.dp) {
                        tabs.forEachIndexed { idx, title ->
                            Tab(
                                selected = selectedTab == idx,
                                onClick = { selectedTab = idx },
                                text = { Text(title) }
                            )
                        }
                    }
                }

                when (selectedTab) {
                    // 0. Activation Payments
                    0 -> {
                        if (payments.isEmpty()) {
                            item { Text("No activation payment records yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        } else {
                            items(payments, key = { it.id }) { pay ->
                                FintechCard(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(pay.userName, style = MaterialTheme.typography.titleMedium)
                                            Text(pay.userEmail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("UTR: ${pay.utrReference} • ${formatInr(pay.amount)}", style = MaterialTheme.typography.labelMedium, color = ext.electricCyan)
                                            Text(formatDateTime(pay.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        StatusBadge(status = pay.status)
                                    }
                                    if (pay.status == "pending_verification") {
                                        Spacer(Modifier.height(10.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { adminViewModel.verifyPayment(pay, true) },
                                                colors = ButtonDefaults.buttonColors(containerColor = ext.neonGreen)
                                            ) { Text("Verify & Activate", color = MaterialTheme.colorScheme.onPrimary) }
                                            OutlinedButton(
                                                onClick = { adminViewModel.verifyPayment(pay, false, "UTR not matched on merchant statement") }
                                            ) { Text("Reject", color = ext.statusError) }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 1. Data Sell Requests
                    1 -> {
                        if (requests.isEmpty()) {
                            item { Text("No data selling requests yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        } else {
                            items(requests, key = { it.id }) { req ->
                                FintechCard(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${req.userName} • ${req.packageName}", style = MaterialTheme.typography.titleMedium)
                                            Text("${req.displayMb.toInt()} MB (${req.networkProvider}) = ${formatInr(req.estimatedPayout)}", style = MaterialTheme.typography.bodyMedium, color = ext.neonGreen)
                                            Text(formatDateTime(req.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        StatusBadge(status = if (req.status == "completed") "approved" else req.status)
                                    }
                                    if (req.status == "pending") {
                                        Spacer(Modifier.height(10.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { adminViewModel.processSellRequest(req, "approved") },
                                                colors = ButtonDefaults.buttonColors(containerColor = ext.neonGreen)
                                            ) { Text("Approve & Credit", color = MaterialTheme.colorScheme.onPrimary) }
                                            OutlinedButton(onClick = { adminViewModel.processSellRequest(req, "rejected", "Verification failed") }) {
                                                Text("Reject", color = ext.statusError)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Withdrawals
                    2 -> {
                        if (withdrawals.isEmpty()) {
                            item { Text("No withdrawal requests yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        } else {
                            items(withdrawals, key = { it.id }) { wd ->
                                FintechCard(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${wd.accountHolderName} • ${formatInr(wd.amount)}", style = MaterialTheme.typography.titleMedium)
                                            Text("UPI: ${wd.upiId}", style = MaterialTheme.typography.labelMedium, color = ext.electricCyan)
                                            Text(formatDateTime(wd.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        StatusBadge(status = wd.status)
                                    }
                                    if (wd.status == "pending" || wd.status == "processing") {
                                        Spacer(Modifier.height(10.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            if (wd.status == "pending") {
                                                OutlinedButton(onClick = { adminViewModel.processWithdrawal(wd, "processing") }) {
                                                    Text("Processing")
                                                }
                                            }
                                            Button(
                                                onClick = { payoutDialogWd = wd },
                                                colors = ButtonDefaults.buttonColors(containerColor = ext.neonGreen)
                                            ) { Text("Mark Paid", color = MaterialTheme.colorScheme.onPrimary) }
                                            OutlinedButton(onClick = { adminViewModel.processWithdrawal(wd, "rejected", "", "Invalid UPI / rejected") }) {
                                                Text("Reject", color = ext.statusError)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Users & Referrals
                    3 -> {
                        item { Text("Pending & Tracked Referrals (${referrals.size})", style = MaterialTheme.typography.titleMedium) }
                        items(referrals, key = { "ref_${it.id}" }) { ref ->
                            FintechCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Referred: ${ref.referredName}", style = MaterialTheme.typography.titleMedium)
                                        Text("Code: ${ref.referralCode} • Reward: ${formatInr(ref.rewardAmount)}", style = MaterialTheme.typography.bodyMedium, color = ext.neonGreen)
                                    }
                                    StatusBadge(status = ref.status)
                                }
                                if (ref.status == "pending") {
                                    Spacer(Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = { adminViewModel.processReferral(ref, true) }) { Text("Credit Bonus") }
                                        OutlinedButton(onClick = { adminViewModel.processReferral(ref, false) }) { Text("Reject", color = ext.statusError) }
                                    }
                                }
                            }
                        }
                        item { Text("Registered Users (${users.size})", style = MaterialTheme.typography.titleMedium) }
                        items(users, key = { "usr_${it.userId}" }) { u ->
                            AdminUserCard(
                                user = u,
                                onToggleBlock = { adminViewModel.toggleUserBlocked(u) },
                                onNotify = { t, m -> adminViewModel.sendNotification(u.userId, t, m, "announcement") }
                            )
                        }
                    }

                    // 4. Packages, Settings, Announcements, Tickets
                    4 -> {
                        item {
                            AdminCatalogAndSettingsSection(
                                packages = packages,
                                settings = settings,
                                tickets = tickets,
                                adminViewModel = adminViewModel
                            )
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }

    payoutDialogWd?.let { wd ->
        var payoutUtr by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { payoutDialogWd = null },
            title = { Text("Confirm Payout (${formatInr(wd.amount)})") },
            text = {
                OutlinedTextField(
                    value = payoutUtr,
                    onValueChange = { payoutUtr = it },
                    label = { Text("Bank / UPI Payout UTR") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        adminViewModel.processWithdrawal(wd, "paid", payoutUtr, "Paid via UPI")
                        payoutDialogWd = null
                    }
                ) { Text("Confirm Paid") }
            },
            dismissButton = {
                TextButton(onClick = { payoutDialogWd = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AdminKpiCard(
    title: String,
    value: String,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    FintechCard(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold),
            color = accent
        )
    }
}
