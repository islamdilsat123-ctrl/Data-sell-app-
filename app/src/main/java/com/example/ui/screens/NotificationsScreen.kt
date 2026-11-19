package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.AppNotification
import com.example.data.model.GlobalAnnouncement
import com.example.data.model.UiState
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FintechBackdrop
import com.example.ui.components.FintechCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatDateTime
import com.example.ui.theme.LocalDilsatColors

@Composable
fun NotificationsScreen(
    notificationsState: UiState<List<AppNotification>>,
    announcements: List<GlobalAnnouncement>,
    onMarkRead: (String) -> Unit,
    onBack: () -> Unit
) {
    val ext = LocalDilsatColors.current
    val notifications = (notificationsState as? UiState.Success)?.data.orEmpty()

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
                            modifier = Modifier.testTag("notifications_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Notifications",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                if (announcements.isNotEmpty()) {
                    item {
                        Text(
                            text = "Important Announcements",
                            style = MaterialTheme.typography.titleMedium,
                            color = ext.vividPurple
                        )
                    }
                    items(announcements, key = { "ann_${it.id}" }) { ann ->
                        FintechCard(
                            accentBorder = ext.vividPurple.copy(alpha = 0.55f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Filled.Campaign,
                                    contentDescription = "Announcement",
                                    tint = ext.vividPurple,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = ann.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = ann.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = formatDateTime(ann.createdAt),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Account Alerts",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                if (notifications.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Filled.Notifications,
                            title = "All caught up!",
                            subtitle = "Payment verifications, data sell approvals, withdrawals, and referral bonuses will appear here."
                        )
                    }
                } else {
                    items(notifications, key = { it.id }) { notif ->
                        FintechCard(
                            accentBorder = if (!notif.read) ext.neonGreen.copy(alpha = 0.6f) else ext.borderSubtle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!notif.read) onMarkRead(notif.id)
                                }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = if (!notif.read) Icons.Filled.NotificationsActive else Icons.Filled.Notifications,
                                        contentDescription = null,
                                        tint = if (!notif.read) ext.neonGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = notif.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                StatusBadge(status = notif.category)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = notif.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = formatDateTime(notif.createdAt) + if (!notif.read) " • Tap to mark read" else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (!notif.read) ext.electricCyan else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
