package iad1tya.echo.music.ui.screens.settings

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.app.NotificationCompat
import androidx.navigation.NavController
import iad1tya.echo.music.BuildConfig
import iad1tya.echo.music.LocalPlayerAwareWindowInsets
import iad1tya.echo.music.MainActivity
import iad1tya.echo.music.R
import iad1tya.echo.music.constants.CheckForUpdatesKey
import iad1tya.echo.music.ui.component.IconButton
import iad1tya.echo.music.ui.component.SwitchPreference
import iad1tya.echo.music.ui.utils.backToMain
import iad1tya.echo.music.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdaterScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val (checkForUpdates, onCheckForUpdatesChange) = rememberPreference(CheckForUpdatesKey, true)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }
    var availableVersion by remember { mutableStateOf<String?>(null) }
    var downloadUrl by remember { mutableStateOf<String?>(null) }
    var releaseNotes by remember { mutableStateOf<List<String>>(emptyList()) }
    var downloadProgress by remember { mutableStateOf<Float?>(null) }
    var downloadedApkUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var isDownloading by remember { mutableStateOf(false) }

    fun installApk(file: java.io.File) {
        if (!file.exists() || file.length() < 1024 * 1024) {
            Toast.makeText(context, "APK file is invalid or incomplete. Please re-download.", Toast.LENGTH_LONG).show()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                Toast.makeText(context, "Please allow 'Install unknown apps' permission to install the update", Toast.LENGTH_LONG).show()
                val permissionIntent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = android.net.Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(permissionIntent)
                return
            }
        }

        val apkUri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.FileProvider",
            file
        )
        downloadedApkUri = apkUri

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val resInfoList = context.packageManager.queryIntentActivities(installIntent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            context.grantUriPermission(resolveInfo.activityInfo.packageName, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(installIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open package installer: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun downloadAndInstallApk(apkUrl: String) {
        isDownloading = true
        downloadProgress = 0f
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(10, java.util.concurrent.TimeUnit.MINUTES)
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .build()
                val request = okhttp3.Request.Builder()
                    .url(apkUrl)
                    .header("User-Agent", "Wavyn-Music-App")
                    .build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw java.io.IOException("HTTP download error: ${response.code}")
                }
                val body = response.body ?: throw java.io.IOException("Empty response body")
                val contentLength = body.contentLength()
                val inputStream = body.byteStream()
                
                val downloadDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
                val apkFile = java.io.File(downloadDir, "Wavyn-Music-update.apk")
                val tempFile = java.io.File(downloadDir, "Wavyn-Music-update.apk.tmp")
                if (tempFile.exists()) tempFile.delete()
                if (apkFile.exists()) apkFile.delete()
                
                val outputStream = java.io.FileOutputStream(tempFile)
                val buffer = ByteArray(32768)
                var bytesRead: Int
                var totalBytesRead = 0L
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    if (contentLength > 0) {
                        withContext(Dispatchers.Main) {
                            downloadProgress = (totalBytesRead.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f)
                        }
                    }
                }
                outputStream.flush()
                outputStream.close()
                inputStream.close()

                if (tempFile.length() < 1024 * 1024) {
                    tempFile.delete()
                    throw java.io.IOException("Download incomplete (only ${tempFile.length()} bytes)")
                }

                tempFile.renameTo(apkFile)

                withContext(Dispatchers.Main) {
                    isDownloading = false
                    installApk(apkFile)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isDownloading = false
                    downloadProgress = null
                    Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun checkForUpdateManually() {
        isChecking = true
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val url = java.net.URL("https://api.github.com/repos/Mannyyy-15/Wavyn-Music/releases/latest")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("User-Agent", "Wavyn-Music-App")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                val responseCode = connection.responseCode
                if (responseCode == 404 || responseCode != 200) {
                    connection.disconnect()
                    withContext(Dispatchers.Main) {
                        isChecking = false
                        Toast.makeText(context, "You're on the latest version (v${BuildConfig.VERSION_NAME})", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                
                val json = org.json.JSONObject(responseText)
                val tagName = json.getString("tag_name")
                val latestVersion = tagName.removePrefix("v")
                val body = json.optString("body", "")
                val assets = json.optJSONArray("assets")
                var apkDownloadUrl: String? = null
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkDownloadUrl = asset.optString("browser_download_url")
                            break
                        }
                    }
                }
                
                val notes = body.split("\n")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && !it.startsWith("#") }
                    .map { line ->
                        line.replace(Regex("^[*-]\\s+"), "")
                            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
                            .replace(Regex("\\*(.*?)\\*"), "$1")
                            .replace(Regex("`(.*?)`"), "$1")
                    }
                    .filter { it.isNotEmpty() }
                
                withContext(Dispatchers.Main) {
                    isChecking = false
                    val isNewer = try {
                        val currentParts = BuildConfig.VERSION_NAME.split(".").map { Regex("\\d+").find(it)?.value?.toIntOrNull() ?: 0 }
                        val latestParts = latestVersion.split(".").map { Regex("\\d+").find(it)?.value?.toIntOrNull() ?: 0 }
                        val length = maxOf(currentParts.size, latestParts.size)
                        var newer = false
                        for (i in 0 until length) {
                            val c = currentParts.getOrElse(i) { 0 }
                            val l = latestParts.getOrElse(i) { 0 }
                            if (l > c) { newer = true; break }
                            if (l < c) { newer = false; break }
                        }
                        newer
                    } catch (e: Exception) {
                        false
                    }

                    if (isNewer) {
                        availableVersion = latestVersion
                        downloadUrl = apkDownloadUrl
                        releaseNotes = notes
                        showUpdateNotification(context, latestVersion)
                        Toast.makeText(context, "New version $latestVersion available!", Toast.LENGTH_LONG).show()
                    } else {
                        availableVersion = null
                        Toast.makeText(context, "You're on the latest version (v${BuildConfig.VERSION_NAME})", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isChecking = false
                    Toast.makeText(context, "You're on the latest version (v${BuildConfig.VERSION_NAME})", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        checkForUpdateManually()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                )
            )
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(
            Modifier.windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(
                    WindowInsetsSides.Top
                )
            )
        )

        Spacer(Modifier.height(4.dp))

        // In-App Update Card (when update is available)
        if (availableVersion != null && availableVersion != BuildConfig.VERSION_NAME) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.update),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "New Version Available",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Version v$availableVersion (Current: v${BuildConfig.VERSION_NAME})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (releaseNotes.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "What's New:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                        releaseNotes.take(5).forEach { note ->
                            Text(
                                text = "• $note",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }

                    if (isDownloading && downloadProgress != null) {
                        Spacer(Modifier.height(14.dp))
                        androidx.compose.material3.LinearProgressIndicator(
                            progress = { downloadProgress!! },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Downloading update: ${(downloadProgress!! * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val downloadDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
                            val apkFile = java.io.File(downloadDir, "Wavyn-Music-update.apk")
                            if (apkFile.exists() && apkFile.length() > 1024 * 1024) {
                                installApk(apkFile)
                            } else if (!isDownloading && downloadUrl != null) {
                                downloadAndInstallApk(downloadUrl!!)
                            }
                        },
                        enabled = !isDownloading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = when {
                                isDownloading -> "Downloading... ${(downloadProgress?.let { (it * 100).toInt() } ?: 0)}%"
                                downloadedApkUri != null -> "Install Update Now"
                                else -> "Download & Install Update"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (downloadUrl != null) {
                        Spacer(Modifier.height(8.dp))
                        androidx.compose.material3.OutlinedButton(
                            onClick = {
                                val browserIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(downloadUrl)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(browserIntent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Download via Browser (Direct)")
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            SwitchPreference(
                title = { Text(stringResource(R.string.check_for_updates)) },
                icon = { Icon(painterResource(R.drawable.update), null) },
                checked = checkForUpdates,
                onCheckedChange = onCheckForUpdatesChange,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable(enabled = !isChecking && !isDownloading) { checkForUpdateManually() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(12.dp))
                }
                Icon(
                    painter = painterResource(R.drawable.update),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (isChecking) "Checking for updates..." else "Check for updates now",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }

    Box {
        // Blurred gradient background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .zIndex(10f)
                .then(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.graphicsLayer {
                            renderEffect = android.graphics.RenderEffect.createBlurEffect(
                                25f,
                                25f,
                                android.graphics.Shader.TileMode.CLAMP
                            ).asComposeRenderEffect()
                        }
                    } else {
                        Modifier
                    }
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.88f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        TopAppBar(
            title = { 
                Text(
                    text = stringResource(R.string.updater),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily(Font(R.font.zalando_sans_expanded)),
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = navController::navigateUp,
                    onLongClick = navController::backToMain,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back),
                        contentDescription = null,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
            ),
            modifier = Modifier.zIndex(11f)
        )
    }
}

private fun showUpdateNotification(context: Context, version: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    // Create notification channel for Android O and above
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "updates",
            "App Updates",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for app updates"
            enableLights(true)
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }
    
    // Create intent to open MainActivity with settings
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra("openSettings", true)
    }
    
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    
    // Build notification
    val notification = NotificationCompat.Builder(context, "updates")
        .setSmallIcon(R.drawable.update)
        .setContentTitle("Wavyn Music Update Available")
        .setContentText("Version $version is now available")
        .setStyle(NotificationCompat.BigTextStyle()
            .bigText("A new version ($version) of Wavyn Music is available. Tap to download and install."))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()
    
    notificationManager.notify(1001, notification)
}
