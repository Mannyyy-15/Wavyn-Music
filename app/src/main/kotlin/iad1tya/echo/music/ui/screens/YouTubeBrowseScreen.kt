@file:Suppress("UNUSED_EXPRESSION")

package iad1tya.echo.music.ui.screens

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.echo.innertube.models.AlbumItem
import com.echo.innertube.models.ArtistItem
import com.echo.innertube.models.EpisodeItem
import com.echo.innertube.models.PlaylistItem
import com.echo.innertube.models.PodcastItem
import com.echo.innertube.models.SongItem
import com.echo.innertube.models.YTItem
import iad1tya.echo.music.LocalPlayerAwareWindowInsets
import iad1tya.echo.music.LocalPlayerConnection
import iad1tya.echo.music.R
import iad1tya.echo.music.constants.ListItemHeight
import iad1tya.echo.music.extensions.togglePlayPause
import iad1tya.echo.music.models.toMediaMetadata
import iad1tya.echo.music.playback.queues.YouTubeQueue
import iad1tya.echo.music.ui.component.IconButton
import iad1tya.echo.music.ui.component.LocalMenuState
import iad1tya.echo.music.ui.component.YouTubeListItem
import iad1tya.echo.music.ui.component.shimmer.GridItemPlaceHolder
import iad1tya.echo.music.ui.component.shimmer.ShimmerHost
import iad1tya.echo.music.ui.component.shimmer.TextPlaceholder
import iad1tya.echo.music.ui.menu.YouTubeAlbumMenu
import iad1tya.echo.music.ui.menu.YouTubeArtistMenu
import iad1tya.echo.music.ui.menu.YouTubePlaylistMenu
import iad1tya.echo.music.ui.menu.YouTubeSongMenu
import iad1tya.echo.music.ui.utils.SnapLayoutInfoProvider
import iad1tya.echo.music.ui.utils.backToMain
import iad1tya.echo.music.viewmodels.YouTubeBrowseViewModel

private fun getGenreHeaderGradient(title: String?): List<Color> {
    val t = title?.lowercase() ?: ""
    return when {
        t.contains("rock") || t.contains("metal") -> listOf(Color(0xFF701A28), Color(0xFF3F0C15), Color(0xFF0F1218))
        t.contains("hip") || t.contains("rap") -> listOf(Color(0xFF0C4A6E), Color(0xFF082F49), Color(0xFF0F1218))
        t.contains("pop") || t.contains("indie") -> listOf(Color(0xFF065F46), Color(0xFF022C22), Color(0xFF0F1218))
        t.contains("dance") || t.contains("electro") || t.contains("techno") -> listOf(Color(0xFF5B21B6), Color(0xFF2E1065), Color(0xFF0F1218))
        t.contains("r&b") || t.contains("soul") -> listOf(Color(0xFF6B21A8), Color(0xFF3B0764), Color(0xFF0F1218))
        t.contains("party") || t.contains("punjabi") -> listOf(Color(0xFF9D174D), Color(0xFF500724), Color(0xFF0F1218))
        t.contains("workout") -> listOf(Color(0xFF9A3412), Color(0xFF431407), Color(0xFF0F1218))
        t.contains("chill") || t.contains("sad") -> listOf(Color(0xFF1E3A8A), Color(0xFF0F172A), Color(0xFF0F1218))
        t.contains("romance") -> listOf(Color(0xFF881337), Color(0xFF4C0519), Color(0xFF0F1218))
        t.contains("bollywood") || t.contains("hindi") || t.contains("jazz") -> listOf(Color(0xFF78350F), Color(0xFF451A03), Color(0xFF0F1218))
        else -> listOf(Color(0xFF4C1D95), Color(0xFF1E1B4B), Color(0xFF0F1218))
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun YouTubeBrowseScreen(
    navController: NavController,
    viewModel: YouTubeBrowseViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val browseResult by viewModel.result.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val displayTitle = browseResult?.title ?: viewModel.passedTitle ?: "Browse"
    val headerGradients = remember(displayTitle) { getGenreHeaderGradient(displayTitle) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0F14))
    ) {
        // Vibrant Top Gradient Header Wash
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = headerGradients,
                        endY = 800f
                    )
                )
        )

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val horizontalLazyGridItemWidthFactor = if (maxWidth * 0.475f >= 320.dp) 0.475f else 0.9f
            val lazyGridState = rememberLazyGridState()
            val snapLayoutInfoProvider = remember(lazyGridState) {
                SnapLayoutInfoProvider(
                    lazyGridState = lazyGridState,
                    positionInLayout = { layoutSize, itemSize ->
                        (layoutSize * horizontalLazyGridItemWidthFactor / 2f - itemSize / 2f)
                    }
                )
            }

            val playerAwareInsets = LocalPlayerAwareWindowInsets.current.asPaddingValues()

            LazyColumn(
                contentPadding = PaddingValues(
                    top = 80.dp,
                    bottom = playerAwareInsets.calculateBottomPadding() + 28.dp
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                // Spotify-Style Large Bold Header Title
                item(key = "category_header_title") {
                    Text(
                        text = displayTitle,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp)
                            .padding(top = 10.dp, bottom = 18.dp)
                    )
                }

                // Shimmer Loading State
                if (browseResult == null) {
                    item(key = "shimmer_loading") {
                        ShimmerHost(
                            modifier = Modifier.animateItem()
                        ) {
                            TextPlaceholder(
                                height = 28.dp,
                                modifier = Modifier
                                    .padding(horizontal = 18.dp, vertical = 8.dp)
                                    .width(180.dp),
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 18.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.padding(bottom = 24.dp)
                            ) {
                                items(4) {
                                    GridItemPlaceHolder()
                                }
                            }
                            TextPlaceholder(
                                height = 28.dp,
                                modifier = Modifier
                                    .padding(horizontal = 18.dp, vertical = 8.dp)
                                    .width(150.dp),
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 18.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(4) {
                                    GridItemPlaceHolder()
                                }
                            }
                        }
                    }
                }

                // Browse Results: Sections & Horizontal Playlists Carousels
                browseResult?.items?.fastForEach { section ->
                    if (section.items.isNotEmpty()) {
                        // Section Header
                        section.title?.let { sectionTitle ->
                            item(key = "section_title_${sectionTitle.hashCode()}") {
                                Text(
                                    text = sectionTitle,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 18.dp)
                                        .padding(top = 22.dp, bottom = 12.dp)
                                )
                            }
                        }

                        // Songs grid if all items are songs
                        if (section.items.all { it is SongItem }) {
                            item(key = "section_songs_${section.title?.hashCode() ?: section.hashCode()}") {
                                LazyHorizontalGrid(
                                    state = lazyGridState,
                                    rows = GridCells.Fixed(4),
                                    flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
                                    contentPadding = PaddingValues(horizontal = 18.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(ListItemHeight * 4)
                                        .animateItem()
                                ) {
                                    items(items = section.items) { song ->
                                        Box(Modifier.width(320.dp)) {
                                            YouTubeListItem(
                                                item = song as SongItem,
                                                isActive = mediaMetadata?.id == song.id,
                                                isPlaying = isPlaying,
                                                isSwipeable = false,
                                                trailingContent = {
                                                    IconButton(
                                                        onClick = {
                                                            menuState.show {
                                                                YouTubeSongMenu(
                                                                    song = song,
                                                                    navController = navController,
                                                                    onDismiss = menuState::dismiss,
                                                                )
                                                            }
                                                        }
                                                    ) {
                                                        Icon(
                                                            painter = painterResource(R.drawable.more_vert),
                                                            contentDescription = null,
                                                        )
                                                    }
                                                },
                                                modifier = Modifier
                                                    .clickable {
                                                        if (song.id == mediaMetadata?.id) {
                                                            playerConnection.player.togglePlayPause()
                                                        } else {
                                                            playerConnection.playQueue(
                                                                YouTubeQueue.radio(
                                                                    song.toMediaMetadata()
                                                                )
                                                            )
                                                        }
                                                    }
                                                    .animateItem()
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // Playlists & Albums Horizontal Carousel (Spotify Style)
                            item(key = "section_items_${section.title?.hashCode() ?: section.hashCode()}") {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 18.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(items = section.items) { item ->
                                        SpotifyPlaylistItemCard(
                                            item = item,
                                            onClick = {
                                                when (item) {
                                                    is AlbumItem -> navController.navigate("album/${item.id}")
                                                    is ArtistItem -> navController.navigate("artist/${item.id}")
                                                    is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                                                    is PodcastItem -> navController.navigate("podcast/${item.id}")
                                                    is SongItem -> {
                                                        playerConnection.playQueue(
                                                            YouTubeQueue.radio(item.toMediaMetadata())
                                                        )
                                                    }
                                                    else -> Unit
                                                }
                                            },
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                menuState.show {
                                                    when (item) {
                                                        is SongItem -> YouTubeSongMenu(
                                                            song = item,
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                        is AlbumItem -> YouTubeAlbumMenu(
                                                            albumItem = item,
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                        is ArtistItem -> YouTubeArtistMenu(
                                                            artist = item,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                        is PlaylistItem -> YouTubePlaylistMenu(
                                                            playlist = item,
                                                            coroutineScope = coroutineScope,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                        is EpisodeItem -> YouTubeSongMenu(
                                                            song = item.asSongItem(),
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                        is PodcastItem -> YouTubePlaylistMenu(
                                                            playlist = item.asPlaylistItem(),
                                                            coroutineScope = coroutineScope,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Top Action Bar (Back & Share)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f))
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back),
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Explore $displayTitle on Wavyn Music!")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share $displayTitle"))
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f))
            ) {
                Icon(
                    painter = painterResource(R.drawable.share),
                    contentDescription = "Share",
                    tint = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SpotifyPlaylistItemCard(
    item: YTItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val subtitle = when (item) {
        is PlaylistItem -> item.author?.name ?: item.songCountText.orEmpty()
        is AlbumItem -> listOfNotNull(item.artists?.joinToString { it.name }, item.year?.toString()).joinToString(" • ")
        is SongItem -> item.artists.joinToString { it.name }
        is ArtistItem -> "Artist"
        else -> ""
    }

    Column(
        modifier = Modifier
            .width(148.dp)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(bottom = 6.dp)
    ) {
        // Thumbnail Card
        Box(
            modifier = Modifier
                .size(148.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = Color.Black.copy(alpha = 0.50f),
                    spotColor = Color.Black.copy(alpha = 0.70f)
                )
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 0.8.dp,
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp)
                )
                .background(Color(0xFF1E222D))
        ) {
            AsyncImage(
                model = item.thumbnail,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(8.dp))

        // Title
        Text(
            text = item.title,
            color = Color.White,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Subtitle / Description / Artists
        if (subtitle.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.60f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp
            )
        }
    }
}
