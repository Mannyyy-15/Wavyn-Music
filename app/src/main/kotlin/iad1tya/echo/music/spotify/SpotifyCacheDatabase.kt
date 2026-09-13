package iad1tya.echo.music.spotify

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import iad1tya.echo.music.utils.potoken.AppContextHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SpotifyCacheDatabase(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                spotify_id TEXT PRIMARY KEY,
                video_id TEXT NOT NULL,
                title TEXT,
                artist TEXT,
                duration INTEGER,
                updated_at INTEGER
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_spotify_id ON $TABLE_NAME(spotify_id)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    companion object {
        private const val DB_NAME = "spotify_cache.db"
        private const val DB_VERSION = 1
        private const val TABLE_NAME = "spotify_song_map"

        @Volatile
        private var instance: SpotifyCacheDatabase? = null

        fun getInstance(): SpotifyCacheDatabase {
            return instance ?: synchronized(this) {
                instance ?: SpotifyCacheDatabase(AppContextHolder.appContext).also { instance = it }
            }
        }
    }

    suspend fun getMappedVideoId(spotifyId: String): String? = withContext(Dispatchers.IO) {
        try {
            val db = readableDatabase
            val cursor = db.query(
                TABLE_NAME,
                arrayOf("video_id"),
                "spotify_id = ?",
                arrayOf(spotifyId),
                null,
                null,
                null
            )
            cursor.use {
                if (it.moveToFirst()) {
                    return@withContext it.getString(0)
                }
            }
        } catch (_: Exception) {}
        null
    }

    suspend fun saveMapping(
        spotifyId: String,
        videoId: String,
        title: String? = null,
        artist: String? = null,
        duration: Int = 0
    ) = withContext(Dispatchers.IO) {
        try {
            val db = writableDatabase
            val values = ContentValues().apply {
                put("spotify_id", spotifyId)
                put("video_id", videoId)
                put("title", title)
                put("artist", artist)
                put("duration", duration)
                put("updated_at", System.currentTimeMillis())
            }
            db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        } catch (_: Exception) {}
    }
}
