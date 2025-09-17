package com.bytecoder.vplay.frontend.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bytecoder.vplay.backend.data.models.MediaItemModel
import com.bytecoder.vplay.backend.managers.AdvancedPlayerManager
import com.bytecoder.vplay.backend.managers.PlayerManager
import com.bytecoder.vplay.backend.managers.SubtitleTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VideoPlayerViewModel(application: Application) : AndroidViewModel(application) {
    
    // Use existing PlayerManager for basic playback integration
    // and AdvancedPlayerManager for subtitle-specific functionality
    private val advancedPlayerManager = AdvancedPlayerManager(application)
    
    private val _currentMedia = MutableStateFlow<MediaItemModel?>(null)
    val currentMedia: StateFlow<MediaItemModel?> = _currentMedia.asStateFlow()
    
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
    
    // Subtitle-related state flows
    private val _availableSubtitles = MutableStateFlow<List<SubtitleTrack>>(emptyList())
    val availableSubtitles: StateFlow<List<SubtitleTrack>> = _availableSubtitles.asStateFlow()
    
    private val _selectedSubtitleTrack = MutableStateFlow<SubtitleTrack?>(null)
    val selectedSubtitleTrack: StateFlow<SubtitleTrack?> = _selectedSubtitleTrack.asStateFlow()
    
    private val _subtitlesEnabled = MutableStateFlow(false)
    val subtitlesEnabled: StateFlow<Boolean> = _subtitlesEnabled.asStateFlow()
    
    private val _showSubtitleDialog = MutableStateFlow(false)
    val showSubtitleDialog: StateFlow<Boolean> = _showSubtitleDialog.asStateFlow()
    
    init {
        // Observe subtitle tracks from AdvancedPlayerManager
        viewModelScope.launch {
            advancedPlayerManager.subtitleTracks.collect { tracks ->
                _availableSubtitles.value = tracks
                // Update selected track based on currently selected
                val currentSelected = tracks.find { it.isSelected }
                _selectedSubtitleTrack.value = currentSelected
                _subtitlesEnabled.value = currentSelected != null
            }
        }
        
        // Sync with PlayerManager state
        PlayerManager.isPlaying.observeForever { isPlaying ->
            _isPlaying.value = isPlaying
        }
        PlayerManager.position.observeForever { position ->
            _currentPosition.value = position
        }
        PlayerManager.duration.observeForever { duration ->
            _duration.value = duration
        }
    }
    
    fun play() {
        viewModelScope.launch {
            _isPlaying.value = true
            PlayerManager.playPause() // Toggles play/pause, will play if paused
        }
    }
    
    fun pause() {
        viewModelScope.launch {
            _isPlaying.value = false
            PlayerManager.playPause() // Toggles play/pause, will pause if playing
        }
    }
    
    fun seekTo(position: Long) {
        viewModelScope.launch {
            _currentPosition.value = position
            PlayerManager.seekTo(position)
        }
    }
    
    fun skipToNext() {
        viewModelScope.launch {
            PlayerManager.next()
        }
    }
    
    fun skipToPrevious() {
        viewModelScope.launch {
            PlayerManager.previous()
        }
    }
    
    fun setVolume(volume: Float) {
        viewModelScope.launch {
            _volume.value = volume.coerceIn(0f, 1f)
            // Note: Volume control would typically be handled at the Android system level
        }
    }
    
    fun setBrightness(brightness: Float) {
        viewModelScope.launch {
            _brightness.value = brightness.coerceIn(0f, 1f)
            // Note: Brightness setting would typically be handled at the Activity level
        }
    }
    
    fun setMuted(muted: Boolean) {
        viewModelScope.launch {
            _isMuted.value = muted
            // Note: Mute functionality would need to be implemented via volume control
        }
    }
    
    fun setFullscreen(fullscreen: Boolean) {
        viewModelScope.launch {
            _isFullscreen.value = fullscreen
            // Implement fullscreen logic
        }
    }
    
    fun enterPictureInPicture() {
        viewModelScope.launch {
            // Implement PiP logic
        }
    }
    
    fun playMedia(media: MediaItemModel) {
        viewModelScope.launch {
            _currentMedia.value = media
            _duration.value = media.durationMs
            _currentPosition.value = 0L
            play()
        }
    }
    
    // Subtitle management functions
    fun selectSubtitleTrack(track: SubtitleTrack?) {
        viewModelScope.launch {
            advancedPlayerManager.selectSubtitleTrack(track?.id)
            _selectedSubtitleTrack.value = track
            _subtitlesEnabled.value = track != null
        }
    }
    
    fun toggleSubtitles() {
        viewModelScope.launch {
            val currentTrack = _selectedSubtitleTrack.value
            if (_subtitlesEnabled.value && currentTrack != null) {
                // Disable subtitles
                selectSubtitleTrack(null)
            } else if (!_subtitlesEnabled.value && _availableSubtitles.value.isNotEmpty()) {
                // Enable first available subtitle track
                selectSubtitleTrack(_availableSubtitles.value.first())
            }
        }
    }
    
    fun showSubtitleDialog() {
        _showSubtitleDialog.value = true
    }
    
    fun hideSubtitleDialog() {
        _showSubtitleDialog.value = false
    }
    
    fun loadExternalSubtitle(uri: Uri, language: String) {
        viewModelScope.launch {
            advancedPlayerManager.loadExternalSubtitle(uri, language)
            // The subtitle tracks will be updated automatically through the StateFlow
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        advancedPlayerManager.release()
    }
}