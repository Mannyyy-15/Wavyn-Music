package iad1tya.echo.music.utils

import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

data class ErrorLogEntry(
    val id: Long = System.currentTimeMillis() + (0..999).random(),
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String,
    val message: String,
    val errorDetails: String? = null,
    val stackTrace: String? = null,
    val metadata: Map<String, String> = emptyMap()
) {
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp))
}

object ErrorLogger {
    private const val MAX_LOGS = 100
    private val logsList = CopyOnWriteArrayList<ErrorLogEntry>()
    private val _logsFlow = MutableStateFlow<List<ErrorLogEntry>>(emptyList())
    val logsFlow: StateFlow<List<ErrorLogEntry>> = _logsFlow.asStateFlow()

    fun logError(
        tag: String,
        message: String,
        throwable: Throwable? = null,
        metadata: Map<String, String> = emptyMap()
    ) {
        val stackTrace = throwable?.let {
            val sw = StringWriter()
            it.printStackTrace(PrintWriter(sw))
            sw.toString()
        }

        val entry = ErrorLogEntry(
            tag = tag,
            message = message,
            errorDetails = throwable?.localizedMessage ?: throwable?.message,
            stackTrace = stackTrace,
            metadata = metadata
        )

        logsList.add(0, entry)
        while (logsList.size > MAX_LOGS) {
            logsList.removeAt(logsList.size - 1)
        }
        _logsFlow.value = logsList.toList()
    }

    fun clearLogs() {
        logsList.clear()
        _logsFlow.value = emptyList()
    }

    fun getFormattedLogs(appVersion: String = "4.2.4"): String {
        val sb = StringBuilder()
        sb.append("=== WAVYN MUSIC ERROR & DIAGNOSTIC LOG ===\n")
        sb.append("Generated At: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
        sb.append("App Version: $appVersion\n")
        sb.append("Device: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT})\n")
        sb.append("Device Board/Hardware: ${Build.BOARD} / ${Build.HARDWARE}\n")
        sb.append("Total Recorded Errors: ${logsList.size}\n")
        sb.append("===========================================\n\n")

        if (logsList.isEmpty()) {
            sb.append("No errors recorded in current session.\n")
            return sb.toString()
        }

        logsList.forEachIndexed { index, entry ->
            sb.append("[$index] ${entry.formattedTime} [${entry.tag}]\n")
            sb.append("Message: ${entry.message}\n")
            if (!entry.errorDetails.isNullOrBlank()) {
                sb.append("Details: ${entry.errorDetails}\n")
            }
            if (entry.metadata.isNotEmpty()) {
                sb.append("Metadata:\n")
                entry.metadata.forEach { (k, v) ->
                    sb.append("  - $k: $v\n")
                }
            }
            if (!entry.stackTrace.isNullOrBlank()) {
                sb.append("Stack Trace:\n${entry.stackTrace.trim()}\n")
            }
            sb.append("-------------------------------------------\n\n")
        }

        return sb.toString()
    }
}
