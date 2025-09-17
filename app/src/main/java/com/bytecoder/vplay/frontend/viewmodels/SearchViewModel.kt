package com.bytecoder.vplay.frontend.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bytecoder.vplay.backend.data.models.MediaItemModel
import com.bytecoder.vplay.backend.managers.MusicLibraryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _searchResults = MutableStateFlow<List<MediaItemModel>>(emptyList())
    val searchResults: StateFlow<List<MediaItemModel>> = _searchResults.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()
    
    private val context = application.applicationContext
    private val musicLibraryManager = MusicLibraryManager(application.applicationContext)
    
    init {
        loadRecentSearches()
    }
    
    fun search(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            _isSearching.value = true
            try {
                // Search in music library
                val tracks = musicLibraryManager.searchTracks(query)
                val mediaItems = tracks.map { track ->
                    MediaItemModel(
                        id = track.id,
                        title = track.title,
                        subtitle = track.artist,
                        uri = track.filePath,
                        isVideo = false,
                        durationMs = track.duration,
                        thumbnailPath = track.albumArtPath
                    )
                }
                
                // You could also search videos here if you have a video library
                // val videos = VideoLibraryManager.searchVideos(query)
                
                _searchResults.value = mediaItems
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }
    
    fun clearResults() {
        _searchResults.value = emptyList()
    }
    
    fun addToRecentSearches(query: String) {
        if (query.isBlank()) return
        
        val currentSearches = _recentSearches.value.toMutableList()
        currentSearches.removeAll { it == query } // Remove if already exists
        currentSearches.add(0, query) // Add to top
        
        // Keep only last 10 searches
        if (currentSearches.size > 10) {
            currentSearches.removeAt(currentSearches.size - 1)
        }
        
        _recentSearches.value = currentSearches
        saveRecentSearches(currentSearches)
    }
    
    fun removeFromRecentSearches(query: String) {
        val currentSearches = _recentSearches.value.toMutableList()
        currentSearches.removeAll { it == query }
        _recentSearches.value = currentSearches
        saveRecentSearches(currentSearches)
    }
    
    private fun loadRecentSearches() {
        val prefs = context.getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
        val searches = prefs.getStringSet("recent_searches", emptySet())?.toList() ?: emptyList()
        _recentSearches.value = searches
    }
    
    private fun saveRecentSearches(searches: List<String>) {
        val prefs = context.getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putStringSet("recent_searches", searches.toSet())
            .apply()
    }
}