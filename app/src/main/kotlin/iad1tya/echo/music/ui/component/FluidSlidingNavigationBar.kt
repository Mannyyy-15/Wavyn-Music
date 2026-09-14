package iad1tya.echo.music.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.palette.graphics.Palette
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import iad1tya.echo.music.LocalPlayerConnection
import iad1tya.echo.music.R
import iad1tya.echo.music.extensions.togglePlayPause
import iad1tya.echo.music.ui.screens.Screens
import iad1tya.echo.music.ui.theme.PlayerColorExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
private fun DynamicEqualizerBars(
    isPlaying: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LivingEqualizer")

    val bar1Phase by infiniteTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = 1.00f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eq_bar1"
    )
    val bar2Phase by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(330, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eq_bar2"
    )
    val bar3Phase by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(480, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eq_bar3"
    )

    val h1 by animateFloatAsState(
        targetValue = if (isPlaying) bar1Phase else 0.22f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "h1"
    )
    val h2 by animateFloatAsState(
        targetValue = if (isPlaying) bar2Phase else 0.22f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "h2"
    )
    val h3 by animateFloatAsState(
        targetValue = if (isPlaying) bar3Phase else 0.22f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "h3"
    )

    Row(
        modifier = modifier
            .height(12.dp)
            .padding(end = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .fillMaxHeight(h1)
                .clip(CircleShape)
                .background(color)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .fillMaxHeight(h2)
                .clip(CircleShape)
                .background(color)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .fillMaxHeight(h3)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FluidSlidingNavigationBar(
    modifier: Modifier = Modifier,
    items: List<Screens>,
    currentRoute: String,
    pureBlack: Boolean,
    slim: Boolean = false,
    onTabSelected: (Screens) -> Unit,
    onTabLongClick: ((Screens) -> Unit)? = null,
    onExpandPlayer: (() -> Unit)? = null,
) {
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    val playerConnection = LocalPlayerConnection.current
    val mediaMetadata by playerConnection?.mediaMetadata?.collectAsState() ?: remember { mutableStateOf(null) }
    val isPlaying by playerConnection?.isPlaying?.collectAsState() ?: remember { mutableStateOf(false) }
    val playbackState by playerConnection?.playbackState?.collectAsState() ?: remember { mutableStateOf(Player.STATE_IDLE) }
    val canSkipNext by playerConnection?.canSkipNext?.collectAsState() ?: remember { mutableStateOf(false) }
    val canSkipPrevious by playerConnection?.canSkipPrevious?.collectAsState() ?: remember { mutableStateOf(false) }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }

    LaunchedEffect(playerConnection, playbackState, isPlaying) {
        val player = playerConnection?.player ?: return@LaunchedEffect
        if (playbackState == Player.STATE_READY) {
            while (isActive) {
                position = player.currentPosition
                duration = player.duration
                delay(500)
            }
        } else {
            position = player.currentPosition
            duration = player.duration
        }
    }

    var gradientColors by remember { mutableStateOf<List<Color>>(emptyList()) }

    LaunchedEffect(mediaMetadata?.thumbnailUrl) {
        mediaMetadata?.thumbnailUrl?.let { url ->
            try {
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .size(100, 100)
                    .allowHardware(false)
                    .build()
                val result = context.imageLoader.execute(request)
                result.image?.let { image ->
                    val palette = withContext(Dispatchers.Default) {
                        val bitmap = image.toBitmap()
                        Palette.from(bitmap)
                            .maximumColorCount(8)
                            .resizeBitmapArea(100 * 100)
                            .generate()
                    }
                    gradientColors = PlayerColorExtractor.extractGradientColors(
                        palette = palette,
                        fallbackColor = Color.Black.toArgb()
                    )
                }
            } catch (e: Exception) {
                gradientColors = emptyList()
            }
        } ?: run {
            gradientColors = emptyList()
        }
    }

    val hasActiveSong = mediaMetadata != null

    val dockBgColor = if (pureBlack) {
        Color(0xF607080B)
    } else {
        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f)
    }

    val dockBorderBrush = Brush.verticalGradient(
        listOf(
            if (pureBlack) Color.White.copy(alpha = 0.20f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
            if (pureBlack) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.10f)
        )
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(30.dp),
                ambientColor = Color.Black.copy(alpha = if (pureBlack) 0.65f else 0.40f),
                spotColor = Color.Black.copy(alpha = if (pureBlack) 0.80f else 0.50f)
            )
            .clip(RoundedCornerShape(30.dp))
            .border(
                width = 1.dp,
                brush = dockBorderBrush,
                shape = RoundedCornerShape(30.dp)
            )
            .background(dockBgColor)
            .fillMaxWidth()
    ) {
        // Dynamic ambient artwork bloom
        if (hasActiveSong && gradientColors.isNotEmpty() && !pureBlack) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                gradientColors.first().copy(alpha = 0.25f),
                                gradientColors.last().copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // TOP TIER: NOW PLAYING STRIP
            AnimatedVisibility(
                visible = hasActiveSong,
                enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    var dragAccumulated by remember { mutableFloatStateOf(0f) }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .padding(horizontal = 12.dp)
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragStart = { dragAccumulated = 0f },
                                    onHorizontalDrag = { _, dragAmount ->
                                        dragAccumulated += dragAmount
                                    },
                                    onDragEnd = {
                                        if (dragAccumulated > 60f && canSkipPrevious) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            playerConnection?.player?.seekToPreviousMediaItem()
                                        } else if (dragAccumulated < -60f && canSkipNext) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            playerConnection?.player?.seekToNext()
                                        }
                                        dragAccumulated = 0f
                                    },
                                    onDragCancel = { dragAccumulated = 0f }
                                )
                            }
                            .clickable {
                                onExpandPlayer?.invoke()
                            }
                    ) {
                        // Album Art (38.dp)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(38.dp)
                        ) {
                            AsyncImage(
                                model = mediaMetadata?.thumbnailUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                error = painterResource(R.drawable.wavyn_logo),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(9.dp))
                                    .border(
                                        width = 0.8.dp,
                                        color = Color.White.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(9.dp)
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Song Info
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = mediaMetadata?.title.orEmpty(),
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.basicMarquee(
                                    iterations = 1,
                                    initialDelayMillis = 3000,
                                    velocity = 30.dp
                                )
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 1.dp)
                            ) {
                                DynamicEqualizerBars(
                                    isPlaying = isPlaying,
                                    color = if (gradientColors.isNotEmpty()) gradientColors.first() else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = mediaMetadata?.artists?.joinToString { it.name }.orEmpty(),
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.basicMarquee(
                                        iterations = 1,
                                        initialDelayMillis = 3000,
                                        velocity = 30.dp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Tactile Play/Pause Bubble
                        var isPlayPressed by remember { mutableStateOf(false) }
                        val playScale by animateFloatAsState(
                            targetValue = if (isPlayPressed) 0.86f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "playScale"
                        )

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .graphicsLayer {
                                    scaleX = playScale
                                    scaleY = playScale
                                }
                                .clip(CircleShape)
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.30f),
                                    shape = CircleShape
                                )
                                .background(
                                    color = if (gradientColors.isNotEmpty())
                                        Color.White.copy(alpha = 0.18f)
                                    else
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    coroutineScope.launch {
                                        isPlayPressed = true
                                        delay(120)
                                        isPlayPressed = false
                                    }
                                    if (playbackState == Player.STATE_ENDED) {
                                        playerConnection?.player?.seekTo(0, 0)
                                        playerConnection?.player?.playWhenReady = true
                                    } else {
                                        playerConnection?.player?.togglePlayPause()
                                    }
                                }
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (playbackState == Player.STATE_ENDED) {
                                        R.drawable.replay
                                    } else if (isPlaying) {
                                        R.drawable.pause
                                    } else {
                                        R.drawable.play
                                    }
                                ),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Skip Next Pill
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .clickable(enabled = canSkipNext) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    playerConnection?.player?.seekToNext()
                                }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.skip_next),
                                contentDescription = null,
                                tint = if (canSkipNext) Color.White.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // HORIZON PROGRESS LINE (1.5.dp)
                    if (duration > 0) {
                        val progressFraction = (position.toFloat() / duration).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.5.dp)
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressFraction)
                                    .fillMaxHeight()
                                    .background(
                                        Brush.horizontalGradient(
                                            if (gradientColors.isNotEmpty()) {
                                                listOf(gradientColors.first(), Color.White.copy(alpha = 0.85f))
                                            } else {
                                                listOf(MaterialTheme.colorScheme.primary, Color.White.copy(alpha = 0.85f))
                                            }
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            // BOTTOM TIER: NAVIGATION TABS
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val tabWidth = maxWidth / items.size
                val pillWidth = if (slim) 52.dp else 56.dp
                val pillHeight = if (slim) 32.dp else 34.dp

                val indicatorOffset by animateDpAsState(
                    targetValue = (tabWidth * selectedIndex) + ((tabWidth - pillWidth) / 2),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "DockPillOffset"
                )

                // Floating Active Capsule Indicator styled with Theme primary container
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset, y = if (slim) 10.dp else 12.dp)
                        .width(pillWidth)
                        .height(pillHeight)
                        .clip(CircleShape)
                        .border(
                            width = 0.8.dp,
                            color = if (pureBlack) Color.White.copy(alpha = 0.18f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                            shape = CircleShape
                        )
                        .background(
                            if (pureBlack) {
                                Color.White.copy(alpha = 0.16f)
                            } else {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
                            }
                        )
                )

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, item ->
                        val isSelected = selectedIndex == index

                        val iconScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.10f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "TabIconScale"
                        )

                        val activeColor = if (pureBlack) Color.White else MaterialTheme.colorScheme.primary
                        val inactiveColor = if (pureBlack) Color.White.copy(alpha = 0.55f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)

                        val tabColor by animateColorAsState(
                            targetValue = if (isSelected) activeColor else inactiveColor,
                            animationSpec = tween(durationMillis = 200),
                            label = "TabColor"
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .combinedClickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onTabSelected(item) },
                                    onLongClick = onTabLongClick?.let { { it(item) } }
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = if (slim) Arrangement.Center else Arrangement.Top
                        ) {
                            if (!slim) {
                                Spacer(modifier = Modifier.height(17.dp))
                            }

                            Icon(
                                painter = painterResource(id = if (isSelected) item.iconIdActive else item.iconIdInactive),
                                contentDescription = stringResource(id = item.titleId),
                                tint = tabColor,
                                modifier = Modifier
                                    .size(23.dp)
                                    .graphicsLayer {
                                        scaleX = iconScale
                                        scaleY = iconScale
                                    }
                            )

                            if (!slim) {
                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = stringResource(id = item.titleId),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = tabColor,
                                    letterSpacing = 0.2.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
