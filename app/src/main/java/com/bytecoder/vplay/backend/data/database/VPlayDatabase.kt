package com.bytecoder.vplay.backend.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.bytecoder.vplay.backend.managers.*

@Database(
    entities = [
        WatchHistoryEntry::class,
        Playlist::class,
        PlaylistItem::class,
        DownloadEntry::class,
        AnalyticsEntry::class,
        BookmarkEntry::class,
        FileMetadata::class,
        MusicTrack::class,
        MusicAlbum::class,
        MusicArtist::class,
        MusicGenre::class,
        MusicPlaylist::class,
        MusicPlaylistTrack::class
    ],
    version = 2,
    exportSchema = false
)
abstract class VPlayDatabase : RoomDatabase() {
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun downloadDao(): DownloadDao
    abstract fun analyticsDao(): AnalyticsDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun fileMetadataDao(): FileMetadataDao
    
    // Music Library DAOs
    abstract fun musicTrackDao(): MusicTrackDao
    abstract fun musicAlbumDao(): MusicAlbumDao
    abstract fun musicArtistDao(): MusicArtistDao
    abstract fun musicGenreDao(): MusicGenreDao
    abstract fun musicPlaylistDao(): MusicPlaylistDao
    abstract fun musicPlaylistTrackDao(): MusicPlaylistTrackDao

    companion object {
        @Volatile
        private var INSTANCE: VPlayDatabase? = null

        fun getDatabase(context: Context): VPlayDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VPlayDatabase::class.java,
                    "vplay_database"
                ).fallbackToDestructiveMigration() // For now, allows schema changes
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}