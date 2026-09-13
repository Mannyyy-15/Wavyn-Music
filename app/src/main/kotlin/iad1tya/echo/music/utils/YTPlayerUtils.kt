package iad1tya.echo.music.utils

import android.net.ConnectivityManager
import androidx.media3.common.PlaybackException
import iad1tya.echo.music.constants.AudioQuality
import iad1tya.echo.music.constants.PlayerStreamClient
import com.echo.innertube.CloudflareDnsResolver
import com.echo.innertube.NewPipeUtils
import com.echo.innertube.YouTube
import com.echo.innertube.models.YouTubeClient
import com.echo.innertube.models.YouTubeClient.Companion.ANDROID_CREATOR
import com.echo.innertube.models.YouTubeClient.Companion.ANDROID_MUSIC
import com.echo.innertube.models.YouTubeClient.Companion.ANDROID_TESTSUITE
import com.echo.innertube.models.YouTubeClient.Companion.ANDROID_UNPLUGGED
import com.echo.innertube.models.YouTubeClient.Companion.ANDROID_VR_NO_AUTH
import com.echo.innertube.models.YouTubeClient.Companion.ANDROID_VR_1_43_32
import com.echo.innertube.models.YouTubeClient.Companion.ANDROID_VR_1_61_48
import com.echo.innertube.models.YouTubeClient.Companion.IOS
import com.echo.innertube.models.YouTubeClient.Companion.IOS_MUSIC
import com.echo.innertube.models.YouTubeClient.Companion.IPADOS
import com.echo.innertube.models.YouTubeClient.Companion.MOBILE
import com.echo.innertube.models.YouTubeClient.Companion.TVHTML5
import com.echo.innertube.models.YouTubeClient.Companion.TVHTML5_SIMPLY_EMBEDDED_PLAYER
import com.echo.innertube.models.YouTubeClient.Companion.VISIONOS
import com.echo.innertube.models.YouTubeClient.Companion.WEB
import com.echo.innertube.models.YouTubeClient.Companion.WEB_CREATOR
import com.echo.innertube.models.YouTubeClient.Companion.WEB_REMIX
import com.echo.innertube.models.response.PlayerResponse
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

object YTPlayerUtils {
    private const val logTag = "YTPlayerUtils"
    private const val FAILED_CLIENT_BACKOFF_MS = 10 * 60 * 1000L

    @Volatile private var cachedPublicVideoId: String? = null
    @Volatile private var streamClientPair: Pair<java.net.Proxy?, OkHttpClient>? = null

    private fun currentStreamClient(): OkHttpClient {
        val current = YouTube.streamProxy
        streamClientPair?.let { (proxy, client) ->
            if (proxy == current) return client
        }
        val client = OkHttpClient.Builder()
            .dns(CloudflareDnsResolver)
            .proxy(current)
            .build()
        streamClientPair = current to client
        return client
    }

    /**
     * The main client is used for metadata and initial streams.
     * [WEB_REMIX] provides correct metadata (loudnessDb) and premium formats.
     */
    private val MAIN_CLIENT: YouTubeClient = WEB_REMIX

    /**
     * Clients used for fallback streams in case the streams of the main client do not work.
     */
    private val STREAM_FALLBACK_CLIENTS: Array<YouTubeClient> = arrayOf(
        MOBILE,
        ANDROID_MUSIC,
        ANDROID_VR_NO_AUTH,
        IOS,
        IOS_MUSIC,
        TVHTML5_SIMPLY_EMBEDDED_PLAYER,
        ANDROID_VR_1_61_48,
        ANDROID_VR_1_43_32,
        ANDROID_CREATOR,
        ANDROID_TESTSUITE,
        ANDROID_UNPLUGGED,
        IPADOS,
        VISIONOS,
        TVHTML5,
        WEB,
        WEB_CREATOR,
        WEB_REMIX
    )

    private data class CachedStreamUrl(
        val url: String,
        val expiresAtMs: Long,
    )

    private val streamUrlCache = ConcurrentHashMap<String, CachedStreamUrl>()
    private val failedStreamClientsUntil = ConcurrentHashMap<String, Long>()

    fun invalidateCachedStreamUrls(videoId: String) {
        val prefix = "$videoId:"
        streamUrlCache.keys.removeIf { it.startsWith(prefix) }
    }

    fun markStreamClientFailed(videoId: String, clientKey: String?, httpStatusCode: Int?) {
        if (httpStatusCode != 403 && httpStatusCode != 429) return
        val normalizedClientKey = normalizeStreamClientKey(clientKey)
        if (normalizedClientKey.isEmpty()) return
        failedStreamClientsUntil[normalizedClientKey] =
            System.currentTimeMillis() + FAILED_CLIENT_BACKOFF_MS
    }

    private fun isStreamClientTemporarilyBlocked(videoId: String, clientKey: String?): Boolean {
        val normalizedClientKey = normalizeStreamClientKey(clientKey)
        if (normalizedClientKey.isEmpty()) return false

        val until = failedStreamClientsUntil[normalizedClientKey] ?: return false
        if (until <= System.currentTimeMillis()) {
            failedStreamClientsUntil.remove(normalizedClientKey)
            return false
        }
        return true
    }

    fun markPreferredClientFailed(videoId: String, client: PlayerStreamClient, httpStatusCode: Int?) {
        markStreamClientFailed(videoId, client.name, httpStatusCode)
    }

    private fun normalizeStreamClientKey(clientKey: String?): String {
        return clientKey?.trim()?.takeIf { it.isNotBlank() }?.uppercase(Locale.US).orEmpty()
    }

    data class PlaybackData(
        val audioConfig: PlayerResponse.PlayerConfig.AudioConfig?,
        val videoDetails: PlayerResponse.VideoDetails?,
        val playbackTracking: PlayerResponse.PlaybackTracking?,
        val format: PlayerResponse.StreamingData.Format,
        val streamUrl: String,
        val streamExpiresInSeconds: Int,
    )

    /**
     * Custom player response intended to use for playback.
     */
    suspend fun playerResponseForPlayback(
        videoId: String,
        playlistId: String? = null,
        audioQuality: AudioQuality,
        connectivityManager: ConnectivityManager,
        preferredStreamClient: PlayerStreamClient = PlayerStreamClient.IOS,
        webClientPoTokenEnabled: Boolean = false,
        useVisitorData: Boolean = false,
        manualGvsPoToken: String? = null,
        manualPlayerPoToken: String? = null,
    ): Result<PlaybackData> = runCatching {
        val attempts =
            when (audioQuality) {
                AudioQuality.HIGH -> listOf(AudioQuality.HIGH, AudioQuality.AUTO)
                AudioQuality.AUTO -> listOf(AudioQuality.AUTO, AudioQuality.HIGH)
                AudioQuality.LOW -> listOf(AudioQuality.LOW)
            }.distinct()

        var lastError: Throwable? = null
        for (attempt in attempts) {
            val attemptResult =
                runCatching {
                    playerResponseForPlaybackOnce(
                        videoId = videoId,
                        playlistId = playlistId,
                        audioQuality = attempt,
                        connectivityManager = connectivityManager,
                        preferredStreamClient = preferredStreamClient,
                    )
                }
            if (attemptResult.isSuccess) return@runCatching attemptResult.getOrThrow()
            lastError = attemptResult.exceptionOrNull()
        }
        throw lastError ?: IllegalStateException("Failed to resolve stream")
    }

    private suspend fun playerResponseForPlaybackOnce(
        videoId: String,
        playlistId: String?,
        audioQuality: AudioQuality,
        connectivityManager: ConnectivityManager,
        preferredStreamClient: PlayerStreamClient,
    ): PlaybackData {
        Timber.tag(logTag).i("Fetching player response for videoId: $videoId, playlistId: $playlistId")
        val isUploadedTrack = playlistId == "MLPT" || playlistId?.startsWith("MLPT") == true
        val apiPlaylistId: String? = if (isUploadedTrack) null else playlistId

        val signatureTimestamp = getSignatureTimestampOrNull(videoId)
            ?: cachedPublicVideoId?.let { fallbackId ->
                Timber.tag(logTag).d("Sig timestamp failed for $videoId, retrying with cached public video: $fallbackId")
                getSignatureTimestampOrNull(fallbackId)
            }
        Timber.tag(logTag).v("Signature timestamp: $signatureTimestamp")

        val isLoggedIn = YouTube.cookie != null
        val sessionId = if (isLoggedIn) YouTube.dataSyncId else YouTube.visitorData
        Timber.tag(logTag).v("Session authentication status: ${if (isLoggedIn) "Logged in" else "Not logged in"} (sessionId=${sessionId.orEmpty()})")

        var format: PlayerResponse.StreamingData.Format? = null
        var streamUrl: String? = null
        var streamExpiresInSeconds: Int? = null
        var streamPlayerResponse: PlayerResponse? = null

        val orderedFallbackClients =
            (
                if (isLoggedIn) {
                    STREAM_FALLBACK_CLIENTS.filter { it.loginSupported } + STREAM_FALLBACK_CLIENTS.filterNot { it.loginSupported }
                } else {
                    STREAM_FALLBACK_CLIENTS.toList()
                }
            ).distinct()

        val preferredYouTubeClient =
            when (preferredStreamClient) {
                PlayerStreamClient.ANDROID_VR -> IOS // Aliased to IOS to bypass bot detection currently blocking VR clients
                PlayerStreamClient.WEB_REMIX -> WEB_REMIX
                PlayerStreamClient.IOS -> IOS
                PlayerStreamClient.ANDROID -> ANDROID_MUSIC
                PlayerStreamClient.TVHTML5 -> TVHTML5_SIMPLY_EMBEDDED_PLAYER
            }

        val metadataClient =
            preferredYouTubeClient.takeIf { preferredStreamClient == PlayerStreamClient.ANDROID_VR } ?: MAIN_CLIENT

        Timber.tag(logTag).i("Fetching metadata response using client: ${metadataClient.clientName}")
        val metadataPlayerResponse = runCatching {
            YouTube.player(videoId, apiPlaylistId, metadataClient, signatureTimestamp).getOrThrow()
        }.getOrNull()
        val audioConfig = metadataPlayerResponse?.playerConfig?.audioConfig
        val videoDetails = metadataPlayerResponse?.videoDetails
        val playbackTracking = metadataPlayerResponse?.playbackTracking
        val expectedDurationMs = videoDetails?.lengthSeconds?.toLongOrNull()?.takeIf { it > 0 }?.times(1000L)

        val streamClients =
            buildList {
                add(preferredYouTubeClient)
                addAll(orderedFallbackClients)
                if (preferredYouTubeClient != MAIN_CLIENT) add(MAIN_CLIENT)
            }.distinct().filterNot { client ->
                val blocked = isStreamClientTemporarilyBlocked(videoId, client.clientName)
                if (blocked) {
                    Timber.tag(logTag).w("Temporarily blocked stream client for $videoId: ${client.clientName}")
                }
                blocked
            }

        val botDetectedClients = mutableSetOf<String>()

        for ((index, client) in streamClients.withIndex()) {
            format = null
            streamUrl = null
            streamExpiresInSeconds = null
            streamPlayerResponse = null

            Timber.tag(logTag).v(
                "Trying ${if (client == MAIN_CLIENT) "MAIN_CLIENT" else "fallback client"} ${index + 1}/${streamClients.size}: ${client.clientName}"
            )

            if (client != MAIN_CLIENT && client.loginRequired && !isLoggedIn) {
                Timber.tag(logTag).w("Skipping client ${client.clientName} - requires login but user is not logged in")
                continue
            }

            streamPlayerResponse =
                if (client == metadataClient) {
                    metadataPlayerResponse
                } else {
                    Timber.tag(logTag).i("Fetching player response for fallback client: ${client.clientName}")
                    YouTube.player(videoId, apiPlaylistId, client, signatureTimestamp).getOrNull()
                }

            if (streamPlayerResponse == null) continue

            if (streamPlayerResponse.playabilityStatus.status != "OK") {
                val reason = streamPlayerResponse.playabilityStatus.reason.orEmpty()
                val isBotDetection = isBotDetectionError(reason)
                Timber.tag(logTag).w(
                    "Player response status not OK: ${streamPlayerResponse.playabilityStatus.status}, reason: $reason, botDetection: $isBotDetection"
                )
                if (isBotDetection) {
                    botDetectedClients.add(client.clientName)
                    failedStreamClientsUntil[normalizeStreamClientKey(client.clientName)] =
                        System.currentTimeMillis() + FAILED_CLIENT_BACKOFF_MS
                }
                continue
            }

            val isMetered = connectivityManager.isActiveNetworkMetered
            val candidates =
                selectAudioFormatCandidates(
                    streamPlayerResponse,
                    audioQuality,
                    isMetered,
                )

            if (candidates.isEmpty()) continue

            var selectedFormat: PlayerResponse.StreamingData.Format? = null
            var selectedUrl: String? = null

            for (candidate in candidates.asSequence().take(6)) {
                if (isLoggedIn && expectedDurationMs != null && isLikelyPreview(candidate, expectedDurationMs)) continue
                val cacheKey = buildCacheKey(videoId, candidate.itag)
                val cached = streamUrlCache[cacheKey]
                val candidateUrl =
                    if (cached != null && cached.expiresAtMs > System.currentTimeMillis()) {
                        cached.url
                    } else {
                        findUrlOrNull(candidate, videoId, client)
                    } ?: continue
                selectedFormat = candidate
                selectedUrl = candidateUrl
                break
            }

            if (selectedFormat == null || selectedUrl == null) continue

            format = selectedFormat
            streamUrl = selectedUrl
            streamExpiresInSeconds = streamPlayerResponse.streamingData?.expiresInSeconds

            if (streamExpiresInSeconds == null) {
                streamPlayerResponse = null
                continue
            }

            Timber.tag(logTag).i("Format found: ${format.mimeType}, bitrate: ${format.bitrate}")
            Timber.tag(logTag).v("Stream expires in: $streamExpiresInSeconds seconds")

            val isPrivatelyOwned = streamPlayerResponse.videoDetails?.musicVideoType ==
                "MUSIC_VIDEO_TYPE_PRIVATELY_OWNED_TRACK" || isUploadedTrack

            if (isPrivatelyOwned) {
                Timber.tag(logTag).d("Skipping validation for privately owned/uploaded track (client: ${client.clientName})")
                break
            }

            val valid = validateStatus(streamUrl, client.userAgent)
            if (valid) {
                Timber.tag(logTag).i("Stream validated successfully with client: ${client.clientName}")
                break
            }

            Timber.tag(logTag).w("Stream validation failed with client: ${client.clientName}, trying next fallback")
            format = null
            streamUrl = null
            streamExpiresInSeconds = null
            streamPlayerResponse = null
        }

        if (streamPlayerResponse == null) {
            if (botDetectedClients.isNotEmpty()) {
                Timber.tag(logTag).e("Bot detection triggered on clients: $botDetectedClients - all clients failed")
                throw PlaybackException(
                    "Sign in to confirm you're not a bot",
                    null,
                    PlaybackException.ERROR_CODE_REMOTE_ERROR
                )
            }
            Timber.tag(logTag).e("Bad stream player response - all clients failed")
            throw Exception("Bad stream player response")
        }

        if (streamPlayerResponse.playabilityStatus.status != "OK") {
            val errorReason = streamPlayerResponse.playabilityStatus.reason
            Timber.tag(logTag).e("Playability status not OK: $errorReason")
            throw PlaybackException(
                errorReason,
                null,
                PlaybackException.ERROR_CODE_REMOTE_ERROR
            )
        }

        if (streamExpiresInSeconds == null) {
            Timber.tag(logTag).e("Missing stream expire time")
            throw Exception("Missing stream expire time")
        }

        if (format == null) {
            Timber.tag(logTag).e("Could not find suitable format for quality: $audioQuality")
            throw Exception("Could not find format for quality: $audioQuality")
        }

        if (streamUrl == null) {
            Timber.tag(logTag).e("Could not find stream url for format: ${format.mimeType}, itag: ${format.itag}")
            throw Exception("Could not find stream url")
        }

        Timber.tag(logTag).i("Successfully obtained playback data with format: ${format.mimeType}, bitrate: ${format.bitrate}")

        streamUrlCache[buildCacheKey(videoId, format.itag)] =
            CachedStreamUrl(
                url = streamUrl,
                expiresAtMs = System.currentTimeMillis() + (streamExpiresInSeconds * 1000L),
            )

        if (!isUploadedTrack) {
            cachedPublicVideoId = videoId
        }

        return PlaybackData(
            audioConfig,
            videoDetails,
            playbackTracking,
            format,
            streamUrl,
            streamExpiresInSeconds,
        )
    }

    /**
     * Simple player response intended to use for metadata only.
     */
    suspend fun playerResponseForMetadata(
        videoId: String,
        playlistId: String? = null,
    ): Result<PlayerResponse> {
        Timber.tag(logTag).i("Fetching metadata-only player response for videoId: $videoId using MAIN_CLIENT: ${MAIN_CLIENT.clientName}")
        return YouTube.player(videoId, playlistId, client = MAIN_CLIENT)
            .onSuccess { Timber.tag(logTag).d("Successfully fetched metadata") }
            .onFailure { Timber.tag(logTag).e(it, "Failed to fetch metadata") }
    }

    private fun selectAudioFormatCandidates(
        playerResponse: PlayerResponse,
        audioQuality: AudioQuality,
        networkMetered: Boolean,
        avoidCodecs: Set<String> = emptySet(),
    ): List<PlayerResponse.StreamingData.Format> {
        Timber.tag(logTag).i("Finding format with audioQuality: $audioQuality, network metered: $networkMetered")

        val audioFormats =
            playerResponse.streamingData?.adaptiveFormats
                ?.asSequence()
                ?.filter { it.isAudio && it.bitrate > 0 }
                ?.filter { it.url != null || it.signatureCipher != null || it.cipher != null }
                ?.filter { format ->
                    val codec = extractCodec(format.mimeType)?.lowercase(Locale.US)
                    codec == null || codec !in avoidCodecs
                }
                ?.toList()
                .orEmpty()

        if (audioFormats.isEmpty()) return emptyList()

        val effectiveQuality =
            when (audioQuality) {
                AudioQuality.AUTO -> if (networkMetered) AudioQuality.HIGH else AudioQuality.HIGH
                else -> audioQuality
            }

        val targetBitrateBps =
            when (effectiveQuality) {
                AudioQuality.LOW -> 70_000
                AudioQuality.HIGH -> 160_000
                AudioQuality.AUTO -> null
            }

        val preferHigher =
            compareByDescending<PlayerResponse.StreamingData.Format> { it.url != null }
                .thenByDescending { it.bitrate }
                .thenByDescending { codecRank(extractCodec(it.mimeType)) }
                .thenByDescending { it.audioSampleRate ?: 0 }

        val preferLowerAboveTarget =
            compareByDescending<PlayerResponse.StreamingData.Format> { it.url != null }
                .thenBy { it.bitrate }
                .thenByDescending { codecRank(extractCodec(it.mimeType)) }
                .thenByDescending { it.audioSampleRate ?: 0 }

        val candidates =
            if (targetBitrateBps == null) {
                audioFormats.sortedWith(preferHigher)
            } else {
                val belowOrEqual = audioFormats.filter { it.bitrate <= targetBitrateBps }
                if (belowOrEqual.isNotEmpty()) {
                    belowOrEqual.sortedWith(preferHigher)
                } else {
                    val aboveOrEqual = audioFormats.filter { it.bitrate >= targetBitrateBps }
                    if (aboveOrEqual.isNotEmpty()) aboveOrEqual.sortedWith(preferLowerAboveTarget)
                    else audioFormats.sortedWith(preferHigher)
                }
            }

        return candidates
    }

    private fun extractCodec(mimeType: String): String? {
        val match = Regex("""codecs="([^"]+)"""").find(mimeType) ?: return null
        return match.groupValues.getOrNull(1)?.split(",")?.firstOrNull()?.trim()
    }

    private fun codecRank(codec: String?): Int =
        when {
            codec.isNullOrBlank() -> 0
            codec.contains("opus", ignoreCase = true) -> 3
            codec.contains("mp4a", ignoreCase = true) -> 2
            else -> 1
        }

    private fun isLikelyPreview(
        format: PlayerResponse.StreamingData.Format,
        expectedDurationMs: Long,
    ): Boolean {
        val approx = format.approxDurationMs?.toLongOrNull() ?: return false
        if (expectedDurationMs < 90_000L) return false
        return approx in 1L..(minOf(90_000L, (expectedDurationMs * 9L) / 10L))
    }

    private fun validateStatus(url: String, userAgent: String): Boolean {
        Timber.tag(logTag).v("Validating stream URL status")
        try {
            val httpUrl = url.toHttpUrlOrNull()
            val clientParam = httpUrl?.queryParameter("c")?.trim().orEmpty()

            val resolvedUserAgent = StreamClientUtils.resolveUserAgent(clientParam).ifEmpty { userAgent }
            val originReferer = StreamClientUtils.resolveOriginReferer(clientParam)

            val probeRanges =
                if (StreamClientUtils.isWebClient(clientParam)) {
                    listOf("bytes=0-0", "bytes=262144-262145", "bytes=1048576-1048577")
                } else {
                    listOf("bytes=0-0", "bytes=262144-262145")
                }

            for (range in probeRanges) {
                val rangeRequest =
                    okhttp3.Request.Builder()
                        .get()
                        .header("User-Agent", resolvedUserAgent)
                        .header("Range", range)
                        .apply {
                            originReferer.origin?.let { header("Origin", it) }
                            originReferer.referer?.let { header("Referer", it) }
                        }.url(url)
                        .build()

                val code = currentStreamClient().newCall(rangeRequest).execute().use { response -> response.code }
                if (code == 403) return false
                if (code !in 200..399 && code != 416) return false
            }

            return true
        } catch (e: Exception) {
            Timber.tag(logTag).e(e, "Stream URL validation failed with exception")
        }
        return false
    }

    private fun getSignatureTimestampOrNull(
        videoId: String
    ): Int? {
        Timber.tag(logTag).i("Getting signature timestamp for videoId: $videoId")
        return NewPipeUtils.getSignatureTimestamp(videoId)
            .onSuccess { Timber.tag(logTag).i("Signature timestamp obtained: $it") }
            .onFailure {
                Timber.tag(logTag).e(it, "Failed to get signature timestamp")
            }
            .getOrNull()
    }

    private fun findUrlOrNull(
        format: PlayerResponse.StreamingData.Format,
        videoId: String,
        client: YouTubeClient? = null,
    ): String? {
        Timber.tag(logTag).i("Finding stream URL for format: ${format.mimeType}, videoId: $videoId")
        var url = NewPipeUtils.getStreamUrl(format, videoId, client)
            .onSuccess { Timber.tag(logTag).i("Stream URL obtained successfully") }
            .onFailure {
                Timber.tag(logTag).e(it, "Failed to get stream URL")
            }
            .getOrNull() ?: return null

        if (client != null) {
            url = StreamClientUtils.patchClientVersion(url, client.clientVersion)
        }

        return url
    }

    private fun buildCacheKey(videoId: String, itag: Int): String {
        return "$videoId:$itag"
    }

    private fun isBotDetectionError(reason: String): Boolean {
        val lower = reason.lowercase(Locale.US)
        return "sign in" in lower ||
                "bot" in lower ||
                "confirm" in lower && "not a" in lower ||
                "verify" in lower && "human" in lower
    }

    fun isBotDetectionException(error: PlaybackException): Boolean {
        val message = error.message.orEmpty()
        if (isBotDetectionError(message)) return true
        var cause: Throwable? = error.cause
        while (cause != null) {
            if (isBotDetectionError(cause.message.orEmpty())) return true
            cause = cause.cause
        }
        return false
    }

    fun forceRefreshForVideo(videoId: String) {
        Timber.tag(logTag).d("Force refreshing caches for videoId: $videoId")
        invalidateCachedStreamUrls(videoId)
    }
}
