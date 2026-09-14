package iad1tya.echo.music.ui.player

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.PI
import kotlin.math.sin

/**
 * Apple Music-style fluid reactive dynamic mesh gradient.
 * Morphs seamlessly between extracted artwork colors with harmonic radial node movement
 * and hardware Gaussian blur on Android 12+.
 */
@Composable
fun FluidMeshPlayerBackground(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    alpha: Float = 0.95f
) {
    if (colors.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF08090E))
        )
        return
    }

    // Harmonized 6-color palette with smooth lerping across track changes
    val fallbackBase = Color(0xFF0C0E17)
    val c0 = colors.getOrElse(0) { fallbackBase }
    val c1 = colors.getOrElse(1) { c0 }
    val c2 = colors.getOrElse(2) { c1 }
    val c3 = colors.getOrElse(3) { c0 }
    val c4 = colors.getOrElse(4) { c2 }
    val c5 = colors.getOrElse(5) { c1 }

    val animColor0 by animateColorAsState(c0, animationSpec = tween(1500, easing = LinearEasing), label = "c0")
    val animColor1 by animateColorAsState(c1, animationSpec = tween(1500, easing = LinearEasing), label = "c1")
    val animColor2 by animateColorAsState(c2, animationSpec = tween(1500, easing = LinearEasing), label = "c2")
    val animColor3 by animateColorAsState(c3, animationSpec = tween(1500, easing = LinearEasing), label = "c3")
    val animColor4 by animateColorAsState(c4, animationSpec = tween(1500, easing = LinearEasing), label = "c4")
    val animColor5 by animateColorAsState(c5, animationSpec = tween(1500, easing = LinearEasing), label = "c5")

    val infiniteTransition = rememberInfiniteTransition(label = "FluidMeshTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "FluidTime"
    )

    fun wave(min: Float, max: Float, phase: Float, speed: Float = 1f): Float {
        val s = sin(2.0 * PI * (time * speed + phase)).toFloat()
        return min + (max - min) * ((s + 1f) * 0.5f)
    }

    // Lissajous & harmonic orbital paths for 6 fluid mesh anchor points
    val p1x = wave(0.05f, 0.55f, 0.00f, 0.9f)
    val p1y = wave(0.05f, 0.45f, 0.08f, 1.1f)
    val r1 = wave(0.70f, 1.35f, 0.15f, 0.7f)

    val p2x = wave(0.50f, 0.95f, 0.20f, 1.0f)
    val p2y = wave(0.10f, 0.50f, 0.28f, 0.8f)
    val r2 = wave(0.65f, 1.40f, 0.35f, 0.9f)

    val p3x = wave(0.10f, 0.60f, 0.40f, 0.8f)
    val p3y = wave(0.40f, 0.85f, 0.48f, 1.2f)
    val r3 = wave(0.80f, 1.50f, 0.55f, 0.6f)

    val p4x = wave(0.45f, 0.90f, 0.60f, 1.1f)
    val p4y = wave(0.45f, 0.95f, 0.68f, 0.9f)
    val r4 = wave(0.75f, 1.45f, 0.75f, 1.0f)

    val p5x = wave(0.20f, 0.80f, 0.80f, 0.7f)
    val p5y = wave(0.25f, 0.75f, 0.88f, 1.0f)
    val r5 = wave(0.60f, 1.30f, 0.95f, 0.8f)

    val p6x = wave(0.30f, 0.70f, 0.50f, 1.3f)
    val p6y = wave(0.50f, 0.90f, 0.15f, 0.7f)
    val r6 = wave(0.70f, 1.60f, 0.40f, 1.1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(alpha)
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.graphicsLayer {
                        renderEffect = RenderEffect.createBlurEffect(
                            70f,
                            70f,
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
                val darkFoundation = Color(0xFF07080D)

                val b1 = Brush.radialGradient(
                    colors = listOf(animColor0.copy(alpha = 0.85f), animColor0.copy(alpha = 0.40f), Color.Transparent),
                    center = Offset(w * p1x, h * p1y),
                    radius = w * r1
                )
                val b2 = Brush.radialGradient(
                    colors = listOf(animColor1.copy(alpha = 0.80f), animColor1.copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(w * p2x, h * p2y),
                    radius = w * r2
                )
                val b3 = Brush.radialGradient(
                    colors = listOf(animColor2.copy(alpha = 0.75f), animColor2.copy(alpha = 0.30f), Color.Transparent),
                    center = Offset(w * p3x, h * p3y),
                    radius = w * r3
                )
                val b4 = Brush.radialGradient(
                    colors = listOf(animColor3.copy(alpha = 0.70f), animColor3.copy(alpha = 0.25f), Color.Transparent),
                    center = Offset(w * p4x, h * p4y),
                    radius = w * r4
                )
                val b5 = Brush.radialGradient(
                    colors = listOf(animColor4.copy(alpha = 0.65f), animColor4.copy(alpha = 0.20f), Color.Transparent),
                    center = Offset(w * p5x, h * p5y),
                    radius = w * r5
                )
                val b6 = Brush.radialGradient(
                    colors = listOf(animColor5.copy(alpha = 0.60f), animColor5.copy(alpha = 0.20f), Color.Transparent),
                    center = Offset(w * p6x, h * p6y),
                    radius = w * r6
                )

                onDrawBehind {
                    drawRect(color = darkFoundation)
                    drawRect(brush = b1)
                    drawRect(brush = b2)
                    drawRect(brush = b3)
                    drawRect(brush = b4)
                    drawRect(brush = b5)
                    drawRect(brush = b6)
                }
            }
    )
}
