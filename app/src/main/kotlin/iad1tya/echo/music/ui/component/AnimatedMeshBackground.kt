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
 * Palette: Dark but clearly visible black, charcoal, graphite grey, slate, and soft luminous white/silver aura.
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
            animation = tween(28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AppMeshTime"
    )

    fun wave(min: Float, max: Float, phase: Float, speed: Float = 1f): Float {
        val s = sin(2.0 * PI * (time * speed + phase)).toFloat()
        return min + (max - min) * ((s + 1f) * 0.5f)
    }

    // Harmonic orbital paths for mesh gradient nodes
    val p1x = wave(0.10f, 0.60f, 0.00f, 0.8f)
    val p1y = wave(0.08f, 0.45f, 0.12f, 1.1f)
    val r1 = wave(0.70f, 1.30f, 0.20f, 0.7f)

    val p2x = wave(0.55f, 0.95f, 0.25f, 1.0f)
    val p2y = wave(0.15f, 0.55f, 0.33f, 0.8f)
    val r2 = wave(0.65f, 1.35f, 0.40f, 0.9f)

    val p3x = wave(0.15f, 0.65f, 0.45f, 0.9f)
    val p3y = wave(0.45f, 0.85f, 0.52f, 1.2f)
    val r3 = wave(0.75f, 1.40f, 0.60f, 0.6f)

    val p4x = wave(0.50f, 0.90f, 0.65f, 1.1f)
    val p4y = wave(0.55f, 0.95f, 0.72f, 0.9f)
    val r4 = wave(0.70f, 1.45f, 0.80f, 1.0f)

    val p5x = wave(0.25f, 0.75f, 0.85f, 0.7f)
    val p5y = wave(0.30f, 0.70f, 0.92f, 1.0f)
    val r5 = wave(0.60f, 1.25f, 0.05f, 0.8f)

    // Luminous soft white / silver accent orb floating gracefully
    val p6x = wave(0.35f, 0.70f, 0.50f, 1.2f)
    val p6y = wave(0.40f, 0.80f, 0.18f, 0.7f)
    val r6 = wave(0.55f, 1.20f, 0.35f, 1.1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.graphicsLayer {
                        renderEffect = RenderEffect.createBlurEffect(
                            75f,
                            75f,
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

                // Top to bottom brand gradient foundation
                val topDark = Color(0xFF000000)
                val midDark = Color(0xFF0C0503)
                val bottomDark = Color(0xFF050100)

                val baseGradient = Brush.verticalGradient(
                    colors = listOf(topDark, midDark, bottomDark),
                    startY = 0f,
                    endY = h
                )

                // Official Branding Gradient Palette:
                // #000000 -> #C10801 (Crimson) -> #F16001 (Flame Orange) -> #D9C3AB (Champagne) + #E85002 (Brand Orange)
                val crimson = Color(0xFFC10801)
                val flameOrange = Color(0xFFF16001)
                val brandOrange = Color(0xFFE85002)
                val champagne = Color(0xFFD9C3AB)
                val deepCrimson = Color(0xFF800500)
                val softWhiteGlow = Color(0xFFFFF0E5)

                val b1 = Brush.radialGradient(
                    colors = listOf(crimson.copy(alpha = 0.50f), crimson.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(w * p1x, h * p1y),
                    radius = w * r1
                )
                val b2 = Brush.radialGradient(
                    colors = listOf(brandOrange.copy(alpha = 0.55f), brandOrange.copy(alpha = 0.20f), Color.Transparent),
                    center = Offset(w * p2x, h * p2y),
                    radius = w * r2
                )
                val b3 = Brush.radialGradient(
                    colors = listOf(flameOrange.copy(alpha = 0.45f), flameOrange.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(w * p3x, h * p3y),
                    radius = w * r3
                )
                val b4 = Brush.radialGradient(
                    colors = listOf(champagne.copy(alpha = 0.30f), champagne.copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(w * p4x, h * p4y),
                    radius = w * r4
                )
                val b5 = Brush.radialGradient(
                    colors = listOf(deepCrimson.copy(alpha = 0.40f), deepCrimson.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(w * p5x, h * p5y),
                    radius = w * r5
                )
                val b6 = Brush.radialGradient(
                    colors = listOf(softWhiteGlow.copy(alpha = 0.18f), softWhiteGlow.copy(alpha = 0.04f), Color.Transparent),
                    center = Offset(w * p6x, h * p6y),
                    radius = w * r6
                )

                onDrawBehind {
                    drawRect(baseGradient)
                    drawRect(b1)
                    drawRect(b2)
                    drawRect(b3)
                    drawRect(b4)
                    drawRect(b5)
                    drawRect(b6)
                }
            }
    )
}
