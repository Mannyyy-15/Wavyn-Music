package iad1tya.echo.music.ui.screens.spotify

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.core.net.toUri
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import iad1tya.echo.music.LocalDatabase
import iad1tya.echo.music.db.entities.PlaylistEntity
import iad1tya.echo.music.db.entities.PlaylistSongMap
import iad1tya.echo.music.playback.ExoDownloadService
import java.time.LocalDateTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import iad1tya.echo.music.LocalPlayerAwareWindowInsets
import iad1tya.echo.music.LocalPlayerConnection
import iad1tya.echo.music.R
import iad1tya.echo.music.extensions.toMediaItem
import iad1tya.echo.music.playback.queues.ListQueue
import iad1tya.echo.music.spotify.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotifyPlaylistViewScreen(
    playlistId: String,
    navController: NavController,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val playerConnection = LocalPlayerConnection.current

    var playlistTitle by remember { mutableStateOf("Spotify Playlist") }
    var playlistCover by remember { mutableStateOf<String?>(null) }
    var playlistOwner by remember { mutableStateOf<String?>(null) }
    var tracks by remember { mutableStateOf<List<SpotifyTrack>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var resolvingIndex by remember { mutableStateOf<Int?>(null) }
    val database = LocalDatabase.current
    var isImportingToLocal by remember { mutableStateOf(false) }
    var isDownloadingPlaylist by remember { mutableStateOf(false) }

    LaunchedEffect(playlistId) {
        isLoading = true
        val known = SpotifyAuthManager.userPlaylists.value.find { it.id == playlistId }
        if (known != null) {
            playlistTitle = known.name
            playlistOwner = if (known.isCollaborative) "Collaborative • ${known.ownerName ?: "Friend"}" else (known.ownerName ?: "Spotify Playlist")
            playlistCover = known.imageUrl
        }
        val token = SpotifyAuthManager.getValidAccessToken()
        if (token != null) {
            val fetchedTracks = if (playlistId == "liked_songs") {
                playlistTitle = "Liked Songs"
                playlistOwner = "Spotify Library"
                SpotifyApiService.getUserSavedTracks(token, limit = 50)
            } else {
                SpotifyApiService.getPlaylistTracks(token, playlistId, limit = 100)
            }
            tracks = fetchedTracks
            if (playlistCover.isNullOrBlank() && fetchedTracks.isNotEmpty()) {
                playlistCover = fetchedTracks.firstOrNull()?.albumArtUrl
            }
        }
        isLoading = false
    }

    fun playTrackAtIndex(index: Int, shuffle: Boolean = false) {
        if (playerConnection == null || tracks.isEmpty()) return
        scope.launch {
            resolvingIndex = index
            val targetTrack = tracks.getOrNull(index) ?: return@launch
            val resolvedMetadata = SpotifyTrackResolver.resolveTrack(targetTrack)
            resolvingIndex = null

            if (resolvedMetadata != null) {
                val mediaItem = resolvedMetadata.toMediaItem()
                playerConnection.playQueue(
                    ListQueue(
                        title = playlistTitle,
                        items = listOf(mediaItem),
                        startIndex = 0
                    )
                )
                // Start prefetching upcoming tracks asynchronously
                SpotifyTrackResolver.prefetchUpcoming(scope, tracks, index + 1, count = 5)
            } else {
                Toast.makeText(context, "Could not match track for playback", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = playlistTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.ArrowBack),
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        modifier = Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Header item
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Artwork
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF1DB954), Color(0xFF191414))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!playlistCover.isNullOrBlank()) {
                                AsyncImage(
                                    model = playlistCover,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    painter = painterResource(R.drawable.ic_spotify),
                                    contentDescription = null,
                                    modifier = Modifier.size(72.dp),
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = playlistTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (!playlistOwner.isNullOrBlank()) {
                            Text(
                                text = "By $playlistOwner • ${tracks.size} songs",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Controls Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { playTrackAtIndex(0) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1DB954),
                                    contentColor = Color.Black
                                )
                            ) {
                                Icon(
                                    painter = rememberVectorPainter(Icons.Rounded.PlayArrow),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Play All",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            FilledTonalIconButton(
                                onClick = {
                                    if (tracks.isNotEmpty()) {
                                        val randomIndex = tracks.indices.random()
                                        playTrackAtIndex(randomIndex, shuffle = true)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    painter = rememberVectorPainter(Icons.Rounded.Shuffle),
                                    contentDescription = "Shuffle"
                                )
                            }

                            // 1-Tap Import to Local Library
                            FilledTonalIconButton(
                                onClick = {
                                    if (tracks.isEmpty() || isImportingToLocal) return@FilledTonalIconButton
                                    scope.launch {
                                        isImportingToLocal = true
                                        Toast.makeText(context, "Importing to Library...", Toast.LENGTH_SHORT).show()
                                        try {
                                            val resolvedList = mutableListOf<iad1tya.echo.music.models.MediaMetadata>()
                                            for (track in tracks) {
                                                val resolved = SpotifyTrackResolver.resolveTrack(track)
                                                if (resolved != null) {
                                                    resolvedList.add(resolved)
                                                }
                                            }
                                            if (resolvedList.isNotEmpty()) {
                                                database.transaction {
                                                    val playlistEntity = PlaylistEntity(
                                                        name = playlistTitle,
                                                        browseId = null,
                                                        thumbnailUrl = playlistCover,
                                                        isEditable = true,
                                                        bookmarkedAt = LocalDateTime.now(),
                                                    )
                                                    insert(playlistEntity)
                                                    resolvedList.forEachIndexed { index, mediaMetadata ->
                                                        insert(mediaMetadata)
                                                        insert(
                                                            PlaylistSongMap(
                                                                songId = mediaMetadata.id,
                                                                playlistId = playlistEntity.id,
                                                                position = index,
                                                            )
                                                        )
                                                    }
                                                }
                                                Toast.makeText(context, "Imported ${resolvedList.size} songs to Library!", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "Could not match songs for import", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                        } finally {
                                            isImportingToLocal = false
                                        }
                                    }
                                },
                                enabled = !isImportingToLocal,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isImportingToLocal) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.PlaylistAdd),
                                        contentDescription = "Import to Local"
                                    )
                                }
                            }

                            // Make Available Offline / Download
                            FilledTonalIconButton(
                                onClick = {
                                    if (tracks.isEmpty() || isDownloadingPlaylist) return@FilledTonalIconButton
                                    scope.launch {
                                        isDownloadingPlaylist = true
                                        Toast.makeText(context, "Resolving tracks for offline download...", Toast.LENGTH_SHORT).show()
                                        try {
                                            var count = 0
                                            for (track in tracks) {
                                                val resolved = SpotifyTrackResolver.resolveTrack(track)
                                                if (resolved != null) {
                                                    database.query {
                                                        insert(resolved)
                                                    }
                                                    val downloadRequest = DownloadRequest.Builder(resolved.id, "echo://${resolved.id}".toUri())
                                                        .setCustomCacheKey(resolved.id)
                                                        .setData(resolved.title.toByteArray())
                                                        .build()
                                                    DownloadService.sendAddDownload(
                                                        context,
                                                        ExoDownloadService::class.java,
                                                        downloadRequest,
                                                        false
                                                    )
                                                    count++
                                                }
                                            }
                                            Toast.makeText(context, "Queued $count songs for download!", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                        } finally {
                                            isDownloadingPlaylist = false
                                        }
                                    }
                                },
                                enabled = !isDownloadingPlaylist,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isDownloadingPlaylist) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        painter = rememberVectorPainter(Icons.Rounded.Download),
                                        contentDescription = "Make Available Offline"
                                    )
                                }
                            }
                        }
                    }
                }

                // Track items
                itemsIndexed(tracks) { index, track ->
                    val isCurrentResolving = resolvingIndex == index

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { playTrackAtIndex(index) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Track number or resolving indicator
                        Box(
                            modifier = Modifier.width(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCurrentResolving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF1DB954)
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        // Album artwork thumbnail
                        AsyncImage(
                            model = track.albumArtUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(Modifier.width(12.dp))

                        // Title & Artists
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = track.artistString,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Duration
                        if (track.durationSeconds > 0) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = formatDuration(track.durationSeconds),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "$m:${if (s < 10) "0$s" else "$s"}"
}
