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
import kotlin.math.sin

/**
 * Animated full-screen Chroma & Blur Thumbnail Player Background.
 * Renders the track's authentic album art over the entire device display with living harmonic
 * scale/pan motion, dual-layer chromatic aberration drift, and cinematic vignette.
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

    val infiniteTransition = rememberInfiniteTransition(label = "ChromaArtworkTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ChromaTime"
    )

    fun wave(min: Float, max: Float, phase: Float, speed: Float = 1f): Float {
        val s = sin(2.0 * PI * (time * speed + phase)).toFloat()
        return min + (max - min) * ((s + 1f) * 0.5f)
    }

    // Primary layer harmonic transformation parameters (slow breathing & floating orbit)
    val scale1 = wave(1.30f, 1.44f, 0.0f, 0.8f)
    val shiftX1 = wave(-30f, 30f, 0.12f, 1.1f)
    val shiftY1 = wave(-40f, 40f, 0.25f, 0.9f)
    val rot1 = wave(-2.5f, 2.5f, 0.40f, 0.7f)

    // Secondary chromatic shift layer transformation parameters (counter-phase drift)
    val scale2 = wave(1.36f, 1.50f, 0.50f, 1.0f)
    val shiftX2 = wave(32f, -32f, 0.65f, 0.85f)
    val shiftY2 = wave(42f, -42f, 0.78f, 1.15f)
    val rot2 = wave(3.0f, -3.0f, 0.90f, 0.75f)

    val chromaPulse = wave(0.40f, 0.65f, 0.35f, 1.3f)

    val saturationMatrix = remember {
        ColorMatrix().apply {
            setToSaturation(1.35f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(alpha)
            .background(Color(0xFF08090E))
    ) {
        if (thumbnailUrl != null) {
            // Base Layer: Full-screen zoomed, heavily blurred authentic artwork with rich saturation
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(thumbnailUrl)
                    .size(180, 180)
                    .allowHardware(false)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale1
                        scaleY = scale1
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

            // Chroma Aberration Layer: Secondary counter-phase blurred artwork creating liquid color morphing
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(thumbnailUrl)
                    .size(140, 140)
                    .allowHardware(false)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.alpha = chromaPulse
                        scaleX = scale2
                        scaleY = scale2
                        translationX = shiftX2 * density
                        translationY = shiftY2 * density
                        rotationZ = rot2
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            renderEffect = RenderEffect.createBlurEffect(
                                105f,
                                105f,
                                Shader.TileMode.CLAMP
                            ).asComposeRenderEffect()
                        }
                    }
                    .then(
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                            Modifier.blur(80.dp)
                        } else {
                            Modifier
                        }
                    )
            )
        }

        // Ambient Palette Glow Accent Nodes (if colors are present)
        if (colors.isNotEmpty()) {
            val c0 = colors.getOrElse(0) { Color.Transparent }
            val c1 = colors.getOrElse(1) { c0 }
            val c2 = colors.getOrElse(2) { c1 }

            val p1x = wave(0.10f, 0.50f, 0.00f, 0.9f)
            val p1y = wave(0.10f, 0.45f, 0.15f, 1.1f)
            val p2x = wave(0.50f, 0.90f, 0.30f, 1.0f)
            val p2y = wave(0.55f, 0.90f, 0.45f, 0.8f)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.35f)
                    .drawWithCache {
                        val w = size.width
                        val h = size.height
                        val b1 = Brush.radialGradient(
                            colors = listOf(c0.copy(alpha = 0.80f), Color.Transparent),
                            center = Offset(w * p1x, h * p1y),
                            radius = w * 0.9f
                        )
                        val b2 = Brush.radialGradient(
                            colors = listOf(c1.copy(alpha = 0.70f), c2.copy(alpha = 0.35f), Color.Transparent),
                            center = Offset(w * p2x, h * p2y),
                            radius = w * 1.1f
                        )
                        onDrawBehind {
                            drawRect(brush = b1)
                            drawRect(brush = b2)
                        }
                    }
            )
        }

        // Cinematic Scrim & Vignette for crystal-clear readability of titles, seekbars, waveform & lyrics
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.38f),
                            Color.Black.copy(alpha = 0.10f),
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.65f)
                        )
                    )
                )
        )
    }
}
