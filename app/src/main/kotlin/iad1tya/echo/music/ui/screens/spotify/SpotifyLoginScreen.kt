package iad1tya.echo.music.ui.screens.spotify

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.*
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import iad1tya.echo.music.LocalPlayerAwareWindowInsets
import iad1tya.echo.music.R
import iad1tya.echo.music.spotify.SpotifyAuthManager
import kotlinx.coroutines.launch

private const val SPOTIFY_ACCOUNTS_URL = "https://accounts.spotify.com/en/login?continue=https%3A%2F%2Fopen.spotify.com%2F"
private const val SPOTIFY_WEB_PLAYER_URL = "https://open.spotify.com/"

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SpotifyLoginScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var currentTargetUrl by remember { mutableStateOf(SPOTIFY_ACCOUNTS_URL) }
    var isLoading by remember { mutableStateOf(true) }
    var loadProgress by remember { mutableFloatStateOf(0.1f) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isAuthenticating by remember { mutableStateOf(false) }
    var showManualDialog by remember { mutableStateOf(false) }
    var manualSpDc by remember { mutableStateOf("") }

    BackHandler(enabled = webViewInstance?.canGoBack() == true) {
        webViewInstance?.goBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
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
                            text = "Log in to Spotify",
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
                    // Switch between Accounts page and Web Player
                    IconButton(
                        onClick = {
                            val nextUrl = if (currentTargetUrl == SPOTIFY_ACCOUNTS_URL) {
                                SPOTIFY_WEB_PLAYER_URL
                            } else {
                                SPOTIFY_ACCOUNTS_URL
                            }
                            currentTargetUrl = nextUrl
                            webViewInstance?.loadUrl(nextUrl)
                            Toast.makeText(
                                context,
                                if (nextUrl == SPOTIFY_WEB_PLAYER_URL) "Switched to Web Player" else "Switched to Login Form",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Rounded.Language),
                            contentDescription = "Toggle Login Mode"
                        )
                    }

                    // Refresh Button
                    IconButton(onClick = { webViewInstance?.reload() }) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Rounded.Refresh),
                            contentDescription = "Reload"
                        )
                    }

                    // Manual Cookie Entry
                    IconButton(onClick = { showManualDialog = true }) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Rounded.Key),
                            contentDescription = "Enter cookie manually"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF121212))
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        setBackgroundColor(android.graphics.Color.parseColor("#121212"))

                        // Enable 1st and 3rd-party cookies (essential for Spotify Next.js login)
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            javaScriptCanOpenWindowsAutomatically = true
                            setSupportMultipleWindows(false)
                            cacheMode = WebSettings.LOAD_DEFAULT

                            // Clean User Agent: remove 'Version/4.0' and '; wv' so Spotify recognizes this as genuine Chrome
                            val defaultUa = userAgentString
                            userAgentString = defaultUa
                                .replace("; wv", "")
                                .replace("Version/4.0 ", "")
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                loadProgress = (newProgress / 100f).coerceIn(0.1f, 1f)
                                if (newProgress >= 100) {
                                    isLoading = false
                                }
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                                errorMessage = null
                                checkCookies(url)
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                checkCookies(url)
                            }

                            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                                super.doUpdateVisitedHistory(view, url, isReload)
                                checkCookies(url)
                            }

                            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                                handler?.proceed()
                            }

                            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    isLoading = false
                                    errorMessage = error?.description?.toString() ?: "Failed to connect to Spotify"
                                }
                            }

                            private fun checkCookies(url: String?) {
                                if (isAuthenticating) return
                                val targetUrl = url ?: "https://open.spotify.com"
                                val cookies = CookieManager.getInstance().getCookie(targetUrl) ?: ""
                                val spotifyCookies = CookieManager.getInstance().getCookie("https://open.spotify.com") ?: ""
                                val accountsCookies = CookieManager.getInstance().getCookie("https://accounts.spotify.com") ?: ""
                                val fullCookies = "$cookies; $spotifyCookies; $accountsCookies"

                                if ("sp_dc=" in fullCookies) {
                                    val spDc = extractCookieValue(fullCookies, "sp_dc")
                                    if (!spDc.isNullOrBlank()) {
                                        isAuthenticating = true
                                        coroutineScope.launch {
                                            val result = SpotifyAuthManager.saveSession(spDc)
                                            result.onSuccess { user ->
                                                Toast.makeText(
                                                    context,
                                                    "Connected as ${user.displayName}!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                navController.navigate("spotify_hub") {
                                                    popUpTo("spotify_login") { inclusive = true }
                                                }
                                            }.onFailure { err ->
                                                isAuthenticating = false
                                                Toast.makeText(
                                                    context,
                                                    "Failed to connect: ${err.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        loadUrl(currentTargetUrl)
                        webViewInstance = this
                    }
                }
            )

            // Progress bar
            if (isLoading && !isAuthenticating) {
                LinearProgressIndicator(
                    progress = { loadProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = Color(0xFF1DB954)
                )
            }

            // Error Card
            if (errorMessage != null && !isAuthenticating) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .align(Alignment.Center),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Connection Issue",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = errorMessage ?: "Could not reach Spotify servers. Please check your internet connection.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    errorMessage = null
                                    isLoading = true
                                    webViewInstance?.loadUrl(SPOTIFY_ACCOUNTS_URL)
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Retry Login")
                            }
                            OutlinedButton(
                                onClick = {
                                    errorMessage = null
                                    isLoading = true
                                    currentTargetUrl = SPOTIFY_WEB_PLAYER_URL
                                    webViewInstance?.loadUrl(SPOTIFY_WEB_PLAYER_URL)
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Web Player")
                            }
                        }
                    }
                }
            }

            // Authenticating Overlay
            if (isAuthenticating) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF1DB954),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Syncing your Spotify account...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    if (showManualDialog) {
        AlertDialog(
            onDismissRequest = { showManualDialog = false },
            title = { Text("Manual sp_dc Cookie") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "If you already have your Spotify 'sp_dc' cookie from your browser, paste it below:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = manualSpDc,
                        onValueChange = { manualSpDc = it },
                        label = { Text("sp_dc Cookie") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cookie = manualSpDc.trim()
                        if (cookie.isNotBlank()) {
                            showManualDialog = false
                            isAuthenticating = true
                            coroutineScope.launch {
                                val result = SpotifyAuthManager.saveSession(cookie)
                                result.onSuccess { user ->
                                    Toast.makeText(
                                        context,
                                        "Connected as ${user.displayName}!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.navigate("spotify_hub") {
                                        popUpTo("spotify_login") { inclusive = true }
                                    }
                                }.onFailure { err ->
                                    isAuthenticating = false
                                    Toast.makeText(
                                        context,
                                        "Failed: ${err.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                ) {
                    Text("Connect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun extractCookieValue(cookieHeader: String, cookieName: String): String? {
    val prefix = "$cookieName="
    for (part in cookieHeader.split(";")) {
        val trimmed = part.trim()
        if (trimmed.startsWith(prefix)) {
            return trimmed.substring(prefix.length)
        }
    }
    return null
}
