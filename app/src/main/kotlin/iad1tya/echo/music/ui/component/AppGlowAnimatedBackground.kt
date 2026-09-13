package iad1tya.echo.music.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

/**
 * App-wide glowing animated gradient background.
 * Creates a luxurious, organic, glowing ambient mesh effect
 * that breathes and drifts softly behind the entire application UI.
 */
@Composable
fun AppGlowAnimatedBackground(
    modifier: Modifier = Modifier,
    colors: List<Color> = emptyList(),
    pureBlack: Boolean = false,
    darkTheme: Boolean = true,
    alpha: Float = 1f,
) {
    val defaultColors = listOf(
        Color(0xFF6C3CE9), // Electric Violet
        Color(0xFF00E5FF), // Neo Cyan
        Color(0xFFFF2A85), // Cyber Magenta
        Color(0xFF00F5A0), // Emerald Neon
        Color(0xFF1E3C72), // Deep Royal Navy
        Color(0xFF8A2387), // Vivid Orchid
    )

    val activeColors = if (colors.isNotEmpty()) colors else defaultColors

    // Smoothly animate each color when songs or palettes change
    val c0 by animateColorAsState(activeColors.getOrElse(0) { defaultColors[0] }, tween(1500), label = "c0")
    val c1 by animateColorAsState(activeColors.getOrElse(1) { defaultColors[1] }, tween(1500), label = "c1")
    val c2 by animateColorAsState(activeColors.getOrElse(2) { defaultColors[2] }, tween(1500), label = "c2")
    val c3 by animateColorAsState(activeColors.getOrElse(3) { defaultColors[3] }, tween(1500), label = "c3")
    val c4 by animateColorAsState(activeColors.getOrElse(4) { defaultColors[4] }, tween(1500), label = "c4")

    val infiniteTransition = rememberInfiniteTransition(label = "AppGlowTransition")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "appGlowProgress"
    )

    val baseBackground = when {
        pureBlack -> Color(0xFF000000)
        darkTheme -> Color(0xFF08090F)
        else -> Color(0xFFF6F8FC)
    }

    val glowAlpha = if (darkTheme) 0.32f * alpha else 0.16f * alpha

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val w = size.width
                val h = size.height

                fun oscillate(min: Float, max: Float, phase: Float, speed: Float = 1f): Float {
                    val v = sin(2.0 * Math.PI * (progress * speed + phase)).toFloat()
                    return min + (max - min) * ((v + 1f) * 0.5f)
                }

                fun oscillateCos(min: Float, max: Float, phase: Float, speed: Float = 1f): Float {
                    val v = cos(2.0 * Math.PI * (progress * speed + phase)).toFloat()
                    return min + (max - min) * ((v + 1f) * 0.5f)
                }

                // 5 harmonic floating glowing orbs
                val o1x = oscillate(0.05f, 0.55f, 0.00f, 1f)
                val o1y = oscillateCos(0.02f, 0.45f, 0.15f, 0.8f)
                val r1 = oscillate(0.85f, 1.45f, 0.30f, 0.7f)

                val o2x = oscillateCos(0.50f, 0.95f, 0.25f, 0.9f)
                val o2y = oscillate(0.05f, 0.60f, 0.40f, 1.1f)
                val r2 = oscillate(0.80f, 1.35f, 0.10f, 0.85f)

                val o3x = oscillate(0.10f, 0.65f, 0.50f, 1.2f)
                val o3y = oscillateCos(0.45f, 0.92f, 0.65f, 0.95f)
                val r3 = oscillate(0.75f, 1.40f, 0.45f, 0.75f)

                val o4x = oscillateCos(0.40f, 0.92f, 0.70f, 0.85f)
                val o4y = oscillate(0.55f, 0.95f, 0.85f, 1.05f)
                val r4 = oscillate(0.90f, 1.50f, 0.20f, 0.9f)

                val o5x = oscillate(0.25f, 0.75f, 0.35f, 0.6f)
                val o5y = oscillateCos(0.25f, 0.75f, 0.55f, 0.65f)
                val r5 = oscillate(0.70f, 1.25f, 0.80f, 0.7f)

                val b1 = Brush.radialGradient(
                    colors = listOf(c0.copy(alpha = glowAlpha * 1.1f), c0.copy(alpha = glowAlpha * 0.45f), Color.Transparent),
                    center = Offset(w * o1x, h * o1y),
                    radius = w * r1
                )
                val b2 = Brush.radialGradient(
                    colors = listOf(c1.copy(alpha = glowAlpha * 1.0f), c1.copy(alpha = glowAlpha * 0.40f), Color.Transparent),
                    center = Offset(w * o2x, h * o2y),
                    radius = w * r2
                )
                val b3 = Brush.radialGradient(
                    colors = listOf(c2.copy(alpha = glowAlpha * 0.95f), c2.copy(alpha = glowAlpha * 0.35f), Color.Transparent),
                    center = Offset(w * o3x, h * o3y),
                    radius = w * r3
                )
                val b4 = Brush.radialGradient(
                    colors = listOf(c3.copy(alpha = glowAlpha * 0.90f), c3.copy(alpha = glowAlpha * 0.30f), Color.Transparent),
                    center = Offset(w * o4x, h * o4y),
                    radius = w * r4
                )
                val b5 = Brush.radialGradient(
                    colors = listOf(c4.copy(alpha = glowAlpha * 0.85f), c4.copy(alpha = glowAlpha * 0.25f), Color.Transparent),
                    center = Offset(w * o5x, h * o5y),
                    radius = w * r5
                )

                onDrawBehind {
                    drawRect(color = baseBackground)
                    drawRect(brush = b1)
                    drawRect(brush = b2)
                    drawRect(brush = b3)
                    drawRect(brush = b4)
                    drawRect(brush = b5)
                }
            }
    )
}
