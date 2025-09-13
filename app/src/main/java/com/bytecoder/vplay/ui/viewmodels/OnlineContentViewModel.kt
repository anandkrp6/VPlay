package com.bytecoder.vplay.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bytecoder.vplay.data.OnlineContentModel
import com.bytecoder.vplay.data.Podcast
import com.bytecoder.vplay.data.RadioStation
import com.bytecoder.vplay.data.LiveStream
import com.bytecoder.vplay.media.PlaybackQueueViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OnlineContentViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _trendingContent = MutableLiveData<List<OnlineContentModel>>()
    val trendingContent: LiveData<List<OnlineContentModel>> = _trendingContent
    
    private val _radioStations = MutableLiveData<List<OnlineContentModel>>()
    val radioStations: LiveData<List<OnlineContentModel>> = _radioStations
    
    private val _podcasts = MutableLiveData<List<OnlineContentModel>>()
    val podcasts: LiveData<List<OnlineContentModel>> = _podcasts
    
    private val _liveStreams = MutableLiveData<List<OnlineContentModel>>()
    val liveStreams: LiveData<List<OnlineContentModel>> = _liveStreams
    
    private val _subscriptions = MutableLiveData<List<OnlineContentModel>>()
    val subscriptions: LiveData<List<OnlineContentModel>> = _subscriptions
    
    private val _history = MutableLiveData<List<OnlineContentModel>>()
    val history: LiveData<List<OnlineContentModel>> = _history
    
    private val _searchResults = MutableLiveData<List<OnlineContentModel>>()
    val searchResults: LiveData<List<OnlineContentModel>> = _searchResults
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: LiveData<Boolean> = _isConnected
    
    // Simulated data - In real app, this would come from APIs
    private val sampleTrendingContent = listOf(
        OnlineContentModel(
            id = "trending_1",
            title = "Popular Music Video 2024",
            channel = "Music Channel",
            description = "Latest trending music video",
            thumbnailUrl = "",
            streamUrl = "https://example.com/video1.mp4",
            duration = "3:45",
            type = "video",
            category = "Music",
            viewCount = "1.2M views",
            publishedDate = "2 days ago"
        ),
        OnlineContentModel(
            id = "trending_2",
            title = "Viral Comedy Sketch",
            channel = "Comedy Central",
            description = "Hilarious comedy content",
            thumbnailUrl = "",
            streamUrl = "https://example.com/video2.mp4",
            duration = "5:20",
            type = "video",
            category = "Comedy",
            viewCount = "890K views",
            publishedDate = "1 day ago"
        ),
        OnlineContentModel(
            id = "trending_3",
            title = "Tech Review: Latest Smartphone",
            channel = "Tech Reviews",
            description = "In-depth review of the newest smartphone",
            thumbnailUrl = "",
            streamUrl = "https://example.com/video3.mp4",
            duration = "12:30",
            type = "video",
            category = "Technology",
            viewCount = "456K views",
            publishedDate = "3 days ago"
        )
    )
    
    private val sampleRadioStations = listOf(
        OnlineContentModel(
            id = "radio_1",
            title = "Classic Rock FM",
            channel = "Rock Music",
            description = "24/7 Classic Rock Hits",
            thumbnailUrl = "",
            streamUrl = "https://example.com/radio1.mp3",
            duration = "Live",
            type = "radio",
            category = "Music",
            isLive = true
        ),
        OnlineContentModel(
            id = "radio_2",
            title = "Jazz Lounge",
            channel = "Jazz Music",
            description = "Smooth Jazz and Blues",
            thumbnailUrl = "",
            streamUrl = "https://example.com/radio2.mp3",
            duration = "Live",
            type = "radio",
            category = "Music",
            isLive = true
        ),
        OnlineContentModel(
            id = "radio_3",
            title = "News Radio 24/7",
            channel = "News Network",
            description = "Latest news and updates",
            thumbnailUrl = "",
            streamUrl = "https://example.com/radio3.mp3",
            duration = "Live",
            type = "radio",
            category = "News",
            isLive = true
        )
    )
    
    private val samplePodcasts = listOf(
        OnlineContentModel(
            id = "podcast_1",
            title = "Tech Talk Weekly",
            channel = "Tech Podcast Network",
            description = "Weekly discussions about latest technology trends",
            thumbnailUrl = "",
            streamUrl = "https://example.com/podcast1.mp3",
            duration = "45:00",
            type = "podcast",
            category = "Technology"
        ),
        OnlineContentModel(
            id = "podcast_2",
            title = "Daily News Briefing",
            channel = "News Podcast",
            description = "Your daily dose of important news",
            thumbnailUrl = "",
            streamUrl = "https://example.com/podcast2.mp3",
            duration = "15:30",
            type = "podcast",
            category = "News"
        ),
        OnlineContentModel(
            id = "podcast_3",
            title = "Mindfulness Meditation",
            channel = "Wellness Podcasts",
            description = "Guided meditation for daily practice",
            thumbnailUrl = "",
            streamUrl = "https://example.com/podcast3.mp3",
            duration = "20:00",
            type = "podcast",
            category = "Health"
        )
    )
    
    private val sampleLiveStreams = listOf(
        OnlineContentModel(
            id = "live_1",
            title = "Gaming Tournament Finals",
            channel = "Esports Network",
            description = "Live championship tournament",
            thumbnailUrl = "",
            streamUrl = "https://example.com/live1.m3u8",
            duration = "Live",
            type = "live",
            category = "Gaming",
            isLive = true,
            viewCount = "12.5K watching"
        ),
        OnlineContentModel(
            id = "live_2",
            title = "Morning News Live",
            channel = "News Channel",
            description = "Live morning news broadcast",
            thumbnailUrl = "",
            streamUrl = "https://example.com/live2.m3u8",
            duration = "Live",
            type = "live",
            category = "News",
            isLive = true,
            viewCount = "8.2K watching"
        )
    )
    
    init {
        checkConnectivity()
    }
    
    fun loadContent() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Simulate network delay
                withContext(Dispatchers.IO) {
                    Thread.sleep(1000)
                }
                
                // Load sample data
                _trendingContent.value = sampleTrendingContent
                _radioStations.value = sampleRadioStations
                _podcasts.value = samplePodcasts
                _liveStreams.value = sampleLiveStreams
                _subscriptions.value = loadSubscriptions()
                _history.value = loadHistory()
                
                _isConnected.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load content: ${e.message}"
                _isConnected.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refreshContent() {
        loadContent()
    }
    
    fun searchContent(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Simulate search
                withContext(Dispatchers.IO) {
                    Thread.sleep(500)
                }
                
                val allContent = sampleTrendingContent + sampleRadioStations + 
                               samplePodcasts + sampleLiveStreams
                
                val filtered = allContent.filter { content ->
                    content.title.contains(query, ignoreCase = true) ||
                    content.channel.contains(query, ignoreCase = true) ||
                    content.category.contains(query, ignoreCase = true)
                }
                
                _searchResults.value = filtered
            } catch (e: Exception) {
                _errorMessage.value = "Search failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun playContent(content: OnlineContentModel, queueViewModel: PlaybackQueueViewModel) {
        viewModelScope.launch {
            try {
                // Add to history
                addToHistory(content)
                
                // Play content through queue system
                queueViewModel.playOnlineContent(content)
                
                // Navigate to appropriate player based on content type
                when (content.type) {
                    "video", "live" -> {
                        // Navigate to video player
                    }
                    "audio", "radio", "podcast" -> {
                        // Navigate to audio player
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to play content: ${e.message}"
            }
        }
    }
    
    fun playUrl(url: String, queueViewModel: PlaybackQueueViewModel) {
        viewModelScope.launch {
            try {
                val content = OnlineContentModel(
                    id = "custom_url_${System.currentTimeMillis()}",
                    title = extractTitleFromUrl(url),
                    channel = "Custom URL",
                    streamUrl = url,
                    duration = "Unknown",
                    type = detectContentType(url),
                    category = "Custom"
                )
                
                playContent(content, queueViewModel)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to play URL: ${e.message}"
            }
        }
    }
    
    fun addToQueue(content: OnlineContentModel, queueViewModel: PlaybackQueueViewModel) {
        viewModelScope.launch {
            try {
                queueViewModel.addOnlineContentToQueue(content)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add to queue: ${e.message}"
            }
        }
    }
    
    fun subscribeToPodcast(podcast: OnlineContentModel) {
        viewModelScope.launch {
            try {
                // Update subscription status
                val updatedPodcast = podcast.copy(isSubscribed = true)
                
                // Add to subscriptions
                val currentSubscriptions = _subscriptions.value?.toMutableList() ?: mutableListOf()
                currentSubscriptions.add(updatedPodcast)
                _subscriptions.value = currentSubscriptions
                
                // Update podcasts list
                val currentPodcasts = _podcasts.value?.toMutableList() ?: mutableListOf()
                val index = currentPodcasts.indexOfFirst { it.id == podcast.id }
                if (index != -1) {
                    currentPodcasts[index] = updatedPodcast
                    _podcasts.value = currentPodcasts
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to subscribe: ${e.message}"
            }
        }
    }
    
    fun clearHistory() {
        _history.value = emptyList()
    }
    
    private fun addToHistory(content: OnlineContentModel) {
        val currentHistory = _history.value?.toMutableList() ?: mutableListOf()
        
        // Remove if already in history
        currentHistory.removeAll { it.id == content.id }
        
        // Add to beginning
        val updatedContent = content.copy(
            lastPlayedTime = System.currentTimeMillis(),
            playCount = content.playCount + 1
        )
        currentHistory.add(0, updatedContent)
        
        // Keep only last 50 items
        if (currentHistory.size > 50) {
            currentHistory.removeAt(currentHistory.size - 1)
        }
        
        _history.value = currentHistory
    }
    
    private fun loadSubscriptions(): List<OnlineContentModel> {
        // Load from local storage or return empty list
        return emptyList()
    }
    
    private fun loadHistory(): List<OnlineContentModel> {
        // Load from local storage or return empty list
        return emptyList()
    }
    
    private fun extractTitleFromUrl(url: String): String {
        return url.substringAfterLast("/").substringBeforeLast(".")
            .replace(Regex("[^a-zA-Z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .takeIf { it.isNotEmpty() } ?: "Custom Stream"
    }
    
    private fun detectContentType(url: String): String {
        return when {
            url.contains(Regex("\\.(mp4|avi|mkv|webm|m3u8)$", RegexOption.IGNORE_CASE)) -> "video"
            url.contains(Regex("\\.(mp3|aac|ogg|flac|m4a)$", RegexOption.IGNORE_CASE)) -> "audio"
            url.contains("youtube", ignoreCase = true) -> "video"
            url.contains("spotify", ignoreCase = true) -> "audio"
            url.contains("radio", ignoreCase = true) -> "radio"
            url.contains("podcast", ignoreCase = true) -> "podcast"
            else -> "unknown"
        }
    }
    
    private fun checkConnectivity() {
        // Check network connectivity
        _isConnected.value = true // Simplified for demo
    }
    
    fun retryConnection() {
        checkConnectivity()
        if (_isConnected.value == true) {
            loadContent()
        }
    }
}