package iad1tya.echo.music.spotify

import android.util.Log
import iad1tya.echo.music.models.MediaMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

/**
 * Spotify Daylist & Dynamic Mood Radar Generator.
 * Generates viral, hyper-specific time-of-day and mood-aware playlist descriptors
 * (e.g., "lo-fi focus monday afternoon", "high energy gym friday evening")
 * matching Spotify's global Daylist formula.
 */
object DaylistRadarGenerator {
    private const val TAG = "DaylistRadarGenerator"

    data class DaylistBundle(
        val title: String,
        val subtitle: String,
        val tracks: List<SpotifyTrack> = emptyList(),
        val isSpotifyNative: Boolean = false,
        val timeVibe: String = "Ambient Flow",
    )

    private val timeSlotKeywords = mapOf(
        "early morning" to listOf("peaceful", "sunrise", "acoustic", "gentle", "first light", "ambient", "mindful", "soft"),
        "morning" to listOf("productive", "upbeat", "bright", "sunny", "morning coffee", "focus", "motivational", "fresh"),
        "midday" to listOf("daytime groove", "indie pop", "coffee shop", "tempo boost", "breezy", "crisp", "steady flow"),
        "afternoon" to listOf("lo-fi beats", "afternoon sun", "chillout", "study session", "head nod", "easygoing", "dreamy"),
        "sunset" to listOf("golden hour", "downtempo", "melancholy", "warm dusk", "synthwave", "twilight", "nostalgic"),
        "evening" to listOf("unwind", "night drive", "r&b chill", "neon glow", "after hours", "cozy vibes", "moody"),
        "night" to listOf("deep thoughts", "late night", "euphoria", "midnight synth", "dark wave", "bedroom pop", "atmospheric"),
        "late night" to listOf("3am introspection", "liminal space", "insomnia beats", "ethereal", "drift away", "distant dreams")
    )

    /**
     * Resolves the active Daylist.
     * If logged in with Spotify, attempts to fetch the user's authentic live Daylist
     * from Spotify's global daylist playlist (spotify:playlist:37i9dQZF1EP6YuccBxUcC1).
     * If not logged in or offline, dynamically generates an authentic Daylist bundle.
     */
    suspend fun resolveActiveDaylist(): DaylistBundle = withContext(Dispatchers.IO) {
        try {
            val token = SpotifyAuthManager.getValidAccessToken()
            if (token != null) {
                val spotifyDaylist = SpotifyApiService.getDaylist(token)
                if (spotifyDaylist != null && spotifyDaylist.second.isNotEmpty()) {
                    val (daylistTitle, tracks) = spotifyDaylist
                    Log.i(TAG, "Using live Spotify Daylist: $daylistTitle (${tracks.size} tracks)")
                    return@withContext DaylistBundle(
                        title = daylistTitle,
                        subtitle = "Live Spotify Daylist • ${tracks.size} tracks",
                        tracks = tracks,
                        isSpotifyNative = true,
                        timeVibe = "Spotify Live",
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to resolve live Spotify Daylist: ${e.message}")
        }

        // Dynamic local Mood Radar generation based on current time & day
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val dayName = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH)?.lowercase(Locale.ROOT) ?: "today"

        val (slotName, keywords) = when (hour) {
            in 5..7 -> "early morning" to timeSlotKeywords["early morning"]!!
            in 8..11 -> "morning" to timeSlotKeywords["morning"]!!
            in 12..14 -> "midday" to timeSlotKeywords["midday"]!!
            in 15..17 -> "afternoon" to timeSlotKeywords["afternoon"]!!
            in 18..19 -> "sunset" to timeSlotKeywords["sunset"]!!
            in 20..22 -> "evening" to timeSlotKeywords["evening"]!!
            23, 0 -> "night" to timeSlotKeywords["night"]!!
            else -> "late night" to timeSlotKeywords["late night"]!!
        }

        val primaryMood = keywords[(hour * 3) % keywords.size]
        val secondaryMood = keywords[(hour * 7 + 2) % keywords.size]
        val generatedTitle = "$primaryMood $secondaryMood $dayName $slotName"

        DaylistBundle(
            title = generatedTitle,
            subtitle = "Dynamic Mood Radar • ${slotName.replaceFirstChar { it.uppercase() }} Mix",
            tracks = emptyList(),
            isSpotifyNative = false,
            timeVibe = primaryMood.replaceFirstChar { it.uppercase() },
        )
    }

    /**
     * Converts daylist SpotifyTracks to MediaMetadata.
     */
    suspend fun resolveDaylistTracks(daylist: DaylistBundle): List<MediaMetadata> = withContext(Dispatchers.IO) {
        if (daylist.tracks.isNotEmpty()) {
            SpotifyTrackResolver.resolveTracks(daylist.tracks)
        } else {
            emptyList()
        }
    }
}
