package com.bytecoder.vplay.backend.managers

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecoder.vplay.backend.managers.MusicLibraryManager
import com.bytecoder.vplay.backend.managers.MusicTrack
import com.bytecoder.vplay.backend.managers.MusicAlbum
import com.bytecoder.vplay.backend.managers.MusicArtist
import com.bytecoder.vplay.backend.managers.MusicPlaylist
import com.bytecoder.vplay.backend.utils.MediaStoreObserver
import com.bytecoder.vplay.backend.controllers.PlayerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MusicLibraryScreenManager : ViewModel() {
    private var musicLibraryManager: MusicLibraryManager? = null
    private var mediaObserver: MediaStoreObserver? = null
    private val playerController = PlayerController.getInstance()
    
    private val _allTracks = MutableStateFlow<List<MusicTrack>>(emptyList())
    private val _tracks = MutableStateFlow<List<MusicTrack>>(emptyList())
    val tracks: StateFlow<List<MusicTrack>> = _tracks.asStateFlow()
    
    private val _albums = MutableStateFlow<List<MusicAlbum>>(emptyList())
    val albums: StateFlow<List<MusicAlbum>> = _albums.asStateFlow()
    
    private val _artists = MutableStateFlow<List<MusicArtist>>(emptyList())
    val artists: StateFlow<List<MusicArtist>> = _artists.asStateFlow()
    
    private val _playlists = MutableStateFlow<List<MusicPlaylist>>(emptyList())
    val playlists: StateFlow<List<MusicPlaylist>> = _playlists.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    var searchQuery by mutableStateOf("")
        private set
        
    var currentSortOption by mutableStateOf("title")
        private set
    
    fun initialize(context: Context) {
        if (musicLibraryManager == null) {
            musicLibraryManager = MusicLibraryManager(context)
            mediaObserver = MediaStoreObserver(context) { mediaType ->
                if (mediaType == MediaStoreObserver.MediaType.AUDIO || 
                    mediaType == MediaStoreObserver.MediaType.BOTH) {
                    refreshLibrary()
                }
            }
            mediaObserver?.startObserving()
            refreshLibrary()
        }
    }
    
    fun refreshLibrary() {
        val manager = musicLibraryManager ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Load all library data in parallel
                val tracksDeferred = async { manager.getAllTracks() }
                val albumsDeferred = async { manager.getAllAlbums() }
                val artistsDeferred = async { manager.getAllArtists() }
                val playlistsDeferred = async { manager.getAllPlaylists() }
                
                val loadedTracks = tracksDeferred.await()
                _allTracks.value = loadedTracks
                filterAndSortTracks() // Apply current search and sort
                _albums.value = albumsDeferred.await()
                _artists.value = artistsDeferred.await()
                _playlists.value = playlistsDeferred.await()
            } catch (e: Exception) {
                _error.value = "Failed to load music library: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Play a single track - delegates to PlayerController
     */
    fun playTrack(track: MusicTrack) {
        playerController.playTrack(track)
    }
    
    /**
     * Play all tracks starting from specified track
     */
    fun playTrackList(tracks: List<MusicTrack>, startIndex: Int = 0) {
        if (tracks.isNotEmpty()) {
            val mediaFiles = tracks.map { track ->
                com.bytecoder.vplay.backend.models.MediaFile(
                    id = track.id,
                    title = track.title,
                    uri = track.uri,
                    isVideo = false,
                    artist = track.artist,
                    album = track.album,
                    duration = track.duration
                )
            }
            playerController.setQueue(mediaFiles, startIndex)
            playerController.play()
        }
    }
    
    /**
     * Add track to current queue
     */
    fun addTrackToQueue(track: MusicTrack) {
        val mediaFile = com.bytecoder.vplay.backend.models.MediaFile(
            id = track.id,
            title = track.title,
            uri = track.uri,
            isVideo = false,
            artist = track.artist,
            album = track.album,
            duration = track.duration
        )
        playerController.addToQueue(mediaFile)
    }
    
    fun toggleFavorite(trackId: String) {
        // TODO: Implement when MusicLibraryManager has this method
        _error.value = "Favorite toggle not yet implemented"
    }
    
    fun createPlaylist(name: String, description: String = "") {
        val manager = musicLibraryManager ?: return
        
        viewModelScope.launch {
            try {
                val playlistId = manager.createPlaylist(name, description)
                if (playlistId.isNotEmpty()) {
                    // Refresh playlists
                    _playlists.value = manager.getAllPlaylists()
                }
            } catch (e: Exception) {
                _error.value = "Failed to create playlist: ${e.message}"
            }
        }
    }
    
    fun addTrackToPlaylist(playlistId: String, trackId: String) {
        val manager = musicLibraryManager ?: return
        
        viewModelScope.launch {
            try {
                manager.addTrackToPlaylist(playlistId, trackId)
                // Refresh playlists to show updated track counts
                _playlists.value = manager.getAllPlaylists()
            } catch (e: Exception) {
                _error.value = "Failed to add track to playlist: ${e.message}"
            }
        }
    }
    
    fun getTracksForAlbum(albumId: String): List<MusicTrack> {
        return _tracks.value.filter { it.album == albumId }
    }
    
    fun getTracksForArtist(artistName: String): List<MusicTrack> {
        return _tracks.value.filter { it.artist == artistName }
    }
    
    fun getTracksForPlaylist(playlistId: String): List<MusicTrack> {
        val manager = musicLibraryManager ?: return emptyList()
        
        return try {
            // This would need to be implemented in MusicLibraryManager
            // For now, return empty list
            emptyList()
        } catch (e: Exception) {
            _error.value = "Failed to load playlist tracks: ${e.message}"
            emptyList()
        }
    }
    
    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            try {
                val manager = musicLibraryManager ?: return@launch
                val success = manager.deletePlaylist(playlistId)
                if (success) {
                    // Refresh playlists to remove the deleted one
                    _playlists.value = manager.getAllPlaylists()
                } else {
                    _error.value = "Failed to delete playlist"
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete playlist: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun updateSearchQuery(query: String) {
        searchQuery = query
        filterAndSortTracks()
    }
    
    fun updateSortOption(sortType: String) {
        if (currentSortOption != sortType) {
            currentSortOption = sortType
            filterAndSortTracks()
        }
    }
    
    private fun filterAndSortTracks() {
        val query = searchQuery.trim()
        val filteredTracks = if (query.isEmpty()) {
            _allTracks.value
        } else {
            _allTracks.value.filter { track ->
                track.title.contains(query, ignoreCase = true) ||
                track.artist.contains(query, ignoreCase = true) ||
                track.album.contains(query, ignoreCase = true)
            }
        }
        
        // Apply sorting
        _tracks.value = applySorting(filteredTracks, currentSortOption)
    }
    
    private fun applySorting(tracks: List<MusicTrack>, sortType: String): List<MusicTrack> {
        return when (sortType) {
            "title" -> tracks.sortedBy { it.title.lowercase() }
            "artist" -> tracks.sortedBy { it.artist.lowercase() }
            "album" -> tracks.sortedBy { it.album.lowercase() }
            "duration" -> tracks.sortedByDescending { it.duration }
            "date_added" -> tracks.sortedByDescending { it.dateAdded }
            else -> tracks
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        mediaObserver?.stopObserving()
    }
}



