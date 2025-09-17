package com.bytecoder.vplay.frontend.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bytecoder.vplay.backend.data.models.MediaItemModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VideosViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _videos = MutableStateFlow<List<MediaItemModel>>(emptyList())
    val videos: StateFlow<List<MediaItemModel>> = _videos.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    var searchQuery by mutableStateOf("")
        private set
    
    init {
        loadVideos()
    }
    
    fun loadVideos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Sample video data - replace with actual implementation
                val sampleVideos = listOf(
                    MediaItemModel(
                        id = "1",
                        title = "Sample Video 1",
                        subtitle = "Duration: 10:30",
                        uri = "file:///sample1.mp4",
                        isVideo = true,
                        durationMs = 630000L
                    ),
                    MediaItemModel(
                        id = "2",
                        title = "Sample Video 2",
                        subtitle = "Duration: 15:45",
                        uri = "file:///sample2.mp4",
                        isVideo = true,
                        durationMs = 945000L
                    ),
                    MediaItemModel(
                        id = "3",
                        title = "Movie Sample",
                        subtitle = "Duration: 2:15:30",
                        uri = "file:///movie.mp4",
                        isVideo = true,
                        durationMs = 8130000L
                    )
                )
                
                _videos.value = sampleVideos
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        searchQuery = query
    }
    
    fun sortVideos(sortType: String) {
        viewModelScope.launch {
            val currentVideos = _videos.value
            val sortedVideos = when (sortType) {
                "title" -> currentVideos.sortedBy { it.title }
                "duration" -> currentVideos.sortedBy { it.durationMs }
                "recent" -> currentVideos.sortedByDescending { it.id }
                "size" -> currentVideos.sortedByDescending { it.durationMs }
                else -> currentVideos
            }
            _videos.value = sortedVideos
        }
    }
    
    fun refreshVideos() {
        loadVideos()
    }
}