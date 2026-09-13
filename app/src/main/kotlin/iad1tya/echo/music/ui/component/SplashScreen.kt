package iad1tya.echo.music.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import iad1tya.echo.music.R

@Composable
fun AppSplashScreen() {
    val transition = rememberInfiniteTransition(label = "splash_infinite")
    val pulseScale by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val glowAlpha by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val introProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        introProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Soft glowing ambient halo behind the logo
        Box(
            modifier = Modifier
                .size(240.dp)
                .graphicsLayer {
                    scaleX = pulseScale * introProgress.value
                    scaleY = pulseScale * introProgress.value
                    alpha = glowAlpha * introProgress.value
                }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF8B5CF6).copy(alpha = 0.45f),
                            Color(0xFF3B82F6).copy(alpha = 0.20f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.graphicsLayer {
                alpha = introProgress.value
                scaleX = 0.85f + 0.15f * introProgress.value
                scaleY = 0.85f + 0.15f * introProgress.value
            }
        ) {
            // Main Logo
            Image(
                painter = painterResource(R.drawable.wavyn_logo_white),
                contentDescription = "Wavyn Music Logo",
                modifier = Modifier
                    .size(160.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "WAVYN",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp,
                color = Color.White.copy(alpha = 0.95f),
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "MUSIC",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 4.sp,
                color = Color.White.copy(alpha = 0.5f),
            )
        }
    }
}
