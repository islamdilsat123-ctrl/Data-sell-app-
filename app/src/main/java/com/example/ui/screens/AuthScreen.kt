package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.data.firebase.onGoogleSignInClicked
import com.example.ui.components.DilsatLogoMark
import com.example.ui.components.FintechBackdrop
import com.example.ui.components.FintechCard
import com.example.ui.components.LottieRawAnimation
import com.example.ui.components.NeonGradientButton
import com.example.ui.theme.LocalDilsatColors

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val ext = LocalDilsatColors.current

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showHelpDialog by remember { mutableStateOf(false) }

    FintechBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.widthIn(max = 520.dp)
            ) {
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    DilsatLogoMark(size = 56.dp)
                    Spacer(Modifier.width(14.dp))
                    LottieRawAnimation(
                        rawResId = R.raw.lottie_data_flow,
                        modifier = Modifier.size(60.dp)
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "Dilsat Data Sell",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Turn your unused 4G, 5G & broadband data into verified INR wallet earnings.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))

                FintechCard(modifier = Modifier.fillMaxWidth()) {
                    AuthFeatureRow(
                        icon = Icons.Filled.Bolt,
                        tint = ext.neonGreen,
                        title = "Instant Data Marketplace",
                        subtitle = "Choose 4G, 5G, Rollover, or Broadband pools at transparent ₹/GB rates."
                    )
                    Spacer(Modifier.height(14.dp))
                    AuthFeatureRow(
                        icon = Icons.Filled.Wallet,
                        tint = ext.electricCyan,
                        title = "Protected Digital Wallet",
                        subtitle = "Every transaction has a unique TXN ID and verified backend ledger."
                    )
                    Spacer(Modifier.height(14.dp))
                    AuthFeatureRow(
                        icon = Icons.Filled.VerifiedUser,
                        tint = ext.vividPurple,
                        title = "₹5 One-Time Activation",
                        subtitle = "Verified UPI onboarding keeps the marketplace trusted and fraud-free."
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .padding(top = 24.dp)
            ) {
                if (errorMessage != null) {
                    FintechCard(
                        accentBorder = ext.statusError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ext.statusError
                        )
                    }
                }

                NeonGradientButton(
                    text = stringResource(R.string.sign_in_with_google),
                    onClick = {
                        errorMessage = null
                        isLoading = true
                        onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                isLoading = false
                                onAuthSuccess()
                            },
                            onAuthError = { msg ->
                                isLoading = false
                                errorMessage = msg
                            },
                            scope = scope,
                            onAuthCancelled = {
                                isLoading = false
                            }
                        )
                    },
                    isLoading = isLoading,
                    icon = Icons.AutoMirrored.Filled.Login,
                    testTag = "google_sign_in_button",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = null,
                        tint = ext.neonGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Secured by Google Identity & Firebase",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextButton(
                    onClick = { showHelpDialog = true },
                    modifier = Modifier.testTag("auth_help_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = "Account recovery help",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Account & Recovery Help")
                }
            }
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Secure Sign-In & Recovery") },
            text = {
                Text(
                    "Dilsat Data Sell uses verified Google Sign-In so your wallet, activation status, and transaction history remain protected without password leaks.\n\nIf you lost access to your Google account, recover it at accounts.google.com/signin/recovery before signing back in."
                )
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
private fun AuthFeatureRow(
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(22.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
