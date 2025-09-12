package com.bytecoder.vplay.playlists

import android.content.Context

class PlaylistRepository(private val context: Context) {
    private val dao = PlaylistDatabase.get(context).playlistDao()

    suspend fun createPlaylist(name: String, description: String?, items: List<PlaylistItem>): Long {
        val id = dao.insertPlaylist(Playlist(name = name, description = description))
        if (items.isNotEmpty()) {
            val withIds = items.map { it.copy(playlistId = id) }
            dao.insertItems(withIds)
        }
        return id
    }

    suspend fun listPlaylists(): List<Playlist> = dao.getPlaylists()

    suspend fun getItems(playlistId: Long): List<PlaylistItem> = dao.getItems(playlistId)
}
