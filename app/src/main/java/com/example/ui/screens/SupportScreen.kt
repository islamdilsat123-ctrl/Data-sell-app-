package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import com.example.data.model.AppSettings
import com.example.data.model.SupportTicket
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FintechBackdrop
import com.example.ui.components.FintechCard
import com.example.ui.components.NeonGradientButton
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatDateTime
import com.example.ui.theme.LocalDilsatColors
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SupportScreen(
    profile: UserProfile?,
    settings: AppSettings,
    ticketsState: UiState<List<SupportTicket>>,
    isSubmitting: Boolean,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val ext = LocalDilsatColors.current
    val tickets = (ticketsState as? UiState.Success)?.data.orEmpty()

    var category by remember { mutableStateOf("payment_issue") }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var utrRef by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    val faqs = remember(settings.activationUpiId, settings.minWithdrawalInr) {
        listOf(
            "Why is there a ₹5 activation fee?" to
                "The one-time ₹5.00 activation fee prevents spam accounts and verifies your UPI identity before unlocking data selling and withdrawals.",
            "How long does ₹5 payment verification take?" to
                "Once you pay to ${settings.activationUpiId} and submit your 12-digit UTR reference, our team verifies it against the merchant statement and unlocks your dashboard automatically.",
            "How are data selling prices calculated?" to
                "Prices per GB depend on the selected package (4G, 5G, Rollover, or Broadband) and are dynamically configured by the marketplace admin.",
            "What is the minimum withdrawal limit?" to
                "The current minimum withdrawal amount is ₹${settings.minWithdrawalInr.toInt()}, sent directly to your UPI ID."
        )
    }

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
                    .widthIn(max = 680.dp),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("support_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "Help Center & Support",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Contact: ${settings.supportEmail}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ext.electricCyan
                            )
                        }
                    }
                }

                // FAQs
                item {
                    Text(
                        text = "Frequently Asked Questions",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(faqs) { (question, answer) ->
                    var expanded by remember { mutableStateOf(false) }
                    FintechCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = question,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = if (expanded) "Collapse" else "Expand"
                            )
                        }
                        AnimatedVisibility(visible = expanded) {
                            Text(
                                text = answer,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                // Submit Support / Payment Issue Ticket
                item {
                    FintechCard(
                        accentBorder = ext.electricCyan.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Contact Admin / Report Payment Issue",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "payment_issue" to "Payment Issue",
                                "withdrawal" to "Withdrawal",
                                "data_sell" to "Data Sell",
                                "account" to "Account",
                                "general" to "General"
                            ).forEach { (key, label) ->
                                FilterChip(
                                    selected = category == key,
                                    onClick = { category = key },
                                    label = { Text(label) }
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it; formError = null },
                            label = { Text("Subject") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("support_subject_input")
                        )
                        Spacer(Modifier.height(8.dp))
                        if (category == "payment_issue" || category == "withdrawal") {
                            OutlinedTextField(
                                value = utrRef,
                                onValueChange = { utrRef = it.uppercase().take(40) },
                                label = { Text("UTR / Transaction Reference (Optional)") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("support_utr_input")
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it; formError = null },
                            label = { Text("Describe your issue") },
                            minLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("support_description_input")
                        )
                        if (formError != null) {
                            Text(
                                text = formError ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ext.statusError,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        NeonGradientButton(
                            text = "SUBMIT SUPPORT TICKET",
                            icon = Icons.Filled.SupportAgent,
                            isLoading = isSubmitting,
                            testTag = "submit_support_ticket_button",
                            onClick = {
                                when {
                                    subject.trim().length < 3 -> formError = "Subject must be at least 3 characters."
                                    description.trim().length < 5 -> formError = "Please describe your issue in detail."
                                    else -> {
                                        viewModel.submitSupportTicket(
                                            profile = profile,
                                            category = category,
                                            subject = subject,
                                            description = description,
                                            utr = utrRef.takeIf { it.isNotBlank() },
                                            onSuccess = {
                                                subject = ""
                                                description = ""
                                                utrRef = ""
                                            }
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // My Support Tickets
                item {
                    Text(
                        text = "Your Support Tickets",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                if (tickets.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Filled.SupportAgent,
                            title = "No open tickets",
                            subtitle = "Submitted support and payment issue reports will appear here."
                        )
                    }
                } else {
                    items(tickets, key = { it.id }) { t ->
                        FintechCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = t.subject,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                StatusBadge(status = t.status)
                            }
                            Text(
                                text = t.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            if (!t.adminReply.isNullOrBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Admin Reply: ${t.adminReply}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = ext.neonGreen
                                )
                            }
                            Text(
                                text = formatDateTime(t.createdAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
