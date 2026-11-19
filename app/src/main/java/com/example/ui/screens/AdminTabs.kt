package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.DataPackage
import com.example.data.model.SupportTicket
import com.example.data.model.UserProfile
import com.example.ui.components.FintechCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatDateTime
import com.example.ui.components.formatInr
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalDilsatColors
import com.example.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminCatalogAndSettingsSection(
    packages: List<DataPackage>,
    settings: AppSettings,
    tickets: List<SupportTicket>,
    adminViewModel: AdminViewModel
) {
    val ext = LocalDilsatColors.current
    var editingPkg by remember { mutableStateOf<DataPackage?>(null) }

    var ratePer100MbText by remember(settings.ratePer100MbInr) { mutableStateOf(settings.ratePer100MbInr.toString()) }
    var minSellDataMbText by remember(settings.minSellDataMb) { mutableStateOf(settings.minSellDataMb.toInt().toString()) }
    var minWithdrawText by remember(settings.minWithdrawalInr) { mutableStateOf(settings.minWithdrawalInr.toString()) }
    var refRewardText by remember(settings.referralRewardInr) { mutableStateOf(settings.referralRewardInr.toString()) }
    var upiText by remember(settings.activationUpiId) { mutableStateOf(settings.activationUpiId) }
    var bannerText by remember(settings.announcementBanner) { mutableStateOf(settings.announcementBanner) }
    var referralsEnabled by remember(settings.referralsEnabled) { mutableStateOf(settings.referralsEnabled) }

    var annTitle by remember { mutableStateOf("") }
    var annBody by remember { mutableStateOf("") }
    var replyTicket by remember { mutableStateOf<SupportTicket?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Marketplace Data Packages
        FintechCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Data Packages & Prices", style = MaterialTheme.typography.titleMedium)
                Button(onClick = { editingPkg = DataPackage() }) {
                    Text("+ Add Package")
                }
            }
            Spacer(Modifier.height(10.dp))
            packages.forEach { pkg ->
                FintechCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${pkg.name} (${pkg.packageType})",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${formatInr(pkg.pricePerGb)}/GB • ${pkg.minGb.toInt()}-${pkg.maxGb.toInt()} GB • ${if (pkg.active) "Active" else "Hidden"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ext.electricCyan
                            )
                        }
                        Row {
                            TextButton(onClick = { editingPkg = pkg }) { Text("Edit") }
                            TextButton(onClick = { adminViewModel.deleteDataPackage(pkg.id) }) {
                                Text("Delete", color = ext.statusError)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }

        // Global Admin Rules & Settings
        FintechCard(modifier = Modifier.fillMaxWidth()) {
            Text("Platform Rules & Limits", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = ratePer100MbText,
                    onValueChange = { ratePer100MbText = it },
                    label = { Text("Rate / 100 MB (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_rate_per_100mb_input")
                )
                OutlinedTextField(
                    value = minSellDataMbText,
                    onValueChange = { minSellDataMbText = it },
                    label = { Text("Min Sell (MB)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_min_sell_mb_input")
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = minWithdrawText,
                onValueChange = { minWithdrawText = it },
                label = { Text("Minimum Withdrawal (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = refRewardText,
                onValueChange = { refRewardText = it },
                label = { Text("Referral Reward (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = upiText,
                onValueChange = { upiText = it },
                label = { Text("Activation Merchant UPI ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = bannerText,
                onValueChange = { bannerText = it },
                label = { Text("Dashboard Announcement Banner") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Referral Program Enabled", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = referralsEnabled, onCheckedChange = { referralsEnabled = it })
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    val rate100Mb = (ratePer100MbText.toDoubleOrNull() ?: settings.ratePer100MbInr).coerceIn(0.1, 10000.0)
                    val minMb = (minSellDataMbText.toDoubleOrNull() ?: settings.minSellDataMb).coerceIn(1.0, 100000.0)
                    val minW = minWithdrawText.toDoubleOrNull() ?: settings.minWithdrawalInr
                    val refR = refRewardText.toDoubleOrNull() ?: settings.referralRewardInr
                    adminViewModel.updateSettings(
                        settings.copy(
                            ratePer100MbInr = rate100Mb,
                            minSellDataMb = minMb,
                            minWithdrawalInr = minW,
                            referralRewardInr = refR,
                            activationUpiId = upiText.ifBlank { settings.activationUpiId },
                            announcementBanner = bannerText,
                            referralsEnabled = referralsEnabled
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_save_settings_button")
            ) {
                Text("Save Platform Settings")
            }
        }

        // Broadcast Announcement
        FintechCard(modifier = Modifier.fillMaxWidth()) {
            Text("Broadcast Announcement", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = annTitle,
                onValueChange = { annTitle = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = annBody,
                onValueChange = { annBody = it },
                label = { Text("Announcement Message") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (annTitle.isNotBlank() && annBody.isNotBlank()) {
                        adminViewModel.broadcastAnnouncement(annTitle, annBody, "high")
                        annTitle = ""
                        annBody = ""
                    }
                },
                enabled = annTitle.isNotBlank() && annBody.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send Global Announcement")
            }
        }

        // Support Tickets Management
        FintechCard(modifier = Modifier.fillMaxWidth()) {
            Text("Support Tickets (${tickets.size})", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            if (tickets.isEmpty()) {
                Text("No support tickets yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                tickets.forEach { ticket ->
                    FintechCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(ticket.subject, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            StatusBadge(status = ticket.status)
                        }
                        Text("${ticket.userName} (${ticket.userEmail})", style = MaterialTheme.typography.labelSmall, color = ext.electricCyan)
                        Text(ticket.description, style = MaterialTheme.typography.bodyMedium)
                        if (!ticket.utrReference.isNullOrBlank()) {
                            Text("UTR: ${ticket.utrReference}", style = MaterialTheme.typography.labelMedium, color = ext.neonGreen)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                            OutlinedButton(onClick = { replyTicket = ticket }) {
                                Text("Reply / Resolve")
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    // Package Editor Dialog
    editingPkg?.let { initial ->
        var name by remember { mutableStateOf(initial.name) }
        var type by remember { mutableStateOf(initial.packageType) }
        var price by remember { mutableStateOf(if (initial.pricePerGb > 0) initial.pricePerGb.toString() else "12.0") }
        var minGb by remember { mutableStateOf(initial.minGb.toString()) }
        var maxGb by remember { mutableStateOf(initial.maxGb.toString()) }
        var desc by remember { mutableStateOf(initial.description) }
        var active by remember { mutableStateOf(initial.active) }

        AlertDialog(
            onDismissRequest = { editingPkg = null },
            title = { Text(if (initial.id.isBlank()) "Create Data Package" else "Edit Data Package") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Package Name") }, singleLine = true)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("4G", "5G", "Broadband", "Rollover").forEach { t ->
                            FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) })
                        }
                    }
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price per GB (₹)") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = minGb, onValueChange = { minGb = it }, label = { Text("Min GB") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = maxGb, onValueChange = { maxGb = it }, label = { Text("Max GB") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Active in Marketplace", modifier = Modifier.weight(1f))
                        Switch(checked = active, onCheckedChange = { active = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = price.toDoubleOrNull() ?: 10.0
                        val mn = minGb.toDoubleOrNull() ?: 1.0
                        val mx = (maxGb.toDoubleOrNull() ?: 100.0).coerceAtLeast(mn)
                        if (name.trim().length >= 2 && desc.trim().isNotEmpty()) {
                            adminViewModel.saveDataPackage(
                                initial.copy(
                                    name = name.trim(),
                                    packageType = type,
                                    pricePerGb = p,
                                    minGb = mn,
                                    maxGb = mx,
                                    description = desc.trim(),
                                    active = active
                                )
                            )
                            editingPkg = null
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingPkg = null }) { Text("Cancel") }
            }
        )
    }

    // Support Ticket Reply Dialog
    replyTicket?.let { ticket ->
        var replyText by remember { mutableStateOf(ticket.adminReply.orEmpty()) }
        var newStatus by remember { mutableStateOf("resolved") }
        AlertDialog(
            onDismissRequest = { replyTicket = null },
            title = { Text("Update Ticket") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("open", "in_progress", "resolved").forEach { st ->
                            FilterChip(selected = newStatus == st, onClick = { newStatus = st }, label = { Text(st) })
                        }
                    }
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        label = { Text("Admin Reply") },
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        adminViewModel.replySupportTicket(ticket, newStatus, replyText)
                        replyTicket = null
                    }
                ) { Text("Update") }
            },
            dismissButton = {
                TextButton(onClick = { replyTicket = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AdminUserCard(
    user: UserProfile,
    onToggleBlock: () -> Unit,
    onNotify: (String, String) -> Unit
) {
    val ext = LocalDilsatColors.current
    var showNotifyDialog by remember { mutableStateOf(false) }

    FintechCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(user.fullName, style = MaterialTheme.typography.titleMedium)
                Text("${user.email} • ${user.mobileNumber}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "Balance: ${formatInr(user.walletBalance)} • Sold: ${user.totalDataSoldGb} GB • Joined: ${formatDateTime(user.createdAt)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Medium),
                    color = ext.electricCyan
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(status = if (user.blocked) "blocked" else user.activationStatus)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onToggleBlock,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (user.blocked) ext.neonGreen else ext.statusError
                )
            ) {
                Text(if (user.blocked) "Unblock User" else "Block User")
            }
            OutlinedButton(onClick = { showNotifyDialog = true }) {
                Text("Notify")
            }
        }
    }

    if (showNotifyDialog) {
        var title by remember { mutableStateOf("") }
        var msg by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNotifyDialog = false },
            title = { Text("Notify ${user.fullName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true)
                    OutlinedTextField(value = msg, onValueChange = { msg = it }, label = { Text("Message") }, minLines = 2)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && msg.isNotBlank()) {
                            onNotify(title, msg)
                            showNotifyDialog = false
                        }
                    }
                ) { Text("Send") }
            },
            dismissButton = { TextButton(onClick = { showNotifyDialog = false }) { Text("Cancel") } }
        )
    }
}
