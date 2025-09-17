package com.bytecoder.vplay.frontend.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bytecoder.vplay.backend.data.models.OnlineContentModel
import com.bytecoder.vplay.backend.data.models.MediaItemModel

class PlaybackQueueViewModel : ViewModel() {
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
    
    private val _repeatMode = MutableLiveData<Int>(0) // 0 = off, 1 = all, 2 = one
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

    fun setCurrentIndex(index: Int) {
        val list = _queue.value ?: return
        if (index in list.indices) {
            _currentIndex.value = index
            _currentMedia.value = list[index]
        }
    }

    fun moveItem(from: Int, to: Int) {
        val list = _queue.value?.toMutableList() ?: return
        if (from !in list.indices || to !in list.indices) return
        val item = list.removeAt(from)
        list.add(to, item)
        _queue.value = list
        // Adjust current index if needed
        val ci = _currentIndex.value ?: return
        _currentIndex.value = when {
            from == ci -> to
            from < ci && to >= ci -> ci - 1
            from > ci && to <= ci -> ci + 1
            else -> ci
        }
    }

    fun skipNext() {
        val list = _queue.value ?: return
        val ci = _currentIndex.value ?: return
        if (ci + 1 < list.size) _currentIndex.value = ci + 1
    }

    fun skipPrevious() {
        val ci = _currentIndex.value ?: return
        if (ci - 1 >= 0) _currentIndex.value = ci - 1
    }
    
    // Online content support
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
    
    private fun parseDurationToMs(duration: String): Long {
        return try {
            when {
                duration.equals("Live", ignoreCase = true) -> 0L
                duration.contains(":") -> {
                    val parts = duration.split(":")
                    when (parts.size) {
                        2 -> { // mm:ss
                            val minutes = parts[0].toLong()
                            val seconds = parts[1].toLong()
                            (minutes * 60 + seconds) * 1000
                        }
                        3 -> { // hh:mm:ss
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
    
    // Playback control methods
    fun play() {
        _isPlaying.value = true
    }
    
    fun pause() {
        _isPlaying.value = false
    }
    
    fun seekTo(positionMs: Long) {
        _currentPosition.value = positionMs
    }
    
    fun skipToNext() {
        val queue = _queue.value ?: return
        val currentIndex = _currentIndex.value ?: return
        if (currentIndex < queue.size - 1) {
            setCurrentIndex(currentIndex + 1)
        }
    }
    
    fun skipToPrevious() {
        val currentIndex = _currentIndex.value ?: return
        if (currentIndex > 0) {
            setCurrentIndex(currentIndex - 1)
        }
    }
    
    fun seekToQueueItem(index: Int) {
        setCurrentIndex(index)
    }
    
    fun setShuffleMode(enabled: Boolean) {
        _isShuffleEnabled.value = enabled
    }
    
    fun setRepeatMode(mode: Int) {
        _repeatMode.value = mode
    }
    
    fun clearQueue() {
        _queue.value = emptyList()
        _currentIndex.value = -1
        _currentMedia.value = null
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
    
    fun saveQueueAsPlaylist(name: String) {
        // TODO: Implement save queue as playlist functionality
        // This would typically involve saving to Room database
    }
}
