package iad1tya.echo.music.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import iad1tya.echo.music.utils.makeTimeString
import kotlin.math.sin

// Deterministic pleasing pseudo-waveform base heights
private val WaveformBaseHeights = floatArrayOf(
    0.28f, 0.45f, 0.70f, 0.52f, 0.88f, 0.65f, 0.40f, 0.78f,
    0.95f, 0.60f, 0.35f, 0.82f, 1.00f, 0.74f, 0.50f, 0.90f,
    0.68f, 0.42f, 0.85f, 0.58f, 0.75f, 0.92f, 0.62f, 0.38f,
    0.80f, 0.98f, 0.70f, 0.48f, 0.86f, 0.64f, 0.40f, 0.76f,
    0.94f, 0.55f, 0.36f, 0.68f, 0.84f, 0.50f, 0.32f
)

@Composable
fun WaveformSeekbar(
    position: Long,
    duration: Long,
    isPlaying: Boolean,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = activeColor.copy(alpha = 0.28f),
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    showTimeLabels: Boolean = true,
) {
    val barCount = WaveformBaseHeights.size
    val totalDuration = duration.coerceAtLeast(1L)
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val currentFraction = if (isDragging) {
        dragFraction
    } else {
        (position.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
    }

    val displayPosition = if (isDragging) {
        (dragFraction * totalDuration).toLong()
    } else {
        position
    }

    val infiniteTransition = rememberInfiniteTransition(label = "WaveformAnim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.2831853f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WaveformPhase"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        if (showTimeLabels) {
            Text(
                text = makeTimeString(displayPosition),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = textColor,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(34.dp)
                .pointerInput(totalDuration) {
                    detectTapGestures { offset ->
                        val fraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onSeek((fraction * totalDuration).toLong())
                    }
                }
                .pointerInput(totalDuration) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            isDragging = false
                            onSeek((dragFraction * totalDuration).toLong())
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val deltaFraction = dragAmount / size.width.toFloat()
                            dragFraction = (dragFraction + deltaFraction).coerceIn(0f, 1f)
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(34.dp)) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val totalBars = barCount
                val barGap = 2.5f.dp.toPx()
                val totalGapWidth = (totalBars - 1) * barGap
                val barWidth = ((canvasWidth - totalGapWidth) / totalBars).coerceAtLeast(1.5f.dp.toPx())
                val minBarHeight = 4.dp.toPx()
                val maxBarHeight = canvasHeight * 0.88f
                val cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)

                for (i in 0 until totalBars) {
                    val barCenterFraction = (i + 0.5f) / totalBars
                    val isBarActive = barCenterFraction <= currentFraction

                    val baseScale = WaveformBaseHeights[i % WaveformBaseHeights.size]
                    val animatedMultiplier = if (isPlaying && !isDragging) {
                        0.82f + 0.18f * sin(phase + i * 0.45f).toFloat()
                    } else {
                        1.0f
                    }

                    val computedHeight = (maxBarHeight * baseScale * animatedMultiplier)
                        .coerceIn(minBarHeight, maxBarHeight)

                    val x = i * (barWidth + barGap)
                    val y = (canvasHeight - computedHeight) / 2f

                    drawRoundRect(
                        color = if (isBarActive) activeColor else inactiveColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, computedHeight),
                        cornerRadius = cornerRadius
                    )
                }
            }
        }

        if (showTimeLabels) {
            Text(
                text = makeTimeString(duration),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = textColor,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
