package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.AppSettings
import com.example.data.model.ReferralRecord
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.preferences.ThemeMode
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
fun ProfileScreen(
    profile: UserProfile,
    settings: AppSettings,
    referralsState: UiState<List<ReferralRecord>>,
    isAdmin: Boolean,
    themeMode: ThemeMode,
    onSelectTheme: (ThemeMode) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val ext = LocalDilsatColors.current
    val referrals = (referralsState as? UiState.Success)?.data.orEmpty()

    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember(profile.fullName) { mutableStateOf(profile.fullName) }
    var editMobile by remember(profile.mobileNumber) { mutableStateOf(profile.mobileNumber) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateProfile(profile.fullName, profile.mobileNumber, uri.toString())
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile Header Card
            item {
                FintechCard(
                    accentBorder = ext.electricCyan.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(2.dp, ext.neonGreen, CircleShape)
                                .clickable {
                                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                                .testTag("profile_photo_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!profile.photoUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = profile.photoUri,
                                    contentDescription = "Profile photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.CameraAlt,
                                    contentDescription = "Change profile photo",
                                    tint = ext.neonGreen
                                )
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.fullName,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = profile.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = profile.mobileNumber,
                                style = MaterialTheme.typography.labelMedium,
                                color = ext.electricCyan
                            )
                            Spacer(Modifier.height(6.dp))
                            StatusBadge(status = profile.activationStatus)
                        }
                        IconButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.testTag("edit_profile_button")
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit profile")
                        }
                    }
                }
            }

            // Referral Program Card
            item {
                FintechCard(
                    accentBorder = ext.vividPurple.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CardGiftcard,
                            contentDescription = "Referral Program",
                            tint = ext.vividPurple
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Refer & Earn ${formatInr(settings.referralRewardInr)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (settings.referralsEnabled) {
                                    "Invite friends with your code and earn rewards after activation."
                                } else {
                                    "Referral program is currently paused by admin."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "YOUR REFERRAL CODE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = profile.referralCode,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ext.neonGreen
                            )
                        }
                        Row {
                            OutlinedButton(
                                onClick = {
                                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cb.setPrimaryClip(ClipData.newPlainText("Referral Code", profile.referralCode))
                                    viewModel.showMessage("Copied code: ${profile.referralCode}")
                                },
                                modifier = Modifier.testTag("copy_referral_button")
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Copy")
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Join Dilsat Data Sell & monetize your unused mobile data! Use my referral code ${profile.referralCode} when registering."
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Invite Friends"))
                                },
                                modifier = Modifier.testTag("share_referral_button")
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = "Invite", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Invite")
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Verified Referrals: ${profile.referralCount}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Earned: ${formatInr(profile.referralEarnings)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = ext.neonGreen
                        )
                    }
                }
            }

            // Theme Appearance
            item {
                FintechCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Palette, contentDescription = null, tint = ext.electricCyan)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Appearance",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = themeMode == mode,
                                onClick = { onSelectTheme(mode) },
                                label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                }
            }

            // Quick Actions: Notifications, Support, Admin Panel, Sign Out
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("profile_notifications_button")
                    ) {
                        Icon(Icons.Filled.Notifications, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Notifications & Announcements")
                    }

                    OutlinedButton(
                        onClick = onOpenSupport,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("profile_support_button")
                    ) {
                        Icon(Icons.Filled.SupportAgent, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Help Center, FAQ & Support")
                    }

                    if (isAdmin) {
                        NeonGradientButton(
                            text = "OPEN ADMIN PANEL",
                            icon = Icons.Filled.AdminPanelSettings,
                            usePurpleAccent = true,
                            testTag = "open_admin_panel_button",
                            onClick = onOpenAdminPanel,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        TextButton(
                            onClick = { viewModel.claimAdminAccess() },
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .testTag("profile_verify_admin_button")
                        ) {
                            Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Verify Admin Role")
                        }
                    }

                    OutlinedButton(
                        onClick = onSignOut,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("profile_sign_out_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = ext.statusError)
                        Spacer(Modifier.width(8.dp))
                        Text("Sign Out", color = ext.statusError)
                    }
                }
            }

            // Referral History
            item {
                Text(
                    text = "Referral History",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (referrals.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Filled.CardGiftcard,
                        title = "No referrals yet",
                        subtitle = "Share your code ${profile.referralCode} to track invited friends here."
                    )
                }
            } else {
                items(referrals, key = { it.id }) { ref ->
                    FintechCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = ref.referredName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = formatDateTime(ref.createdAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatInr(ref.rewardAmount),
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                    color = ext.neonGreen
                                )
                                StatusBadge(status = ref.status)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editMobile,
                        onValueChange = { editMobile = it.filter { ch -> ch.isDigit() }.take(15) },
                        label = { Text("Mobile Number") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(editName, editMobile, profile.photoUri)
                        showEditDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
