package com.bytecoder.vplay.backend.managers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bytecoder.vplay.backend.data.models.MediaFile
import com.bytecoder.vplay.backend.controllers.PlayerController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AudioPlayerScreenManager handles all audio playback operations and UI state
 * management for audio player screens. Works in conjunction with PlayerController
 * for unified playback control across the application.
 */
class AudioPlayerScreenManager @Inject constructor(
    application: Application,
    private val playerController: PlayerController
) : AndroidViewModel(application) {
    
    // Audio-specific UI state
    private val _showEqualizer = MutableStateFlow(false)
    val showEqualizer: StateFlow<Boolean> = _showEqualizer.asStateFlow()
    
    private val _showSleepTimer = MutableStateFlow(false)
    val showSleepTimer: StateFlow<Boolean> = _showSleepTimer.asStateFlow()
    
    private val _sleepTimerMinutes = MutableStateFlow(0)
    val sleepTimerMinutes: StateFlow<Int> = _sleepTimerMinutes.asStateFlow()
    
    private val _showLyrics = MutableStateFlow(false)
    val showLyrics: StateFlow<Boolean> = _showLyrics.asStateFlow()
    
    private val _currentLyrics = MutableStateFlow<String?>(null)
    val currentLyrics: StateFlow<String?> = _currentLyrics.asStateFlow()
    
    private val _audioVisualizerEnabled = MutableStateFlow(true)
    val audioVisualizerEnabled: StateFlow<Boolean> = _audioVisualizerEnabled.asStateFlow()
    
    private val _crossfadeEnabled = MutableStateFlow(false)
    val crossfadeEnabled: StateFlow<Boolean> = _crossfadeEnabled.asStateFlow()
    
    private val _crossfadeDurationMs = MutableStateFlow(3000L)
    val crossfadeDurationMs: StateFlow<Long> = _crossfadeDurationMs.asStateFlow()
    
    // Delegate core playback state to PlayerController
    val currentMedia = playerController.currentMedia
    val isPlaying = playerController.isPlaying
    val currentPosition = playerController.currentPosition
    val duration = playerController.duration
    val playbackSpeed = playerController.playbackSpeed
    val repeatMode = playerController.repeatMode
    val shuffleMode = playerController.shuffleMode
    val isBuffering = playerController.isBuffering

    /**
     * Initialize audio player
     */
    fun initializePlayer() {
        playerController.initializePlayer(PlayerController.PlayerType.AUDIO)
    }

    /**
     * Play audio media
     */
    fun playMedia(media: MediaFile) {
        viewModelScope.launch {
            playerController.playMedia(media)
            loadLyrics(media)
        }
    }

    /**
     * Play/pause toggle
     */
    fun togglePlayPause() {
        playerController.togglePlayPause()
    }

    /**
     * Play audio
     */
    fun play() {
        playerController.play()
    }

    /**
     * Pause audio
     */
    fun pause() {
        playerController.pause()
    }

    /**
     * Seek to position
     */
    fun seekTo(position: Long) {
        playerController.seekTo(position)
    }

    /**
     * Skip to next track
     */
    fun skipToNext() {
        playerController.playNext()
    }

    /**
     * Skip to previous track
     */
    fun skipToPrevious() {
        playerController.playPrevious()
    }

    /**
     * Set volume (0.0 to 1.0)
     */
    fun setVolume(volume: Float) {
        // Volume is handled by PlayerController through device volume
        // This could be enhanced for app-specific volume control
    }

    /**
     * Toggle shuffle mode
     */
    fun toggleShuffle() {
        playerController.toggleShuffle()
    }

    /**
     * Set repeat mode
     */
    fun setRepeatMode(mode: Int) {
        playerController.setRepeatMode(mode)
    }

    /**
     * Set playback speed
     */
    fun setPlaybackSpeed(speed: Float) {
        playerController.setPlaybackSpeed(speed)
    }

    /**
     * Show/hide equalizer
     */
    fun toggleEqualizer() {
        _showEqualizer.value = !_showEqualizer.value
    }

    /**
     * Show/hide sleep timer
     */
    fun toggleSleepTimer() {
        _showSleepTimer.value = !_showSleepTimer.value
    }

    /**
     * Set sleep timer
     */
    fun setSleepTimer(minutes: Int) {
        viewModelScope.launch {
            _sleepTimerMinutes.value = minutes
            if (minutes > 0) {
                // Implement sleep timer logic
                delay(minutes * 60 * 1000L)
                pause()
                _sleepTimerMinutes.value = 0
            }
        }
    }

    /**
     * Toggle lyrics display
     */
    fun toggleLyrics() {
        _showLyrics.value = !_showLyrics.value
    }

    /**
     * Load lyrics for current media
     */
    private fun loadLyrics(media: MediaFile) {
        viewModelScope.launch {
            // Implement lyrics loading logic
            // This could involve:
            // 1. Reading embedded lyrics from audio metadata
            // 2. Loading .lrc files from the same directory
            // 3. Online lyrics service integration
            _currentLyrics.value = "Lyrics not available"
        }
    }

    /**
     * Toggle audio visualizer
     */
    fun toggleAudioVisualizer() {
        _audioVisualizerEnabled.value = !_audioVisualizerEnabled.value
    }

    /**
     * Toggle crossfade
     */
    fun toggleCrossfade() {
        _crossfadeEnabled.value = !_crossfadeEnabled.value
    }

    /**
     * Set crossfade duration
     */
    fun setCrossfadeDuration(durationMs: Long) {
        _crossfadeDurationMs.value = durationMs
    }

    /**
     * Add to queue
     */
    fun addToQueue(media: MediaFile) {
        playerController.addToQueue(media)
    }

    /**
     * Set queue and start playing
     */
    fun setQueue(mediaList: List<MediaFile>, startIndex: Int = 0) {
        playerController.setQueue(mediaList, startIndex)
    }

    /**
     * Get current queue
     */
    fun getCurrentQueue() = playerController.getCurrentQueue()

    /**
     * Clear queue
     */
    fun clearQueue() {
        playerController.clearQueue()
    }

    /**
     * Update position periodically
     */
    fun updatePosition() {
        playerController.updateCurrentPosition()
    }

    override fun onCleared() {
        super.onCleared()
        // PlayerController lifecycle is managed separately
    }
}



