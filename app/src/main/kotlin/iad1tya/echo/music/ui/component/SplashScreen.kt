package iad1tya.echo.music.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import iad1tya.echo.music.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

@Composable
fun AppSplashScreen() {
    val revealProgress = remember { Animatable(0f) }
    val waveOffset = remember { Animatable(-0.3f) } // Start closer so lines are visible immediately
    
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    LaunchedEffect(Unit) {
        // Start wave animation immediately
        launch {
            waveOffset.animateTo(
                targetValue = 1.5f,
                animationSpec = tween(1800, easing = LinearOutSlowInEasing) // Slightly faster sweep
            )
        }
        
        // Start reveal animation immediately
        launch {
            revealProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(2000, easing = FastOutLinearInEasing)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Backdrop wave lines
        Box(modifier = Modifier.fillMaxSize()) {
            repeat(5) { index ->
                val speedMult = 0.8f + (index * 0.15f)
                val lineAlpha = 0.25f - (index * 0.04f)
                val height = 3.dp + (index * 1.5).dp
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height)
                        .align(Alignment.Center)
                        .offset(y = (-60 + index * 30).dp)
                        .graphicsLayer {
                            translationX = (waveOffset.value * size.width * speedMult) - (size.width / 2)
                            alpha = lineAlpha * (1f - revealProgress.value)
                        }
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, Color.White, Color.Transparent),
                                startX = 0f,
                                endX = 500f
                            )
                        )
                )
            }
        }

        Box(contentAlignment = Alignment.Center) {
            // The Logo with Wavy Reveal
            Image(
                painter = painterResource(R.drawable.wavyn_logo_white),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(240.dp)
                    .scale(pulseScale)
                    .graphicsLayer(
                        clip = true,
                        shape = object : androidx.compose.ui.graphics.Shape {
                            override fun createOutline(
                                size: androidx.compose.ui.geometry.Size,
                                layoutDirection: androidx.compose.ui.unit.LayoutDirection,
                                density: androidx.compose.ui.unit.Density
                            ): androidx.compose.ui.graphics.Outline {
                                val path = androidx.compose.ui.graphics.Path().apply {
                                    val progress = revealProgress.value
                                    val endX = size.width * progress
                                    
                                    moveTo(0f, 0f)
                                    lineTo(endX, 0f)
                                    
                                    // Wavy edge
                                    val waveHeight = 40f * (1f - progress)
                                    val waveCount = 4
                                    val stepY = size.height / (waveCount * 2)
                                    for (i in 0 until (waveCount * 2)) {
                                        val y = (i + 1) * stepY
                                        val cx = endX + (if (i % 2 == 0) waveHeight else -waveHeight)
                                        quadraticTo(cx, y - stepY / 2, endX, y)
                                    }
                                    
                                    lineTo(0f, size.height)
                                    close()
                                }
                                return androidx.compose.ui.graphics.Outline.Generic(path)
                            }
                        }
                    )
            )
        }
    }
}
