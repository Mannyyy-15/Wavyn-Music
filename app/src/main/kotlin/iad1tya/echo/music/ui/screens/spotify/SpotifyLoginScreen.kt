package iad1tya.echo.music.ui.screens.spotify

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.webkit.*
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Key
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import iad1tya.echo.music.LocalPlayerAwareWindowInsets
import iad1tya.echo.music.R
import iad1tya.echo.music.spotify.SpotifyAuthManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

private const val SPOTIFY_LOGIN_URL = "https://accounts.spotify.com/en/login"
private const val USER_AGENT_DESKTOP =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SpotifyLoginScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadProgress by remember { mutableFloatStateOf(0.1f) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isAuthenticating by remember { mutableStateOf(false) }
    var showManualDialog by remember { mutableStateOf(false) }
    var manualSpDc by remember { mutableStateOf("") }

    val tokenFetchStarted = remember { AtomicBoolean(false) }

    BackHandler(enabled = webViewInstance?.canGoBack() == true) {
        webViewInstance?.goBack()
    }

    // Enable WebView debugging for remote diagnostics
    LaunchedEffect(Unit) {
        try {
            WebView.setWebContentsDebuggingEnabled(true)
        } catch (_: Exception) {}
    }

    // Continuous cookie poller across Spotify domains
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            if (tokenFetchStarted.get()) continue
            val spDc = extractCookie("sp_dc")
            if (!spDc.isNullOrBlank() && tokenFetchStarted.compareAndSet(false, true)) {
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
                        tokenFetchStarted.set(false)
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
                    // Open in External Browser (Chrome / Firefox / Brave)
                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(SPOTIFY_LOGIN_URL))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open browser: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.OpenInNew),
                            contentDescription = "Open in external browser"
                        )
                    }

                    // Refresh Button
                    IconButton(
                        onClick = {
                            errorMessage = null
                            isLoading = true
                            webViewInstance?.reload()
                        }
                    ) {
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
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)

                    WebView(ctx).apply {
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        webViewInstance = this
                        cookieManager.setAcceptThirdPartyCookies(this, true)
                        setBackgroundColor(android.graphics.Color.parseColor("#121212"))

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            javaScriptCanOpenWindowsAutomatically = true
                            @Suppress("DEPRECATION")
                            setSupportMultipleWindows(false)
                            cacheMode = WebSettings.LOAD_DEFAULT
                            userAgentString = USER_AGENT_DESKTOP
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
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
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

                            @Suppress("OVERRIDE_DEPRECATION")
                            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                return false
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                return false
                            }
                        }

                        loadUrl(SPOTIFY_LOGIN_URL)
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
                                    webViewInstance?.loadUrl(SPOTIFY_LOGIN_URL)
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Retry Login")
                            }
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(SPOTIFY_LOGIN_URL))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Open in Browser")
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
            title = {
                Text(
                    "Manual Spotify Login",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "You can log in to Spotify on your browser and paste your 'sp_dc' cookie below, or use it directly.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(SPOTIFY_LOGIN_URL))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open Spotify in Web Browser")
                    }

                    OutlinedTextField(
                        value = manualSpDc,
                        onValueChange = { manualSpDc = it },
                        label = { Text("sp_dc Cookie Value") },
                        placeholder = { Text("AQB...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cookie = manualSpDc.trim()
                        if (cookie.isNotBlank()) {
                            showManualDialog = false
                            isAuthenticating = true
                            tokenFetchStarted.set(true)
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
                                    tokenFetchStarted.set(false)
                                    Toast.makeText(
                                        context,
                                        "Failed: ${err.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    },
                    enabled = manualSpDc.isNotBlank()
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

/**
 * Searches the cookie jar across all Spotify domains for a cookie by [name].
 */
private fun extractCookie(name: String): String? {
    val cookieManager = CookieManager.getInstance()
    val domains = listOf(
        "https://open.spotify.com",
        "https://accounts.spotify.com",
        "https://spotify.com",
    )
    for (domain in domains) {
        val allCookies = cookieManager.getCookie(domain) ?: continue
        val match = allCookies.split(";")
            .mapNotNull {
                val parts = it.trim().split("=", limit = 2)
                if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
            }
            .firstOrNull { it.first == name && it.second.isNotBlank() }
            ?.second
        if (!match.isNullOrBlank()) {
            return match
        }
    }
    return null
}
