package iad1tya.echo.music.ui.player

import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.draw.shadow
import kotlinx.coroutines.delay
import iad1tya.echo.music.constants.DynamicIslandMaxWidth
import iad1tya.echo.music.constants.DynamicIslandWidthFraction
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import iad1tya.echo.music.LocalDatabase
import iad1tya.echo.music.LocalPlayerConnection
import iad1tya.echo.music.R
import iad1tya.echo.music.constants.MiniPlayerHeight
import iad1tya.echo.music.constants.FloatingCompactMaxWidth
import iad1tya.echo.music.constants.FloatingCompactWidthFraction
import iad1tya.echo.music.constants.OldNavbarStyleKey
import iad1tya.echo.music.constants.PureBlackMiniPlayerKey
import iad1tya.echo.music.constants.SwipeSensitivityKey
import iad1tya.echo.music.constants.ThumbnailCornerRadius
import iad1tya.echo.music.db.entities.ArtistEntity
import iad1tya.echo.music.extensions.togglePlayPause
import iad1tya.echo.music.models.MediaMetadata
import iad1tya.echo.music.utils.rememberPreference
import iad1tya.echo.music.ui.theme.PlayerColorExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.palette.graphics.Palette
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.toArgb

private fun Modifier.tvFocusableHighlight(shape: Shape): Modifier = composed {
    val context = LocalContext.current
    val isTvDevice = remember(context) {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
    }
    var isFocused by remember { mutableStateOf(false) }

    if (isTvDevice) {
        this
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = shape
            )
    } else {
        this
    }
}

@Composable
fun MiniPlayer(
    position: Long,
    duration: Long,
    modifier: Modifier = Modifier,
    pureBlack: Boolean,
) {
    val useNewMiniPlayerDesign = true

    if (useNewMiniPlayerDesign) {
        NewMiniPlayer(
            position = position,
            duration = duration,
            modifier = modifier,
            pureBlack = pureBlack
        )
    } else {
        // NEW: Wrap LegacyMiniPlayer in a Box to allow alignment on tablet landscape.
        // The outer Box fills the width, providing a container for the inner player to be aligned within.
        Box(modifier = modifier.fillMaxWidth()) {
            LegacyMiniPlayer(
                position = position,
                duration = duration,
                // NEW: Align the player to the end if it's a tablet in landscape.
                // This modifier is passed to LegacyMiniPlayer and applied to its root Box.
                modifier = if (
                    LocalConfiguration.current.screenWidthDp >= 600 &&
                    LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
                ) {
                    Modifier.align(Alignment.CenterEnd)
                } else {
                    Modifier.align(Alignment.Center)
                },
                pureBlack = pureBlack
            )
        }
    }
}

@Composable
private fun DynamicEqualizerBars(
    isPlaying: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "DynamicIslandEqualizer")

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
            .height(13.dp)
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

@Composable
private fun NewMiniPlayer(
    position: Long,
    duration: Long,
    modifier: Modifier = Modifier,
    pureBlack: Boolean,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val database = LocalDatabase.current
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val playbackState by playerConnection.playbackState.collectAsState()
    val error by playerConnection.error.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()

    val currentView = LocalView.current
    val layoutDirection = LocalLayoutDirection.current
    val coroutineScope = rememberCoroutineScope()
    val swipeSensitivity by rememberPreference(SwipeSensitivityKey, 0.73f)
    val swipeThumbnail by rememberPreference(iad1tya.echo.music.constants.SwipeThumbnailKey, true)
    val pureBlackMiniPlayer by rememberPreference(PureBlackMiniPlayerKey, false)
    val oldNavbarStyle by rememberPreference(OldNavbarStyleKey, false)

    val configuration = LocalConfiguration.current
    val isTabletLandscape = configuration.screenWidthDp >= 600 &&
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Extract gradient colors from album art
    val context = LocalContext.current
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
        }
    }

    val offsetXAnimatable = remember { Animatable(0f) }
    var dragStartTime by remember { mutableLongStateOf(0L) }
    var totalDragDistance by remember { mutableFloatStateOf(0f) }

    // Optimized spring spec for responsive island physics
    val animationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh,
        visibilityThreshold = 0.1f
    )

    fun calculateAutoSwipeThreshold(swipeSensitivity: Float): Int {
        return (600 / (1f + kotlin.math.exp(-(-11.44748 * swipeSensitivity + 9.04945)))).roundToInt()
    }
    val autoSwipeThreshold = calculateAutoSwipeThreshold(swipeSensitivity)

    val isEffectivelyPureBlack = pureBlack || pureBlackMiniPlayer

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(MiniPlayerHeight)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
            .padding(horizontal = 8.dp)
            .let { baseModifier ->
                if (swipeThumbnail) {
                    baseModifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                dragStartTime = System.currentTimeMillis()
                                totalDragDistance = 0f
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    offsetXAnimatable.animateTo(
                                        targetValue = 0f,
                                        animationSpec = animationSpec
                                    )
                                }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                val adjustedDragAmount =
                                    if (layoutDirection == LayoutDirection.Rtl) -dragAmount else dragAmount
                                val canPrev = playerConnection.player.previousMediaItemIndex != -1
                                val canNext = playerConnection.player.nextMediaItemIndex != -1
                                val allowLeft = adjustedDragAmount < 0 && canNext
                                val allowRight = adjustedDragAmount > 0 && canPrev
                                if (allowLeft || allowRight) {
                                    totalDragDistance += kotlin.math.abs(adjustedDragAmount)
                                    coroutineScope.launch {
                                        offsetXAnimatable.snapTo(offsetXAnimatable.value + adjustedDragAmount)
                                    }
                                }
                            },
                            onDragEnd = {
                                val dragDuration = System.currentTimeMillis() - dragStartTime
                                val velocity = if (dragDuration > 0) totalDragDistance / dragDuration else 0f
                                val currentOffset = offsetXAnimatable.value

                                val minDistanceThreshold = 50f
                                val velocityThreshold = (swipeSensitivity * -8.25f) + 8.5f

                                val shouldChangeSong = (
                                    kotlin.math.abs(currentOffset) > minDistanceThreshold &&
                                    velocity > velocityThreshold
                                ) || (kotlin.math.abs(currentOffset) > autoSwipeThreshold)

                                if (shouldChangeSong) {
                                    val isRightSwipe = currentOffset > 0
                                    if (isRightSwipe && canSkipPrevious) {
                                        playerConnection.player.seekToPreviousMediaItem()
                                    } else if (!isRightSwipe && canSkipNext) {
                                        playerConnection.player.seekToNext()
                                    }
                                }

                                coroutineScope.launch {
                                    offsetXAnimatable.animateTo(
                                        targetValue = 0f,
                                        animationSpec = animationSpec
                                    )
                                }
                            }
                        )
                    }
                } else {
                    baseModifier
                }
            }
    ) {
        // Floating Mini Player Card (Spotify & Apple Music Style)
        Box(
            modifier = Modifier
                .then(
                    if (isTabletLandscape) {
                        Modifier
                            .width(460.dp)
                            .align(Alignment.CenterEnd)
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                    }
                )
                .height(58.dp)
                .offset { IntOffset(offsetXAnimatable.value.roundToInt(), 0) }
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(14.dp),
                    ambientColor = Color.Black.copy(alpha = if (isEffectivelyPureBlack) 0.60f else 0.35f),
                    spotColor = Color.Black.copy(alpha = if (isEffectivelyPureBlack) 0.75f else 0.50f)
                )
                .clip(RoundedCornerShape(14.dp))
                .border(
                    width = 0.8.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (isEffectivelyPureBlack) 0.16f else 0.22f),
                            Color.White.copy(alpha = if (isEffectivelyPureBlack) 0.04f else 0.06f)
                        )
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
                .then(
                    if (isEffectivelyPureBlack) {
                        Modifier.background(Color(0xFF07080B))
                    } else {
                        Modifier
                            .background(Color(0xFF13151B))
                            .then(
                                if (gradientColors.isNotEmpty()) {
                                    Modifier.background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF13151B),
                                                gradientColors.first().copy(alpha = 0.28f),
                                                Color(0xFF13151B)
                                            )
                                        )
                                    )
                                } else {
                                    Modifier
                                }
                            )
                    }
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, end = 8.dp),
            ) {
                // Modern Album Artwork (Rounded Square like Spotify & Apple Music)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 0.6.dp,
                            color = Color.White.copy(alpha = 0.20f),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    mediaMetadata?.let { metadata ->
                        AsyncImage(
                            model = metadata.thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.wavyn_logo),
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Song Typography & Live Equalizer
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    mediaMetadata?.let { metadata ->
                        AnimatedContent(
                            targetState = metadata.title,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "island_title",
                        ) { title ->
                            Text(
                                text = title,
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.basicMarquee(
                                    iterations = 1,
                                    initialDelayMillis = 3000,
                                    velocity = 30.dp
                                ),
                            )
                        }

                        if (metadata.artists.any { it.name.isNotBlank() }) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 1.dp)
                            ) {
                                DynamicEqualizerBars(
                                    isPlaying = isPlaying,
                                    color = if (gradientColors.isNotEmpty()) {
                                        gradientColors.first()
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                                AnimatedContent(
                                    targetState = metadata.artists.joinToString { it.name },
                                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                                    label = "island_artists",
                                ) { artists ->
                                    Text(
                                        text = artists,
                                        color = Color.White.copy(alpha = 0.65f),
                                        fontSize = 11.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.basicMarquee(
                                            iterations = 1,
                                            initialDelayMillis = 3000,
                                            velocity = 30.dp
                                        ),
                                    )
                                }
                            }
                        }

                        // Error indicator
                        AnimatedVisibility(
                            visible = error != null,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            Text(
                                text = "Error playing",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                // Tactile Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Sleek Skip Previous
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .tvFocusableHighlight(CircleShape)
                            .clip(CircleShape)
                            .clickable(enabled = canSkipPrevious) {
                                playerConnection.player.seekToPreviousMediaItem()
                            }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.skip_previous),
                            contentDescription = null,
                            tint = if (canSkipPrevious)
                                Color.White.copy(alpha = 0.85f)
                            else
                                Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.size(19.dp)
                        )
                    }

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
                            .size(36.dp)
                            .graphicsLayer {
                                scaleX = playScale
                                scaleY = playScale
                            }
                            .tvFocusableHighlight(CircleShape)
                            .clip(CircleShape)
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.35f),
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
                                coroutineScope.launch {
                                    isPlayPressed = true
                                    delay(120)
                                    isPlayPressed = false
                                }
                                if (playbackState == Player.STATE_ENDED) {
                                    playerConnection.player.seekTo(0, 0)
                                    playerConnection.player.playWhenReady = true
                                } else {
                                    playerConnection.player.togglePlayPause()
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
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Sleek Skip Next
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .tvFocusableHighlight(CircleShape)
                            .clip(CircleShape)
                            .clickable(enabled = canSkipNext) {
                                playerConnection.player.seekToNext()
                            }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.skip_next),
                            contentDescription = null,
                            tint = if (canSkipNext)
                                Color.White.copy(alpha = 0.85f)
                            else
                                Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            // Contour Hairline Progress Bar
            if (duration > 0) {
                val progressFraction = (position.toFloat() / duration).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .align(Alignment.BottomCenter)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    if (gradientColors.isNotEmpty()) {
                                        listOf(
                                            gradientColors.first(),
                                            Color.White.copy(alpha = 0.90f)
                                        )
                                    } else {
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            Color.White.copy(alpha = 0.90f)
                                        )
                                    }
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun LegacyMiniPlayer(
    position: Long,
    duration: Long,
    modifier: Modifier = Modifier,
    pureBlack: Boolean,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val playbackState by playerConnection.playbackState.collectAsState()
    val error by playerConnection.error.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()

    val currentView = LocalView.current
    val layoutDirection = LocalLayoutDirection.current
    val coroutineScope = rememberCoroutineScope()
    val swipeSensitivity by rememberPreference(SwipeSensitivityKey, 0.73f)
    val swipeThumbnail by rememberPreference(iad1tya.echo.music.constants.SwipeThumbnailKey, true)
    val pureBlackMiniPlayer by rememberPreference(PureBlackMiniPlayerKey, false)

    // NEW: Get screen configuration to determine if it's a tablet in landscape mode.
    val configuration = LocalConfiguration.current
    val isTabletLandscape = configuration.screenWidthDp >= 600 &&
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Extract gradient colors from album art
    val context = LocalContext.current
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
        }
    }

    val offsetXAnimatable = remember { Animatable(0f) }
    var dragStartTime by remember { mutableLongStateOf(0L) }
    var totalDragDistance by remember { mutableFloatStateOf(0f) }

    val animationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    fun calculateAutoSwipeThreshold(swipeSensitivity: Float): Int {
        return (600 / (1f + kotlin.math.exp(-(-11.44748 * swipeSensitivity + 9.04945)))).roundToInt()
    }
    val autoSwipeThreshold = calculateAutoSwipeThreshold(swipeSensitivity)

    Box(
        modifier = modifier
            .then(
                // NEW: Conditionally set the width based on the device configuration.
                if (isTabletLandscape) {
                    Modifier.width(500.dp)
                } else {
                    Modifier
                        .fillMaxWidth(FloatingCompactWidthFraction)
                        .widthIn(max = FloatingCompactMaxWidth)
                }
            )
            .height(MiniPlayerHeight)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
            // NEW: Clip the shape BEFORE applying the background.
            // This ensures that the background is applied to the clipped, rounded shape,
            // preventing sharp edges when the width is reduced.
            .clip(RoundedCornerShape(28.dp))
            .then(
                if (pureBlack || pureBlackMiniPlayer) {
                    Modifier.background(Color.Black)
                } else if (gradientColors.isNotEmpty()) {
                    Modifier.background(
                        Brush.verticalGradient(
                            colors = gradientColors
                        )
                    )
                } else {
                    Modifier.background(
                        color = MaterialTheme.colorScheme.surfaceContainer
                    )
                }
            )
            .let { baseModifier ->
                if (swipeThumbnail) {
                    baseModifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                dragStartTime = System.currentTimeMillis()
                                totalDragDistance = 0f
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    offsetXAnimatable.animateTo(
                                        targetValue = 0f,
                                        animationSpec = animationSpec
                                    )
                                }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                val adjustedDragAmount =
                                    if (layoutDirection == LayoutDirection.Rtl) -dragAmount else dragAmount
                                val canSkipPrevious = playerConnection.player.previousMediaItemIndex != -1
                                val canSkipNext = playerConnection.player.nextMediaItemIndex != -1
                                val allowLeft = adjustedDragAmount < 0 && canSkipNext
                                val allowRight = adjustedDragAmount > 0 && canSkipPrevious
                                if (allowLeft || allowRight) {
                                    totalDragDistance += kotlin.math.abs(adjustedDragAmount)
                                    coroutineScope.launch {
                                        offsetXAnimatable.snapTo(offsetXAnimatable.value + adjustedDragAmount)
                                    }
                                }
                            },
                            onDragEnd = {
                                val dragDuration = System.currentTimeMillis() - dragStartTime
                                val velocity = if (dragDuration > 0) totalDragDistance / dragDuration else 0f
                                val currentOffset = offsetXAnimatable.value

                                val minDistanceThreshold = 50f
                                val velocityThreshold = (swipeSensitivity * -8.25f) + 8.5f

                                val shouldChangeSong = (
                                    kotlin.math.abs(currentOffset) > minDistanceThreshold &&
                                    velocity > velocityThreshold
                                ) || (kotlin.math.abs(currentOffset) > autoSwipeThreshold)

                                if (shouldChangeSong) {
                                    val isRightSwipe = currentOffset > 0

                                    if (isRightSwipe && canSkipPrevious) {
                                        playerConnection.player.seekToPreviousMediaItem()
                                    } else if (!isRightSwipe && canSkipNext) {
                                        playerConnection.player.seekToNext()
                                    }
                                }

                                coroutineScope.launch {
                                    offsetXAnimatable.animateTo(
                                        targetValue = 0f,
                                        animationSpec = animationSpec
                                    )
                                }
                            }
                        )
                    }
                } else {
                    baseModifier
                }
            }
    ) {
        LinearProgressIndicator(
            progress = { (position.toFloat() / duration).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.BottomCenter),
            drawStopIndicator = {},
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetXAnimatable.value.roundToInt(), 0) }
                .padding(end = 12.dp),
        ) {
            Box(Modifier.weight(1f)) {
                mediaMetadata?.let {
                    LegacyMiniMediaInfo(
                        mediaMetadata = it,
                        error = error,
                        pureBlack = pureBlack,
                        hasGradient = gradientColors.isNotEmpty(),
                        modifier = Modifier.padding(horizontal = 6.dp),
                    )
                }
            }

            // Previous button
            IconButton(
                enabled = canSkipPrevious,
                onClick = playerConnection::seekToPrevious,
            ) {
                Icon(
                    painter = painterResource(R.drawable.skip_previous),
                    contentDescription = null,
                    tint = if (gradientColors.isNotEmpty()) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }

            // Play/Pause button
            IconButton(
                onClick = {
                    if (playbackState == Player.STATE_ENDED) {
                        playerConnection.player.seekTo(0, 0)
                        playerConnection.player.playWhenReady = true
                    } else {
                        playerConnection.player.togglePlayPause()
                    }
                },
            ) {
                Icon(
                    painter = painterResource(
                        if (playbackState == Player.STATE_ENDED) {
                            R.drawable.replay
                        } else if (isPlaying) {
                            R.drawable.pause
                        } else {
                            R.drawable.play
                        },
                    ),
                    contentDescription = null,
                    tint = if (gradientColors.isNotEmpty()) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }

            // Next button
            IconButton(
                enabled = canSkipNext,
                onClick = playerConnection::seekToNext,
            ) {
                Icon(
                    painter = painterResource(R.drawable.skip_next),
                    contentDescription = null,
                    tint = if (gradientColors.isNotEmpty()) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Visual indicator
        if (offsetXAnimatable.value.absoluteValue > 50f) {
            Box(
                modifier = Modifier
                    .align(if (offsetXAnimatable.value > 0) Alignment.CenterStart else Alignment.CenterEnd)
                    .padding(horizontal = 16.dp)
            ) {
                Icon(
                    painter = painterResource(
                        if (offsetXAnimatable.value > 0) R.drawable.skip_previous else R.drawable.skip_next
                    ),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(
                        alpha = (offsetXAnimatable.value.absoluteValue / autoSwipeThreshold).coerceIn(0f, 1f)
                    ),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun LegacyMiniMediaInfo(
    mediaMetadata: MediaMetadata,
    error: PlaybackException?,
    pureBlack: Boolean,
    hasGradient: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .padding(6.dp)
                .size(48.dp)
                .clip(RoundedCornerShape(ThumbnailCornerRadius))
        ) {
            // Simple background instead of expensive blur
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            // Main thumbnail
            AsyncImage(
                model = mediaMetadata.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                error = painterResource(R.drawable.wavyn_logo),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(ThumbnailCornerRadius)),
            )

            androidx.compose.animation.AnimatedVisibility(
                visible = error != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            color = if (pureBlack) Color.Black else Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(ThumbnailCornerRadius),
                        ),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.info),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp),
        ) {
            AnimatedContent(
                targetState = mediaMetadata.title,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "",
            ) { title ->
                Text(
                    text = title,
                    color = if (hasGradient) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee(),
                )
            }

            if (mediaMetadata.artists.any { it.name.isNotBlank() }) {
                AnimatedContent(
                    targetState = mediaMetadata.artists.joinToString { it.name },
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "",
                ) { artists ->
                    Text(
                        text = artists,
                        color = if (hasGradient) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.secondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
