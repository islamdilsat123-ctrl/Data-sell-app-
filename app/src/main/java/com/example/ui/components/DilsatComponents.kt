package com.example.ui.components

import androidx.annotation.RawRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalDilsatColors
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

fun formatInr(amount: Double): String = "₹${String.format(Locale.US, "%.2f", amount)}"

fun formatDateTime(timestamp: Timestamp?): String {
    val date = timestamp?.toDate() ?: return "Just now"
    return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(date)
}

@Composable
fun FintechBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = LocalDilsatColors.current
    val bg = MaterialTheme.colorScheme.background
    val cyan = colors.electricCyan
    val green = colors.neonGreen
    val purple = colors.vividPurple

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .drawBehind {
                val alphaFactor = if (colors.isDark) 0.14f else 0.08f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(cyan.copy(alpha = alphaFactor), Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.1f),
                        radius = size.width * 0.65f
                    ),
                    radius = size.width * 0.65f,
                    center = Offset(size.width * 0.85f, size.height * 0.1f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(purple.copy(alpha = alphaFactor), Color.Transparent),
                        center = Offset(size.width * 0.1f, size.height * 0.45f),
                        radius = size.width * 0.6f
                    ),
                    radius = size.width * 0.6f,
                    center = Offset(size.width * 0.1f, size.height * 0.45f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(green.copy(alpha = alphaFactor * 0.8f), Color.Transparent),
                        center = Offset(size.width * 0.8f, size.height * 0.88f),
                        radius = size.width * 0.55f
                    ),
                    radius = size.width * 0.55f,
                    center = Offset(size.width * 0.8f, size.height * 0.88f)
                )
            }
    ) {
        content()
    }
}

@Composable
fun DilsatLogoMark(
    size: Dp = 72.dp,
    modifier: Modifier = Modifier
) {
    val ext = LocalDilsatColors.current
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        // Outer rounded hexagon-shield frame
        drawRoundRect(
            brush = Brush.linearGradient(listOf(ext.neonGreen, ext.electricCyan, ext.vividPurple)),
            topLeft = Offset(w * 0.06f, h * 0.06f),
            size = Size(w * 0.88f, h * 0.88f),
            cornerRadius = CornerRadius(w * 0.24f, h * 0.24f),
            style = Stroke(width = w * 0.06f)
        )
        // Stylized D contour
        val dPath = Path().apply {
            moveTo(w * 0.30f, h * 0.26f)
            lineTo(w * 0.52f, h * 0.26f)
            cubicTo(w * 0.72f, h * 0.26f, w * 0.76f, h * 0.74f, w * 0.52f, h * 0.74f)
            lineTo(w * 0.30f, h * 0.74f)
            close()
        }
        drawPath(
            path = dPath,
            brush = Brush.linearGradient(listOf(ext.neonGreen, ext.electricCyan)),
            style = Stroke(width = w * 0.08f, cap = StrokeCap.Round)
        )
        // Signal bolt bar inside D
        drawLine(
            color = ext.vividPurple,
            start = Offset(w * 0.45f, h * 0.58f),
            end = Offset(w * 0.55f, h * 0.40f),
            strokeWidth = w * 0.07f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun NeonGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "primary_action_button",
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    usePurpleAccent: Boolean = false
) {
    val ext = LocalDilsatColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 450f),
        label = "btn_scale"
    )
    val gradient = if (usePurpleAccent) {
        listOf(ext.electricCyan, ext.vividPurple)
    } else {
        listOf(ext.neonGreen, ext.electricCyan)
    }

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .scale(scale)
            .height(54.dp)
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (enabled) Brush.horizontalGradient(gradient)
                    else Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.5.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (enabled) Color(0xFF04101B) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (enabled) Color(0xFF04101B) else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun FintechCard(
    modifier: Modifier = Modifier,
    accentBorder: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val ext = LocalDilsatColors.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (ext.isDark) 0.92f else 1f)
        ),
        border = BorderStroke(1.dp, accentBorder ?: ext.borderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = if (ext.isDark) 0.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun AnimatedCurrencyText(
    amount: Double,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.displaySmall,
    color: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    val animatedValue by animateFloatAsState(
        targetValue = amount.toFloat(),
        animationSpec = spring(stiffness = 120f, dampingRatio = 0.85f),
        label = "currency_anim"
    )
    Text(
        text = "₹${String.format(Locale.US, "%.2f", animatedValue.toDouble())}",
        style = style.copy(fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold),
        color = color,
        modifier = modifier
    )
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val ext = LocalDilsatColors.current
    val normalized = status.lowercase(Locale.US)
    val (bg, fg, label) = when (normalized) {
        "activated", "verified", "completed", "paid", "credited", "resolved" ->
            Triple(ext.statusSuccess.copy(alpha = 0.16f), ext.statusSuccess, normalized.replaceFirstChar { it.uppercase() })
        "approved", "processing", "in_progress" ->
            Triple(ext.statusProcessing.copy(alpha = 0.16f), ext.statusProcessing, normalized.replace("_", " ").replaceFirstChar { it.uppercase() })
        "pending", "pending_verification", "open" ->
            Triple(ext.statusPending.copy(alpha = 0.16f), ext.statusPending, normalized.replace("_", " ").replaceFirstChar { it.uppercase() })
        "rejected", "failed", "blocked" ->
            Triple(ext.statusError.copy(alpha = 0.16f), ext.statusError, normalized.replaceFirstChar { it.uppercase() })
        else ->
            Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, normalized.replaceFirstChar { it.uppercase() })
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, fg.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun LottieRawAnimation(
    @RawRes rawResId: Int,
    modifier: Modifier = Modifier,
    iterations: Int = LottieConstants.IterateForever
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(rawResId))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
    )
}

@Composable
fun UpiQrGraphic(
    seedText: String,
    modifier: Modifier = Modifier
) {
    val ext = LocalDilsatColors.current
    val grid = 13
    val hash = remember(seedText) { seedText.hashCode() }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cell = size.width / grid
            for (r in 0 until grid) {
                for (c in 0 until grid) {
                    val isCorner = (r < 4 && c < 4) || (r < 4 && c >= grid - 4) || (r >= grid - 4 && c < 4)
                    val isCenter = r in 5..7 && c in 5..7
                    val bit = ((hash xor (r * 31 + c * 17)) and 1) == 1
                    val drawCell = when {
                        isCenter -> false
                        isCorner -> {
                            val lr = if (r >= grid - 4) r - (grid - 4) else r
                            val lc = if (c >= grid - 4) c - (grid - 4) else c
                            lr == 0 || lr == 3 || lc == 0 || lc == 3 || (lr in 1..2 && lc in 1..2 && (lr == 1 && lc == 1))
                        }
                        else -> bit
                    }
                    if (drawCell) {
                        drawRoundRect(
                            color = Color(0xFF0A0F1E),
                            topLeft = Offset(c * cell + 1f, r * cell + 1f),
                            size = Size(cell - 2f, cell - 2f),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(ext.neonGreen),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "₹",
                color = Color(0xFF060B16),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    FintechCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
