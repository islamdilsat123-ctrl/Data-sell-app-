package com.example.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.ReferralCodeLookup
import com.example.ui.components.FintechBackdrop
import com.example.ui.components.FintechCard
import com.example.ui.components.NeonGradientButton
import com.example.ui.theme.LocalDilsatColors
import com.example.ui.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseUser

@Composable
fun RegistrationScreen(
    firebaseUser: FirebaseUser,
    viewModel: MainViewModel,
    isSubmitting: Boolean,
    onSignOut: () -> Unit
) {
    val ext = LocalDilsatColors.current
    var fullName by remember { mutableStateOf(firebaseUser.displayName.orEmpty()) }
    var mobileNumber by remember { mutableStateOf("") }
    val email = firebaseUser.email.orEmpty().ifBlank { "user@dilsatdata.com" }
    var photoUri by remember { mutableStateOf(firebaseUser.photoUrl?.toString()) }
    var referralInput by remember { mutableStateOf("") }
    var verifiedReferrer by remember { mutableStateOf<ReferralCodeLookup?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    FintechBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.widthIn(max = 560.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Complete Registration",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Step 1 of 2 • Create your Dilsat profile",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ext.electricCyan
                        )
                    }
                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier.testTag("reg_sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign out",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Profile photo picker
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(2.dp, ext.neonGreen, CircleShape)
                        .clickable {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                        .testTag("pick_profile_photo_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (!photoUri.isNullOrBlank()) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "Selected profile photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "Add profile photo",
                            tint = ext.neonGreen,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
                Text(
                    text = "Tap to choose profile photo (optional)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                )

                Spacer(Modifier.height(20.dp))

                FintechCard(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it; validationError = null },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_full_name_input")
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { input ->
                            mobileNumber = input.filter { it.isDigit() }.take(15)
                            validationError = null
                        },
                        label = { Text("Mobile Number") },
                        placeholder = { Text("10-digit mobile number") },
                        leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_mobile_input")
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Verified Email Address") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Verified",
                                tint = ext.neonGreen
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = referralInput,
                            onValueChange = {
                                referralInput = it.uppercase().filter { ch -> ch.isLetterOrDigit() }.take(16)
                                verifiedReferrer = null
                            },
                            label = { Text("Referral Code (Optional)") },
                            leadingIcon = { Icon(Icons.Filled.CardGiftcard, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reg_referral_input")
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = {
                                if (referralInput.isNotBlank()) {
                                    viewModel.validateReferralCode(referralInput) { lookup ->
                                        verifiedReferrer = lookup
                                    }
                                }
                            },
                            enabled = referralInput.length >= 6,
                            modifier = Modifier.testTag("verify_referral_button")
                        ) {
                            Text("Verify")
                        }
                    }
                    if (verifiedReferrer != null) {
                        Text(
                            text = "Invited by ${verifiedReferrer?.ownerName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ext.neonGreen,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }

                if (validationError != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = validationError ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ext.statusError
                    )
                }

                Spacer(Modifier.height(20.dp))

                NeonGradientButton(
                    text = "CONTINUE TO ₹5 ACTIVATION",
                    isLoading = isSubmitting,
                    testTag = "reg_submit_button",
                    onClick = {
                        val cleanName = fullName.trim()
                        val cleanMobile = mobileNumber.trim()
                        when {
                            cleanName.length < 2 -> validationError = "Please enter your full name."
                            cleanMobile.length < 10 -> validationError = "Please enter a valid 10-digit mobile number."
                            else -> {
                                viewModel.createProfile(
                                    fullName = cleanName,
                                    mobileNumber = cleanMobile,
                                    email = email,
                                    photoUri = photoUri,
                                    referralCode = referralInput.takeIf { it.isNotBlank() }
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))
                TextButton(
                    onClick = onSignOut,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Switch Google Account")
                }
            }
        }
    }
}
