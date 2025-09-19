package com.bytecoder.vplay.backend.managers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bytecoder.vplay.backend.data.models.MediaFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * SearchScreenManager handles all search operations across music and video libraries.
 * Provides unified search functionality with suggestions, recent searches, and filters.
 */
class SearchScreenManager(
    application: Application,
    private val musicLibraryScreenManager: MusicLibraryScreenManager,
    private val videoLibraryScreenManager: VideoLibraryScreenManager
) : AndroidViewModel(application) {
    
    private val context = application.applicationContext
    private val prefs: SharedPreferences = context.getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // Search state
    private val _searchResults = MutableStateFlow<List<MediaFile>>(emptyList())
    val searchResults: StateFlow<List<MediaFile>> = _searchResults.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // Recent searches
    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()
    
    // Search suggestions
    private val _searchSuggestions = MutableStateFlow<List<String>>(emptyList())
    val searchSuggestions: StateFlow<List<String>> = _searchSuggestions.asStateFlow()
    
    // Search filters
    private val _searchFilter = MutableStateFlow(SearchFilter.ALL)
    val searchFilter: StateFlow<SearchFilter> = _searchFilter.asStateFlow()
    
    private val _sortBy = MutableStateFlow(SearchSortBy.RELEVANCE)
    val sortBy: StateFlow<SearchSortBy> = _sortBy.asStateFlow()
    
    // Search categories
    private val _musicResults = MutableStateFlow<List<MediaFile>>(emptyList())
    val musicResults: StateFlow<List<MediaFile>> = _musicResults.asStateFlow()
    
    private val _videoResults = MutableStateFlow<List<MediaFile>>(emptyList())
    val videoResults: StateFlow<List<MediaFile>> = _videoResults.asStateFlow()
    
    private val _artistResults = MutableStateFlow<List<String>>(emptyList())
    val artistResults: StateFlow<List<String>> = _artistResults.asStateFlow()
    
    private val _albumResults = MutableStateFlow<List<String>>(emptyList())
    val albumResults: StateFlow<List<String>> = _albumResults.asStateFlow()

    enum class SearchFilter {
        ALL, MUSIC, VIDEO, ARTIST, ALBUM, PLAYLIST
    }

    enum class SearchSortBy {
        RELEVANCE, NAME_ASC, NAME_DESC, DATE_ASC, DATE_DESC, DURATION_ASC, DURATION_DESC
    }
    
    init {
        loadRecentSearches()
    }

    /**
     * Perform search across all media types
     */
    fun search(query: String) {
        if (query.isBlank()) {
            clearResults()
            return
        }
        
        _searchQuery.value = query
        
        viewModelScope.launch {
            _isSearching.value = true
            try {
                val musicResults = mutableListOf<MediaFile>()
                val videoResults = mutableListOf<MediaFile>()
                val artistResults = mutableListOf<String>()
                val albumResults = mutableListOf<String>()
                
                when (_searchFilter.value) {
                    SearchFilter.ALL -> {
                        // Search in all categories
                        musicResults.addAll(searchMusic(query))
                        videoResults.addAll(searchVideos(query))
                        artistResults.addAll(searchArtists(query))
                        albumResults.addAll(searchAlbums(query))
                    }
                    SearchFilter.MUSIC -> {
                        musicResults.addAll(searchMusic(query))
                    }
                    SearchFilter.VIDEO -> {
                        videoResults.addAll(searchVideos(query))
                    }
                    SearchFilter.ARTIST -> {
                        artistResults.addAll(searchArtists(query))
                        // Also add music by these artists
                        artistResults.forEach { artist ->
                            musicResults.addAll(getMusicByArtist(artist))
                        }
                    }
                    SearchFilter.ALBUM -> {
                        albumResults.addAll(searchAlbums(query))
                        // Also add music from these albums
                        albumResults.forEach { album ->
                            musicResults.addAll(getMusicByAlbum(album))
                        }
                    }
                    SearchFilter.PLAYLIST -> {
                        // Search in playlists (if implemented)
                    }
                }
                
                // Combine and sort results
                val allResults = mutableListOf<MediaFile>()
                allResults.addAll(musicResults)
                allResults.addAll(videoResults)
                
                _musicResults.value = musicResults
                _videoResults.value = videoResults
                _artistResults.value = artistResults
                _albumResults.value = albumResults
                _searchResults.value = sortResults(allResults)
                
                // Generate suggestions based on current query
                generateSuggestions(query)
                
            } catch (e: Exception) {
                clearResults()
            } finally {
                _isSearching.value = false
            }
        }
    }

    /**
     * Search in music library
     */
    private suspend fun searchMusic(query: String): List<MediaFile> {
        return musicLibraryScreenManager.searchTracks(query)
    }

    /**
     * Search in video library
     */
    private suspend fun searchVideos(query: String): List<MediaFile> {
        return videoLibraryScreenManager.searchVideos(query)
    }

    /**
     * Search for artists
     */
    private suspend fun searchArtists(query: String): List<String> {
        return musicLibraryScreenManager.searchArtists(query)
    }

    /**
     * Search for albums
     */
    private suspend fun searchAlbums(query: String): List<String> {
        return musicLibraryScreenManager.searchAlbums(query)
    }

    /**
     * Get music by artist
     */
    private suspend fun getMusicByArtist(artist: String): List<MediaFile> {
        return musicLibraryScreenManager.getTracksByArtist(artist)
    }

    /**
     * Get music by album
     */
    private suspend fun getMusicByAlbum(album: String): List<MediaFile> {
        return musicLibraryScreenManager.getTracksByAlbum(album)
    }

    /**
     * Sort search results based on current sort option
     */
    private fun sortResults(results: List<MediaFile>): List<MediaFile> {
        return when (_sortBy.value) {
            SearchSortBy.RELEVANCE -> results // Keep original relevance order
            SearchSortBy.NAME_ASC -> results.sortedBy { it.title }
            SearchSortBy.NAME_DESC -> results.sortedByDescending { it.title }
            SearchSortBy.DATE_ASC -> results.sortedBy { it.dateAdded }
            SearchSortBy.DATE_DESC -> results.sortedByDescending { it.dateAdded }
            SearchSortBy.DURATION_ASC -> results.sortedBy { it.duration }
            SearchSortBy.DURATION_DESC -> results.sortedByDescending { it.duration }
        }
    }

    /**
     * Generate search suggestions based on query
     */
    private fun generateSuggestions(query: String) {
        viewModelScope.launch {
            val suggestions = mutableListOf<String>()
            
            // Add matching artists
            val artists = searchArtists(query).take(3)
            suggestions.addAll(artists)
            
            // Add matching albums
            val albums = searchAlbums(query).take(3)
            suggestions.addAll(albums)
            
            // Add matching song titles
            val tracks = searchMusic(query).take(3)
            suggestions.addAll(tracks.map { it.title })
            
            _searchSuggestions.value = suggestions.distinct().take(10)
        }
    }

    /**
     * Clear all search results
     */
    fun clearResults() {
        _searchResults.value = emptyList()
        _musicResults.value = emptyList()
        _videoResults.value = emptyList()
        _artistResults.value = emptyList()
        _albumResults.value = emptyList()
        _searchSuggestions.value = emptyList()
        _searchQuery.value = ""
    }

    /**
     * Set search filter
     */
    fun setSearchFilter(filter: SearchFilter) {
        _searchFilter.value = filter
        // Re-run search with new filter if we have a query
        if (_searchQuery.value.isNotBlank()) {
            search(_searchQuery.value)
        }
    }

    /**
     * Set sort option
     */
    fun setSortBy(sortBy: SearchSortBy) {
        _sortBy.value = sortBy
        // Re-sort current results
        _searchResults.value = sortResults(_searchResults.value)
    }

    /**
     * Add query to recent searches
     */
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
        saveRecentSearches()
    }

    /**
     * Clear recent searches
     */
    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
        saveRecentSearches()
    }

    /**
     * Remove specific recent search
     */
    fun removeRecentSearch(query: String) {
        val currentSearches = _recentSearches.value.toMutableList()
        currentSearches.remove(query)
        _recentSearches.value = currentSearches
        saveRecentSearches()
    }

    /**
     * Load recent searches from preferences
     */
    private fun loadRecentSearches() {
        val searchesJson = prefs.getString("recent_searches", "[]")
        val type = object : TypeToken<List<String>>() {}.type
        val searches = try {
            gson.fromJson<List<String>>(searchesJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList<String>()
        }
        _recentSearches.value = searches
    }

    /**
     * Save recent searches to preferences
     */
    private fun saveRecentSearches() {
        val searchesJson = gson.toJson(_recentSearches.value)
        prefs.edit().putString("recent_searches", searchesJson).apply()
    }

    /**
     * Get search suggestions for auto-complete
     */
    fun getSearchSuggestions(partialQuery: String): List<String> {
        if (partialQuery.length < 2) return emptyList()
        
        val suggestions = mutableListOf<String>()
        
        // Add from recent searches
        suggestions.addAll(_recentSearches.value.filter { 
            it.contains(partialQuery, ignoreCase = true) 
        })
        
        // Add quick suggestions (could be enhanced with more logic)
        val quickSuggestions = listOf(
            "Rock music", "Pop songs", "Classical", "Jazz", "Electronic",
            "Movies", "TV Shows", "Documentaries", "Music Videos"
        ).filter { it.contains(partialQuery, ignoreCase = true) }
        
        suggestions.addAll(quickSuggestions)
        
        return suggestions.distinct().take(8)
    }

    /**
     * Perform voice search (placeholder for future implementation)
     */
    fun performVoiceSearch(voiceQuery: String) {
        search(voiceQuery)
        addToRecentSearches(voiceQuery)
    }
}



