package iad1tya.echo.music.spotify

import android.util.Log
import com.echo.innertube.CloudflareDnsResolver
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object SpotifyApiService {
    private const val TAG = "SpotifyApiService"
    private const val BASE_URL = "https://api.spotify.com/v1"
    private const val PARTNER_GQL_URL = "https://api-partner.spotify.com/pathfinder/v1/query"
    private const val SPCLIENT_BASE = "https://spclient.wg.spotify.com"

    // Persisted GraphQL Query Hashes from Spotify Web Player bundle
    private const val HASH_LIBRARY_V3 = "390c78e5b951029bad359785e69b07b536a509c581cbcd0aded5e5067f187455"
    private const val HASH_FETCH_LIBRARY_TRACKS = "087278b20b743578a6262c2b0b4bcd20d879c503cc359a2285baf083ef944240"
    private const val HASH_FETCH_PLAYLIST = "86dde7b9d9356e2369414647cf6950cfed96e778e129cfdfc99aea6c1613b3b0"
    private const val HASH_HOME = "76243c78b0e20ecdbe41b794dec8cbe73f75e585b0a7201b8d2e84578412847a"

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .dns(CloudflareDnsResolver)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private fun buildHeaders(builder: Request.Builder, accessToken: String): Request.Builder {
        return builder
            .header("Authorization", "Bearer $accessToken")
            .header("app-platform", "WebPlayer")
            .header("Origin", "https://open.spotify.com")
            .header("Referer", "https://open.spotify.com/")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
    }

    private fun buildRequest(url: String, accessToken: String): Request {
        return buildHeaders(Request.Builder().url(url), accessToken).build()
    }

    private suspend fun executeGraphQL(
        accessToken: String,
        operationName: String,
        sha256Hash: String,
        variables: JsonObject
    ): JsonObject? = withContext(Dispatchers.IO) {
        try {
            val extensions = JsonObject().apply {
                add("persistedQuery", JsonObject().apply {
                    addProperty("version", 1)
                    addProperty("sha256Hash", sha256Hash)
                })
            }

            val payload = JsonObject().apply {
                addProperty("operationName", operationName)
                add("variables", variables)
                add("extensions", extensions)
            }

            val requestBody = gson.toJson(payload).toRequestBody(JSON_MEDIA_TYPE)
            val request = buildHeaders(Request.Builder().url(PARTNER_GQL_URL).post(requestBody), accessToken).build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.w(TAG, "executeGraphQL $operationName failed: HTTP ${response.code} - $body")
                return@withContext null
            }

            gson.fromJson(body, JsonObject::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "executeGraphQL $operationName exception: ${e.message}", e)
            null
        }
    }

    /**
     * Converts Spotify image URIs (spotify:image:..., spotify:mosaic:...) to direct CDN URLs.
     */
    fun resolveSpotifyImageUrl(rawUrl: String?): String? {
        if (rawUrl.isNullOrBlank()) return null
        if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) return rawUrl
        if (rawUrl.startsWith("spotify:image:")) {
            val hash = rawUrl.removePrefix("spotify:image:").trim()
            return "https://i.scdn.co/image/$hash"
        }
        if (rawUrl.startsWith("spotify:mosaic:")) {
            val parts = rawUrl.removePrefix("spotify:mosaic:").split(":")
            val firstHash = parts.firstOrNull { it.isNotBlank() } ?: return null
            return "https://i.scdn.co/image/$firstHash"
        }
        return rawUrl
    }

    /**
     * Fetches current authenticated user's profile.
     * Uses internal spclient endpoint first, falls back to REST /me.
     */
    suspend fun getMe(accessToken: String): SpotifyUser? = withContext(Dispatchers.IO) {
        // 1. Try spclient user-profile-view
        try {
            val request = buildRequest("$SPCLIENT_BASE/user-profile-view/v3/profile/me", accessToken)
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val json = gson.fromJson(body, JsonObject::class.java)
                    val uri = json.get("uri")?.asString ?: "spotify:user:me"
                    val id = uri.removePrefix("spotify:user:")
                    val name = json.get("name")?.asString ?: "Spotify User"
                    val img = resolveSpotifyImageUrl(json.get("image_url")?.asString)
                    val followers = json.get("followers_count")?.asInt ?: 0

                    Log.i(TAG, "getMe resolved via spclient: $name ($id)")
                    return@withContext SpotifyUser(
                        id = id,
                        displayName = name,
                        email = null,
                        avatarUrl = img,
                        product = "free",
                        followersCount = followers
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "getMe spclient failed: ${e.message}")
        }

        // 2. Fallback to REST /me
        try {
            val request = buildRequest("$BASE_URL/me", accessToken)
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
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

                return@withContext SpotifyUser(
                    id = id,
                    displayName = displayName,
                    email = email,
                    avatarUrl = resolveSpotifyImageUrl(avatarUrl),
                    product = product,
                    followersCount = followers
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "getMe REST exception: ${e.message}", e)
        }

        null
    }

    /**
     * Fetches all playlists owned, followed, or collaborated on by the user.
     * Combines GraphQL libraryV3, spclient profile playlists, and REST endpoints.
     */
    suspend fun getUserPlaylists(accessToken: String, limit: Int = 50, offset: Int = 0): List<SpotifyPlaylist> = withContext(Dispatchers.IO) {
        val playlistMap = linkedMapOf<String, SpotifyPlaylist>()

        // 1. Fetch from GraphQL libraryV3 (covers user playlists, collaborative playlists with friends, followed playlists)
        try {
            val variables = JsonObject().apply {
                add("filters", JsonArray().apply { add("Playlists") })
                add("order", null)
                add("textFilter", null)
                add("features", JsonArray().apply {
                    add("LIKED_SONGS")
                    add("YOUR_EPISODES_V2")
                })
                addProperty("limit", limit)
                addProperty("offset", offset)
                addProperty("flatten", false)
                add("expandedFolders", JsonArray())
                add("folderUri", null)
                addProperty("includeFoldersWhenFlattening", true)
            }

            val gqlResult = executeGraphQL(accessToken, "libraryV3", HASH_LIBRARY_V3, variables)
            if (gqlResult != null && gqlResult.has("data")) {
                val me = gqlResult.getAsJsonObject("data")?.getAsJsonObject("me")
                val libraryV3 = me?.getAsJsonObject("libraryV3")
                val items = libraryV3?.getAsJsonArray("items")

                if (items != null) {
                    for (elem in items) {
                        val itemWrapper = elem.asJsonObject ?: continue
                        val itemObj = itemWrapper.getAsJsonObject("item") ?: continue
                        val dataObj = itemObj.getAsJsonObject("data") ?: continue
                        val typename = dataObj.get("__typename")?.asString ?: ""

                        if (typename == "Playlist" || typename.contains("Playlist")) {
                            val uri = dataObj.get("uri")?.asString ?: itemObj.get("_uri")?.asString ?: continue
                            val id = uri.removePrefix("spotify:playlist:").trim()
                            val name = dataObj.get("name")?.asString ?: "Spotify Playlist"
                            val desc = dataObj.get("description")?.asString

                            var ownerName: String? = null
                            val ownerV2 = dataObj.getAsJsonObject("ownerV2")?.getAsJsonObject("data")
                            if (ownerV2 != null) {
                                ownerName = ownerV2.get("name")?.asString ?: ownerV2.get("username")?.asString
                            }

                            // Detect if collaborative: check attributes or owner
                            val isCollab = dataObj.get("collaborative")?.asBoolean
                                ?: dataObj.getAsJsonArray("attributes")?.any {
                                    it.asString.contains("COLLABORATIVE", ignoreCase = true)
                                } ?: false

                            // Resolve cover image
                            var imgUrl: String? = null
                            val imagesItems = dataObj.getAsJsonObject("images")?.getAsJsonArray("items")
                            if (imagesItems != null && imagesItems.size() > 0) {
                                val sources = imagesItems.get(0).asJsonObject.getAsJsonArray("sources")
                                if (sources != null && sources.size() > 0) {
                                    imgUrl = sources.get(0).asJsonObject.get("url")?.asString
                                }
                            }
                            if (imgUrl == null) {
                                val coverSources = dataObj.getAsJsonObject("coverArt")?.getAsJsonArray("sources")
                                if (coverSources != null && coverSources.size() > 0) {
                                    imgUrl = coverSources.get(0).asJsonObject.get("url")?.asString
                                }
                            }

                            val totalCount = dataObj.getAsJsonObject("content")?.get("totalCount")?.asInt
                                ?: dataObj.getAsJsonObject("tracks")?.get("total")?.asInt ?: 0

                            playlistMap[id] = SpotifyPlaylist(
                                id = id,
                                name = name,
                                description = desc,
                                imageUrl = resolveSpotifyImageUrl(imgUrl),
                                trackCount = totalCount,
                                ownerName = ownerName,
                                isCollaborative = isCollab
                            )
                        }
                    }
                    Log.i(TAG, "libraryV3 returned ${playlistMap.size} playlists")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "libraryV3 exception: ${e.message}")
        }

        // 2. Query spclient user-profile-view playlists (public playlists on profile)
        try {
            val request = buildRequest("$SPCLIENT_BASE/user-profile-view/v3/profile/me/playlists", accessToken)
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val json = gson.fromJson(body, JsonObject::class.java)
                    val publicPlaylists = json.getAsJsonArray("public_playlists")
                    if (publicPlaylists != null) {
                        for (elem in publicPlaylists) {
                            val item = elem.asJsonObject ?: continue
                            val uri = item.get("uri")?.asString ?: continue
                            val id = uri.removePrefix("spotify:playlist:").trim()
                            if (playlistMap.containsKey(id)) continue

                            val name = item.get("name")?.asString ?: "Spotify Playlist"
                            val imgUrl = resolveSpotifyImageUrl(item.get("image_url")?.asString)
                            val owner = item.get("owner_name")?.asString

                            playlistMap[id] = SpotifyPlaylist(
                                id = id,
                                name = name,
                                description = null,
                                imageUrl = imgUrl,
                                trackCount = 0,
                                ownerName = owner,
                                isCollaborative = false
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "spclient playlists exception: ${e.message}")
        }

        // 3. Fallback: REST /me/playlists
        if (playlistMap.isEmpty()) {
            try {
                val request = buildRequest("$BASE_URL/me/playlists?limit=$limit&offset=$offset", accessToken)
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val json = gson.fromJson(body, JsonObject::class.java)
                        val items = json.getAsJsonArray("items")
                        if (items != null) {
                            for (element in items) {
                                val item = element.asJsonObject ?: continue
                                val id = item.get("id")?.asString ?: continue
                                val name = item.get("name")?.asString ?: "Untitled Playlist"
                                val desc = item.get("description")?.asString
                                val tracksCount = item.getAsJsonObject("tracks")?.get("total")?.asInt ?: 0
                                val owner = item.getAsJsonObject("owner")?.get("display_name")?.asString
                                val isCollab = item.get("collaborative")?.asBoolean ?: false

                                val images = item.getAsJsonArray("images")
                                val imageUrl = if (images != null && images.size() > 0) {
                                    images.get(0).asJsonObject.get("url")?.asString
                                } else null

                                playlistMap[id] = SpotifyPlaylist(
                                    id = id,
                                    name = name,
                                    description = desc,
                                    imageUrl = resolveSpotifyImageUrl(imageUrl),
                                    trackCount = tracksCount,
                                    ownerName = owner,
                                    isCollaborative = isCollab
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "REST me/playlists exception: ${e.message}")
            }
        }

        playlistMap.values.toList()
    }

    /**
     * Fetches tracks for a given Spotify playlist.
     * Uses GraphQL fetchPlaylist first, falls back to REST /playlists/{id}/tracks.
     */
    suspend fun getPlaylistTracks(accessToken: String, playlistId: String, limit: Int = 100, offset: Int = 0): List<SpotifyTrack> = withContext(Dispatchers.IO) {
        // 1. Try GraphQL fetchPlaylist
        try {
            val variables = JsonObject().apply {
                addProperty("uri", "spotify:playlist:$playlistId")
                addProperty("offset", offset)
                addProperty("limit", limit)
                addProperty("enableWatchFeedEntrypoint", true)
            }

            val gqlResult = executeGraphQL(accessToken, "fetchPlaylist", HASH_FETCH_PLAYLIST, variables)
            if (gqlResult != null && gqlResult.has("data")) {
                val playlistV2 = gqlResult.getAsJsonObject("data")?.getAsJsonObject("playlistV2")
                val content = playlistV2?.getAsJsonObject("content")
                val items = content?.getAsJsonArray("items")

                if (items != null && items.size() > 0) {
                    val tracks = mutableListOf<SpotifyTrack>()
                    for (elem in items) {
                        val itemWrapper = elem.asJsonObject ?: continue
                        val itemV2 = itemWrapper.getAsJsonObject("itemV2") ?: continue
                        val trackData = itemV2.getAsJsonObject("data") ?: continue
                        val typename = trackData.get("__typename")?.asString ?: ""
                        if (typename != "Track" && typename != "Episode" && typename.isNotBlank()) continue

                        val uri = trackData.get("uri")?.asString ?: continue
                        val id = uri.removePrefix("spotify:track:").removePrefix("spotify:episode:").trim()
                        val name = trackData.get("name")?.asString ?: continue
                        val durationMs = trackData.getAsJsonObject("trackDuration")?.get("totalMilliseconds")?.asLong ?: 0L

                        val artists = mutableListOf<String>()
                        val artistsItems = trackData.getAsJsonObject("artists")?.getAsJsonArray("items")
                        if (artistsItems != null) {
                            for (art in artistsItems) {
                                val artName = art.asJsonObject?.getAsJsonObject("profile")?.get("name")?.asString
                                if (!artName.isNullOrBlank()) artists.add(artName)
                            }
                        }

                        val albumObj = trackData.getAsJsonObject("albumOfTrack")
                        val albumName = albumObj?.get("name")?.asString
                        var albumArtUrl: String? = null
                        val coverSources = albumObj?.getAsJsonObject("coverArt")?.getAsJsonArray("sources")
                        if (coverSources != null && coverSources.size() > 0) {
                            albumArtUrl = coverSources.get(0).asJsonObject.get("url")?.asString
                        }

                        tracks.add(
                            SpotifyTrack(
                                id = id,
                                title = name,
                                artists = if (artists.isNotEmpty()) artists else listOf("Spotify Artist"),
                                albumName = albumName,
                                albumArtUrl = resolveSpotifyImageUrl(albumArtUrl),
                                durationMs = durationMs
                            )
                        )
                    }

                    if (tracks.isNotEmpty()) {
                        Log.i(TAG, "fetchPlaylist GraphQL returned ${tracks.size} tracks for $playlistId")
                        return@withContext tracks
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchPlaylist GraphQL exception: ${e.message}")
        }

        // 2. Fallback to REST /playlists/{id}/tracks
        try {
            val url = "$BASE_URL/playlists/$playlistId/tracks?limit=$limit&offset=$offset"
            val request = buildRequest(url, accessToken)
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val json = gson.fromJson(body, JsonObject::class.java)
                    val items = json.getAsJsonArray("items")
                    if (items != null) {
                        return@withContext parseTrackItems(items)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getPlaylistTracks REST exception: ${e.message}", e)
        }

        emptyList()
    }

    /**
     * Fetches user's saved / liked tracks from Spotify.
     * Uses GraphQL fetchLibraryTracks first, falls back to REST /me/tracks.
     */
    suspend fun getUserSavedTracks(accessToken: String, limit: Int = 50, offset: Int = 0): List<SpotifyTrack> = withContext(Dispatchers.IO) {
        // 1. Try GraphQL fetchLibraryTracks
        try {
            val variables = JsonObject().apply {
                addProperty("offset", offset)
                addProperty("limit", limit)
            }

            val gqlResult = executeGraphQL(accessToken, "fetchLibraryTracks", HASH_FETCH_LIBRARY_TRACKS, variables)
            if (gqlResult != null && gqlResult.has("data")) {
                val me = gqlResult.getAsJsonObject("data")?.getAsJsonObject("me")
                val library = me?.getAsJsonObject("library")
                val tracksObj = library?.getAsJsonObject("tracks")
                val items = tracksObj?.getAsJsonArray("items")

                if (items != null && items.size() > 0) {
                    val tracks = mutableListOf<SpotifyTrack>()
                    for (elem in items) {
                        val itemWrapper = elem.asJsonObject ?: continue
                        val trackWrapper = itemWrapper.getAsJsonObject("track") ?: continue
                        val trackData = trackWrapper.getAsJsonObject("data") ?: trackWrapper

                        val uri = trackData.get("uri")?.asString ?: trackData.get("id")?.asString ?: continue
                        val id = uri.removePrefix("spotify:track:").trim()
                        val name = trackData.get("name")?.asString ?: continue
                        val durationMs = trackData.getAsJsonObject("trackDuration")?.get("totalMilliseconds")?.asLong
                            ?: trackData.get("duration_ms")?.asLong ?: 0L

                        val artists = mutableListOf<String>()
                        val artistsItems = trackData.getAsJsonObject("artists")?.getAsJsonArray("items")
                        if (artistsItems != null) {
                            for (art in artistsItems) {
                                val artName = art.asJsonObject?.getAsJsonObject("profile")?.get("name")?.asString
                                    ?: art.asJsonObject?.get("name")?.asString
                                if (!artName.isNullOrBlank()) artists.add(artName)
                            }
                        }

                        val albumObj = trackData.getAsJsonObject("albumOfTrack") ?: trackData.getAsJsonObject("album")
                        val albumName = albumObj?.get("name")?.asString
                        var albumArtUrl: String? = null
                        val coverSources = albumObj?.getAsJsonObject("coverArt")?.getAsJsonArray("sources")
                            ?: albumObj?.getAsJsonArray("images")
                        if (coverSources != null && coverSources.size() > 0) {
                            albumArtUrl = coverSources.get(0).asJsonObject.get("url")?.asString
                        }

                        tracks.add(
                            SpotifyTrack(
                                id = id,
                                title = name,
                                artists = if (artists.isNotEmpty()) artists else listOf("Spotify Artist"),
                                albumName = albumName,
                                albumArtUrl = resolveSpotifyImageUrl(albumArtUrl),
                                durationMs = durationMs
                            )
                        )
                    }

                    if (tracks.isNotEmpty()) {
                        Log.i(TAG, "fetchLibraryTracks GraphQL returned ${tracks.size} liked songs")
                        return@withContext tracks
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchLibraryTracks GraphQL exception: ${e.message}")
        }

        // 2. Fallback to REST /me/tracks
        try {
            val url = "$BASE_URL/me/tracks?limit=$limit&offset=$offset"
            val request = buildRequest(url, accessToken)
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val json = gson.fromJson(body, JsonObject::class.java)
                    val items = json.getAsJsonArray("items")
                    if (items != null) {
                        return@withContext parseTrackItems(items)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getUserSavedTracks REST exception: ${e.message}", e)
        }

        emptyList()
    }

    /**
     * Fetches user's top tracks / on repeat.
     * Uses GraphQL home feed sections first, falls back to REST /me/top/tracks.
     */
    suspend fun getTopTracks(accessToken: String, timeRange: String = "medium_term", limit: Int = 20): List<SpotifyTrack> = withContext(Dispatchers.IO) {
        // 1. Try GraphQL home query
        try {
            val variables = JsonObject().apply {
                addProperty("timeZone", "Asia/Kolkata")
                addProperty("sp_t", "")
                add("facet", null)
                addProperty("sectionItemsLimit", 10)
                addProperty("homeEndUserIntegration", "INTEGRATION_WEB_PLAYER")
            }

            val gqlResult = executeGraphQL(accessToken, "home", HASH_HOME, variables)
            if (gqlResult != null && gqlResult.has("data")) {
                val home = gqlResult.getAsJsonObject("data")?.getAsJsonObject("home")
                val sections = home?.getAsJsonObject("sectionContainer")
                    ?.getAsJsonObject("sections")?.getAsJsonArray("items")

                if (sections != null) {
                    val tracks = mutableListOf<SpotifyTrack>()
                    for (section in sections) {
                        val sectionItems = section.asJsonObject?.getAsJsonObject("sectionItems")?.getAsJsonArray("items")
                            ?: continue
                        for (item in sectionItems) {
                            val content = item.asJsonObject?.getAsJsonObject("content")?.getAsJsonObject("data")
                                ?: continue
                            val typename = content.get("__typename")?.asString ?: ""
                            if (typename == "Track" || typename.contains("Track")) {
                                val uri = content.get("uri")?.asString ?: continue
                                val id = uri.removePrefix("spotify:track:").trim()
                                val name = content.get("name")?.asString ?: continue
                                val durationMs = content.getAsJsonObject("trackDuration")?.get("totalMilliseconds")?.asLong ?: 0L

                                val artists = mutableListOf<String>()
                                val artistsItems = content.getAsJsonObject("artists")?.getAsJsonArray("items")
                                if (artistsItems != null) {
                                    for (art in artistsItems) {
                                        val artName = art.asJsonObject?.getAsJsonObject("profile")?.get("name")?.asString
                                            ?: art.asJsonObject?.get("name")?.asString
                                        if (!artName.isNullOrBlank()) artists.add(artName)
                                    }
                                }

                                var albumArtUrl: String? = null
                                val coverSources = content.getAsJsonObject("albumOfTrack")?.getAsJsonObject("coverArt")?.getAsJsonArray("sources")
                                if (coverSources != null && coverSources.size() > 0) {
                                    albumArtUrl = coverSources.get(0).asJsonObject.get("url")?.asString
                                }

                                tracks.add(
                                    SpotifyTrack(
                                        id = id,
                                        title = name,
                                        artists = if (artists.isNotEmpty()) artists else listOf("Spotify Artist"),
                                        albumName = null,
                                        albumArtUrl = resolveSpotifyImageUrl(albumArtUrl),
                                        durationMs = durationMs
                                    )
                                )
                            }
                        }
                    }

                    if (tracks.isNotEmpty()) {
                        Log.i(TAG, "home GraphQL returned ${tracks.size} top/trending tracks")
                        return@withContext tracks.take(limit)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "home GraphQL exception: ${e.message}")
        }

        // 2. Fallback to REST /me/top/tracks
        try {
            val url = "$BASE_URL/me/top/tracks?time_range=$timeRange&limit=$limit"
            val request = buildRequest(url, accessToken)
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val json = gson.fromJson(body, JsonObject::class.java)
                    val items = json.getAsJsonArray("items")
                    if (items != null) {
                        val tracks = mutableListOf<SpotifyTrack>()
                        for (element in items) {
                            val trackObj = element.asJsonObject ?: continue
                            parseTrackObject(trackObj)?.let { tracks.add(it) }
                        }
                        return@withContext tracks
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getTopTracks REST exception: ${e.message}", e)
        }

        emptyList()
    }

    /**
     * Fetches user's top artists from Spotify.
     */
    suspend fun getTopArtists(accessToken: String, timeRange: String = "medium_term", limit: Int = 20): List<SpotifyArtist> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/me/top/artists?time_range=$timeRange&limit=$limit"
            val request = buildRequest(url, accessToken)
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val json = gson.fromJson(body, JsonObject::class.java)
                    val items = json.getAsJsonArray("items")
                    if (items != null) {
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

                            artists.add(SpotifyArtist(id, name, resolveSpotifyImageUrl(imageUrl), genres))
                        }
                        return@withContext artists
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "getTopArtists exception: ${e.message}")
        }

        emptyList()
    }

    /**
     * Fetches available genre seeds or returns curated list.
     */
    suspend fun getAvailableGenreSeeds(accessToken: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/recommendations/available-genre-seeds"
            val request = buildRequest(url, accessToken)
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val json = gson.fromJson(body, JsonObject::class.java)
                    val genres = json.getAsJsonArray("genres")
                    if (genres != null) {
                        val list = genres.mapNotNull { it.asString }
                        if (list.isNotEmpty()) return@withContext list
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "getAvailableGenreSeeds fallback: ${e.message}")
        }
        defaultGenreSeeds
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
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val json = gson.fromJson(body, JsonObject::class.java)
                    val tracksArray = json.getAsJsonArray("tracks")
                    if (tracksArray != null) {
                        val tracks = mutableListOf<SpotifyTrack>()
                        for (element in tracksArray) {
                            val trackObj = element.asJsonObject ?: continue
                            parseTrackObject(trackObj)?.let { tracks.add(it) }
                        }
                        return@withContext tracks
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "getRecommendations exception: ${e.message}")
        }
        emptyList()
    }

    private fun parseTrackItems(items: JsonArray): List<SpotifyTrack> {
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
            artists = if (artists.isNotEmpty()) artists else listOf("Spotify Artist"),
            albumName = albumName,
            albumArtUrl = resolveSpotifyImageUrl(albumArtUrl),
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
