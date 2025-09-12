package com.bytecoder.vplay.playlists

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Playlist::class, PlaylistItem::class], version = 1)
abstract class PlaylistDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile private var INSTANCE: PlaylistDatabase? = null
        fun get(context: Context): PlaylistDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                PlaylistDatabase::class.java,
                "vplay_playlists.db"
            ).build().also { INSTANCE = it }
        }
    }
}
