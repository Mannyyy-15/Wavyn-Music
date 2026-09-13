package iad1tya.echo.music.spotify

import android.util.Log
import com.echo.innertube.YouTube
import com.echo.innertube.models.SongItem
import iad1tya.echo.music.models.MediaMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

object SpotifyTrackResolver {
    private const val TAG = "SpotifyTrackResolver"
    private val cacheDb by lazy { SpotifyCacheDatabase.getInstance() }

    /**
     * Resolves a SpotifyTrack into a playable MediaMetadata object.
     * Uses local SQLite cache for instant lookup; falls back to InnerTube search if uncached.
     */
    suspend fun resolveTrack(track: SpotifyTrack): MediaMetadata? = withContext(Dispatchers.IO) {
        try {
            // 1. Check local cache
            val cachedVideoId = cacheDb.getMappedVideoId(track.id)
            if (!cachedVideoId.isNullOrBlank()) {
                return@withContext buildMediaMetadata(track, cachedVideoId)
            }

            // 2. Query InnerTube search
            val targetDurationSeconds = track.durationSeconds
            val cleanTitle = cleanTrackTitle(track.title)
            val query = "$cleanTitle ${track.artists.joinToString(" ")}"

            val searchResult = YouTube.search(query, YouTube.SearchFilter.FILTER_SONG)
            val items = searchResult.getOrNull()?.items ?: emptyList()
            val songItems = items.filterIsInstance<SongItem>()

            var matchedSong: SongItem? = null

            // First pass: find candidate matching duration within 10s
            if (targetDurationSeconds > 0 && songItems.isNotEmpty()) {
                matchedSong = songItems.firstOrNull { candidate ->
                    candidate.duration?.let { dur ->
                        abs(dur - targetDurationSeconds) <= 10
                    } ?: false
                }
            }

            // Second pass: fallback to first song item
            if (matchedSong == null) {
                matchedSong = songItems.firstOrNull()
            }

            val resolvedVideoId = matchedSong?.id ?: run {
                // Third pass: video search fallback
                val genericResult = YouTube.search(query, YouTube.SearchFilter.FILTER_VIDEO)
                val genericItems = genericResult.getOrNull()?.items ?: emptyList()
                val genericSong = genericItems.firstOrNull()
                genericSong?.id
            }

            if (!resolvedVideoId.isNullOrBlank()) {
                // Save in SQLite cache for 0ms future plays
                cacheDb.saveMapping(
                    spotifyId = track.id,
                    videoId = resolvedVideoId,
                    title = track.title,
                    artist = track.artistString,
                    duration = targetDurationSeconds
                )
                return@withContext buildMediaMetadata(track, resolvedVideoId)
            }

            Log.w(TAG, "No match found for Spotify track: ${track.title} - ${track.artistString}")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving Spotify track: ${e.message}", e)
            null
        }
    }

    /**
     * Resolves a batch of Spotify tracks into MediaMetadata.
     */
    suspend fun resolveTracks(tracks: List<SpotifyTrack>): List<MediaMetadata> = withContext(Dispatchers.IO) {
        tracks.mapNotNull { resolveTrack(it) }
    }

    /**
     * Prefetches upcoming tracks in background so skipping is instantaneous.
     */
    fun prefetchUpcoming(scope: CoroutineScope, tracks: List<SpotifyTrack>, startIndex: Int, count: Int = 4) {
        scope.launch(Dispatchers.IO) {
            val slice = tracks.drop(startIndex).take(count)
            slice.forEach { track ->
                if (cacheDb.getMappedVideoId(track.id) == null) {
                    resolveTrack(track)
                }
            }
        }
    }

    private fun cleanTrackTitle(title: String): String {
        // Strip out remaster tags, feat tags in parens, etc. for cleaner query matching
        return title
            .replace(Regex("(?i)-\\s*remaster(ed)?.*"), "")
            .replace(Regex("(?i)\\(\\s*remaster(ed)?.*\\)"), "")
            .replace(Regex("(?i)\\(\\s*feat\\..*?\\)"), "")
            .replace(Regex("(?i)\\[.*?edition\\]"), "")
            .trim()
    }

    private fun buildMediaMetadata(track: SpotifyTrack, videoId: String): MediaMetadata {
        return MediaMetadata(
            id = videoId,
            title = track.title,
            artists = track.artists.map { MediaMetadata.Artist(id = null, name = it) },
            duration = track.durationSeconds,
            thumbnailUrl = track.albumArtUrl,
            album = track.albumName?.let { MediaMetadata.Album(id = "", title = it) },
        )
    }
}
