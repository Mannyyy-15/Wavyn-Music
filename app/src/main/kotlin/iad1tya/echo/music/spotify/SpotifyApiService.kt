package iad1tya.echo.music.spotify

import android.util.Log
import com.echo.innertube.CloudflareDnsResolver
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object SpotifyApiService {
    private const val TAG = "SpotifyApiService"
    private const val BASE_URL = "https://api.spotify.com/v1"

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .dns(CloudflareDnsResolver)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private fun buildRequest(url: String, accessToken: String): Request {
        return Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $accessToken")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .build()
    }

    /**
     * Fetches current authenticated user's profile.
     */
    suspend fun getMe(accessToken: String): SpotifyUser? = withContext(Dispatchers.IO) {
        try {
            val request = buildRequest("$BASE_URL/me", accessToken)
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "getMe failed: HTTP ${response.code}")
                return@withContext null
            }
            val body = response.body?.string() ?: return@withContext null
            val json = gson.fromJson(body, JsonObject::class.java)

            val id = json.get("id")?.asString ?: ""
            val displayName = json.get("display_name")?.asString ?: "Spotify User"
            val email = json.get("email")?.asString
            val product = json.get("product")?.asString
            val followers = json.getAsJsonObject("followers")?.get("total")?.asInt ?: 0
            val images = json.getAsJsonArray("images")
            val avatarUrl = if (images != null && images.size() > 0) {
                images.get(0).asJsonObject.get("url")?.asString
            } else null

            SpotifyUser(
                id = id,
                displayName = displayName,
                email = email,
                avatarUrl = avatarUrl,
                product = product,
                followersCount = followers
            )
        } catch (e: Exception) {
            Log.e(TAG, "getMe exception: ${e.message}", e)
            null
        }
    }

    /**
     * Fetches all playlists owned or followed by the user.
     */
    suspend fun getUserPlaylists(accessToken: String, limit: Int = 50, offset: Int = 0): List<SpotifyPlaylist> = withContext(Dispatchers.IO) {
        try {
            val request = buildRequest("$BASE_URL/me/playlists?limit=$limit&offset=$offset", accessToken)
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "getUserPlaylists failed: HTTP ${response.code}")
                return@withContext emptyList()
            }
            val body = response.body?.string() ?: return@withContext emptyList()
            val json = gson.fromJson(body, JsonObject::class.java)
            val items = json.getAsJsonArray("items") ?: return@withContext emptyList()

            val playlists = mutableListOf<SpotifyPlaylist>()
            for (element in items) {
                val item = element.asJsonObject ?: continue
                val id = item.get("id")?.asString ?: continue
                val name = item.get("name")?.asString ?: "Untitled Playlist"
                val desc = item.get("description")?.asString
                val tracksCount = item.getAsJsonObject("tracks")?.get("total")?.asInt ?: 0
                val owner = item.getAsJsonObject("owner")?.get("display_name")?.asString

                val images = item.getAsJsonArray("images")
                val imageUrl = if (images != null && images.size() > 0) {
                    images.get(0).asJsonObject.get("url")?.asString
                } else null

                playlists.add(
                    SpotifyPlaylist(
                        id = id,
                        name = name,
                        description = desc,
                        imageUrl = imageUrl,
                        trackCount = tracksCount,
                        ownerName = owner
                    )
                )
            }
            playlists
        } catch (e: Exception) {
            Log.e(TAG, "getUserPlaylists exception: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Fetches tracks for a given Spotify playlist.
     */
    suspend fun getPlaylistTracks(accessToken: String, playlistId: String, limit: Int = 100, offset: Int = 0): List<SpotifyTrack> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/playlists/$playlistId/tracks?limit=$limit&offset=$offset"
            val request = buildRequest(url, accessToken)
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "getPlaylistTracks failed: HTTP ${response.code}")
                return@withContext emptyList()
            }
            val body = response.body?.string() ?: return@withContext emptyList()
            val json = gson.fromJson(body, JsonObject::class.java)
            val items = json.getAsJsonArray("items") ?: return@withContext emptyList()

            parseTrackItems(items)
        } catch (e: Exception) {
            Log.e(TAG, "getPlaylistTracks exception: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Fetches user's saved / liked tracks from Spotify.
     */
    suspend fun getUserSavedTracks(accessToken: String, limit: Int = 50, offset: Int = 0): List<SpotifyTrack> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/me/tracks?limit=$limit&offset=$offset"
            val request = buildRequest(url, accessToken)
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "getUserSavedTracks failed: HTTP ${response.code}")
                return@withContext emptyList()
            }
            val body = response.body?.string() ?: return@withContext emptyList()
            val json = gson.fromJson(body, JsonObject::class.java)
            val items = json.getAsJsonArray("items") ?: return@withContext emptyList()

            parseTrackItems(items)
        } catch (e: Exception) {
            Log.e(TAG, "getUserSavedTracks exception: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Fetches user's top artists from Spotify.
     */
    suspend fun getTopArtists(accessToken: String, timeRange: String = "medium_term", limit: Int = 20): List<SpotifyArtist> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/me/top/artists?time_range=$timeRange&limit=$limit"
            val request = buildRequest(url, accessToken)
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext emptyList()
            }
            val body = response.body?.string() ?: return@withContext emptyList()
            val json = gson.fromJson(body, JsonObject::class.java)
            val items = json.getAsJsonArray("items") ?: return@withContext emptyList()

            val artists = mutableListOf<SpotifyArtist>()
            for (element in items) {
                val item = element.asJsonObject ?: continue
                val id = item.get("id")?.asString ?: continue
                val name = item.get("name")?.asString ?: continue
                val images = item.getAsJsonArray("images")
                val imageUrl = if (images != null && images.size() > 0) {
                    images.get(0).asJsonObject.get("url")?.asString
                } else null
                val genresJson = item.getAsJsonArray("genres")
                val genres = genresJson?.mapNotNull { it.asString } ?: emptyList()

                artists.add(SpotifyArtist(id, name, imageUrl, genres))
            }
            artists
        } catch (e: Exception) {
            Log.e(TAG, "getTopArtists exception: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Fetches user's top tracks from Spotify.
     */
    suspend fun getTopTracks(accessToken: String, timeRange: String = "medium_term", limit: Int = 20): List<SpotifyTrack> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/me/top/tracks?time_range=$timeRange&limit=$limit"
            val request = buildRequest(url, accessToken)
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext emptyList()
            }
            val body = response.body?.string() ?: return@withContext emptyList()
            val json = gson.fromJson(body, JsonObject::class.java)
            val items = json.getAsJsonArray("items") ?: return@withContext emptyList()

            val tracks = mutableListOf<SpotifyTrack>()
            for (element in items) {
                val trackObj = element.asJsonObject ?: continue
                parseTrackObject(trackObj)?.let { tracks.add(it) }
            }
            tracks
        } catch (e: Exception) {
            Log.e(TAG, "getTopTracks exception: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Fetches all available 120+ genre seeds from Spotify.
     */
    suspend fun getAvailableGenreSeeds(accessToken: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/recommendations/available-genre-seeds"
            val request = buildRequest(url, accessToken)
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext defaultGenreSeeds
            }
            val body = response.body?.string() ?: return@withContext defaultGenreSeeds
            val json = gson.fromJson(body, JsonObject::class.java)
            val genres = json.getAsJsonArray("genres") ?: return@withContext defaultGenreSeeds

            val list = genres.mapNotNull { it.asString }
            if (list.isNotEmpty()) list else defaultGenreSeeds
        } catch (e: Exception) {
            Log.w(TAG, "getAvailableGenreSeeds failed, using default list: ${e.message}")
            defaultGenreSeeds
        }
    }

    /**
     * Generates Spotify recommendations based on seed genres, artists, or tracks.
     */
    suspend fun getRecommendations(
        accessToken: String,
        seedGenres: List<String> = emptyList(),
        seedArtists: List<String> = emptyList(),
        seedTracks: List<String> = emptyList(),
        limit: Int = 30
    ): List<SpotifyTrack> = withContext(Dispatchers.IO) {
        try {
            val queryParams = mutableListOf("limit=$limit")
            if (seedGenres.isNotEmpty()) {
                queryParams.add("seed_genres=${seedGenres.take(3).joinToString(",")}")
            }
            if (seedArtists.isNotEmpty()) {
                queryParams.add("seed_artists=${seedArtists.take(2).joinToString(",")}")
            }
            if (seedTracks.isNotEmpty()) {
                queryParams.add("seed_tracks=${seedTracks.take(2).joinToString(",")}")
            }

            val url = "$BASE_URL/recommendations?${queryParams.joinToString("&")}"
            val request = buildRequest(url, accessToken)
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "getRecommendations failed: HTTP ${response.code}")
                return@withContext emptyList()
            }
            val body = response.body?.string() ?: return@withContext emptyList()
            val json = gson.fromJson(body, JsonObject::class.java)
            val tracksArray = json.getAsJsonArray("tracks") ?: return@withContext emptyList()

            val tracks = mutableListOf<SpotifyTrack>()
            for (element in tracksArray) {
                val trackObj = element.asJsonObject ?: continue
                parseTrackObject(trackObj)?.let { tracks.add(it) }
            }
            tracks
        } catch (e: Exception) {
            Log.e(TAG, "getRecommendations exception: ${e.message}", e)
            emptyList()
        }
    }

    private fun parseTrackItems(items: com.google.gson.JsonArray): List<SpotifyTrack> {
        val tracks = mutableListOf<SpotifyTrack>()
        for (element in items) {
            val itemObj = element.asJsonObject ?: continue
            val trackObj = (if (itemObj.has("track")) itemObj.getAsJsonObject("track") else itemObj) ?: continue
            parseTrackObject(trackObj)?.let { tracks.add(it) }
        }
        return tracks
    }

    private fun parseTrackObject(trackObj: JsonObject): SpotifyTrack? {
        val id = trackObj.get("id")?.asString ?: return null
        val name = trackObj.get("name")?.asString ?: return null
        val durationMs = trackObj.get("duration_ms")?.asLong ?: 0L

        val artists = mutableListOf<String>()
        val artistsArray = trackObj.getAsJsonArray("artists")
        if (artistsArray != null) {
            for (artistElement in artistsArray) {
                artistElement.asJsonObject?.get("name")?.asString?.let { artists.add(it) }
            }
        }

        val albumObj = trackObj.getAsJsonObject("album")
        val albumName = albumObj?.get("name")?.asString
        val albumImages = albumObj?.getAsJsonArray("images")
        val albumArtUrl = if (albumImages != null && albumImages.size() > 0) {
            albumImages.get(0).asJsonObject.get("url")?.asString
        } else null

        val isrc = trackObj.getAsJsonObject("external_ids")?.get("isrc")?.asString

        return SpotifyTrack(
            id = id,
            title = name,
            artists = artists,
            albumName = albumName,
            albumArtUrl = albumArtUrl,
            durationMs = durationMs,
            isrc = isrc
        )
    }

    // Default 120+ curated Spotify genres if offline or API is restricted
    val defaultGenreSeeds = listOf(
        "acoustic", "afrobeat", "alt-rock", "alternative", "ambient", "anime", "black-metal",
        "bluegrass", "blues", "bossanova", "brazil", "breakbeat", "british", "cantopop",
        "chicago-house", "children", "chill", "classical", "club", "comedy", "country",
        "dance", "dancehall", "death-metal", "deep-house", "detroit-techno", "disco",
        "disney", "drum-and-bass", "dub", "dubstep", "edm", "electro", "electronic",
        "emo", "folk", "forro", "french", "funk", "garage", "german", "gospel",
        "goth", "grindcore", "groove", "grunge", "guitar", "happy", "hard-rock",
        "hardcore", "hardstyle", "heavy-metal", "hip-hop", "holidays", "honky-tonk",
        "house", "idm", "indian", "indie", "indie-pop", "industrial", "iranian",
        "j-dance", "j-idol", "j-pop", "j-rock", "jazz", "k-pop", "kids", "latin",
        "latino", "malay", "mandopop", "metal", "metal-misc", "metalcore", "minimal-techno",
        "movies", "mpb", "new-age", "new-release", "opera", "pagode", "party",
        "philippines-opm", "piano", "pop", "pop-film", "post-dubstep", "power-pop",
        "progressive-house", "psych-rock", "punk", "punk-rock", "r-n-b", "rainy-day",
        "reggae", "reggaeton", "road-trip", "rock", "rock-n-roll", "rockabilly",
        "romance", "sad", "salsa", "samba", "sertanejo", "show-tunes", "singer-songwriter",
        "ska", "sleep", "songwriter", "soul", "soundtracks", "spanish", "study",
        "summer", "swedish", "synth-pop", "tango", "techno", "trance", "trip-hop",
        "turkish", "work-out", "world-music"
    )
}
