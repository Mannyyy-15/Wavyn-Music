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
import iad1tya.echo.music.constants.AppBackgroundStyle
import iad1tya.echo.music.ui.theme.PlayerColorExtractor
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Static cache to avoid re-extracting colors across recompositions
private val appBackgroundColorsCache = mutableMapOf<String, List<Color>>()

/**
 * Animated dynamic organic liquid water gradient background for Wavyn Music.
 * Supports Dynamic Glow Spots (localized ambient glowing spots on pure dark background)
 * and Solid Minimal Dark styles, with dynamic adaptation to active track artwork.
 */
@Composable
fun AnimatedMeshBackground(
    thumbnailUrl: String? = null,
    appBackgroundStyle: AppBackgroundStyle = AppBackgroundStyle.DYNAMIC_GLOW,
    modifier: Modifier = Modifier,
    pureBlack: Boolean = false,
) {
    if (pureBlack || appBackgroundStyle == AppBackgroundStyle.SOLID_DARK) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF040507))
        )
        return
    }

    val context = LocalContext.current
    var extractedColors by remember { mutableStateOf<List<Color>>(emptyList()) }

    // Curated rich modern cosmic liquid water palette
    val defaultLiquidPalette = remember {
        listOf(
            Color(0xFF1E335A), // Deep vibrant indigo (top-left fluid node)
            Color(0xFF0E455E), // Luminous teal-cyan (right-bottom fluid node)
            Color(0xFF331952), // Royal magenta-violet (bottom-left deep accent)
            Color(0xFF1A2B4C)  // Ambient midnight accent
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

    val animColor0 by animateColorAsState(c0, animationSpec = tween(1600, easing = LinearEasing), label = "appC0")
    val animColor1 by animateColorAsState(c1, animationSpec = tween(1600, easing = LinearEasing), label = "appC1")
    val animColor2 by animateColorAsState(c2, animationSpec = tween(1600, easing = LinearEasing), label = "appC2")

    val infiniteTransition = rememberInfiniteTransition(label = "AppLiquidMeshTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(28000, easing = LinearEasing),
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

    // Focused, localized organic glowing spots (leaving majority of canvas as pure dark space):
    // Spot 1: Concentrated Top-Left Ambient Glow Spot
    val p1x = wave(-0.04f, 0.28f, 0.00f, 0.65f)
    val p1y = cosWave(-0.04f, 0.22f, 0.15f, 0.75f)
    val r1 = wave(0.42f, 0.68f, 0.10f, 0.55f)

    // Spot 2: Concentrated Lower-Right Ambient Glow Spot
    val p2x = cosWave(0.68f, 1.02f, 0.30f, 0.75f)
    val p2y = wave(0.50f, 0.82f, 0.45f, 0.65f)
    val r2 = wave(0.38f, 0.62f, 0.25f, 0.70f)

    // Spot 3: Subtle Bottom-Left Ambient Glimmer Spot
    val p3x = wave(-0.05f, 0.22f, 0.60f, 0.80f)
    val p3y = cosWave(0.78f, 1.02f, 0.70f, 0.60f)
    val r3 = wave(0.28f, 0.48f, 0.55f, 0.60f)

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
                val baseDark = Color(0xFF030406)

                // Localized radial glow spots with sharp outer falloff
                val b1 = Brush.radialGradient(
                    colors = listOf(
                        animColor0.copy(alpha = 0.46f),
                        animColor0.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(w * p1x, h * p1y),
                    radius = w * r1
                )
                val b2 = Brush.radialGradient(
                    colors = listOf(
                        animColor1.copy(alpha = 0.38f),
                        animColor1.copy(alpha = 0.09f),
                        Color.Transparent
                    ),
                    center = Offset(w * p2x, h * p2y),
                    radius = w * r2
                )
                val b3 = Brush.radialGradient(
                    colors = listOf(
                        animColor2.copy(alpha = 0.28f),
                        animColor2.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(w * p3x, h * p3y),
                    radius = w * r3
                )

                onDrawBehind {
                    // Pure deep velvet dark base canvas (dominates 75%+ of view)
                    drawRect(baseDark)
                    // Discrete localized ambient spots
                    drawRect(b1)
                    drawRect(b2)
                    drawRect(b3)
                }
            }
    )
}

