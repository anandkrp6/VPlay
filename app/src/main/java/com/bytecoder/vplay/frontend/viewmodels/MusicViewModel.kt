package com.bytecoder.vplay.frontend.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.bytecoder.vplay.backend.data.models.MediaItemModel

class MusicViewModel : ViewModel() {
    private val _songs = MutableStateFlow<List<MediaItemModel>>(emptyList())
    val songs: StateFlow<List<MediaItemModel>> = _songs.asStateFlow()
    
    private val _albums = MutableStateFlow<List<AlbumModel>>(emptyList())
    val albums: StateFlow<List<AlbumModel>> = _albums.asStateFlow()
    
    private val _artists = MutableStateFlow<List<ArtistModel>>(emptyList())
    val artists: StateFlow<List<ArtistModel>> = _artists.asStateFlow()
    
    private val _playlists = MutableStateFlow<List<PlaylistModel>>(emptyList())
    val playlists: StateFlow<List<PlaylistModel>> = _playlists.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    fun loadMusic() {
        _isLoading.value = true
        // Sample data for development
        _songs.value = generateSampleSongs()
        _albums.value = generateSampleAlbums()
        _artists.value = generateSampleArtists()
        _playlists.value = generateSamplePlaylists()
        _isLoading.value = false
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun sortSongs(sortType: SortType) {
        val currentSongs = _songs.value
        _songs.value = when (sortType) {
            SortType.TITLE -> currentSongs.sortedBy { it.title }
            SortType.ARTIST -> currentSongs.sortedBy { it.subtitle }
            SortType.DURATION -> currentSongs.sortedBy { it.durationMs }
            SortType.DATE_ADDED -> currentSongs.sortedBy { it.id }
        }
    }
    
    private fun generateSampleSongs(): List<MediaItemModel> {
        return listOf(
            MediaItemModel("1", "Sample Song 1", "Artist 1", "", false, 180000, ""),
            MediaItemModel("2", "Sample Song 2", "Artist 2", "", false, 210000, ""),
            MediaItemModel("3", "Sample Song 3", "Artist 1", "", false, 195000, "")
        )
    }
    
    private fun generateSampleAlbums(): List<AlbumModel> {
        return listOf(
            AlbumModel("1", "Sample Album 1", "Artist 1", "", 12, 2400000),
            AlbumModel("2", "Sample Album 2", "Artist 2", "", 8, 1800000)
        )
    }
    
    private fun generateSampleArtists(): List<ArtistModel> {
        return listOf(
            ArtistModel("1", "Artist 1", "", 25, 2),
            ArtistModel("2", "Artist 2", "", 15, 1)
        )
    }
    
    private fun generateSamplePlaylists(): List<PlaylistModel> {
        return listOf(
            PlaylistModel("1", "Favorites", "My favorite songs", 10, 2100000),
            PlaylistModel("2", "Workout", "High energy tracks", 20, 4200000)
        )
    }
}

data class AlbumModel(
    val id: String,
    val name: String,
    val artist: String,
    val artworkPath: String = "",
    val trackCount: Int = 0,
    val duration: Long = 0L
)

data class ArtistModel(
    val id: String,
    val name: String,
    val imagePath: String = "",
    val trackCount: Int = 0,
    val albumCount: Int = 0
)

enum class SortType {
    TITLE, ARTIST, DURATION, DATE_ADDED
}