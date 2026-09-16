package iad1tya.echo.music.ui.component

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.PI
import kotlin.math.sin

/**
 * Animated dynamic mesh gradient background for Wavyn Music.
 * Navy-blue-to-black aurora gradient concentrated at the top-left,
 * smoothly fading to deep black around 45% of screen height.
 */
@Composable
fun AnimatedMeshBackground(
    modifier: Modifier = Modifier,
    pureBlack: Boolean = false,
) {
    if (pureBlack) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
        )
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "AppMeshTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AppMeshTime"
    )

    fun wave(min: Float, max: Float, phase: Float, speed: Float = 1f): Float {
        val s = sin(2.0 * PI * (time * speed + phase)).toFloat()
        return min + (max - min) * ((s + 1f) * 0.5f)
    }

    // Orb positions — concentrated top-left, drifting slowly
    val p1x = wave(-0.08f, 0.28f, 0.00f, 0.5f)
    val p1y = wave(-0.06f, 0.18f, 0.12f, 0.6f)
    val r1 = wave(0.60f, 1.10f, 0.20f, 0.5f)

    val p2x = wave(0.10f, 0.50f, 0.25f, 0.7f)
    val p2y = wave(-0.02f, 0.24f, 0.33f, 0.6f)
    val r2 = wave(0.55f, 1.05f, 0.40f, 0.7f)

    val p3x = wave(-0.04f, 0.35f, 0.45f, 0.6f)
    val p3y = wave(0.08f, 0.30f, 0.52f, 0.8f)
    val r3 = wave(0.50f, 0.90f, 0.60f, 0.5f)

    val p4x = wave(0.25f, 0.65f, 0.65f, 0.8f)
    val p4y = wave(-0.06f, 0.16f, 0.72f, 0.6f)
    val r4 = wave(0.45f, 0.85f, 0.80f, 0.9f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.graphicsLayer {
                        renderEffect = RenderEffect.createBlurEffect(
                            80f,
                            80f,
                            Shader.TileMode.CLAMP
                        ).asComposeRenderEffect()
                    }
                } else {
                    Modifier
                }
            )
            .drawWithCache {
                val w = size.width
                val h = size.height

                // Base color
                val baseDark = Color(0xFF050709)

                // Navy blue palette — deep, rich, not too bright
                val deepNavy = Color(0xFF1E3A5F)       // Deep navy blue
                val richBlue = Color(0xFF1A4B8C)        // Rich medium navy
                val darkSapphire = Color(0xFF0F2B52)    // Darker sapphire foundation
                val softIndigo = Color(0xFF2C3E6B)      // Muted indigo accent

                // Diagonal beam from top-left
                val diagonalBeam = Brush.linearGradient(
                    colors = listOf(
                        richBlue.copy(alpha = 0.55f),
                        deepNavy.copy(alpha = 0.45f),
                        softIndigo.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(w * 0.80f, h * 0.42f)
                )

                // Radial orbs for organic glow
                val b1 = Brush.radialGradient(
                    colors = listOf(
                        richBlue.copy(alpha = 0.70f),
                        deepNavy.copy(alpha = 0.30f),
                        Color.Transparent
                    ),
                    center = Offset(w * p1x, h * p1y),
                    radius = w * r1
                )
                val b2 = Brush.radialGradient(
                    colors = listOf(
                        darkSapphire.copy(alpha = 0.60f),
                        darkSapphire.copy(alpha = 0.22f),
                        Color.Transparent
                    ),
                    center = Offset(w * p2x, h * p2y),
                    radius = w * r2
                )
                val b3 = Brush.radialGradient(
                    colors = listOf(
                        deepNavy.copy(alpha = 0.50f),
                        deepNavy.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = Offset(w * p3x, h * p3y),
                    radius = w * r3
                )
                val b4 = Brush.radialGradient(
                    colors = listOf(
                        softIndigo.copy(alpha = 0.40f),
                        softIndigo.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(w * p4x, h * p4y),
                    radius = w * r4
                )

                // Vertical fade — gradient ends around 45% screen height
                val fadeScrim = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Transparent,
                        baseDark.copy(alpha = 0.50f),
                        baseDark.copy(alpha = 0.85f),
                        baseDark
                    ),
                    startY = h * 0.20f,
                    endY = h * 0.48f
                )

                onDrawBehind {
                    // Base pitch black canvas
                    drawRect(baseDark)
                    // Layered aurora orbs
                    drawRect(diagonalBeam)
                    drawRect(b1)
                    drawRect(b2)
                    drawRect(b3)
                    drawRect(b4)
                    // Smooth fade to black
                    drawRect(fadeScrim)
                    // Solid black from 48% downward
                    drawRect(baseDark, topLeft = Offset(0f, h * 0.48f))
                }
            }
    )
}
