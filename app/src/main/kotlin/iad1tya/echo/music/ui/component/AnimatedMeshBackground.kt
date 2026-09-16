package iad1tya.echo.music.ui.component

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import iad1tya.echo.music.ui.theme.PlayerColorExtractor
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Static cache to avoid re-extracting colors across recompositions
private val appBackgroundColorsCache = mutableMapOf<String, List<Color>>()

/**
 * Animated dynamic organic liquid water gradient background for Wavyn Music.
 * Dynamically adapts to the currently playing song's thumbnail palette or falls back to
 * a rich, curated cosmic liquid palette with organic multi-node fluid motion across the full display.
 */
@Composable
fun AnimatedMeshBackground(
    thumbnailUrl: String? = null,
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

    val context = LocalContext.current
    var extractedColors by remember { mutableStateOf<List<Color>>(emptyList()) }

    // Curated rich modern cosmic liquid water palette
    val defaultLiquidPalette = remember {
        listOf(
            Color(0xFF182A4D), // Deep indigo (top-left fluid node)
            Color(0xFF0C3D52), // Luminous teal-cyan (right-bottom fluid node)
            Color(0xFF2C1648), // Royal magenta-violet (bottom-left deep accent)
            Color(0xFF1D355E), // Luminous sapphire (top-right drift)
            Color(0xFF13223A)  // Ambient midnight center swell
        )
    }

    LaunchedEffect(thumbnailUrl) {
        if (!thumbnailUrl.isNullOrBlank()) {
            val cached = appBackgroundColorsCache[thumbnailUrl]
            if (cached != null && cached.isNotEmpty()) {
                extractedColors = cached
                return@LaunchedEffect
            }
            val colors = PlayerColorExtractor.extractFromUrl(context, thumbnailUrl)
            if (colors.isNotEmpty()) {
                appBackgroundColorsCache[thumbnailUrl] = colors
                extractedColors = colors
            } else {
                extractedColors = emptyList()
            }
        } else {
            extractedColors = emptyList()
        }
    }

    // Active color nodes with smooth cross-track liquid color morphing
    val c0 = extractedColors.getOrElse(0) { defaultLiquidPalette[0] }
    val c1 = extractedColors.getOrElse(1) { defaultLiquidPalette[1] }
    val c2 = extractedColors.getOrElse(2) { defaultLiquidPalette[2] }
    val c3 = extractedColors.getOrElse(3) { defaultLiquidPalette[3] }
    val c4 = extractedColors.getOrElse(4) { defaultLiquidPalette[4] }

    val animColor0 by animateColorAsState(c0, animationSpec = tween(1600, easing = LinearEasing), label = "appC0")
    val animColor1 by animateColorAsState(c1, animationSpec = tween(1600, easing = LinearEasing), label = "appC1")
    val animColor2 by animateColorAsState(c2, animationSpec = tween(1600, easing = LinearEasing), label = "appC2")
    val animColor3 by animateColorAsState(c3, animationSpec = tween(1600, easing = LinearEasing), label = "appC3")
    val animColor4 by animateColorAsState(c4, animationSpec = tween(1600, easing = LinearEasing), label = "appC4")

    val infiniteTransition = rememberInfiniteTransition(label = "AppLiquidMeshTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(32000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AppLiquidTime"
    )

    fun wave(min: Float, max: Float, phase: Float, speed: Float = 1f): Float {
        val s = sin(2.0 * PI * (time * speed + phase)).toFloat()
        return min + (max - min) * ((s + 1f) * 0.5f)
    }

    fun cosWave(min: Float, max: Float, phase: Float, speed: Float = 1f): Float {
        val c = cos(2.0 * PI * (time * speed + phase)).toFloat()
        return min + (max - min) * ((c + 1f) * 0.5f)
    }

    // Multi-node organic liquid water orb paths across the whole screen:
    // Node 1: Top-Left Liquid Swell
    val p1x = wave(-0.06f, 0.38f, 0.00f, 0.7f)
    val p1y = cosWave(-0.05f, 0.32f, 0.15f, 0.85f)
    val r1 = wave(0.65f, 1.20f, 0.10f, 0.6f)

    // Node 2: Right-Side Mid/Bottom Liquid Swell
    val p2x = cosWave(0.60f, 1.05f, 0.30f, 0.8f)
    val p2y = wave(0.46f, 0.82f, 0.45f, 0.75f)
    val r2 = wave(0.55f, 1.05f, 0.25f, 0.9f)

    // Node 3: Bottom-Left Deep Accent Swell
    val p3x = wave(-0.08f, 0.35f, 0.60f, 0.9f)
    val p3y = cosWave(0.68f, 1.02f, 0.70f, 0.65f)
    val r3 = wave(0.50f, 0.95f, 0.55f, 0.8f)

    // Node 4: Top-Right Soft Atmosphere Drift
    val p4x = cosWave(0.55f, 0.98f, 0.80f, 0.75f)
    val p4y = wave(-0.02f, 0.35f, 0.20f, 1.05f)
    val r4 = wave(0.45f, 0.90f, 0.40f, 0.7f)

    // Node 5: Center Floating Ambient Liquid Eddy
    val p5x = wave(0.20f, 0.75f, 0.50f, 1.1f)
    val p5y = cosWave(0.25f, 0.65f, 0.85f, 0.95f)
    val r5 = wave(0.55f, 1.10f, 0.65f, 0.65f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.graphicsLayer {
                        renderEffect = RenderEffect.createBlurEffect(
                            95f,
                            95f,
                            Shader.TileMode.CLAMP
                        ).asComposeRenderEffect()
                    }
                } else {
                    Modifier.blur(65.dp)
                }
            )
            .drawWithCache {
                val w = size.width
                val h = size.height
                val baseDark = Color(0xFF07080C)

                // Liquid water radial nodes
                val b1 = Brush.radialGradient(
                    colors = listOf(
                        animColor0.copy(alpha = 0.55f),
                        animColor0.copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = Offset(w * p1x, h * p1y),
                    radius = w * r1
                )
                val b2 = Brush.radialGradient(
                    colors = listOf(
                        animColor1.copy(alpha = 0.46f),
                        animColor1.copy(alpha = 0.16f),
                        Color.Transparent
                    ),
                    center = Offset(w * p2x, h * p2y),
                    radius = w * r2
                )
                val b3 = Brush.radialGradient(
                    colors = listOf(
                        animColor2.copy(alpha = 0.40f),
                        animColor2.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(w * p3x, h * p3y),
                    radius = w * r3
                )
                val b4 = Brush.radialGradient(
                    colors = listOf(
                        animColor3.copy(alpha = 0.38f),
                        animColor3.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(w * p4x, h * p4y),
                    radius = w * r4
                )
                val b5 = Brush.radialGradient(
                    colors = listOf(
                        animColor4.copy(alpha = 0.32f),
                        animColor4.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(w * p5x, h * p5y),
                    radius = w * r5
                )

                onDrawBehind {
                    // Deep dark base canvas
                    drawRect(baseDark)
                    // Layered organic liquid water swells
                    drawRect(b1)
                    drawRect(b2)
                    drawRect(b3)
                    drawRect(b4)
                    drawRect(b5)
                    // Atmospheric subtle darkening for crisp text readability across full-page scrolling
                    drawRect(Color.Black.copy(alpha = 0.22f))
                }
            }
    )
}

