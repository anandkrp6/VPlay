package com.bytecoder.vplay.frontend.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.content.Context
import com.bytecoder.vplay.backend.data.models.OnlineContentModel
import com.bytecoder.vplay.backend.data.models.MediaItemModel
import com.bytecoder.vplay.backend.managers.MusicLibraryManager

class PlaybackQueueViewModel : ViewModel() {
    private var musicLibraryManager: MusicLibraryManager? = null
    
    fun setMusicLibraryManager(context: Context) {
        if (musicLibraryManager == null) {
            musicLibraryManager = MusicLibraryManager(context)
        }
    }
    
    private val _queue = MutableLiveData<List<MediaItemModel>>(emptyList())
    val queue: LiveData<List<MediaItemModel>> = _queue

    private val _currentIndex = MutableLiveData<Int>(-1)
    val currentIndex: LiveData<Int> = _currentIndex
    
    private val _currentMedia = MutableLiveData<MediaItemModel?>(null)
    val currentMedia: LiveData<MediaItemModel?> = _currentMedia
    
    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: LiveData<Boolean> = _isPlaying
    
    private val _currentPosition = MutableLiveData<Long>(0L)
    val currentPosition: LiveData<Long> = _currentPosition
    
    private val _duration = MutableLiveData<Long>(0L)
    val duration: LiveData<Long> = _duration
    
    private val _isShuffleEnabled = MutableLiveData<Boolean>(false)
    val isShuffleEnabled: LiveData<Boolean> = _isShuffleEnabled
    
    private val _repeatMode = MutableLiveData<Int>(0)
    val repeatMode: LiveData<Int> = _repeatMode

    fun setQueue(items: List<MediaItemModel>, startIndex: Int = 0) {
        _queue.value = items
        _currentIndex.value = startIndex.coerceIn(items.indices)
    }

    fun addToQueue(item: MediaItemModel) {
        val list = _queue.value?.toMutableList() ?: mutableListOf()
        list.add(item)
        _queue.value = list
        if ((_currentIndex.value ?: -1) == -1) _currentIndex.value = 0
    }

    fun addAllToQueue(items: List<MediaItemModel>) {
        val list = _queue.value?.toMutableList() ?: mutableListOf()
        list.addAll(items)
        _queue.value = list
        if ((_currentIndex.value ?: -1) == -1 && list.isNotEmpty()) _currentIndex.value = 0
    }

    fun playOnlineContent(content: OnlineContentModel) {
        val mediaItem = MediaItemModel(
            id = content.id,
            title = content.title,
            subtitle = content.channel,
            uri = content.streamUrl,
            isVideo = content.type == "video" || content.type == "live",
            durationMs = parseDurationToMs(content.duration)
        )
        setQueue(listOf(mediaItem), 0)
    }
    
    private fun parseDurationToMs(duration: String): Long {
        return try {
            when {
                duration.equals("Live", ignoreCase = true) -> 0L
                duration.contains(":") -> {
                    val parts = duration.split(":")
                    when (parts.size) {
                        2 -> {
                            val minutes = parts[0].toLong()
                            val seconds = parts[1].toLong()
                            (minutes * 60 + seconds) * 1000
                        }
                        3 -> {
                            val hours = parts[0].toLong()
                            val minutes = parts[1].toLong()
                            val seconds = parts[2].toLong()
                            (hours * 3600 + minutes * 60 + seconds) * 1000
                        }
                        else -> 0L
                    }
                }
                else -> 0L
            }
        } catch (e: Exception) {
            0L
        }
    }
    
    fun saveQueueAsPlaylist(name: String) {
        val currentQueue = _queue.value ?: return
        if (currentQueue.isEmpty()) return
        
        val manager = musicLibraryManager ?: return
        
        viewModelScope.launch {
            try {
                val playlistId = manager.createPlaylist(
                    name = name,
                    description = "Created from playback queue"
                )
                
                if (playlistId.isNotEmpty()) {
                    currentQueue.forEach { mediaItem ->
                        manager.addTrackToPlaylist(playlistId, mediaItem.id)
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun addOnlineContentToQueue(content: OnlineContentModel) {
        val mediaItem = MediaItemModel(
            id = content.id,
            title = content.title,
            subtitle = content.channel,
            uri = content.streamUrl,
            isVideo = content.type == "video" || content.type == "live",
            durationMs = parseDurationToMs(content.duration)
        )
        addToQueue(mediaItem)
    }
    
    fun setCurrentIndex(index: Int) {
        val list = _queue.value ?: return
        if (index in list.indices) {
            _currentIndex.value = index
            _currentMedia.value = list[index]
        }
    }
    
    fun seekToQueueItem(index: Int) {
        setCurrentIndex(index)
    }
    
    fun seekTo(positionMs: Long) {
        _currentPosition.value = positionMs
    }
    
    fun setShuffleMode(enabled: Boolean) {
        _isShuffleEnabled.value = enabled
    }
    
    fun skipToPrevious() {
        val currentIndex = _currentIndex.value ?: return
        if (currentIndex > 0) {
            setCurrentIndex(currentIndex - 1)
        }
    }
    
    fun pause() {
        _isPlaying.value = false
    }
    
    fun play() {
        _isPlaying.value = true
    }
    
    fun skipToNext() {
        val queue = _queue.value ?: return
        val currentIndex = _currentIndex.value ?: return
        if (currentIndex < queue.size - 1) {
            setCurrentIndex(currentIndex + 1)
        }
    }
    
    fun setRepeatMode(mode: Int) {
        _repeatMode.value = mode
    }
    
    fun clearQueue() {
        _queue.value = emptyList()
        _currentIndex.value = -1
        _currentMedia.value = null
        _isPlaying.value = false
        _currentPosition.value = 0L
        _duration.value = 0L
    }
    
    fun removeFromQueue(index: Int) {
        val list = _queue.value?.toMutableList() ?: return
        if (index in list.indices) {
            list.removeAt(index)
            _queue.value = list
            
            val currentIndex = _currentIndex.value ?: -1
            when {
                list.isEmpty() -> {
                    _currentIndex.value = -1
                    _currentMedia.value = null
                }
                index < currentIndex -> _currentIndex.value = currentIndex - 1
                index == currentIndex && currentIndex >= list.size -> {
                    _currentIndex.value = list.size - 1
                }
            }
        }
    }
    
    fun moveItem(from: Int, to: Int) {
        val list = _queue.value?.toMutableList() ?: return
        if (from !in list.indices || to !in list.indices) return
        val item = list.removeAt(from)
        list.add(to, item)
        _queue.value = list
        
        val ci = _currentIndex.value ?: return
        _currentIndex.value = when {
            from == ci -> to
            from < ci && to >= ci -> ci - 1
            from > ci && to <= ci -> ci + 1
            else -> ci
        }
    }
    
    fun refreshVideoLibrary() {
        viewModelScope.launch {
            // Refresh video library from device storage
            // This would typically scan for new video files
            try {
                // Implementation would involve scanning device for video files
                // and updating the video library
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}