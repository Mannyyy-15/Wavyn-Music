package iad1tya.echo.music.ui.screens.spotify

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.text.style.TextAlign
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
fun SpotifyHubScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val playerConnection = LocalPlayerConnection.current

    val isLoggedIn by SpotifyAuthManager.isLoggedIn.collectAsState()
    val currentUser by SpotifyAuthManager.currentUser.collectAsState()
    val syncedPlaylists by SpotifyAuthManager.userPlaylists.collectAsState()
    val likedTracksCount by SpotifyAuthManager.likedTracksCount.collectAsState()

    var localPlaylists by remember { mutableStateOf<List<SpotifyPlaylist>>(emptyList()) }
    val playlists = if (syncedPlaylists.isNotEmpty()) syncedPlaylists else localPlaylists

    var topArtists by remember { mutableStateOf<List<SpotifyArtist>>(emptyList()) }
    var topTracks by remember { mutableStateOf<List<SpotifyTrack>>(emptyList()) }
    var genreSeeds by remember { mutableStateOf<List<String>>(SpotifyApiService.defaultGenreSeeds) }
    var selectedGenre by remember { mutableStateOf<String?>(null) }
    var genreRadioLoading by remember { mutableStateOf(false) }
    var isLoadingData by remember { mutableStateOf(true) }
    var isSyncing by remember { mutableStateOf(false) }

    suspend fun loadHubData() {
        val token = SpotifyAuthManager.getValidAccessToken()
        if (token != null) {
            SpotifyAuthManager.syncLibrary()
            if (syncedPlaylists.isEmpty()) {
                localPlaylists = SpotifyApiService.getUserPlaylists(token, limit = 50)
            }
            topArtists = SpotifyApiService.getTopArtists(token, limit = 15)
            topTracks = SpotifyApiService.getTopTracks(token, limit = 15)
            genreSeeds = SpotifyApiService.getAvailableGenreSeeds(token)
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            navController.navigate("spotify_login") {
                popUpTo("spotify_hub") { inclusive = true }
            }
            return@LaunchedEffect
        }

        isLoadingData = true
        loadHubData()
        isLoadingData = false
    }

    fun playGenreRadio(genre: String) {
        if (playerConnection == null) return
        scope.launch {
            selectedGenre = genre
            genreRadioLoading = true
            val token = SpotifyAuthManager.getValidAccessToken()
            if (token != null) {
                val recommended = SpotifyApiService.getRecommendations(
                    accessToken = token,
                    seedGenres = listOf(genre),
                    limit = 30
                )
                if (recommended.isNotEmpty()) {
                    val firstTrack = recommended.first()
                    val resolvedMetadata = SpotifyTrackResolver.resolveTrack(firstTrack)
                    if (resolvedMetadata != null) {
                        playerConnection.playQueue(
                            ListQueue(
                                title = "${genre.replaceFirstChar { it.uppercase() }} Radio (Spotify)",
                                items = listOf(resolvedMetadata.toMediaItem()),
                                startIndex = 0
                            )
                        )
                        SpotifyTrackResolver.prefetchUpcoming(scope, recommended, startIndex = 1, count = 5)
                        Toast.makeText(context, "Playing $genre radio (Ad-Free)!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Could not match $genre track", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "No recommendations found for $genre", Toast.LENGTH_SHORT).show()
                }
            }
            genreRadioLoading = false
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_spotify),
                            contentDescription = null,
                            modifier = Modifier.size(26.dp),
                            tint = Color(0xFF1DB954)
                        )
                        Text(
                            text = "Spotify Hub",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.ArrowBack),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                isSyncing = true
                                Toast.makeText(context, "Syncing Spotify library...", Toast.LENGTH_SHORT).show()
                                loadHubData()
                                isSyncing = false
                                Toast.makeText(context, "Spotify library synced!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF1DB954)
                            )
                        } else {
                            Icon(
                                painter = rememberVectorPainter(Icons.Rounded.Refresh),
                                contentDescription = "Sync Library"
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                                SpotifyAuthManager.logout()
                                Toast.makeText(context, "Disconnected from Spotify", Toast.LENGTH_SHORT).show()
                                navController.navigateUp()
                            }
                        }
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Rounded.Logout),
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        },
        modifier = Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
    ) { innerPadding ->
        if (isLoadingData) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF1DB954))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // User Profile Header Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.70f)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF1DB954).copy(alpha = 0.25f), Color.Transparent)
                                    )
                                )
                                .padding(18.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (!currentUser?.avatarUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = currentUser?.avatarUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, Color(0xFF1DB954), CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1DB954)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = currentUser?.displayName?.take(1)?.uppercase() ?: "S",
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentUser?.displayName ?: "Spotify User",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF1DB954).copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "AD-FREE HYBRID",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF1DB954),
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = "• ${playlists.size} Playlists",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Library Shortcuts (Liked Songs & Top Tracks)
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Your Spotify Library",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Liked Songs Card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(90.dp)
                                    .clickable {
                                        navController.navigate("spotify_playlist/liked_songs")
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.70f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF450AF5).copy(alpha = 0.35f), Color.Transparent)
                                            )
                                        )
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF450AF5), Color(0xFF8E8EE5))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = rememberVectorPainter(Icons.Rounded.Favorite),
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Liked Songs",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (likedTracksCount > 0) "$likedTracksCount songs" else "Play library",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Top Tracks Card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(90.dp)
                                    .clickable {
                                        if (topTracks.isNotEmpty() && playerConnection != null) {
                                            scope.launch {
                                                val firstTrack = topTracks.first()
                                                val resolved = SpotifyTrackResolver.resolveTrack(firstTrack)
                                                if (resolved != null) {
                                                    playerConnection.playQueue(
                                                        ListQueue(
                                                            title = "Spotify On Repeat",
                                                            items = listOf(resolved.toMediaItem()),
                                                            startIndex = 0
                                                        )
                                                    )
                                                    SpotifyTrackResolver.prefetchUpcoming(scope, topTracks, 1, 5)
                                                    Toast.makeText(context, "Playing your Top Songs!", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.70f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF1DB954).copy(alpha = 0.25f), Color.Transparent)
                                            )
                                        )
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF1DB954), Color(0xFF1AA34A))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = rememberVectorPainter(Icons.Rounded.TrendingUp),
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Top Songs",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "On Repeat",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 120+ Spotify Genre Seeds & Recommendation Engine
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Spotify Genre Recommendations",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tap any genre for an instant ad-free radio mix",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (genreRadioLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF1DB954)
                                )
                            }
                        }

                        // Curated prominent genres at the front
                        val prominentGenres = listOf(
                            "bollywood", "punjabi", "hip-hop", "pop", "lo-fi", "rock",
                            "edm", "indie", "chill", "workout", "dance", "r-n-b",
                            "anime", "jazz", "acoustic", "classical", "party", "sleep"
                        )
                        val fullGenreList = (prominentGenres + genreSeeds).distinct()

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(fullGenreList) { genre ->
                                val isSelected = selectedGenre == genre
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { playGenreRadio(genre) },
                                    label = {
                                        Text(
                                            text = genre.replaceFirstChar { it.uppercase() },
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF1DB954),
                                        selectedLabelColor = Color.Black,
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    )
                                )
                            }
                        }
                    }
                }

                // My Spotify Playlists
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Your Playlists",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        if (playlists.isEmpty()) {
                            Text(
                                text = "No playlists found on this Spotify account.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        } else {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(playlists) { playlist ->
                                    Column(
                                        modifier = Modifier
                                            .width(140.dp)
                                            .clickable {
                                                navController.navigate("spotify_playlist/${playlist.id}")
                                            }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(140.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(
                                                    Brush.linearGradient(
                                                        listOf(Color(0xFF1DB954).copy(alpha = 0.4f), Color(0xFF191414))
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (!playlist.imageUrl.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = playlist.imageUrl,
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_spotify),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(52.dp),
                                                    tint = Color.White
                                                )
                                            }
                                        }

                                        Spacer(Modifier.height(8.dp))

                                        Text(
                                            text = playlist.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (playlist.isCollaborative) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFF1DB954).copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        text = "COLLAB",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color(0xFF1DB954),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = if (playlist.trackCount > 0) "${playlist.trackCount} songs" else if (!playlist.ownerName.isNullOrBlank()) "By ${playlist.ownerName}" else "Spotify",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Top Artists
                if (topArtists.isNotEmpty()) {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Your Top Artists",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(topArtists) { artist ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .width(90.dp)
                                            .clickable {
                                                scope.launch {
                                                    val token = SpotifyAuthManager.getValidAccessToken()
                                                    if (token != null && playerConnection != null) {
                                                        val recommendations = SpotifyApiService.getRecommendations(
                                                            accessToken = token,
                                                            seedArtists = listOf(artist.id),
                                                            limit = 30
                                                        )
                                                        if (recommendations.isNotEmpty()) {
                                                            val first = recommendations.first()
                                                            val resolved = SpotifyTrackResolver.resolveTrack(first)
                                                            if (resolved != null) {
                                                                playerConnection.playQueue(
                                                                    ListQueue(
                                                                        title = "${artist.name} Radio",
                                                                        items = listOf(resolved.toMediaItem()),
                                                                        startIndex = 0
                                                                    )
                                                                )
                                                                SpotifyTrackResolver.prefetchUpcoming(scope, recommendations, 1, 5)
                                                                Toast.makeText(context, "Playing ${artist.name} Radio!", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                    ) {
                                        AsyncImage(
                                            model = artist.imageUrl,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = artist.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
