package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AppSettings
import com.example.data.model.DataPackage
import com.example.data.model.DataSellRequest
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.ui.components.AnimatedCurrencyText
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FintechCard
import com.example.ui.components.LottieRawAnimation
import com.example.ui.components.NeonGradientButton
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatDateTime
import com.example.ui.components.formatInr
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalDilsatColors
import com.example.ui.viewmodel.MainViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

private fun formatMbNumber(mb: Int): String = NumberFormat.getIntegerInstance(Locale.US).format(mb)

private fun formatRateCompact(rate: Double): String {
    return if (rate % 1.0 == 0.0) "₹${rate.toInt()}" else formatInr(rate)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SellDataScreen(
    profile: UserProfile,
    settings: AppSettings,
    packagesState: UiState<List<DataPackage>>,
    sellRequestsState: UiState<List<DataSellRequest>>,
    isSubmitting: Boolean,
    viewModel: MainViewModel
) {
    val ext = LocalDilsatColors.current
    val packages = (packagesState as? UiState.Success)?.data.orEmpty()
    val requests = (sellRequestsState as? UiState.Success)?.data.orEmpty()

    val minSellMb = settings.minSellDataMb.coerceAtLeast(1.0)
    val minSellMbInt = minSellMb.roundToInt()
    val ratePer100Mb = settings.ratePer100MbInr.coerceAtLeast(0.01)

    var selectedPkgId by remember(packages) {
        mutableStateOf(packages.firstOrNull()?.id ?: "standard_mb_pool")
    }
    val selectedPkg = packages.firstOrNull { it.id == selectedPkgId } ?: packages.firstOrNull()
    val fallbackTypes = listOf("4G", "5G", "Rollover", "Broadband")
    var selectedFallbackType by remember { mutableStateOf("4G") }

    var quantityMbText by remember { mutableStateOf("500") }
    val parsedMb = quantityMbText.toDoubleOrNull() ?: 0.0
    val isQuantityValid = parsedMb >= minSellMb && parsedMb <= 10_000_000.0
    val estimatedPayout = if (parsedMb > 0.0) {
        (((parsedMb / 100.0) * ratePer100Mb) * 100.0).roundToInt() / 100.0
    } else {
        0.0
    }

    val animatedMbDisplay by animateIntAsState(
        targetValue = parsedMb.roundToInt().coerceIn(0, 10_000_000),
        animationSpec = spring(stiffness = 160f, dampingRatio = 0.85f),
        label = "mb_calc_anim"
    )

    val providers = listOf("Jio", "Airtel", "Vi", "BSNL", "Home Fiber")
    var selectedProvider by remember { mutableStateOf(providers.first()) }
    var statusFilter by remember { mutableStateOf("all") }

    val quickPresets = remember { listOf(100, 500, 1000, 5000, 10000) }

    val filteredRequests = remember(requests, statusFilter) {
        when (statusFilter) {
            "all" -> requests
            "approved" -> requests.filter { it.status == "approved" || it.status == "completed" }
            else -> requests.filter { it.status == statusFilter }
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
            // Header
            item {
                Column {
                    Text(
                        text = "Data Sell Marketplace",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Enter your unused data in MB to calculate earnings at ${formatRateCompact(ratePer100Mb)} per 100 MB.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Premium Animated Data Sell Calculator Hero
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Transparent,
                    border = BorderStroke(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(listOf(ext.neonGreen, ext.electricCyan, ext.vividPurple))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = if (ext.isDark) {
                                        listOf(Color(0xFF0C1A33), Color(0xFF14183C), Color(0xFF082730))
                                    } else {
                                        listOf(Color(0xFFE6FAF4), Color(0xFFE0F2FE), Color(0xFFF0EBFF))
                                    }
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(18.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Calculate,
                                        contentDescription = "Data Sell Calculator",
                                        tint = ext.neonGreen,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "DATA SELL CALCULATOR",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = ext.electricCyan
                                        )
                                        Text(
                                            text = "Rate: ${formatRateCompact(ratePer100Mb)} / 100 MB • Min: ${formatMbNumber(minSellMbInt)} MB",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                LottieRawAnimation(
                                    rawResId = R.raw.lottie_data_flow,
                                    modifier = Modifier.size(44.dp)
                                )
                            }

                            Spacer(Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "QUANTITY",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${formatMbNumber(animatedMbDisplay)} MB",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = String.format(Locale.US, "%.2f GB", parsedMb / 1000.0),
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                        color = ext.electricCyan
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "ESTIMATED EARNINGS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    AnimatedCurrencyText(
                                        amount = estimatedPayout,
                                        style = MaterialTheme.typography.displaySmall,
                                        color = if (isQuantityValid) ext.neonGreen else ext.statusPending,
                                        modifier = Modifier.testTag("calculator_estimated_earnings")
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                                border = BorderStroke(1.dp, ext.borderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Formula: (${formatMbNumber(parsedMb.roundToInt())} MB ÷ 100) × ${formatRateCompact(ratePer100Mb)} = ${formatInr(estimatedPayout)}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Input, Presets, Slider & Validation Card
            item {
                FintechCard(
                    accentBorder = if (!isQuantityValid) ext.statusError.copy(alpha = 0.65f) else ext.electricCyan.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "1. Enter Data Quantity (MB)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = quantityMbText,
                        onValueChange = { raw ->
                            val filtered = raw.filter { it.isDigit() }
                            if (filtered.length <= 7) {
                                quantityMbText = filtered
                            }
                        },
                        label = { Text("Data Quantity in MB (Min ${formatMbNumber(minSellMbInt)} MB)") },
                        suffix = {
                            Text(
                                text = "MB",
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = ext.electricCyan
                            )
                        },
                        isError = !isQuantityValid,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sell_quantity_input")
                    )

                    AnimatedVisibility(
                        visible = !isQuantityValid,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WarningAmber,
                                contentDescription = "Minimum limit warning",
                                tint = ext.statusError,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Minimum sell quantity is ${formatMbNumber(minSellMbInt)} MB. Sell Data is disabled below ${formatMbNumber(minSellMbInt)} MB.",
                                style = MaterialTheme.typography.labelMedium,
                                color = ext.statusError
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    val sliderMin = minSellMb.toFloat()
                    val sliderMax = maxOf(10_000f, sliderMin + 100f)
                    Slider(
                        value = parsedMb.toFloat().coerceIn(sliderMin, sliderMax),
                        onValueChange = { value ->
                            val stepped = ((value / 50f).roundToInt() * 50).coerceAtLeast(minSellMbInt)
                            quantityMbText = stepped.toString()
                        },
                        valueRange = sliderMin..sliderMax,
                        colors = SliderDefaults.colors(
                            thumbColor = ext.neonGreen,
                            activeTrackColor = ext.neonGreen,
                            inactiveTrackColor = ext.borderSubtle
                        )
                    )

                    Text(
                        text = "Quick Select Packs",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickPresets.filter { it >= minSellMbInt || it == 100 }.forEach { presetMb ->
                            val effectivePreset = presetMb.coerceAtLeast(minSellMbInt)
                            val presetPay = (((effectivePreset / 100.0) * ratePer100Mb) * 100.0).roundToInt() / 100.0
                            val selected = parsedMb.roundToInt() == effectivePreset
                            FilterChip(
                                selected = selected,
                                onClick = { quantityMbText = effectivePreset.toString() },
                                label = {
                                    Text(
                                        text = "${formatMbNumber(effectivePreset)} MB = ${formatRateCompact(presetPay)}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily)
                                    )
                                },
                                modifier = Modifier.testTag("preset_mb_$effectivePreset")
                            )
                        }
                    }
                }
            }

            // Network & Data Type Selection + Clear Pre-Submit Summary
            item {
                FintechCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "2. Select Network & Data Type",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "Network Provider",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        providers.forEach { provider ->
                            FilterChip(
                                selected = selectedProvider == provider,
                                onClick = { selectedProvider = provider },
                                label = { Text(provider) },
                                modifier = Modifier.testTag("provider_chip_$provider")
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Data Pool Type",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    if (packages.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            packages.forEach { pkg ->
                                val isSelected = pkg.id == selectedPkg?.id
                                val pkgIcon = when (pkg.packageType) {
                                    "5G" -> Icons.Filled.Speed
                                    "Broadband" -> Icons.Filled.Router
                                    "Rollover" -> Icons.Filled.Wifi
                                    else -> Icons.Filled.CellTower
                                }
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) ext.neonGreen.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) ext.neonGreen else ext.borderSubtle
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedPkgId = pkg.id }
                                        .testTag("pkg_card_${pkg.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = pkgIcon,
                                            contentDescription = pkg.packageType,
                                            tint = if (isSelected) ext.neonGreen else ext.electricCyan,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = pkg.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = pkg.description,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        StatusBadge(status = pkg.packageType)
                                    }
                                }
                            }
                        }
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            fallbackTypes.forEach { type ->
                                FilterChip(
                                    selected = selectedFallbackType == type,
                                    onClick = { selectedFallbackType = type },
                                    label = { Text(type) }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = ext.borderSubtle)
                    Spacer(Modifier.height(12.dp))

                    // Pre-submission clear Quantity & Payment confirmation box
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Quantity to Sell",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${formatMbNumber(parsedMb.roundToInt())} MB",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estimated Payment",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatInr(estimatedPayout),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isQuantityValid) ext.neonGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Verification policy",
                            tint = ext.electricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Requests are reviewed by admin before crediting your wallet.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                    NeonGradientButton(
                        text = if (isQuantityValid) {
                            "SELL DATA (${formatMbNumber(parsedMb.roundToInt())} MB = ${formatInr(estimatedPayout)})"
                        } else {
                            "MIN ${formatMbNumber(minSellMbInt)} MB REQUIRED"
                        },
                        icon = Icons.Filled.Bolt,
                        enabled = isQuantityValid && !isSubmitting,
                        isLoading = isSubmitting,
                        testTag = "submit_sell_request_button",
                        onClick = {
                            if (isQuantityValid) {
                                val pkgId = selectedPkg?.id ?: "standard_mb_pool"
                                val pkgName = selectedPkg?.name ?: "$selectedFallbackType Data Pool"
                                val pkgType = selectedPkg?.packageType ?: selectedFallbackType
                                viewModel.submitSellRequest(
                                    profile = profile,
                                    packageType = pkgType,
                                    packageName = pkgName,
                                    packageId = pkgId,
                                    quantityMb = parsedMb,
                                    ratePer100MbInr = ratePer100Mb,
                                    minSellDataMb = minSellMb,
                                    provider = selectedProvider,
                                    onSuccess = {}
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Tracking List Header & Filter Chips (Pending, Approved, Rejected)
            item {
                Text(
                    text = "Your Data Sell Requests",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("all", "pending", "approved", "rejected").forEach { filter ->
                        FilterChip(
                            selected = statusFilter == filter,
                            onClick = { statusFilter = filter },
                            label = { Text(filter.replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }

            if (filteredRequests.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Filled.Bolt,
                        title = "No selling requests",
                        subtitle = "Submitted requests appear here with Pending, Approved, or Rejected status."
                    )
                }
            } else {
                items(filteredRequests, key = { it.id }) { req ->
                    val displayStatus = if (req.status == "completed") "approved" else req.status
                    FintechCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${formatMbNumber(req.displayMb.roundToInt())} MB • ${req.packageName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${req.networkProvider} (${req.packageType}) • ${formatDateTime(req.createdAt)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (!req.adminNote.isNullOrBlank()) {
                                    Text(
                                        text = "Note: ${req.adminNote}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ext.electricCyan
                                    )
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatInr(req.estimatedPayout),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = ext.neonGreen
                                )
                                Spacer(Modifier.height(4.dp))
                                StatusBadge(status = displayStatus)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}
