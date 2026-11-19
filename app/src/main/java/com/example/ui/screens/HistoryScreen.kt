package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.UiState
import com.example.data.model.WalletTransaction
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FintechCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatDateTime
import com.example.ui.components.formatInr
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalDilsatColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HistoryScreen(
    transactionsState: UiState<List<WalletTransaction>>
) {
    val ext = LocalDilsatColors.current
    val transactions = (transactionsState as? UiState.Success)?.data.orEmpty()
    var searchQuery by remember { mutableStateOf("") }
    var filterDirection by remember { mutableStateOf("all") }
    var selectedTx by remember { mutableStateOf<WalletTransaction?>(null) }

    val filtered = remember(transactions, searchQuery, filterDirection) {
        transactions.filter { tx ->
            val matchesDir = filterDirection == "all" || tx.direction == filterDirection
            val q = searchQuery.trim().lowercase()
            val matchesSearch = q.isEmpty() ||
                tx.referenceCode.lowercase().contains(q) ||
                tx.description.lowercase().contains(q) ||
                tx.type.lowercase().contains(q)
            matchesDir && matchesSearch
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Transaction History",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Verified financial ledger • Tap any item for full TXN details",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search TXN ID or description...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search transactions") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_search_input")
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "all" to "All",
                        "credit" to "Credits",
                        "debit" to "Withdrawals",
                        "fee" to "Activation Fee"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = filterDirection == key,
                            onClick = { filterDirection = key },
                            label = { Text(label) }
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        title = "No matching transactions",
                        subtitle = "Verified activation fees, data sale credits, referral bonuses, and withdrawals appear here."
                    )
                }
            } else {
                items(filtered, key = { it.id }) { tx ->
                    FintechCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTx = tx }
                            .testTag("tx_card_${tx.id}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tx.referenceCode,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ext.electricCyan
                                )
                                Text(
                                    text = tx.description,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = formatDateTime(tx.createdAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                val sign = if (tx.direction == "credit") "+" else "-"
                                val amtColor = if (tx.direction == "credit") ext.neonGreen else MaterialTheme.colorScheme.onSurface
                                Text(
                                    text = "$sign${formatInr(tx.amount)}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = amtColor
                                )
                                Spacer(Modifier.height(4.dp))
                                StatusBadge(status = tx.status)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }

    selectedTx?.let { tx ->
        AlertDialog(
            onDismissRequest = { selectedTx = null },
            title = { Text("Transaction Receipt") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailLine("Transaction ID", tx.referenceCode)
                    DetailLine("Type", tx.type.replace("_", " ").uppercase())
                    DetailLine("Amount", formatInr(tx.amount))
                    DetailLine("Direction", tx.direction.uppercase())
                    DetailLine("Status", tx.status.uppercase())
                    DetailLine("Timestamp", formatDateTime(tx.createdAt))
                    DetailLine("Description", tx.description)
                    if (!tx.relatedId.isNullOrBlank()) {
                        DetailLine("Reference Doc", tx.relatedId)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedTx = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
