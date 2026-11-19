package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.DilsatLogoMark
import com.example.ui.components.FintechBackdrop
import com.example.ui.components.LottieRawAnimation
import com.example.ui.theme.LocalDilsatColors
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }
    val ext = LocalDilsatColors.current

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(550, easing = FastOutSlowInEasing))
        scale.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))
        delay(1200)
        onFinished()
    }

    FintechBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(180.dp)
                    .scale(scale.value)
                    .alpha(alpha.value)
            ) {
                LottieRawAnimation(
                    rawResId = R.raw.lottie_splash_pulse,
                    modifier = Modifier.size(180.dp)
                )
                DilsatLogoMark(size = 86.dp)
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "DILSAT DATA SELL",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.alpha(alpha.value)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = ext.electricCyan,
                modifier = Modifier.alpha(alpha.value)
            )
        }
    }
}
