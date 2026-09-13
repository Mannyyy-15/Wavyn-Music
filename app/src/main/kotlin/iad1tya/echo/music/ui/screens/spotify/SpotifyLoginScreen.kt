package iad1tya.echo.music.ui.screens.spotify

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import iad1tya.echo.music.LocalPlayerAwareWindowInsets
import iad1tya.echo.music.R
import iad1tya.echo.music.spotify.SpotifyAuthManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SpotifyLoginScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var isAuthenticating by remember { mutableStateOf(false) }
    var showManualDialog by remember { mutableStateOf(false) }
    var manualSpDc by remember { mutableStateOf("") }

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
                            tint = MaterialTheme.colorScheme.primary
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
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.userAgentString =
                            "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
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

                            private fun checkCookies(url: String?) {
                                if (isAuthenticating) return
                                val targetUrl = url ?: "https://open.spotify.com"
                                val cookies = CookieManager.getInstance().getCookie(targetUrl) ?: ""
                                val spotifyCookies = CookieManager.getInstance().getCookie("https://open.spotify.com") ?: ""
                                val fullCookies = "$cookies; $spotifyCookies"

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

                        loadUrl("https://accounts.spotify.com/login?continue=https%3A%2F%2Fopen.spotify.com%2F")
                    }
                }
            )

            if (isLoading && !isAuthenticating) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }

            if (isAuthenticating) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
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
