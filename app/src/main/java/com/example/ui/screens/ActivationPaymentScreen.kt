package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.ActivationPayment
import com.example.data.model.AppSettings
import com.example.data.model.UserProfile
import com.example.ui.components.FintechBackdrop
import com.example.ui.components.FintechCard
import com.example.ui.components.LottieRawAnimation
import com.example.ui.components.NeonGradientButton
import com.example.ui.components.StatusBadge
import com.example.ui.components.UpiQrGraphic
import com.example.ui.components.formatDateTime
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalDilsatColors
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ActivationPaymentScreen(
    profile: UserProfile,
    settings: AppSettings,
    latestPayment: ActivationPayment?,
    isSubmitting: Boolean,
    isAdmin: Boolean,
    viewModel: MainViewModel,
    onEnterDashboard: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val ext = LocalDilsatColors.current
    var forceRetryForm by remember { mutableStateOf(false) }
    var upiAttempted by remember { mutableStateOf(false) }
    var utrInput by remember { mutableStateOf("") }
    var upiLaunchNote by remember { mutableStateOf<String?>(null) }

    val upiLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        upiAttempted = true
        val response = result.data?.getStringExtra("response").orEmpty()
        val parsedMap = response.split("&").mapNotNull { part ->
            val kv = part.split("=")
            if (kv.size == 2) kv[0].lowercase() to kv[1] else null
        }.toMap()
        val txnRef = parsedMap["txnref"] ?: parsedMap["approvalrefno"]
        if (!txnRef.isNullOrBlank()) {
            utrInput = txnRef.uppercase().filter { it.isLetterOrDigit() }.take(30)
        }
        val status = parsedMap["status"]?.lowercase()
        upiLaunchNote = when (status) {
            "success" -> "UPI app returned ref ${txnRef ?: ""}. Submit UTR below for backend verification."
            "failure", "failed" -> "UPI app reported payment failure. Retry or verify UTR if debited."
            else -> "Completed UPI payment? Enter the 12-digit UTR / Transaction ID below for backend verification."
        }
    }

    FintechBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.widthIn(max = 560.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dilsat Account Gate",
                        style = MaterialTheme.typography.titleMedium,
                        color = ext.electricCyan
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isAdmin) {
                            IconButton(
                                onClick = onOpenAdminPanel,
                                modifier = Modifier.testTag("activation_admin_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AdminPanelSettings,
                                    contentDescription = "Open Admin Panel",
                                    tint = ext.vividPurple
                                )
                            }
                        }
                        IconButton(
                            onClick = onOpenSupport,
                            modifier = Modifier.testTag("activation_support_icon")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SupportAgent,
                                contentDescription = "Contact Support",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = onSignOut,
                            modifier = Modifier.testTag("activation_sign_out_icon")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Sign out",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                when {
                    // 1. Activated -> Animated Success Screen with green checkmark
                    profile.isActivated -> {
                        FintechCard(
                            accentBorder = ext.statusSuccess,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                LottieRawAnimation(
                                    rawResId = R.raw.lottie_payment_success,
                                    iterations = 1,
                                    modifier = Modifier.size(160.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Payment Verified & Activated!",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = ext.statusSuccess,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Your ₹${settings.activationFeeInr.toInt()} activation fee is verified. Your dashboard, data marketplace, and wallet are now unlocked.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(22.dp))
                                NeonGradientButton(
                                    text = "UNLOCK DASHBOARD",
                                    onClick = onEnterDashboard,
                                    testTag = "unlock_dashboard_button",
                                    icon = Icons.Filled.VerifiedUser,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // 2. Pending Backend Verification
                    profile.isPendingVerification && !forceRetryForm -> {
                        FintechCard(
                            accentBorder = ext.statusProcessing,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                LottieRawAnimation(
                                    rawResId = R.raw.lottie_payment_pending,
                                    modifier = Modifier.size(140.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                StatusBadge(status = "pending_verification")
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = "Verifying Your ₹${settings.activationFeeInr.toInt()} Payment",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Your payment proof is recorded on our secure backend and awaiting verification. Once verified, this screen unlocks automatically in real time.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                if (latestPayment != null) {
                                    Spacer(Modifier.height(14.dp))
                                    FintechCard(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "UTR: ${latestPayment.utrReference}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = ext.electricCyan
                                        )
                                        Text(
                                            text = "Paid to: ${latestPayment.upiId}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Submitted: ${formatDateTime(latestPayment.createdAt)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                OutlinedButton(
                                    onClick = onOpenSupport,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("pending_contact_support_button")
                                ) {
                                    Icon(Icons.Filled.SupportAgent, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.contact_support))
                                }
                            }
                        }
                    }

                    // 3. Payment Failed / Rejected Screen
                    profile.isRejected && !forceRetryForm -> {
                        FintechCard(
                            accentBorder = ext.statusError,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(ext.statusError.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ErrorOutline,
                                        contentDescription = "Payment Failed",
                                        tint = ext.statusError,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                Spacer(Modifier.height(14.dp))
                                Text(
                                    text = "Payment Verification Failed",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = ext.statusError,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = profile.activationFailureReason
                                        ?: latestPayment?.adminNote
                                        ?: "Your UTR reference could not be verified against ${settings.activationUpiId}.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(20.dp))
                                NeonGradientButton(
                                    text = stringResource(R.string.retry_payment),
                                    onClick = { forceRetryForm = true },
                                    icon = Icons.Filled.Refresh,
                                    testTag = "retry_payment_button",
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = onOpenSupport,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("failed_contact_support_button")
                                ) {
                                    Icon(Icons.Filled.SupportAgent, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.contact_support))
                                }
                            }
                        }
                    }

                    // 4. Activation Payment Form
                    else -> {
                        FintechCard(
                            accentBorder = ext.electricCyan.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(ext.neonGreen.copy(alpha = 0.16f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Lock,
                                            contentDescription = "Secure payment",
                                            tint = ext.neonGreen
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = stringResource(R.string.activate_title),
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = stringResource(R.string.activate_subtitle),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(18.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                                    .padding(18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "ACTIVATION FEE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ext.electricCyan
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "₹${String.format(java.util.Locale.US, "%.2f", settings.activationFeeInr)}",
                                        style = MaterialTheme.typography.displayLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                                        color = ext.neonGreen
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Official Merchant UPI ID",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = settings.activationUpiId,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = ext.electricCyan
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("UPI ID", settings.activationUpiId))
                                            viewModel.showMessage("Copied UPI ID: ${settings.activationUpiId}")
                                        },
                                        modifier = Modifier.testTag("copy_upi_button")
                                    ) {
                                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Copy UPI ID")
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                UpiQrGraphic(
                                    seedText = settings.activationUpiId,
                                    modifier = Modifier.size(104.dp)
                                )
                            }

                            Spacer(Modifier.height(18.dp))

                            NeonGradientButton(
                                text = "PAY ₹${settings.activationFeeInr.toInt()} NOW",
                                icon = Icons.Filled.Payment,
                                testTag = "pay_activation_button",
                                onClick = {
                                    val feeFormatted = String.format(java.util.Locale.US, "%.2f", settings.activationFeeInr)
                                    val uri = Uri.Builder()
                                        .scheme("upi")
                                        .authority("pay")
                                        .appendQueryParameter("pa", settings.activationUpiId)
                                        .appendQueryParameter("pn", "Dilsat Data Sell")
                                        .appendQueryParameter("am", feeFormatted)
                                        .appendQueryParameter("cu", "INR")
                                        .appendQueryParameter("tn", "Dilsat Activation ${profile.userId.take(6)}")
                                        .build()
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    val chooser = Intent.createChooser(intent, "Pay ₹${settings.activationFeeInr.toInt()} via UPI")
                                    try {
                                        upiLauncher.launch(chooser)
                                    } catch (_: Exception) {
                                        upiAttempted = true
                                        upiLaunchNote = "No UPI app detected on this device. Pay ₹$feeFormatted to ${settings.activationUpiId} from any UPI app and submit your UTR below."
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(16.dp))

                            AnimatedVisibility(visible = upiLaunchNote != null) {
                                Text(
                                    text = upiLaunchNote ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ext.electricCyan,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )
                            }

                            Text(
                                text = "Verify Payment Transaction",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Enter your 12-digit UPI UTR / Reference number after paying. Access unlocks only after backend verification.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = utrInput,
                                onValueChange = { utrInput = it.uppercase().filter { c -> c.isLetterOrDigit() }.take(30) },
                                label = { Text("UPI Reference / UTR Number") },
                                placeholder = { Text("e.g., 432198765432") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("activation_utr_input")
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    forceRetryForm = false
                                    viewModel.submitActivationPayment(profile, utrInput, settings)
                                },
                                enabled = utrInput.length >= 8 && !isSubmitting,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_utr_button")
                            ) {
                                Text(if (isSubmitting) "Submitting..." else "Submit UTR for Verification")
                            }
                        }
                    }
                }

                if (!isAdmin) {
                    Spacer(Modifier.height(16.dp))
                    TextButton(
                        onClick = { viewModel.claimAdminAccess() },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .testTag("verify_admin_access_button")
                    ) {
                        Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Authorized Admin Verification")
                    }
                }
            }
        }
    }
}
