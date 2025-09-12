package com.bytecoder.vplay.playlists

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<PlaylistItem>)

    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Query("SELECT * FROM Playlist ORDER BY name ASC")
    suspend fun getPlaylists(): List<Playlist>

    @Query("SELECT * FROM PlaylistItem WHERE playlistId = :playlistId ORDER BY orderInPlaylist ASC")
    suspend fun getItems(playlistId: Long): List<PlaylistItem>

    @Query("DELETE FROM PlaylistItem WHERE playlistId = :playlistId")
    suspend fun clearItems(playlistId: Long)

    @Transaction
    suspend fun replaceItems(playlistId: Long, items: List<PlaylistItem>) {
        clearItems(playlistId)
        insertItems(items)
    }
}
