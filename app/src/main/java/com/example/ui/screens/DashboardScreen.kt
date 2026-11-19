package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.AppNotification
import com.example.data.model.AppSettings
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.model.WalletTransaction
import com.example.ui.components.AnimatedCurrencyText
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FintechCard
import com.example.ui.components.LottieRawAnimation
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatDateTime
import com.example.ui.components.formatInr
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalDilsatColors
import java.util.Locale

@Composable
fun DashboardScreen(
    profile: UserProfile,
    settings: AppSettings,
    transactionsState: UiState<List<WalletTransaction>>,
    notificationsState: UiState<List<AppNotification>>,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onNavigateSellData: () -> Unit,
    onNavigateWallet: () -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateNotifications: () -> Unit,
    onNavigateProfile: () -> Unit
) {
    val ext = LocalDilsatColors.current
    val unreadCount = (notificationsState as? UiState.Success)?.data?.count { !it.read } ?: 0
    val recentTransactions = (transactionsState as? UiState.Success)?.data?.take(4).orEmpty()

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
            // Top Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onNavigateProfile)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(ext.neonGreen, ext.electricCyan))
                                )
                                .padding(2.dp)
                        ) {
                            if (!profile.photoUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = profile.photoUri,
                                    contentDescription = "User avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = profile.fullName.take(1).uppercase().ifBlank { "D" },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = ext.neonGreen
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Welcome back,",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = profile.fullName,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier.testTag("dashboard_theme_toggle")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = "Toggle light or dark theme",
                                tint = ext.electricCyan
                            )
                        }
                        IconButton(
                            onClick = onNavigateNotifications,
                            modifier = Modifier.testTag("dashboard_notifications_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge(containerColor = ext.neonGreen, contentColor = Color(0xFF060B16)) {
                                            Text(unreadCount.coerceAtMost(99).toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = "Notifications",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Announcement Banner
            item {
                AnimatedVisibility(visible = settings.announcementBanner.isNotBlank()) {
                    FintechCard(
                        accentBorder = ext.vividPurple.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Campaign,
                                contentDescription = "Announcement",
                                tint = ext.vividPurple,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = settings.announcementBanner,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Hero Wallet & Earnings Card
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = if (ext.isDark) {
                                        listOf(Color(0xFF0F203D), Color(0xFF151C42), Color(0xFF0B2A35))
                                    } else {
                                        listOf(Color(0xFFE0F7F1), Color(0xFFE0F2FE), Color(0xFFEDE9FE))
                                    }
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "TOTAL WALLET BALANCE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = ext.electricCyan
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    AnimatedCurrencyText(
                                        amount = profile.walletBalance,
                                        style = MaterialTheme.typography.displayLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                LottieRawAnimation(
                                    rawResId = R.raw.lottie_data_flow,
                                    modifier = Modifier.size(58.dp)
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                HeroSubStat(
                                    label = "Today's Earnings",
                                    value = formatInr(profile.todayEarnings),
                                    accent = ext.neonGreen
                                )
                                HeroSubStat(
                                    label = "Total Earnings",
                                    value = formatInr(profile.totalEarnings),
                                    accent = ext.electricCyan
                                )
                                HeroSubStat(
                                    label = "Pending",
                                    value = formatInr(profile.pendingBalance),
                                    accent = ext.vividPurple
                                )
                            }
                        }
                    }
                }
            }

            // Quick Actions Grid
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionTile(
                        label = "SELL DATA",
                        icon = Icons.Filled.Bolt,
                        accent = ext.neonGreen,
                        testTag = "qa_sell_data",
                        onClick = onNavigateSellData,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        label = "MY WALLET",
                        icon = Icons.Filled.AccountBalanceWallet,
                        accent = ext.electricCyan,
                        testTag = "qa_my_wallet",
                        onClick = onNavigateWallet,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionTile(
                        label = "WITHDRAW",
                        icon = Icons.Filled.Payments,
                        accent = ext.vividPurple,
                        testTag = "qa_withdraw",
                        onClick = onNavigateWallet,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        label = "TRANSACTION HISTORY",
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        accent = ext.statusPending,
                        testTag = "qa_tx_history",
                        onClick = onNavigateHistory,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Animated Statistics Grid
            item {
                Text(
                    text = "Live Marketplace Stats",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val soldInMb = profile.totalDataSoldGb > 0.0 && profile.totalDataSoldGb < 1.0
                    StatMetricCard(
                        title = "Total Data Sold",
                        numericValue = if (soldInMb) profile.totalDataSoldGb * 1000.0 else profile.totalDataSoldGb,
                        suffix = if (soldInMb) " MB" else " GB",
                        isDecimal = !soldInMb,
                        icon = Icons.Filled.CellTower,
                        accent = ext.neonGreen,
                        modifier = Modifier.weight(1f)
                    )
                    val pendingInMb = profile.pendingDataGb > 0.0 && profile.pendingDataGb < 1.0
                    StatMetricCard(
                        title = "Pending Data",
                        numericValue = if (pendingInMb) profile.pendingDataGb * 1000.0 else profile.pendingDataGb,
                        suffix = if (pendingInMb) " MB" else " GB",
                        isDecimal = !pendingInMb,
                        icon = Icons.Filled.HourglassTop,
                        accent = ext.statusPending,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = "Total Transactions",
                        numericValue = profile.totalTransactions.toDouble(),
                        suffix = "",
                        isDecimal = false,
                        icon = Icons.Filled.SwapVert,
                        accent = ext.electricCyan,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Referral Rewards",
                        numericValue = profile.referralEarnings,
                        prefix = "₹",
                        isDecimal = true,
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        accent = ext.vividPurple,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Recent Transactions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "View all",
                        style = MaterialTheme.typography.labelLarge,
                        color = ext.electricCyan,
                        modifier = Modifier
                            .clickable(onClick = onNavigateHistory)
                            .padding(6.dp)
                    )
                }
            }

            if (recentTransactions.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        title = "No transactions yet",
                        subtitle = "Submit your first data-selling request to start earning."
                    )
                }
            } else {
                items(recentTransactions, key = { it.id }) { tx ->
                    FintechCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tx.description,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${tx.referenceCode} • ${formatDateTime(tx.createdAt)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(10.dp))
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
                                StatusBadge(status = tx.status)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun HeroSubStat(label: String, value: String, accent: Color) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold
            ),
            color = accent
        )
    }
}

@Composable
private fun QuickActionTile(
    label: String,
    icon: ImageVector,
    accent: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatMetricCard(
    title: String,
    numericValue: Double,
    prefix: String = "",
    suffix: String = "",
    isDecimal: Boolean,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val animated by animateFloatAsState(
        targetValue = numericValue.toFloat(),
        animationSpec = spring(stiffness = 130f, dampingRatio = 0.85f),
        label = "stat_anim"
    )
    FintechCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        val formatted = if (isDecimal) {
            String.format(Locale.US, "%.2f", animated.toDouble())
        } else {
            animated.toInt().toString()
        }
        Text(
            text = "$prefix$formatted$suffix",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
