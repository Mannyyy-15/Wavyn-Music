package iad1tya.echo.music.spotify

import android.util.Log
import androidx.datastore.preferences.core.edit
import com.echo.innertube.CloudflareDnsResolver
import com.google.gson.Gson
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

object SpotifyAuthManager {
    private const val TAG = "SpotifyAuthManager"
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
     * Saves a new session using the sp_dc cookie obtained from login.
     */
    suspend fun saveSession(spDc: String): Result<SpotifyUser> = withContext(Dispatchers.IO) {
        try {
            val tokenResult = requestTokenWithSpDc(spDc)
            if (tokenResult == null) {
                return@withContext Result.failure(Exception("Failed to obtain Spotify access token"))
            }

            val (accessToken, expirationTimestamp) = tokenResult
            val user = SpotifyApiService.getMe(accessToken)
                ?: return@withContext Result.failure(Exception("Failed to fetch Spotify user profile"))

            // Save to DataStore
            context.dataStore.edit { prefs ->
                prefs[SpotifySpDcKey] = spDc
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
     * Refreshes automatically if expired using sp_dc cookie.
     */
    suspend fun getValidAccessToken(): String? = withContext(Dispatchers.IO) {
        try {
            val prefs = context.dataStore.data.first()
            val spDc = prefs[SpotifySpDcKey] ?: return@withContext null
            val cachedToken = prefs[SpotifyAccessTokenKey]
            val expiration = prefs[SpotifyTokenExpirationKey] ?: 0L

            val now = System.currentTimeMillis()
            // If token is valid for at least another 60 seconds, reuse it
            if (!cachedToken.isNullOrBlank() && expiration > (now + 60_000L)) {
                return@withContext cachedToken
            }

            // Otherwise refresh token using sp_dc
            val refreshed = requestTokenWithSpDc(spDc) ?: return@withContext null
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

    private fun requestTokenWithSpDc(spDc: String): Pair<String, Long>? {
        return try {
            val url = "https://open.spotify.com/get_access_token?reason=transport&productType=web_player"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Cookie", "sp_dc=$spDc")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "requestTokenWithSpDc failed: HTTP ${response.code}")
                return null
            }
            val body = response.body?.string() ?: return null
            val json = gson.fromJson(body, JsonObject::class.java)

            val isAnonymous = json.get("isAnonymous")?.asBoolean ?: true
            if (isAnonymous) {
                Log.w(TAG, "Spotify returned anonymous token, sp_dc may be invalid or expired")
                return null
            }

            val accessToken = json.get("accessToken")?.asString ?: return null
            val expiration = json.get("accessTokenExpirationTimestampMs")?.asLong
                ?: (System.currentTimeMillis() + 3600_000L)

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
