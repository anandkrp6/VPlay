package com.bytecoder.vplay.frontend.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.bytecoder.vplay.backend.managers.MusicLibraryManager
import com.bytecoder.vplay.backend.managers.MusicPlaylist
import com.bytecoder.vplay.backend.managers.MusicTrack

class PlaylistDetailViewModel : ViewModel() {
    private val _playlist = MutableStateFlow<MusicPlaylist?>(null)
    val playlist: StateFlow<MusicPlaylist?> = _playlist.asStateFlow()
    
    private val _tracks = MutableStateFlow<List<MusicTrack>>(emptyList())
    val tracks: StateFlow<List<MusicTrack>> = _tracks.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private var musicLibraryManager: MusicLibraryManager? = null
    private var currentPlaylistId: String? = null
    
    fun initialize(context: Context, playlistId: String) {
        musicLibraryManager = MusicLibraryManager(context)
        currentPlaylistId = playlistId
        loadPlaylistData()
    }
    
    private fun loadPlaylistData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val playlistId = currentPlaylistId ?: return@launch
                val manager = musicLibraryManager ?: return@launch
                
                // Load playlist info
                val allPlaylists = manager.getAllPlaylists()
                val currentPlaylist = allPlaylists.find { it.id == playlistId }
                _playlist.value = currentPlaylist
                
                // Load playlist tracks
                val playlistTracks = manager.getPlaylistTracks(playlistId)
                _tracks.value = playlistTracks
                
            } catch (e: Exception) {
                _error.value = "Failed to load playlist: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun removeTrackFromPlaylist(trackId: String) {
        viewModelScope.launch {
            try {
                val playlistId = currentPlaylistId ?: return@launch
                val manager = musicLibraryManager ?: return@launch
                
                manager.removeTrackFromPlaylist(playlistId, trackId)
                
                // Reload tracks
                val updatedTracks = manager.getPlaylistTracks(playlistId)
                _tracks.value = updatedTracks
                
                // Update playlist info (track count might have changed)
                val allPlaylists = manager.getAllPlaylists()
                val updatedPlaylist = allPlaylists.find { it.id == playlistId }
                _playlist.value = updatedPlaylist
                
            } catch (e: Exception) {
                _error.value = "Failed to remove track: ${e.message}"
            }
        }
    }
    
    fun updatePlaylist(name: String, description: String) {
        viewModelScope.launch {
            try {
                val playlistId = currentPlaylistId ?: return@launch
                val manager = musicLibraryManager ?: return@launch
                
                val success = manager.updatePlaylist(playlistId, name, description.takeIf { it.isNotBlank() })
                
                if (success) {
                    // Reload playlist data to get the updated info
                    val allPlaylists = manager.getAllPlaylists()
                    val updatedPlaylist = allPlaylists.find { it.id == playlistId }
                    _playlist.value = updatedPlaylist
                } else {
                    _error.value = "Failed to update playlist"
                }
                
            } catch (e: Exception) {
                _error.value = "Failed to update playlist: ${e.message}"
            }
        }
    }
    
    fun deletePlaylist() {
        viewModelScope.launch {
            try {
                val playlistId = currentPlaylistId ?: return@launch
                val manager = musicLibraryManager ?: return@launch
                
                manager.deletePlaylist(playlistId)
                
            } catch (e: Exception) {
                _error.value = "Failed to delete playlist: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}