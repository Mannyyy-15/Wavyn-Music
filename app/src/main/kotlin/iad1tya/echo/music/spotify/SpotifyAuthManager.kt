package iad1tya.echo.music.spotify

import android.util.Log
import androidx.datastore.preferences.core.edit
import com.echo.innertube.CloudflareDnsResolver
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import iad1tya.echo.music.constants.*
import iad1tya.echo.music.utils.dataStore
import iad1tya.echo.music.utils.potoken.AppContextHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object SpotifyAuthManager {
    private const val TAG = "SpotifyAuthManager"
    private const val TOKEN_URL = "https://open.spotify.com/api/token"
    private const val SERVER_TIME_URL = "https://open.spotify.com/api/server-time"
    private const val NUANCE_GIST_URL = "https://api.github.com/gists/22ed9c6ba463899e933427f7de1f0eef"
    private const val FALLBACK_SECRET = "GM3TMMJTGYZTQNZVGM4DINJZHA4TGOBYGMZTCMRTGEYDSMJRHE4TEOBUG4YTCMRUGQ4DQOJUGQYTAMRRGA2TCMJSHE3TCMBY"
    private const val FALLBACK_VER = 61
    private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .dns(CloudflareDnsResolver)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val context get() = AppContextHolder.appContext
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow<SpotifyUser?>(null)
    val currentUser: StateFlow<SpotifyUser?> = _currentUser.asStateFlow()

    init {
        scope.launch {
            loadSavedSession()
        }
    }

    private suspend fun loadSavedSession() {
        try {
            val prefs = context.dataStore.data.first()
            val spDc = prefs[SpotifySpDcKey]
            val userId = prefs[SpotifyUserIdKey]
            val userName = prefs[SpotifyUserNameKey]
            val userEmail = prefs[SpotifyUserEmailKey]
            val userAvatar = prefs[SpotifyUserAvatarKey]
            val userProduct = prefs[SpotifyUserProductKey]

            if (!spDc.isNullOrBlank()) {
                _isLoggedIn.value = true
                if (!userId.isNullOrBlank() && !userName.isNullOrBlank()) {
                    _currentUser.value = SpotifyUser(
                        id = userId,
                        displayName = userName,
                        email = userEmail,
                        avatarUrl = userAvatar,
                        product = userProduct
                    )
                }
                // Refresh profile in background
                val token = getValidAccessToken()
                if (token != null) {
                    SpotifyApiService.getMe(token)?.let { freshUser ->
                        updateSavedUser(freshUser)
                    }
                }
            } else {
                _isLoggedIn.value = false
                _currentUser.value = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading Spotify session: ${e.message}", e)
        }
    }

    /**
     * Saves a new session using the sp_dc (and optional sp_key) cookie obtained from login.
     */
    suspend fun saveSession(spDc: String, spKey: String = ""): Result<SpotifyUser> = withContext(Dispatchers.IO) {
        try {
            val tokenResult = requestTokenWithSpDc(spDc, spKey)
            if (tokenResult == null) {
                return@withContext Result.failure(Exception("Failed to obtain Spotify access token"))
            }

            val (accessToken, expirationTimestamp) = tokenResult
            val user = SpotifyApiService.getMe(accessToken)
                ?: return@withContext Result.failure(Exception("Failed to fetch Spotify user profile"))

            // Save to DataStore
            context.dataStore.edit { prefs ->
                prefs[SpotifySpDcKey] = spDc
                if (spKey.isNotBlank()) {
                    prefs[SpotifySpKeyKey] = spKey
                }
                prefs[SpotifyAccessTokenKey] = accessToken
                prefs[SpotifyTokenExpirationKey] = expirationTimestamp
                prefs[SpotifyUserIdKey] = user.id
                prefs[SpotifyUserNameKey] = user.displayName
                user.email?.let { prefs[SpotifyUserEmailKey] = it }
                user.avatarUrl?.let { prefs[SpotifyUserAvatarKey] = it }
                user.product?.let { prefs[SpotifyUserProductKey] = it }
            }

            _isLoggedIn.value = true
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "saveSession error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Returns a valid Spotify bearer access token.
     * Refreshes automatically if expired using sp_dc cookie with TOTP authentication.
     */
    suspend fun getValidAccessToken(): String? = withContext(Dispatchers.IO) {
        try {
            val prefs = context.dataStore.data.first()
            val spDc = prefs[SpotifySpDcKey] ?: return@withContext null
            val spKey = prefs[SpotifySpKeyKey] ?: ""
            val cachedToken = prefs[SpotifyAccessTokenKey]
            val expiration = prefs[SpotifyTokenExpirationKey] ?: 0L

            val now = System.currentTimeMillis()
            // If token is valid for at least another 60 seconds, reuse it
            if (!cachedToken.isNullOrBlank() && expiration > (now + 60_000L)) {
                return@withContext cachedToken
            }

            // Otherwise refresh token using sp_dc + TOTP
            val refreshed = requestTokenWithSpDc(spDc, spKey) ?: return@withContext null
            val (newToken, newExpiration) = refreshed

            context.dataStore.edit { editPrefs ->
                editPrefs[SpotifyAccessTokenKey] = newToken
                editPrefs[SpotifyTokenExpirationKey] = newExpiration
            }

            newToken
        } catch (e: Exception) {
            Log.e(TAG, "getValidAccessToken error: ${e.message}", e)
            null
        }
    }

    private fun fetchNuance(): Pair<String, Int> {
        return try {
            val request = Request.Builder()
                .url(NUANCE_GIST_URL)
                .header("User-Agent", "Mozilla/5.0")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return FALLBACK_SECRET to FALLBACK_VER
            val body = response.body?.string() ?: return FALLBACK_SECRET to FALLBACK_VER
            val json = gson.fromJson(body, JsonObject::class.java)
            val files = json.getAsJsonObject("files") ?: return FALLBACK_SECRET to FALLBACK_VER
            val firstFile = files.entrySet().firstOrNull()?.value?.asJsonObject ?: return FALLBACK_SECRET to FALLBACK_VER
            val content = firstFile.get("content")?.asString ?: return FALLBACK_SECRET to FALLBACK_VER
            val nuances = gson.fromJson(content, JsonArray::class.java)
            var bestSecret = FALLBACK_SECRET
            var bestVer = FALLBACK_VER
            for (elem in nuances) {
                val obj = elem.asJsonObject
                val v = obj.get("v")?.asInt ?: 0
                val s = obj.get("s")?.asString ?: ""
                if (v >= bestVer && s.isNotBlank()) {
                    bestVer = v
                    bestSecret = s
                }
            }
            bestSecret to bestVer
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch nuance from gist, using fallback: ${e.message}")
            FALLBACK_SECRET to FALLBACK_VER
        }
    }

    private fun fetchServerTime(): Long {
        return try {
            val request = Request.Builder()
                .url(SERVER_TIME_URL)
                .header("User-Agent", "Mozilla/5.0")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return System.currentTimeMillis() / 1000L
            val body = response.body?.string() ?: return System.currentTimeMillis() / 1000L
            val json = gson.fromJson(body, JsonObject::class.java)
            json.get("serverTime")?.asLong ?: (System.currentTimeMillis() / 1000L)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch Spotify server-time, using system clock: ${e.message}")
            System.currentTimeMillis() / 1000L
        }
    }

    private fun generateTotp(secret: String, serverTimeSec: Long): String {
        return try {
            val key = base32Decode(secret)
            val interval = 30L
            val timeStep = serverTimeSec / interval
            val timeBytes = ByteArray(8)
            var v = timeStep
            for (i in 7 downTo 0) {
                timeBytes[i] = (v and 0xFF).toByte()
                v = v shr 8
            }
            val mac = Mac.getInstance("HmacSHA1")
            mac.init(SecretKeySpec(key, "HmacSHA1"))
            val hash = mac.doFinal(timeBytes)
            val offset = (hash[hash.size - 1].toInt() and 0x0F)
            val code = ((hash[offset].toInt() and 0x7F) shl 24) or
                    ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                    ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                    (hash[offset + 3].toInt() and 0xFF)
            val otp = code % 1_000_000
            otp.toString().padStart(6, '0')
        } catch (e: Exception) {
            Log.e(TAG, "generateTotp error: ${e.message}", e)
            ""
        }
    }

    private fun base32Decode(input: String): ByteArray {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val cleaned = input.uppercase().replace("=", "")
        val output = mutableListOf<Byte>()
        var buffer = 0
        var bitsLeft = 0
        for (c in cleaned) {
            val value = alphabet.indexOf(c)
            if (value < 0) continue
            buffer = (buffer shl 5) or value
            bitsLeft += 5
            if (bitsLeft >= 8) {
                bitsLeft -= 8
                output.add(((buffer shr bitsLeft) and 0xFF).toByte())
            }
        }
        return output.toByteArray()
    }

    /**
     * Obtains an internal web player bearer access token using sp_dc cookie and TOTP.
     */
    private fun requestTokenWithSpDc(spDc: String, spKey: String = ""): Pair<String, Long>? {
        return try {
            val (secret, ver) = fetchNuance()
            val serverTime = fetchServerTime()
            val totp = generateTotp(secret, serverTime)

            val url = "$TOKEN_URL?reason=transport&productType=web-player&totp=$totp&totpServer=$totp&totpVer=$ver"
            val cookieHeader = buildString {
                append("sp_dc=$spDc")
                if (spKey.isNotBlank()) {
                    append("; sp_key=$spKey")
                }
            }

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Language", "en")
                .header("Cookie", cookieHeader)
                .build()

            val response = client.newCall(request).execute()
            val responseCode = response.code
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.w(TAG, "requestTokenWithSpDc failed: HTTP $responseCode - $body")
                return null
            }

            val json = gson.fromJson(body, JsonObject::class.java)
            val isAnonymous = json.get("isAnonymous")?.asBoolean ?: true
            if (isAnonymous) {
                Log.w(TAG, "Spotify returned anonymous token, sp_dc may be invalid or expired: $body")
                return null
            }

            val accessToken = json.get("accessToken")?.asString ?: return null
            val expiration = json.get("accessTokenExpirationTimestampMs")?.asLong
                ?: (System.currentTimeMillis() + 3600_000L)

            Log.i(TAG, "Successfully acquired Spotify bearer access token, expires at $expiration")
            accessToken to expiration
        } catch (e: Exception) {
            Log.e(TAG, "requestTokenWithSpDc exception: ${e.message}", e)
            null
        }
    }

    private suspend fun updateSavedUser(user: SpotifyUser) {
        _currentUser.value = user
        context.dataStore.edit { prefs ->
            prefs[SpotifyUserIdKey] = user.id
            prefs[SpotifyUserNameKey] = user.displayName
            user.email?.let { prefs[SpotifyUserEmailKey] = it }
            user.avatarUrl?.let { prefs[SpotifyUserAvatarKey] = it }
            user.product?.let { prefs[SpotifyUserProductKey] = it }
        }
    }

    /**
     * Clears all Spotify credentials and logs out.
     */
    suspend fun logout() = withContext(Dispatchers.IO) {
        try {
            context.dataStore.edit { prefs ->
                prefs.remove(SpotifySpDcKey)
                prefs.remove(SpotifySpKeyKey)
                prefs.remove(SpotifyAccessTokenKey)
                prefs.remove(SpotifyTokenExpirationKey)
                prefs.remove(SpotifyUserIdKey)
                prefs.remove(SpotifyUserNameKey)
                prefs.remove(SpotifyUserEmailKey)
                prefs.remove(SpotifyUserAvatarKey)
                prefs.remove(SpotifyUserProductKey)
            }
            _isLoggedIn.value = false
            _currentUser.value = null
        } catch (e: Exception) {
            Log.e(TAG, "logout error: ${e.message}", e)
        }
    }
}
