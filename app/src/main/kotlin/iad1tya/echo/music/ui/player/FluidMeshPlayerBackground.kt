package iad1tya.echo.music.ui.player

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated full-screen Living Liquid & Water Chroma Player Background.
 * Renders the track's authentic album art over the entire device display with dynamic organic
 * fluid water waves, asymmetric liquid squish/stretch, dual-layer chromatic dispersion, and
 * flowing color mesh nodes.
 */
@Composable
fun FluidMeshPlayerBackground(
    thumbnailUrl: String? = null,
    colors: List<Color> = emptyList(),
    modifier: Modifier = Modifier,
    alpha: Float = 0.95f
) {
    AnimatedChromaThumbnailBackground(
        thumbnailUrl = thumbnailUrl,
        colors = colors,
        modifier = modifier,
        alpha = alpha
    )
}

/**
 * Backward compatibility overload if called with only colors.
 */
@Composable
fun FluidMeshPlayerBackground(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    alpha: Float = 0.95f
) {
    AnimatedChromaThumbnailBackground(
        thumbnailUrl = null,
        colors = colors,
        modifier = modifier,
        alpha = alpha
    )
}

@Composable
fun AnimatedChromaThumbnailBackground(
    thumbnailUrl: String?,
    colors: List<Color> = emptyList(),
    modifier: Modifier = Modifier,
    alpha: Float = 0.95f
) {
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "LiquidWaterTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "LiquidTime"
    )

    fun wave(min: Float, max: Float, phase: Float, speed: Float = 1f): Float {
        val s = sin(2.0 * PI * (time * speed + phase)).toFloat()
        return min + (max - min) * ((s + 1f) * 0.5f)
    }

    fun cosWave(min: Float, max: Float, phase: Float, speed: Float = 1f): Float {
        val c = cos(2.0 * PI * (time * speed + phase)).toFloat()
        return min + (max - min) * ((c + 1f) * 0.5f)
    }

    // Layer 1: Primary Liquid Water Current (asymmetric squish & flow)
    val scaleX1 = wave(1.30f, 1.56f, 0.00f, 1.0f)
    val scaleY1 = cosWave(1.34f, 1.62f, 0.25f, 0.9f)
    val shiftX1 = wave(-52f, 52f, 0.10f, 1.2f)
    val shiftY1 = cosWave(-64f, 64f, 0.35f, 0.85f)
    val rot1 = wave(-4.5f, 4.5f, 0.50f, 0.75f)

    // Layer 2: Counter-Current Liquid Wave Dispersion (opposing phase liquid refraction)
    val scaleX2 = cosWave(1.36f, 1.64f, 0.40f, 1.15f)
    val scaleY2 = wave(1.30f, 1.58f, 0.70f, 0.95f)
    val shiftX2 = cosWave(56f, -56f, 0.60f, 0.9f)
    val shiftY2 = wave(68f, -68f, 0.80f, 1.1f)
    val rot2 = cosWave(5.0f, -5.0f, 0.20f, 0.8f)

    val chromaPulse = wave(0.55f, 0.85f, 0.15f, 1.3f)

    // 6 Fluid Liquid Water Color Mesh Orb Nodes
    val p1x = wave(0.05f, 0.55f, 0.00f, 0.9f)
    val p1y = cosWave(0.05f, 0.45f, 0.12f, 1.1f)
    val r1 = wave(0.70f, 1.35f, 0.20f, 0.8f)

    val p2x = cosWave(0.50f, 0.95f, 0.25f, 1.0f)
    val p2y = wave(0.10f, 0.55f, 0.35f, 0.85f)
    val r2 = wave(0.65f, 1.40f, 0.40f, 0.95f)

    val p3x = wave(0.08f, 0.60f, 0.45f, 0.85f)
    val p3y = cosWave(0.40f, 0.88f, 0.55f, 1.2f)
    val r3 = wave(0.75f, 1.50f, 0.60f, 0.7f)

    val p4x = cosWave(0.45f, 0.92f, 0.65f, 1.1f)
    val p4y = wave(0.45f, 0.95f, 0.75f, 0.9f)
    val r4 = wave(0.70f, 1.45f, 0.80f, 1.0f)

    val p5x = wave(0.20f, 0.80f, 0.85f, 0.75f)
    val p5y = cosWave(0.25f, 0.75f, 0.90f, 1.05f)
    val r5 = wave(0.60f, 1.30f, 0.10f, 0.85f)

    val saturationMatrix = remember {
        ColorMatrix().apply {
            setToSaturation(1.45f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(alpha)
            .background(Color(0xFF07080C))
    ) {
        // Underlay: Dynamic Multi-Node Flowing Liquid Color Swells
        if (colors.isNotEmpty()) {
            val c0 = colors.getOrElse(0) { Color(0xFF1E3A5F) }
            val c1 = colors.getOrElse(1) { c0 }
            val c2 = colors.getOrElse(2) { c1 }
            val c3 = colors.getOrElse(3) { c0 }
            val c4 = colors.getOrElse(4) { c2 }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.60f)
                    .drawWithCache {
                        val w = size.width
                        val h = size.height
                        val b1 = Brush.radialGradient(
                            colors = listOf(c0.copy(alpha = 0.85f), c0.copy(alpha = 0.35f), Color.Transparent),
                            center = Offset(w * p1x, h * p1y),
                            radius = w * r1
                        )
                        val b2 = Brush.radialGradient(
                            colors = listOf(c1.copy(alpha = 0.80f), c1.copy(alpha = 0.30f), Color.Transparent),
                            center = Offset(w * p2x, h * p2y),
                            radius = w * r2
                        )
                        val b3 = Brush.radialGradient(
                            colors = listOf(c2.copy(alpha = 0.75f), c2.copy(alpha = 0.25f), Color.Transparent),
                            center = Offset(w * p3x, h * p3y),
                            radius = w * r3
                        )
                        val b4 = Brush.radialGradient(
                            colors = listOf(c3.copy(alpha = 0.70f), c3.copy(alpha = 0.20f), Color.Transparent),
                            center = Offset(w * p4x, h * p4y),
                            radius = w * r4
                        )
                        val b5 = Brush.radialGradient(
                            colors = listOf(c4.copy(alpha = 0.65f), c4.copy(alpha = 0.18f), Color.Transparent),
                            center = Offset(w * p5x, h * p5y),
                            radius = w * r5
                        )
                        onDrawBehind {
                            drawRect(brush = b1)
                            drawRect(brush = b2)
                            drawRect(brush = b3)
                            drawRect(brush = b4)
                            drawRect(brush = b5)
                        }
                    }
            )
        }

        if (thumbnailUrl != null) {
            // Layer 1: Full-Screen Flowing Liquid Artwork Current with Liquid Squeezing & Saturation
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(thumbnailUrl)
                    .size(200, 200)
                    .allowHardware(false)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scaleX1
                        scaleY = scaleY1
                        translationX = shiftX1 * density
                        translationY = shiftY1 * density
                        rotationZ = rot1
                        colorFilter = ColorFilter.colorMatrix(saturationMatrix)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            renderEffect = RenderEffect.createBlurEffect(
                                85f,
                                85f,
                                Shader.TileMode.CLAMP
                            ).asComposeRenderEffect()
                        }
                    }
                    .then(
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                            Modifier.blur(65.dp)
                        } else {
                            Modifier
                        }
                    )
            )

            // Layer 2: Secondary Liquid Wave Counter-Current (Flowing Chromatic Dispersion)
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(thumbnailUrl)
                    .size(160, 160)
                    .allowHardware(false)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.alpha = chromaPulse
                        scaleX = scaleX2
                        scaleY = scaleY2
                        translationX = shiftX2 * density
                        translationY = shiftY2 * density
                        rotationZ = rot2
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            renderEffect = RenderEffect.createBlurEffect(
                                110f,
                                110f,
                                Shader.TileMode.CLAMP
                            ).asComposeRenderEffect()
                        }
                    }
                    .then(
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                            Modifier.blur(85.dp)
                        } else {
                            Modifier
                        }
                    )
            )
        }

        // Cinematic Scrim & Vignette for crystal-clear readability of titles, seekbars, waveform & lyrics
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.08f),
                            Color.Black.copy(alpha = 0.22f),
                            Color.Black.copy(alpha = 0.62f)
                        )
                    )
                )
        )
    }
}
