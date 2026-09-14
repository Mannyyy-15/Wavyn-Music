package iad1tya.echo.music.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.echo.innertube.models.AlbumItem
import com.echo.innertube.models.ArtistItem
import com.echo.innertube.models.BrowseEndpoint
import com.echo.innertube.models.EpisodeItem
import com.echo.innertube.models.PlaylistItem
import com.echo.innertube.models.PodcastItem
import com.echo.innertube.models.SongItem
import com.echo.innertube.models.WatchEndpoint
import com.echo.innertube.models.YTItem
import com.echo.innertube.pages.HomePage
import com.echo.innertube.utils.parseCookieString
import iad1tya.echo.music.LocalDatabase
import iad1tya.echo.music.LocalPlayerAwareWindowInsets
import iad1tya.echo.music.LocalPlayerConnection
import iad1tya.echo.music.R
import iad1tya.echo.music.constants.GridThumbnailHeight
import iad1tya.echo.music.constants.InnerTubeCookieKey
import iad1tya.echo.music.constants.ListItemHeight
import iad1tya.echo.music.constants.ListThumbnailSize
import iad1tya.echo.music.constants.ThumbnailCornerRadius
import iad1tya.echo.music.db.entities.Album
import iad1tya.echo.music.db.entities.Artist
import iad1tya.echo.music.db.entities.LocalItem
import iad1tya.echo.music.db.entities.Playlist
import iad1tya.echo.music.db.entities.Song
import iad1tya.echo.music.extensions.togglePlayPause
import iad1tya.echo.music.models.toMediaMetadata
import iad1tya.echo.music.playback.queues.ListQueue
import iad1tya.echo.music.playback.queues.LocalAlbumRadio
import iad1tya.echo.music.extensions.toMediaItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import iad1tya.echo.music.playback.queues.YouTubeAlbumRadio
import iad1tya.echo.music.playback.queues.YouTubeQueue
import iad1tya.echo.music.ui.component.AlbumGridItem
import iad1tya.echo.music.ui.component.AlbumListItem
import iad1tya.echo.music.ui.component.ArtistGridItem
import iad1tya.echo.music.ui.component.ArtistListItem
import iad1tya.echo.music.ui.component.ChipsRow
import iad1tya.echo.music.ui.component.LocalBottomSheetPageState
import iad1tya.echo.music.ui.component.LocalMenuState
import iad1tya.echo.music.ui.component.NavigationTitle
import iad1tya.echo.music.ui.component.SongGridItem
import iad1tya.echo.music.ui.component.SongListItem
import iad1tya.echo.music.ui.component.YouTubeGridItem
import iad1tya.echo.music.ui.component.YouTubeListItem
import iad1tya.echo.music.ui.component.shimmer.GridItemPlaceHolder
import iad1tya.echo.music.ui.component.shimmer.ShimmerHost
import iad1tya.echo.music.ui.component.shimmer.TextPlaceholder
import iad1tya.echo.music.ui.menu.AlbumMenu
import iad1tya.echo.music.ui.menu.ArtistMenu
import iad1tya.echo.music.ui.menu.SongMenu
import iad1tya.echo.music.ui.menu.YouTubeAlbumMenu
import iad1tya.echo.music.ui.menu.YouTubeArtistMenu
import iad1tya.echo.music.ui.menu.YouTubePlaylistMenu
import iad1tya.echo.music.ui.menu.YouTubeSongMenu
import iad1tya.echo.music.ui.utils.SnapLayoutInfoProvider
import iad1tya.echo.music.utils.rememberPreference
import iad1tya.echo.music.viewmodels.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min

data class QuickLovedSong(
    val id: String,
    val title: String,
    val subtitle: String,
    val thumbnailUrl: String?,
    val playAction: () -> Unit,
    val menuAction: () -> Unit,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LovedSongTile(
    item: QuickLovedSong,
    isActive: Boolean,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "TileScale"
    )
    val coroutineScope = rememberCoroutineScope()

    val containerColor = if (isActive) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.65f)
    }
    val borderColor = if (isActive) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .border(BorderStroke(0.75.dp, borderColor), RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = {
                    coroutineScope.launch {
                        isPressed = true
                        delay(100)
                        isPressed = false
                    }
                    item.playAction()
                },
                onLongClick = item.menuAction
            )
    ) {
        // Left Artwork (flush with tile edge, rounded start corners)
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (!item.thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.music_note),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Title and Subtitle
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            if (item.subtitle.isNotBlank()) {
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }

        // Right side: Active equalizer or indicator bubble
        if (isActive) {
            Box(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val bottomSheetPageState = LocalBottomSheetPageState.current
    val database = LocalDatabase.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val haptic = LocalHapticFeedback.current

    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val quickPicks by viewModel.quickPicks.collectAsState()
    val forgottenFavorites by viewModel.forgottenFavorites.collectAsState()
    val keepListening by viewModel.keepListening.collectAsState()
    val similarRecommendations by viewModel.similarRecommendations.collectAsState()
    val accountPlaylists by viewModel.accountPlaylists.collectAsState()
    val homePage by viewModel.homePage.collectAsState()
    val explorePage by viewModel.explorePage.collectAsState()

    val allLocalItems by viewModel.allLocalItems.collectAsState()
    val allYtItems by viewModel.allYtItems.collectAsState()
    val selectedChip by viewModel.selectedChip.collectAsState()

    val isLoading: Boolean by viewModel.isLoading.collectAsState()
    val isMoodAndGenresLoading = isLoading && explorePage?.moodAndGenres == null
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()

    val quickPicksLazyGridState = rememberLazyGridState()
    val forgottenFavoritesLazyGridState = rememberLazyGridState()
    val keepListeningLazyGridState = rememberLazyGridState()

    val accountName by viewModel.accountName.collectAsState()
    val accountImageUrl by viewModel.accountImageUrl.collectAsState()
    val innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) {
        "SAPISID" in parseCookieString(innerTubeCookie)
    }
    val url = if (isLoggedIn) accountImageUrl else null

    val isSpotifyLoggedIn by iad1tya.echo.music.spotify.SpotifyAuthManager.isLoggedIn.collectAsState()
    val currentSpotifyUser by iad1tya.echo.music.spotify.SpotifyAuthManager.currentUser.collectAsState()
    val spotifyPlaylists by iad1tya.echo.music.spotify.SpotifyAuthManager.userPlaylists.collectAsState()
    val spotifyLikedCount by iad1tya.echo.music.spotify.SpotifyAuthManager.likedTracksCount.collectAsState()
    val mostPlayedSongs by database.mostPlayedSongs(fromTimeStamp = 0, limit = 6).collectAsState(initial = emptyList())

    val lovedSongs = remember(mostPlayedSongs, quickPicks, keepListening) {
        val result = mutableListOf<QuickLovedSong>()

        // 1. Database most played songs
        mostPlayedSongs.forEach { song ->
            result.add(
                QuickLovedSong(
                    id = song.id,
                    title = song.song.title,
                    subtitle = song.artists.joinToString { it.name },
                    thumbnailUrl = song.song.thumbnailUrl,
                    playAction = {
                        if (song.id == mediaMetadata?.id) {
                            playerConnection.player.togglePlayPause()
                        } else {
                            playerConnection.playQueue(
                                YouTubeQueue.radio(song.toMediaMetadata())
                            )
                        }
                    },
                    menuAction = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuState.show {
                            SongMenu(
                                originalSong = song,
                                navController = navController,
                                onDismiss = menuState::dismiss,
                                compactMode = true,
                            )
                        }
                    }
                )
            )
        }

        // 2. Supplement with keepListening if needed
        if (result.size < 6 && keepListening != null) {
            for (item in keepListening) {
                if (result.size >= 6) break
                if (item is Song && result.none { it.id == item.id }) {
                    result.add(
                        QuickLovedSong(
                            id = item.id,
                            title = item.song.title,
                            subtitle = item.artists.joinToString { it.name },
                            thumbnailUrl = item.song.thumbnailUrl,
                            playAction = {
                                if (item.id == mediaMetadata?.id) {
                                    playerConnection.player.togglePlayPause()
                                } else {
                                    playerConnection.playQueue(
                                        YouTubeQueue.radio(item.toMediaMetadata())
                                    )
                                }
                            },
                            menuAction = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                menuState.show {
                                    SongMenu(
                                        originalSong = item,
                                        navController = navController,
                                        onDismiss = menuState::dismiss,
                                        compactMode = true,
                                    )
                                }
                            }
                        )
                    )
                }
            }
        }

        // 3. Supplement with quickPicks if still under 6
        if (result.size < 6 && quickPicks != null) {
            for (qp in quickPicks) {
                if (result.size >= 6) break
                if (result.none { it.id == qp.id }) {
                    result.add(
                        QuickLovedSong(
                            id = qp.id,
                            title = qp.song.title,
                            subtitle = qp.artists.joinToString { it.name },
                            thumbnailUrl = qp.song.thumbnailUrl,
                            playAction = {
                                if (qp.id == mediaMetadata?.id) {
                                    playerConnection.player.togglePlayPause()
                                } else {
                                    playerConnection.playQueue(
                                        YouTubeQueue.radio(qp.toMediaMetadata())
                                    )
                                }
                            },
                            menuAction = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                menuState.show {
                                    SongMenu(
                                        originalSong = qp,
                                        navController = navController,
                                        onDismiss = menuState::dismiss,
                                        compactMode = true,
                                    )
                                }
                            }
                        )
                    )
                }
            }
        }

        result.take(6)
    }

    val scope = rememberCoroutineScope()
    val lazylistState = rememberLazyListState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val scrollToTop =
        backStackEntry?.savedStateHandle?.getStateFlow("scrollToTop", false)?.collectAsState()

    LaunchedEffect(scrollToTop?.value) {
        if (scrollToTop?.value == true) {
            lazylistState.animateScrollToItem(0)
            backStackEntry?.savedStateHandle?.set("scrollToTop", false)
        }
    }

    LaunchedEffect(isSpotifyLoggedIn) {
        if (isSpotifyLoggedIn) {
            iad1tya.echo.music.spotify.SpotifyAuthManager.syncLibrary()
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { lazylistState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                val len = lazylistState.layoutInfo.totalItemsCount
                if (lastVisibleIndex != null && lastVisibleIndex >= len - 3) {
                    viewModel.loadMoreYouTubeItems(homePage?.continuation)
                }
            }
    }

    if (selectedChip != null) {
        BackHandler {
            // if a chip is selected, go back to the normal homepage first
            viewModel.toggleChip(selectedChip)
        }
    }

    val localGridItem: @Composable (LocalItem) -> Unit = {
        when (it) {
            is Song -> SongGridItem(
                song = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            if (it.id == mediaMetadata?.id) {
                                playerConnection.player.togglePlayPause()
                            } else {
                                playerConnection.playQueue(
                                    YouTubeQueue.radio(it.toMediaMetadata()),
                                )
                            }
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(
                                HapticFeedbackType.LongPress,
                            )
                            menuState.show {
                                SongMenu(
                                    originalSong = it,
                                    navController = navController,
                                    onDismiss = menuState::dismiss,
                                )
                            }
                        },
                    ),
                isActive = it.id == mediaMetadata?.id,
                isPlaying = isPlaying,
            )

            is Album -> AlbumGridItem(
                album = it,
                isActive = it.id == mediaMetadata?.album?.id,
                isPlaying = isPlaying,
                coroutineScope = scope,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            navController.navigate("album/${it.id}")
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            menuState.show {
                                AlbumMenu(
                                    originalAlbum = it,
                                    navController = navController,
                                    onDismiss = menuState::dismiss
                                )
                            }
                        }
                    )
            )

            is Artist -> ArtistGridItem(
                artist = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            navController.navigate("artist/${it.id}")
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(
                                HapticFeedbackType.LongPress,
                            )
                            menuState.show {
                                ArtistMenu(
                                    originalArtist = it,
                                    coroutineScope = scope,
                                    onDismiss = menuState::dismiss,
                                )
                            }
                        },
                    ),
            )

            is Playlist -> {}
        }
    }

    val ytGridItem: @Composable (YTItem) -> Unit = { item ->
        YouTubeGridItem(
            item = item,
            isActive = item.id in listOf(mediaMetadata?.album?.id, mediaMetadata?.id),
            isPlaying = isPlaying,
            coroutineScope = scope,
            thumbnailRatio = 1f,
            modifier = Modifier
                .combinedClickable(
                    onClick = {
                        when (item) {
                            is SongItem -> playerConnection.playQueue(
                                YouTubeQueue(
                                    item.endpoint ?: WatchEndpoint(
                                        videoId = item.id
                                    ), item.toMediaMetadata()
                                )
                            )

                            is AlbumItem -> navController.navigate("album/${item.id}")
                            is ArtistItem -> navController.navigate("artist/${item.id}")
                            is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                            is EpisodeItem -> playerConnection.playQueue(
                                YouTubeQueue(
                                    item.endpoint ?: WatchEndpoint(videoId = item.id),
                                    item.asSongItem().toMediaMetadata()
                                )
                            )
                            is PodcastItem -> navController.navigate("podcast/${item.id}")
                        }
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuState.show {
                            when (item) {
                                is SongItem -> YouTubeSongMenu(
                                    song = item,
                                    navController = navController,
                                    onDismiss = menuState::dismiss
                                )

                                is AlbumItem -> YouTubeAlbumMenu(
                                    albumItem = item,
                                    navController = navController,
                                    onDismiss = menuState::dismiss
                                )

                                is ArtistItem -> YouTubeArtistMenu(
                                    artist = item,
                                    onDismiss = menuState::dismiss
                                )

                                is PlaylistItem -> YouTubePlaylistMenu(
                                    playlist = item,
                                    coroutineScope = scope,
                                    onDismiss = menuState::dismiss
                                )
                                is EpisodeItem -> YouTubeSongMenu(
                                    song = item.asSongItem(),
                                    navController = navController,
                                    onDismiss = menuState::dismiss
                                )
                                is PodcastItem -> YouTubePlaylistMenu(
                                    playlist = item.asPlaylistItem(),
                                    coroutineScope = scope,
                                    onDismiss = menuState::dismiss
                                )
                            }
                        }
                    }
                )
        )
    }

    val ytCleanGridItem: @Composable (YTItem) -> Unit = { item ->
        YouTubeGridItem(
            item = item,
            isActive = item.id in listOf(mediaMetadata?.album?.id, mediaMetadata?.id),
            isPlaying = isPlaying,
            coroutineScope = scope,
            thumbnailRatio = 1f,
            showSubtitle = false,
            modifier = Modifier
                .combinedClickable(
                    onClick = {
                        when (item) {
                            is SongItem -> playerConnection.playQueue(
                                YouTubeQueue(
                                    item.endpoint ?: WatchEndpoint(
                                        videoId = item.id
                                    ), item.toMediaMetadata()
                                )
                            )

                            is AlbumItem -> navController.navigate("album/${item.id}")
                            is ArtistItem -> navController.navigate("artist/${item.id}")
                            is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                            is EpisodeItem -> playerConnection.playQueue(
                                YouTubeQueue(
                                    item.endpoint ?: WatchEndpoint(videoId = item.id),
                                    item.asSongItem().toMediaMetadata()
                                )
                            )
                            is PodcastItem -> navController.navigate("podcast/${item.id}")
                        }
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuState.show {
                            when (item) {
                                is SongItem -> YouTubeSongMenu(
                                    song = item,
                                    navController = navController,
                                    onDismiss = menuState::dismiss
                                )

                                is AlbumItem -> YouTubeAlbumMenu(
                                    albumItem = item,
                                    navController = navController,
                                    onDismiss = menuState::dismiss
                                )

                                is ArtistItem -> YouTubeArtistMenu(
                                    artist = item,
                                    onDismiss = menuState::dismiss
                                )

                                is PlaylistItem -> YouTubePlaylistMenu(
                                    playlist = item,
                                    coroutineScope = scope,
                                    onDismiss = menuState::dismiss
                                )
                                is EpisodeItem -> YouTubeSongMenu(
                                    song = item.asSongItem(),
                                    navController = navController,
                                    onDismiss = menuState::dismiss
                                )
                                is PodcastItem -> YouTubePlaylistMenu(
                                    playlist = item.asPlaylistItem(),
                                    coroutineScope = scope,
                                    onDismiss = menuState::dismiss
                                )
                            }
                        }
                    }
                )
        )
    }

    LaunchedEffect(quickPicks) {
        quickPicksLazyGridState.scrollToItem(0)
    }

    LaunchedEffect(forgottenFavorites) {
        forgottenFavoritesLazyGridState.scrollToItem(0)
    }

    LaunchedEffect(keepListening) {
        keepListeningLazyGridState.scrollToItem(0)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pullToRefresh(
                state = pullRefreshState,
                isRefreshing = isRefreshing,
                onRefresh = viewModel::refresh
            ),
        contentAlignment = Alignment.TopStart
    ) {
        val horizontalLazyGridItemWidthFactor = if (maxWidth * 0.475f >= 320.dp) 0.475f else 1.0f
        val horizontalLazyGridItemWidth = maxWidth * horizontalLazyGridItemWidthFactor
        val quickPicksSnapLayoutInfoProvider = remember(quickPicksLazyGridState) {
            SnapLayoutInfoProvider(
                lazyGridState = quickPicksLazyGridState,
                positionInLayout = { layoutSize, itemSize ->
                    (layoutSize * horizontalLazyGridItemWidthFactor / 2f - itemSize / 2f)
                }
            )
        }
        val forgottenFavoritesSnapLayoutInfoProvider = remember(forgottenFavoritesLazyGridState) {
            SnapLayoutInfoProvider(
                lazyGridState = forgottenFavoritesLazyGridState,
                positionInLayout = { layoutSize, itemSize ->
                    (layoutSize * horizontalLazyGridItemWidthFactor / 2f - itemSize / 2f)
                }
            )
        }
        val keepListeningSnapLayoutInfoProvider = remember(keepListeningLazyGridState) {
            SnapLayoutInfoProvider(
                lazyGridState = keepListeningLazyGridState,
                positionInLayout = { layoutSize, itemSize ->
                    (layoutSize * horizontalLazyGridItemWidthFactor / 2f - itemSize / 2f)
                }
            )
        }

        val localListItem: @Composable (LocalItem) -> Unit = { item ->
            when (item) {
                is Song -> {
                    val song by database.song(item.id).collectAsState(initial = item)
                    SongListItem(
                        song = song!!,
                        showInLibraryIcon = true,
                        isActive = song!!.id == mediaMetadata?.id,
                        isPlaying = isPlaying,
                        isSwipeable = false,
                        trailingContent = {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E2129))
                                    .clickable {
                                        if (song!!.id == mediaMetadata?.id) {
                                            playerConnection.player.togglePlayPause()
                                        } else {
                                            playerConnection.playQueue(
                                                YouTubeQueue.radio(
                                                    song!!.toMediaMetadata()
                                                )
                                            )
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                val isCurrent = song!!.id == mediaMetadata?.id
                                Icon(
                                    painter = painterResource(
                                        if (isCurrent && isPlaying) R.drawable.pause else R.drawable.play
                                    ),
                                    contentDescription = if (isCurrent && isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        },
                        modifier = Modifier
                            .width(horizontalLazyGridItemWidth)
                            .combinedClickable(
                                onClick = {
                                    if (song!!.id == mediaMetadata?.id) {
                                        playerConnection.player.togglePlayPause()
                                    } else {
                                        playerConnection.playQueue(
                                            YouTubeQueue.radio(
                                                song!!.toMediaMetadata()
                                            )
                                        )
                                    }
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    menuState.show {
                                        SongMenu(
                                            originalSong = song!!,
                                            navController = navController,
                                            onDismiss = menuState::dismiss,
                                            compactMode = true,
                                        )
                                    }
                                }
                            )
                    )
                }

                is Album -> {
                    AlbumListItem(
                        album = item,
                        isActive = item.id == mediaMetadata?.album?.id,
                        isPlaying = isPlaying,
                        trailingContent = {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E2129))
                                    .clickable {
                                        navController.navigate("album/${item.id}")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.play),
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        },
                        modifier = Modifier
                            .width(horizontalLazyGridItemWidth)
                            .combinedClickable(
                                onClick = {
                                    navController.navigate("album/${item.id}")
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    menuState.show {
                                        AlbumMenu(
                                            originalAlbum = item,
                                            navController = navController,
                                            onDismiss = menuState::dismiss
                                        )
                                    }
                                }
                            )
                    )
                }

                is Artist -> {
                    ArtistListItem(
                        artist = item,
                        trailingContent = {
                            IconButton(
                                onClick = {
                                    menuState.show {
                                        ArtistMenu(
                                            originalArtist = item,
                                            coroutineScope = scope,
                                            onDismiss = menuState::dismiss
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.more_horiz),
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier
                            .width(horizontalLazyGridItemWidth)
                            .combinedClickable(
                                onClick = {
                                    navController.navigate("artist/${item.id}")
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    menuState.show {
                                        ArtistMenu(
                                            originalArtist = item,
                                            coroutineScope = scope,
                                            onDismiss = menuState::dismiss
                                        )
                                    }
                                }
                            )
                    )
                }

                is Playlist -> {}
            }
        }

        LazyColumn(
            state = lazylistState,
            contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
        ) {
            item(key = "home_greeting") {
                val hour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
                val greeting = remember(hour) {
                    when (hour) {
                        in 5..11 -> "Good morning"
                        in 12..16 -> "Good afternoon"
                        else -> "Good evening"
                    }
                }
                val resolvedUserName = remember(isLoggedIn, accountName, isSpotifyLoggedIn, currentSpotifyUser) {
                    val spotifyName = currentSpotifyUser?.displayName?.trim()
                    if (isSpotifyLoggedIn && !spotifyName.isNullOrBlank() && !spotifyName.equals("Spotify User", ignoreCase = true) && !spotifyName.equals("Spotify", ignoreCase = true)) {
                        spotifyName
                    } else if (isLoggedIn && accountName.isNotBlank()) {
                        val cleanName = if (accountName.contains("@")) accountName.substringBefore("@") else accountName
                        cleanName.trim()
                    } else {
                        "User"
                    }
                }

                Text(
                    text = "$greeting, $resolvedUserName",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily(Font(R.font.zalando_sans_expanded)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 8.dp)
                )
            }



            if (selectedChip == null) {
                // SECTION 1: 6 most played songs in 2-column x 3-row grid directly under greeting
                if (lovedSongs.isNotEmpty()) {
                    item(key = "yours_loved_grid") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val rowCount = (lovedSongs.size + 1) / 2
                            for (r in 0 until rowCount) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val left = lovedSongs.getOrNull(r * 2)
                                    val right = lovedSongs.getOrNull(r * 2 + 1)

                                    if (left != null) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            LovedSongTile(
                                                item = left,
                                                isActive = left.id == mediaMetadata?.id,
                                                isPlaying = isPlaying
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }

                                    if (right != null) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            LovedSongTile(
                                                item = right,
                                                isActive = right.id == mediaMetadata?.id,
                                                isPlaying = isPlaying
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                // SECTION 2: "Your Sonic Vault" (User playlists, clean title without right badge)
                if (isSpotifyLoggedIn && (spotifyPlaylists.isNotEmpty() || spotifyLikedCount > 0)) {
                    item(key = "sonic_vault_title") {
                        NavigationTitle(
                            title = "Your Sonic Vault",
                            modifier = Modifier.animateItem()
                        )
                    }

                    item(key = "sonic_vault_playlists_row") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            // Liked Songs Card
                            item(key = "spotify_liked_card") {
                                androidx.compose.material3.Card(
                                    modifier = Modifier
                                        .width(135.dp)
                                        .clickable { navController.navigate("spotify_playlist/liked_songs") },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = androidx.compose.material3.CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.70f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(115.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                                        listOf(Color(0xFF450AF5), Color(0xFF8E8EE5))
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.favorite),
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = "Liked Songs",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (spotifyLikedCount > 0) "$spotifyLikedCount songs" else "Spotify Library",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            // Spotify User Playlists (including collaborative)
                            items(spotifyPlaylists, key = { "spotify_${it.id}" }) { sp ->
                                Column(
                                    modifier = Modifier
                                        .width(135.dp)
                                        .clickable { navController.navigate("spotify_playlist/${sp.id}") }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(135.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!sp.imageUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = sp.imageUrl,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_spotify),
                                                contentDescription = null,
                                                modifier = Modifier.size(48.dp),
                                                tint = Color(0xFF1DB954)
                                            )
                                        }
                                        // Tiny Spotify indicator in bottom right
                                        androidx.compose.material3.Surface(
                                            shape = CircleShape,
                                            color = Color.Black.copy(alpha = 0.7f),
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(6.dp)
                                                .size(20.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_spotify),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint = Color(0xFF1DB954)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = sp.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (sp.isCollaborative) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF1DB954))
                                            )
                                        }
                                        Text(
                                            text = if (sp.trackCount > 0) "${sp.trackCount} songs" else if (!sp.ownerName.isNullOrBlank() && !sp.ownerName.equals("Micael Widell", ignoreCase = true) && sp.ownerName != "me") "By ${sp.ownerName}" else "Spotify",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // SECTION 3: "Keep Listening"
                keepListening?.takeIf { it.isNotEmpty() }?.let { keepListening ->
                    item(key = "keep_listening_title") {
                        NavigationTitle(
                            title = stringResource(R.string.keep_listening),
                            modifier = Modifier.animateItem()
                        )
                    }

                    item(key = "keep_listening_list") {
                        val rows = min(4, keepListening.size)
                        LazyHorizontalGrid(
                            state = keepListeningLazyGridState,
                            rows = GridCells.Fixed(rows),
                            flingBehavior = rememberSnapFlingBehavior(keepListeningSnapLayoutInfoProvider),
                            contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
                                .asPaddingValues(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ListItemHeight * rows)
                                .animateItem()
                        ) {
                            items(
                                items = keepListening.distinctBy { it.id },
                                key = { it.id }
                            ) { item ->
                                localListItem(item)
                            }
                        }
                    }
                }

                // SECTION 4: "Tailored For You" (Recommended niche songs with Shuffle button)
                quickPicks?.takeIf { it.isNotEmpty() }?.let { quickPicks ->
                    item(key = "tailored_vibe_title") {
                        NavigationTitle(
                            title = "Tailored For You",
                            action = {
                                androidx.compose.material3.Button(
                                    onClick = {
                                        val shuffled = quickPicks.shuffled().map { it.toMediaItem() }
                                        playerConnection.playQueue(
                                            ListQueue(
                                                title = "Tailored For You",
                                                items = shuffled,
                                            )
                                        )
                                    },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.shuffle),
                                        contentDescription = "Shuffle",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Shuffle",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            modifier = Modifier.animateItem()
                        )
                    }

                    item(key = "tailored_vibe_list") {
                        val rows = min(4, quickPicks.size)
                        LazyHorizontalGrid(
                            state = quickPicksLazyGridState,
                            rows = GridCells.Fixed(rows),
                            flingBehavior = rememberSnapFlingBehavior(quickPicksSnapLayoutInfoProvider),
                            contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
                                .asPaddingValues(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ListItemHeight * rows)
                                .animateItem()
                        ) {
                            items(
                                items = quickPicks.distinctBy { it.id },
                                key = { it.id }
                            ) { originalSong ->
                                val song by database.song(originalSong.id)
                                    .collectAsState(initial = originalSong)

                                SongListItem(
                                    song = song!!,
                                    showInLibraryIcon = true,
                                    isActive = song!!.id == mediaMetadata?.id,
                                    isPlaying = isPlaying,
                                    isSwipeable = false,
                                    trailingContent = {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF1E2129))
                                                .clickable {
                                                    if (song!!.id == mediaMetadata?.id) {
                                                        playerConnection.player.togglePlayPause()
                                                    } else {
                                                        playerConnection.playQueue(
                                                            YouTubeQueue.radio(
                                                                song!!.toMediaMetadata()
                                                            )
                                                        )
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val isCurrent = song!!.id == mediaMetadata?.id
                                            Icon(
                                                painter = painterResource(
                                                    if (isCurrent && isPlaying) R.drawable.pause else R.drawable.play
                                                ),
                                                contentDescription = if (isCurrent && isPlaying) "Pause" else "Play",
                                                tint = Color.White,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .width(horizontalLazyGridItemWidth)
                                        .combinedClickable(
                                            onClick = {
                                                if (song!!.id == mediaMetadata?.id) {
                                                    playerConnection.player.togglePlayPause()
                                                } else {
                                                    playerConnection.playQueue(
                                                        YouTubeQueue.radio(
                                                            song!!.toMediaMetadata()
                                                        )
                                                    )
                                                }
                                            },
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                menuState.show {
                                                    SongMenu(
                                                        originalSong = song!!,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss,
                                                        compactMode = true,
                                                    )
                                                }
                                            }
                                        )
                                )
                            }
                        }
                    }
                }

                accountPlaylists?.takeIf { it.isNotEmpty() }?.let { accountPlaylists ->
                    item(key = "account_playlists_title") {
                        NavigationTitle(
                            label = stringResource(R.string.your_youtube_playlists),
                            title = accountName,
                            thumbnail = {
                                if (url != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(url)
                                            .diskCachePolicy(CachePolicy.ENABLED)
                                            .diskCacheKey(url)
                                            .crossfade(false)
                                            .build(),
                                        placeholder = painterResource(id = R.drawable.person),
                                        error = painterResource(id = R.drawable.person),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(ListThumbnailSize)
                                            .clip(RoundedCornerShape(ThumbnailCornerRadius))
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(id = R.drawable.person),
                                        contentDescription = null,
                                        modifier = Modifier.size(ListThumbnailSize)
                                    )
                                }
                            },
                            onClick = {
                                navController.navigate("account")
                            },
                            modifier = Modifier.animateItem()
                        )
                    }

                    item(key = "account_playlists_list") {
                        LazyRow(
                            contentPadding = WindowInsets.systemBars
                                .only(WindowInsetsSides.Horizontal)
                                .asPaddingValues(),
                            modifier = Modifier.animateItem()
                        ) {
                            items(
                                items = accountPlaylists.distinctBy { it.id },
                                key = { it.id },
                            ) { item ->
                                ytGridItem(item)
                            }
                        }
                    }
                }

                forgottenFavorites?.takeIf { it.isNotEmpty() }?.let { forgottenFavorites ->
                    item(key = "forgotten_favorites_title") {
                        NavigationTitle(
                            title = stringResource(R.string.forgotten_favorites),
                            modifier = Modifier.animateItem()
                        )
                    }

                    item(key = "forgotten_favorites_list") {
                        // take min in case list size is less than 4
                        val rows = min(4, forgottenFavorites.size)
                        LazyHorizontalGrid(
                            state = forgottenFavoritesLazyGridState,
                            rows = GridCells.Fixed(rows),
                            contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
                                .asPaddingValues(),
                            flingBehavior = rememberSnapFlingBehavior(
                                forgottenFavoritesSnapLayoutInfoProvider
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ListItemHeight * rows)
                                .animateItem()
                        ) {
                            items(
                                items = forgottenFavorites.distinctBy { it.id },
                                key = { it.id }
                            ) { originalSong ->
                                val song by database.song(originalSong.id)
                                    .collectAsState(initial = originalSong)

                                SongListItem(
                                    song = song!!,
                                    showInLibraryIcon = true,
                                    isActive = song!!.id == mediaMetadata?.id,
                                    isPlaying = isPlaying,
                                    isSwipeable = false,
                                    trailingContent = {
                                        IconButton(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                menuState.show {
                                                    SongMenu(
                                                        originalSong = song!!,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss
                                                    )
                                                }
                                            }
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.more_vert),
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .width(horizontalLazyGridItemWidth)
                                        .combinedClickable(
                                            onClick = {
                                                if (song!!.id == mediaMetadata?.id) {
                                                    playerConnection.player.togglePlayPause()
                                                } else {
                                                    playerConnection.playQueue(
                                                        YouTubeQueue.radio(
                                                            song!!.toMediaMetadata()
                                                        )
                                                    )
                                                }
                                            },
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                menuState.show {
                                                    SongMenu(
                                                        originalSong = song!!,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss
                                                    )
                                                }
                                            }
                                        )
                                )
                            }
                        }
                    }
                }

                similarRecommendations?.forEachIndexed { index, recommendation ->
                    item(key = "similar_to_title_$index") {
                        NavigationTitle(
                            label = stringResource(R.string.similar_to),
                            title = recommendation.title.title,
                            thumbnail = recommendation.title.thumbnailUrl?.let { thumbnailUrl ->
                                {
                                    val shape = RoundedCornerShape(
                                        ThumbnailCornerRadius
                                    )
                                    AsyncImage(
                                        model = thumbnailUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(ListThumbnailSize)
                                            .clip(shape)
                                    )
                                }
                            },
                            onClick = {
                                when (recommendation.title) {
                                    is Song -> navController.navigate("album/${recommendation.title.album!!.id}")
                                    is Album -> navController.navigate("album/${recommendation.title.id}")
                                    is Artist -> navController.navigate("artist/${recommendation.title.id}")
                                    is Playlist -> {}
                                }
                            },
                            modifier = Modifier.animateItem()
                        )
                    }

                    item(key = "similar_to_list_$index") {
                        LazyRow(
                            contentPadding = WindowInsets.systemBars
                                .only(WindowInsetsSides.Horizontal)
                                .asPaddingValues(),
                            modifier = Modifier.animateItem()
                        ) {
                            items(recommendation.items) { item ->
                                ytCleanGridItem(item)
                            }
                        }
                    }
                }
            }

            homePage?.sections?.forEachIndexed { index, section ->
                item(key = "home_section_title_$index") {
                    NavigationTitle(
                        title = section.title,
                        label = section.label,
                        thumbnail = section.thumbnail?.let { thumbnailUrl ->
                            {
                                val shape = RoundedCornerShape(
                                    ThumbnailCornerRadius
                                )
                                AsyncImage(
                                    model = thumbnailUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(ListThumbnailSize)
                                        .clip(shape)
                                )
                            }
                        },
                        onClick = section.endpoint?.browseId?.let { browseId ->
                            {
                                if (browseId == "FEmusic_moods_and_genres")
                                    navController.navigate("mood_and_genres")
                                else
                                    navController.navigate("browse/$browseId")
                            }
                        },
                        modifier = Modifier.animateItem()
                    )
                }

                item(key = "home_section_list_$index") {
                    LazyRow(
                        contentPadding = WindowInsets.systemBars
                            .only(WindowInsetsSides.Horizontal)
                            .asPaddingValues(),
                        modifier = Modifier.animateItem()
                    ) {
                        items(section.items) { item ->
                            ytCleanGridItem(item)
                        }
                    }
                }
            }

            if (isLoading || homePage?.continuation != null && homePage?.sections?.isNotEmpty() == true) {
                item(key = "loading_shimmer") {
                    ShimmerHost(
                        modifier = Modifier.animateItem()
                    ) {
                        TextPlaceholder(
                            height = 36.dp,
                            modifier = Modifier
                                .padding(12.dp)
                                .width(250.dp),
                        )
                        LazyRow(
                            contentPadding = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(),
                        ) {
                            items(4) {
                                GridItemPlaceHolder()
                            }
                        }
                    }
                }
            }

            if (selectedChip == null) {
                explorePage?.moodAndGenres?.let { moodAndGenres ->
                    item(key = "mood_and_genres_title") {
                        NavigationTitle(
                            title = stringResource(R.string.mood_and_genres),
                            onClick = {
                                navController.navigate("mood_and_genres")
                            },
                            modifier = Modifier.animateItem()
                        )
                    }
                    item(key = "mood_and_genres_list") {
                        LazyHorizontalGrid(
                            rows = GridCells.Fixed(4),
                            contentPadding = PaddingValues(6.dp),
                            modifier = Modifier
                                .height((MoodAndGenresButtonHeight + 12.dp) * 4 + 12.dp)
                                .animateItem()
                        ) {
                            items(moodAndGenres) {
                                MoodAndGenresButton(
                                    title = it.title,
                                    onClick = {
                                        navController.navigate("youtube_browse/${it.endpoint.browseId}?params=${it.endpoint.params}")
                                    },
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .width(180.dp)
                                )
                            }
                        }
                    }
                }

                if (isMoodAndGenresLoading) {
                    item(key = "mood_and_genres_shimmer") {
                        ShimmerHost(
                            modifier = Modifier.animateItem()
                        ) {
                            TextPlaceholder(
                                height = 36.dp,
                                modifier = Modifier
                                    .padding(vertical = 12.dp, horizontal = 12.dp)
                                    .width(250.dp),
                            )

                            repeat(4) {
                                Row {
                                    repeat(2) {
                                        TextPlaceholder(
                                            height = MoodAndGenresButtonHeight,
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier
                                                .padding(horizontal = 12.dp)
                                                .width(200.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Indicator(
            isRefreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(LocalPlayerAwareWindowInsets.current.asPaddingValues()),
        )
    }
}
