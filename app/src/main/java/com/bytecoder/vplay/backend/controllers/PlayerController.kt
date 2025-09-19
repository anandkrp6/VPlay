package com.bytecoder.vplay.backend.controllers

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.bytecoder.vplay.backend.managers.AdvancedPlayerManager
import com.bytecoder.vplay.backend.models.MediaFile
import com.bytecoder.vplay.backend.models.Video
import com.bytecoder.vplay.backend.managers.MusicTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Single source of truth for all media playback in the app.
 * No other component should have direct access to any player.
 * All media playback requests must go through this controller.
 */
class PlayerController private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: PlayerController? = null
        
        fun getInstance(): PlayerController {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PlayerController().also { INSTANCE = it }
            }
        }
    }
    
    // Core player managers - ONLY PlayerController has access to these
    private var playerManager: PlayerManager? = null
    private var advancedPlayerManager: AdvancedPlayerManager? = null
    private var context: Context? = null
    
    // Playback state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _currentMedia = MutableStateFlow<MediaFile?>(null)
    val currentMedia: StateFlow<MediaFile?> = _currentMedia.asStateFlow()
    
    private val _playbackQueue = MutableStateFlow<List<MediaFile>>(emptyList())
    val playbackQueue: StateFlow<List<MediaFile>> = _playbackQueue.asStateFlow()
    
    private val _currentQueueIndex = MutableStateFlow(0)
    val currentQueueIndex: StateFlow<Int> = _currentQueueIndex.asStateFlow()
    
    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()
    
    private val _shuffleMode = MutableStateFlow(false)
    val shuffleMode: StateFlow<Boolean> = _shuffleMode.asStateFlow()
    
    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError: StateFlow<String?> = _playbackError.asStateFlow()
    
    // Player types
    enum class PlayerType { AUDIO, VIDEO }
    private var currentPlayerType: PlayerType? = null
    
    /**
     * Initialize the PlayerController with context
     * Must be called before any playback operations
     */
    fun initialize(context: Context) {
        this.context = context
        if (playerManager == null) {
            playerManager = PlayerManager.getInstance(context)
            advancedPlayerManager = AdvancedPlayerManager(context)
            setupPlayerListeners()
        }
    }
    
    /**
     * Play a single media file
     */
    fun playMedia(media: MediaFile) {
        setQueue(listOf(media), 0)
        play()
    }
    
    /**
     * Play a music track
     */
    fun playTrack(track: MusicTrack) {
        val mediaFile = MediaFile(
            id = track.id,
            title = track.title,
            uri = track.uri,
            isVideo = false,
            artist = track.artist,
            album = track.album,
            duration = track.duration
        )
        playMedia(mediaFile)
    }
    
    /**
     * Play a video
     */
    fun playVideo(video: Video) {
        val mediaFile = MediaFile(
            id = video.id,
            title = video.title,
            uri = video.uri,
            isVideo = true,
            duration = video.duration
        )
        playMedia(mediaFile)
    }
    
    /**
     * Set the playback queue
     */
    fun setQueue(queue: List<MediaFile>, startIndex: Int = 0) {
        _playbackQueue.value = queue
        _currentQueueIndex.value = startIndex.coerceIn(0, queue.size - 1)
        
        if (queue.isNotEmpty()) {
            val currentMedia = queue[_currentQueueIndex.value]
            _currentMedia.value = currentMedia
            
            // Determine player type and prepare media
            currentPlayerType = if (currentMedia.isVideo) PlayerType.VIDEO else PlayerType.AUDIO
            prepareCurrentMedia()
        }
    }
    
    /**
     * Add media to queue
     */
    fun addToQueue(media: MediaFile) {
        val currentQueue = _playbackQueue.value.toMutableList()
        currentQueue.add(media)
        _playbackQueue.value = currentQueue
    }
    
    /**
     * Remove media from queue
     */
    fun removeFromQueue(index: Int) {
        val currentQueue = _playbackQueue.value.toMutableList()
        if (index in 0 until currentQueue.size) {
            currentQueue.removeAt(index)
            _playbackQueue.value = currentQueue
            
            // Adjust current index if necessary
            val currentIndex = _currentQueueIndex.value
            if (index <= currentIndex && currentIndex > 0) {
                _currentQueueIndex.value = currentIndex - 1
            }
        }
    }
    
    /**
     * Move to specific queue position
     */
    fun seekToQueueItem(index: Int) {
        val queue = _playbackQueue.value
        if (index in 0 until queue.size) {
            _currentQueueIndex.value = index
            _currentMedia.value = queue[index]
            currentPlayerType = if (queue[index].isVideo) PlayerType.VIDEO else PlayerType.AUDIO
            prepareCurrentMedia()
            play()
        }
    }
    
    /**
     * Play/Resume playback
     */
    fun play() {
        when (currentPlayerType) {
            PlayerType.AUDIO -> playerManager?.play()
            PlayerType.VIDEO -> advancedPlayerManager?.play()
            null -> _playbackError.value = "No media selected"
        }
    }
    
    /**
     * Pause playback
     */
    fun pause() {
        when (currentPlayerType) {
            PlayerType.AUDIO -> playerManager?.pause()
            PlayerType.VIDEO -> advancedPlayerManager?.pause()
            null -> return
        }
    }
    
    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }
    
    /**
     * Skip to next track
     */
    fun next() {
        val queue = _playbackQueue.value
        val currentIndex = _currentQueueIndex.value
        
        if (currentIndex < queue.size - 1) {
            seekToQueueItem(currentIndex + 1)
        } else if (_repeatMode.value == Player.REPEAT_MODE_ALL) {
            seekToQueueItem(0)
        }
    }
    
    /**
     * Skip to previous track
     */
    fun previous() {
        val currentIndex = _currentQueueIndex.value
        
        if (currentIndex > 0) {
            seekToQueueItem(currentIndex - 1)
        } else if (_repeatMode.value == Player.REPEAT_MODE_ALL) {
            val queue = _playbackQueue.value
            if (queue.isNotEmpty()) {
                seekToQueueItem(queue.size - 1)
            }
        }
    }
    
    /**
     * Seek to position
     */
    fun seekTo(positionMs: Long) {
        when (currentPlayerType) {
            PlayerType.AUDIO -> playerManager?.seekTo(positionMs)
            PlayerType.VIDEO -> advancedPlayerManager?.seekTo(positionMs)
            null -> return
        }
    }
    
    /**
     * Set repeat mode
     */
    fun setRepeatMode(repeatMode: Int) {
        _repeatMode.value = repeatMode
        when (currentPlayerType) {
            PlayerType.AUDIO -> playerManager?.setRepeatMode(repeatMode)
            PlayerType.VIDEO -> advancedPlayerManager?.setRepeatMode(repeatMode)
            null -> return
        }
    }
    
    /**
     * Set shuffle mode
     */
    fun setShuffleMode(enabled: Boolean) {
        _shuffleMode.value = enabled
        when (currentPlayerType) {
            PlayerType.AUDIO -> playerManager?.setShuffleMode(enabled)
            PlayerType.VIDEO -> advancedPlayerManager?.setShuffleMode(enabled)
            null -> return
        }
    }
    
    /**
     * Get the ExoPlayer instance for video display
     * ONLY for UI rendering - no control operations allowed
     */
    fun getVideoPlayer(): ExoPlayer? {
        return advancedPlayerManager?.getPlayer()
    }
    
    /**
     * Stop playback and clear queue
     */
    fun stop() {
        playerManager?.stop()
        advancedPlayerManager?.stop()
        _playbackQueue.value = emptyList()
        _currentMedia.value = null
        _currentQueueIndex.value = 0
        currentPlayerType = null
    }
    
    /**
     * Release all resources
     */
    fun release() {
        playerManager?.release()
        advancedPlayerManager?.release()
        playerManager = null
        advancedPlayerManager = null
        currentPlayerType = null
    }
    
    /**
     * Clear any playback errors
     */
    fun clearError() {
        _playbackError.value = null
    }
    
    // Private helper methods
    private fun prepareCurrentMedia() {
        val media = _currentMedia.value ?: return
        
        when (currentPlayerType) {
            PlayerType.AUDIO -> {
                val queue = _playbackQueue.value.map { mediaFile ->
                    MediaItem.Builder()
                        .setUri(mediaFile.uri)
                        .setMediaId(mediaFile.id)
                        .build()
                }
                playerManager?.setQueue(queue)
                playerManager?.seekToItem(_currentQueueIndex.value)
            }
            PlayerType.VIDEO -> {
                advancedPlayerManager?.loadMedia(media.uri)
            }
            null -> return
        }
    }
    
    private fun setupPlayerListeners() {
        // Setup listeners for both players to update state
        playerManager?.let { manager ->
            // Audio player state updates
            // This would need to be implemented in PlayerManager to provide callbacks
        }
        
        advancedPlayerManager?.let { manager ->
            // Video player state updates
            // This would need to be implemented in AdvancedPlayerManager to provide callbacks
        }
    }
}


