package iad1tya.echo.music.spotify

import androidx.compose.runtime.Immutable
import java.io.Serializable

@Immutable
data class SpotifyUser(
    val id: String,
    val displayName: String,
    val email: String? = null,
    val avatarUrl: String? = null,
    val product: String? = null, // "free", "premium"
    val followersCount: Int = 0,
) : Serializable

@Immutable
data class SpotifyPlaylist(
    val id: String,
    val name: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val trackCount: Int = 0,
    val ownerName: String? = null,
) : Serializable

@Immutable
data class SpotifyTrack(
    val id: String,
    val title: String,
    val artists: List<String>,
    val albumName: String? = null,
    val albumArtUrl: String? = null,
    val durationMs: Long = 0,
    val isrc: String? = null,
) : Serializable {
    val artistString: String
        get() = artists.joinToString(", ")

    val durationSeconds: Int
        get() = (durationMs / 1000).toInt()
}

@Immutable
data class SpotifyArtist(
    val id: String,
    val name: String,
    val imageUrl: String? = null,
    val genres: List<String> = emptyList(),
) : Serializable

@Immutable
data class SpotifyGenre(
    val id: String,
    val name: String,
) : Serializable
