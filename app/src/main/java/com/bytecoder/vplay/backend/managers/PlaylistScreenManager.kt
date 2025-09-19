package com.bytecoder.vplay.backend.managers

import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.bytecoder.vplay.backend.managers.MusicLibraryManager
import com.bytecoder.vplay.backend.data.models.MediaFile
import com.bytecoder.vplay.backend.models.Playlist
import android.app.Application

/**
 * PlaylistScreenManager handles all playlist operations including creation,
 * deletion, modification, and playlist content management.
 */
class PlaylistScreenManager(application: Application) : AndroidViewModel(application) {
    
    private val context: Context = application.applicationContext
    private val musicLibraryManager = MusicLibraryManager(context)
    
    // Playlist state
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()
    
    private val _currentPlaylist = MutableStateFlow<Playlist?>(null)
    val currentPlaylist: StateFlow<Playlist?> = _currentPlaylist.asStateFlow()
    
    private val _playlistTracks = MutableStateFlow<List<MediaFile>>(emptyList())
    val playlistTracks: StateFlow<List<MediaFile>> = _playlistTracks.asStateFlow()
    
    // UI state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _showCreatePlaylistDialog = MutableStateFlow(false)
    val showCreatePlaylistDialog: StateFlow<Boolean> = _showCreatePlaylistDialog.asStateFlow()
    
    private val _showDeleteConfirmDialog = MutableStateFlow(false)
    val showDeleteConfirmDialog: StateFlow<Boolean> = _showDeleteConfirmDialog.asStateFlow()
    
    private val _playlistToDelete = MutableStateFlow<Playlist?>(null)
    val playlistToDelete: StateFlow<Playlist?> = _playlistToDelete.asStateFlow()
    
    // Search and filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _filteredPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val filteredPlaylists: StateFlow<List<Playlist>> = _filteredPlaylists.asStateFlow()
    
    private val _sortBy = MutableStateFlow(PlaylistSortBy.NAME_ASC)
    val sortBy: StateFlow<PlaylistSortBy> = _sortBy.asStateFlow()

    enum class PlaylistSortBy {
        NAME_ASC, NAME_DESC, DATE_ASC, DATE_DESC, SIZE_ASC, SIZE_DESC
    }
    
    init {
        loadPlaylists()
    }

    /**
     * Load all playlists
     */
    fun loadPlaylists() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val allPlaylists = musicLibraryManager.getAllPlaylists()
                _playlists.value = allPlaylists.map { musicPlaylist ->
                    Playlist(
                        id = musicPlaylist.id,
                        name = musicPlaylist.name,
                        trackCount = musicPlaylist.trackIds.size,
                        createdDate = musicPlaylist.createdDate,
                        modifiedDate = musicPlaylist.modifiedDate,
                        description = musicPlaylist.description
                    )
                }
                
                filterAndSortPlaylists()
            } catch (e: Exception) {
                _error.value = "Failed to load playlists: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load specific playlist details
     */
    fun loadPlaylistDetails(playlistId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val allPlaylists = musicLibraryManager.getAllPlaylists()
                val musicPlaylist = allPlaylists.find { it.id == playlistId }
                
                if (musicPlaylist != null) {
                    _currentPlaylist.value = Playlist(
                        id = musicPlaylist.id,
                        name = musicPlaylist.name,
                        trackCount = musicPlaylist.trackIds.size,
                        createdDate = musicPlaylist.createdDate,
                        modifiedDate = musicPlaylist.modifiedDate,
                        description = musicPlaylist.description
                    )
                    
                    // Load playlist tracks
                    val tracks = musicPlaylist.trackIds.mapNotNull { trackId ->
                        // Convert MusicTrack to MediaFile
                        val musicTrack = musicLibraryManager.getTrackById(trackId)
                        musicTrack?.let {
                            MediaFile(
                                id = it.id,
                                title = it.title,
                                artist = it.artist,
                                album = it.album,
                                duration = it.duration,
                                filePath = it.filePath,
                                type = "audio",
                                size = it.size,
                                dateAdded = it.dateAdded,
                                albumArtPath = it.albumArtPath
                            )
                        }
                    }
                    _playlistTracks.value = tracks
                }
            } catch (e: Exception) {
                _error.value = "Failed to load playlist details: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Create new playlist
     */
    fun createPlaylist(name: String, description: String = "") {
        viewModelScope.launch {
            try {
                musicLibraryManager.createPlaylist(name, description)
                loadPlaylists()
                _showCreatePlaylistDialog.value = false
            } catch (e: Exception) {
                _error.value = "Failed to create playlist: ${e.message}"
            }
        }
    }

    /**
     * Delete playlist
     */
    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            try {
                musicLibraryManager.deletePlaylist(playlist.id)
                loadPlaylists()
                _showDeleteConfirmDialog.value = false
                _playlistToDelete.value = null
            } catch (e: Exception) {
                _error.value = "Failed to delete playlist: ${e.message}"
            }
        }
    }

    /**
     * Add track to playlist
     */
    fun addTrackToPlaylist(playlistId: String, track: MediaFile) {
        viewModelScope.launch {
            try {
                musicLibraryManager.addTrackToPlaylist(playlistId, track.id)
                loadPlaylistDetails(playlistId)
            } catch (e: Exception) {
                _error.value = "Failed to add track to playlist: ${e.message}"
            }
        }
    }

    /**
     * Remove track from playlist
     */
    fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            try {
                musicLibraryManager.removeTrackFromPlaylist(playlistId, trackId)
                loadPlaylistDetails(playlistId)
            } catch (e: Exception) {
                _error.value = "Failed to remove track from playlist: ${e.message}"
            }
        }
    }

    /**
     * Update playlist info
     */
    fun updatePlaylist(playlistId: String, name: String, description: String) {
        viewModelScope.launch {
            try {
                musicLibraryManager.updatePlaylistInfo(playlistId, name, description)
                loadPlaylists()
                loadPlaylistDetails(playlistId)
            } catch (e: Exception) {
                _error.value = "Failed to update playlist: ${e.message}"
            }
        }
    }

    /**
     * Search playlists
     */
    fun searchPlaylists(query: String) {
        _searchQuery.value = query
        filterAndSortPlaylists()
    }

    /**
     * Set sort option
     */
    fun setSortBy(sortBy: PlaylistSortBy) {
        _sortBy.value = sortBy
        filterAndSortPlaylists()
    }

    /**
     * Filter and sort playlists based on current criteria
     */
    private fun filterAndSortPlaylists() {
        val query = _searchQuery.value
        val sort = _sortBy.value
        
        var filtered = _playlists.value
        
        // Apply search filter
        if (query.isNotBlank()) {
            filtered = filtered.filter { playlist ->
                playlist.name.contains(query, ignoreCase = true) ||
                playlist.description.contains(query, ignoreCase = true)
            }
        }
        
        // Apply sorting
        filtered = when (sort) {
            PlaylistSortBy.NAME_ASC -> filtered.sortedBy { it.name }
            PlaylistSortBy.NAME_DESC -> filtered.sortedByDescending { it.name }
            PlaylistSortBy.DATE_ASC -> filtered.sortedBy { it.createdDate }
            PlaylistSortBy.DATE_DESC -> filtered.sortedByDescending { it.createdDate }
            PlaylistSortBy.SIZE_ASC -> filtered.sortedBy { it.trackCount }
            PlaylistSortBy.SIZE_DESC -> filtered.sortedByDescending { it.trackCount }
        }
        
        _filteredPlaylists.value = filtered
    }

    /**
     * Show create playlist dialog
     */
    fun showCreatePlaylistDialog() {
        _showCreatePlaylistDialog.value = true
    }

    /**
     * Hide create playlist dialog
     */
    fun hideCreatePlaylistDialog() {
        _showCreatePlaylistDialog.value = false
    }

    /**
     * Show delete confirmation dialog
     */
    fun showDeleteConfirmDialog(playlist: Playlist) {
        _playlistToDelete.value = playlist
        _showDeleteConfirmDialog.value = true
    }

    /**
     * Hide delete confirmation dialog
     */
    fun hideDeleteConfirmDialog() {
        _showDeleteConfirmDialog.value = false
        _playlistToDelete.value = null
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Refresh playlists
     */
    fun refresh() {
        loadPlaylists()
    }

    /**
     * Get playlist by ID
     */
    fun getPlaylistById(playlistId: String): Playlist? {
        return _playlists.value.find { it.id == playlistId }
    }
}



