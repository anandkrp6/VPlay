package com.bytecoder.vplay.backend.managers

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bytecoder.vplay.backend.data.models.MediaItemModel
import com.bytecoder.vplay.backend.managers.VideoLibraryManager
import com.bytecoder.vplay.backend.managers.VideoScanResult
import com.bytecoder.vplay.backend.utils.MediaStoreObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VideoLibraryScreenManager(application: Application) : AndroidViewModel(application) {
    
    private val videoLibraryManager = VideoLibraryManager(application)
    private val mediaObserver = MediaStoreObserver(application) { mediaType ->
        if (mediaType == MediaStoreObserver.MediaType.VIDEO || 
            mediaType == MediaStoreObserver.MediaType.BOTH) {
            refreshVideos()
        }
    }
    
    private val _allVideos = MutableStateFlow<List<MediaItemModel>>(emptyList())
    private val _videos = MutableStateFlow<List<MediaItemModel>>(emptyList())
    val videos: StateFlow<List<MediaItemModel>> = _videos.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _permissionDenied = MutableStateFlow(false)
    val permissionDenied: StateFlow<Boolean> = _permissionDenied.asStateFlow()
    
    var searchQuery by mutableStateOf("")
        private set
        
    var currentSortOption by mutableStateOf("title")
        private set
    
    init {
        loadVideos()
        startAutoRefresh()
    }
    
    fun startAutoRefresh() {
        mediaObserver.startObserving()
    }
    
    fun stopAutoRefresh() {
        mediaObserver.stopObserving()
    }
    
    override fun onCleared() {
        super.onCleared()
        stopAutoRefresh()
    }
    
    fun updateSearchQuery(query: String) {
        searchQuery = query
        filterVideos()
    }
    
    private fun filterVideos() {
        val query = searchQuery.trim()
        val filteredVideos = if (query.isEmpty()) {
            _allVideos.value
        } else {
            _allVideos.value.filter { video ->
                video.title.contains(query, ignoreCase = true) ||
                (video.subtitle?.contains(query, ignoreCase = true) == true)
            }
        }
        
        // Apply sorting
        _videos.value = applySorting(filteredVideos, currentSortOption)
    }
    
    private fun applySorting(videos: List<MediaItemModel>, sortType: String): List<MediaItemModel> {
        return when (sortType) {
            "title" -> videos.sortedBy { it.title.lowercase() }
            "duration" -> videos.sortedByDescending { it.durationMs }
            "date_added" -> videos.sortedByDescending { it.id.toLongOrNull() ?: 0 }
            "size" -> videos.sortedByDescending { it.durationMs } // Using duration as proxy for size
            else -> videos
        }
    }
    
    fun updateSortOption(sortType: String) {
        if (currentSortOption != sortType) {
            currentSortOption = sortType
            filterVideos() // Re-apply filtering with new sort
        }
    }
    
    fun loadVideos() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _permissionDenied.value = false
            
            when (val result = videoLibraryManager.scanVideosFromDevice()) {
                is VideoScanResult.Success -> {
                    val videoItems = result.videos.map { video ->
                        MediaItemModel(
                            id = video.id,
                            title = video.title,
                            subtitle = "Duration: ${videoLibraryManager.formatDuration(video.duration)} • ${video.resolution} • ${videoLibraryManager.formatFileSize(video.size)}",
                            uri = video.filePath,
                            isVideo = true,
                            durationMs = video.duration,
                            thumbnailPath = video.thumbnailPath
                        )
                    }
                    _allVideos.value = videoItems
                    filterVideos() // Apply current search filter
                }
                is VideoScanResult.Error -> {
                    _error.value = result.message
                    _allVideos.value = emptyList()
                    _videos.value = emptyList()
                }
                is VideoScanResult.PermissionDenied -> {
                    _permissionDenied.value = true
                    _error.value = "Storage permission required to load videos"
                    _allVideos.value = emptyList()
                    _videos.value = emptyList()
                }
            }
            _isLoading.value = false
        }
    }
    
    fun sortVideos(sortType: String) {
        updateSortOption(sortType)
    }
    
    fun refreshVideos() {
        loadVideos()
    }
    
    fun getVideoById(videoId: String): MediaItemModel? {
        return _allVideos.value.find { it.id == videoId }
    }
}



