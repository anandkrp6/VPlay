package com.bytecoder.vplay.backend.managers

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.bytecoder.vplay.backend.managers.SubtitleTrack
import com.bytecoder.vplay.backend.models.Video
import com.bytecoder.vplay.backend.controllers.PlayerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VideoPlayerScreenManager(application: Application) : AndroidViewModel(application) {
    
    private val playerController = PlayerController.getInstance()
    
    // State flows for video player
    private val _currentMedia = MutableStateFlow<Video?>(null)
    val currentMedia: StateFlow<Video?> = _currentMedia.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()
    
    private val _brightness = MutableStateFlow(0.5f)
    val brightness: StateFlow<Float> = _brightness.asStateFlow()
    
    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()
    
    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()
    
    // Subtitle-related state
    private val _availableSubtitles = MutableStateFlow<List<SubtitleTrack>>(emptyList())
    val availableSubtitles: StateFlow<List<SubtitleTrack>> = _availableSubtitles.asStateFlow()
    
    private val _selectedSubtitleTrack = MutableStateFlow<SubtitleTrack?>(null)
    val selectedSubtitleTrack: StateFlow<SubtitleTrack?> = _selectedSubtitleTrack.asStateFlow()
    
    private val _subtitlesEnabled = MutableStateFlow(false)
    val subtitlesEnabled: StateFlow<Boolean> = _subtitlesEnabled.asStateFlow()
    
    private val _showSubtitleDialog = MutableStateFlow(false)
    val showSubtitleDialog: StateFlow<Boolean> = _showSubtitleDialog.asStateFlow()
    
    init {
        // Start observing player state
        observePlayerState()
    }
    
    private fun observePlayerState() {
        viewModelScope.launch {
            // Update state flows based on player controller state
            playerController.isPlaying.collect { playing ->
                _isPlaying.value = playing
            }
        }
        
        viewModelScope.launch {
            playerController.currentPosition.collect { position ->
                _currentPosition.value = position
            }
        }
        
        viewModelScope.launch {
            playerController.duration.collect { dur ->
                _duration.value = dur
            }
        }
    }
    
    fun initializePlayer() {
        playerController.initializePlayer(getApplication<Application>().applicationContext)
    }
    
    fun playMedia(video: Video) {
        viewModelScope.launch {
            _currentMedia.value = video
            val mediaItem = MediaItem.fromUri(Uri.parse(video.path))
            playerController.playMedia(mediaItem)
        }
    }
    
    fun seekTo(position: Long) {
        playerController.seekTo(position)
        _currentPosition.value = position
    }
    
    fun setBrightness(brightness: Float) {
        _brightness.value = brightness.coerceIn(0f, 1f)
    }
    
    fun setVolume(volume: Float) {
        _volume.value = volume.coerceIn(0f, 1f)
        playerController.setVolume(volume)
    }
    
    fun toggleMute() {
        val newMutedState = !_isMuted.value
        _isMuted.value = newMutedState
        playerController.setVolume(if (newMutedState) 0f else _volume.value)
    }
    
    fun toggleFullscreen() {
        _isFullscreen.value = !_isFullscreen.value
    }
    
    fun loadExternalSubtitle(uri: Uri, language: String) {
        viewModelScope.launch {
            // Create subtitle track
            val subtitleTrack = SubtitleTrack(
                id = uri.toString(),
                language = language,
                uri = uri
            )
            
            // Add to available subtitles
            val currentSubtitles = _availableSubtitles.value.toMutableList()
            currentSubtitles.add(subtitleTrack)
            _availableSubtitles.value = currentSubtitles
            
            // Auto-select if it's the first subtitle
            if (_selectedSubtitleTrack.value == null) {
                _selectedSubtitleTrack.value = subtitleTrack
                _subtitlesEnabled.value = true
            }
        }
    }
    
    fun selectSubtitleTrack(track: SubtitleTrack?) {
        _selectedSubtitleTrack.value = track
        _subtitlesEnabled.value = track != null
    }
    
    fun toggleSubtitles() {
        _subtitlesEnabled.value = !_subtitlesEnabled.value
        if (!_subtitlesEnabled.value) {
            _selectedSubtitleTrack.value = null
        }
    }
    
    fun showSubtitleDialog() {
        _showSubtitleDialog.value = true
    }
    
    fun hideSubtitleDialog() {
        _showSubtitleDialog.value = false
    }
    
    fun getPlayer(): ExoPlayer? {
        return playerController.getCurrentPlayer()
    }
    
    fun play() {
        playerController.play()
    }
    
    fun pause() {
        playerController.pause()
    }
    
    fun stop() {
        playerController.stop()
    }
    
    override fun onCleared() {
        super.onCleared()
        playerController.release()
    }
}

