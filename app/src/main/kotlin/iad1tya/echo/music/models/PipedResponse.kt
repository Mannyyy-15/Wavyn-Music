package iad1tya.echo.music.models

import kotlinx.serialization.Serializable

@Serializable
data class PipedResponse(
    val audioStreams: List<PipedAudioStream>? = null
)

@Serializable
data class PipedAudioStream(
    val url: String,
    val format: String,
    val quality: String,
    val mimeType: String,
    val codec: String? = null,
    val bitrate: Int,
    val contentLength: Long? = null
)
