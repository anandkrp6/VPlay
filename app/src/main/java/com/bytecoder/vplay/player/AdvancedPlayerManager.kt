package com.bytecoder.vplay.player

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.source.SingleSampleMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.datasource.DefaultDataSourceFactory
import com.bytecoder.vplay.analytics.AnalyticsManager
import com.bytecoder.vplay.settings.EqualizerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import android.media.MediaMetadataRetriever
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.audiofx.Equalizer
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher

// Data classes for advanced player features
data class SubtitleTrack(
    val id: String,
    val language: String,
    val label: String,
    val uri: Uri,
    val mimeType: String,
    val isSelected: Boolean = false,
    val isDefault: Boolean = false
)

data class AudioTrack(
    val id: String,
    val language: String,
    val label: String,
    val channels: Int,
    val bitrate: Int,
    val isSelected: Boolean = false,
    val isDefault: Boolean = false
)

data class VideoTrack(
    val id: String,
    val label: String,
    val width: Int,
    val height: Int,
    val bitrate: Int,
    val frameRate: Float,
    val isSelected: Boolean = false,
    val isDefault: Boolean = false
)

data class ChapterInfo(
    val id: String,
    val title: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val thumbnailUri: Uri? = null
)

data class ABRepeatState(
    val isEnabled: Boolean = false,
    val pointA: Long? = null,
    val pointB: Long? = null
)

data class PlaybackSpeed(
    val speed: Float,
    val label: String
) {
    companion object {
        val AVAILABLE_SPEEDS = listOf(
            PlaybackSpeed(0.25f, "0.25x"),
            PlaybackSpeed(0.5f, "0.5x"),
            PlaybackSpeed(0.75f, "0.75x"),
            PlaybackSpeed(1.0f, "Normal"),
            PlaybackSpeed(1.25f, "1.25x"),
            PlaybackSpeed(1.5f, "1.5x"),
            PlaybackSpeed(1.75f, "1.75x"),
            PlaybackSpeed(2.0f, "2x"),
            PlaybackSpeed(2.5f, "2.5x"),
            PlaybackSpeed(3.0f, "3x")
        )
    }
}

enum class GestureAction {
    SEEK_FORWARD,
    SEEK_BACKWARD,
    VOLUME_UP,
    VOLUME_DOWN,
    BRIGHTNESS_UP,
    BRIGHTNESS_DOWN,
    PLAY_PAUSE,
    NONE
}

data class GestureSettings(
    val doubleTapToSeek: Boolean = true,
    val seekInterval: Long = 10000, // 10 seconds
    val swipeToSeek: Boolean = true,
    val verticalSwipeVolume: Boolean = true,
    val verticalSwipeBrightness: Boolean = true,
    val pinchToZoom: Boolean = true,
    val longPressSpeedChange: Boolean = true,
    val longPressSpeedMultiplier: Float = 2.0f
)

data class EqualizerBand(
    val frequency: Int, // in Hz
    val gain: Float,   // in dB (-15.0 to +15.0)
    val label: String
)

data class EqualizerPreset(
    val id: String,
    val name: String,
    val bands: List<EqualizerBand>,
    val isCustom: Boolean = false
)

@OptIn(UnstableApi::class)
class AdvancedPlayerManager(private val context: Context) {
    
    // Analytics is handled by the singleton object
    private var exoPlayer: ExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null
    
    // State flows for reactive UI updates
    private val _subtitleTracks = MutableStateFlow<List<SubtitleTrack>>(emptyList())
    val subtitleTracks: StateFlow<List<SubtitleTrack>> = _subtitleTracks.asStateFlow()
    
    private val _audioTracks = MutableStateFlow<List<AudioTrack>>(emptyList())
    val audioTracks: StateFlow<List<AudioTrack>> = _audioTracks.asStateFlow()
    
    private val _videoTracks = MutableStateFlow<List<VideoTrack>>(emptyList())
    val videoTracks: StateFlow<List<VideoTrack>> = _videoTracks.asStateFlow()
    
    private val _chapters = MutableStateFlow<List<ChapterInfo>>(emptyList())
    val chapters: StateFlow<List<ChapterInfo>> = _chapters.asStateFlow()
    
    private val _abRepeatState = MutableStateFlow(ABRepeatState())
    val abRepeatState: StateFlow<ABRepeatState> = _abRepeatState.asStateFlow()
    
    private val _currentSpeed = MutableStateFlow(PlaybackSpeed.AVAILABLE_SPEEDS[3]) // Normal speed
    val currentSpeed: StateFlow<PlaybackSpeed> = _currentSpeed.asStateFlow()
    
    private val _gestureSettings = MutableStateFlow(GestureSettings())
    val gestureSettings: StateFlow<GestureSettings> = _gestureSettings.asStateFlow()
    
    private val _equalizerPresets = MutableStateFlow(getDefaultEqualizerPresets())
    val equalizerPresets: StateFlow<List<EqualizerPreset>> = _equalizerPresets.asStateFlow()
    
    private val _currentEqualizerPreset = MutableStateFlow<EqualizerPreset?>(null)
    val currentEqualizerPreset: StateFlow<EqualizerPreset?> = _currentEqualizerPreset.asStateFlow()
    
    private val _isEqualizerEnabled = MutableStateFlow(false)
    val isEqualizerEnabled: StateFlow<Boolean> = _isEqualizerEnabled.asStateFlow()
    
    // Player initialization
    fun initializePlayer(): ExoPlayer {
        if (exoPlayer == null) {
            trackSelector = DefaultTrackSelector(context)
            exoPlayer = ExoPlayer.Builder(context)
                .setTrackSelector(trackSelector!!)
                .build()
                
            setupPlayerListener()
            initializeAudioProcessor()
        }
        return exoPlayer!!
    }
    
    private fun setupPlayerListener() {
        exoPlayer?.addListener(object : Player.Listener {
            override fun onTracksChanged(tracks: Tracks) {
                updateAvailableTracks(tracks)
            }
            
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                checkABRepeat()
            }
            
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                _currentSpeed.value = PlaybackSpeed.AVAILABLE_SPEEDS.find { 
                    it.speed == playbackParameters.speed 
                } ?: PlaybackSpeed.AVAILABLE_SPEEDS[3]
            }
        })
    }
    
    // Media loading with subtitles and multiple tracks
    fun loadMedia(
        mediaUri: Uri,
        subtitleUris: List<Pair<Uri, String>> = emptyList(), // Uri to language pairs
        startPosition: Long = 0
    ) {
        val player = initializePlayer()
        
        try {
            val dataSourceFactory = DefaultDataSourceFactory(context, "VPlay")
            val extractorsFactory = DefaultExtractorsFactory()
            
            // Create main media source
            val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory)
                .createMediaSource(MediaItem.fromUri(mediaUri))
            
            // Create subtitle sources if any
            val subtitleSources = subtitleUris.map { (uri, language) ->
                val subtitleMediaItem = MediaItem.SubtitleConfiguration.Builder(uri)
                    .setMimeType(getMimeTypeFromUri(uri))
                    .setLanguage(language)
                    .build()
                
                SingleSampleMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(subtitleMediaItem, C.TIME_UNSET)
            }
            
            // Merge video and subtitle sources
            val finalSource: MediaSource = if (subtitleSources.isNotEmpty()) {
                MergingMediaSource(*arrayOf(videoSource, *subtitleSources.toTypedArray()))
            } else {
                videoSource
            }
            
            player.setMediaSource(finalSource)
            player.seekTo(startPosition)
            player.prepare()
            
            // Load chapters if available
            loadChapters(mediaUri)
            
            AnalyticsManager.trackFeatureUsage("advanced_player_media_loaded", mapOf(
                "media_uri" to mediaUri.toString(),
                "subtitle_count" to subtitleUris.size.toString(),
                "start_position" to startPosition.toString()
            ))
            
        } catch (e: Exception) {
            AnalyticsManager.trackError("advanced_player_load_failed", e.message)
        }
    }
    
    // Subtitle management
    fun getAvailableSubtitleTracks(): List<SubtitleTrack> = _subtitleTracks.value
    
    fun selectSubtitleTrack(trackId: String?) {
        trackSelector?.let { selector ->
            val player = exoPlayer ?: return
            
            if (trackId == null) {
                // Disable subtitles
                selector.setParameters(
                    selector.buildUponParameters()
                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                )
            } else {
                // Enable specific subtitle track
                val tracks = player.currentTracks
                for (trackGroup in tracks.groups) {
                    if (trackGroup.type == C.TRACK_TYPE_TEXT) {
                        for (i in 0 until trackGroup.length) {
                            val format = trackGroup.getTrackFormat(i)
                            if (format.id == trackId) {
                                val override = TrackSelectionOverride(trackGroup.mediaTrackGroup, i)
                                selector.setParameters(
                                    selector.buildUponParameters()
                                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                                        .addOverride(override)
                                )
                                break
                            }
                        }
                    }
                }
            }
            
            AnalyticsManager.trackFeatureUsage("subtitle_track_selected", mapOf(
                "track_id" to (trackId ?: "disabled")
            ))
        }
    }
    
    fun loadExternalSubtitle(subtitleUri: Uri, language: String) {
        try {
            // Create a new subtitle track from the external file
            val mimeType = getMimeTypeFromUri(subtitleUri)
            val label = "${language} (External)"
            
            val externalSubtitle = SubtitleTrack(
                id = "external_${System.currentTimeMillis()}",
                language = language,
                label = label,
                uri = subtitleUri,
                mimeType = mimeType,
                isSelected = false
            )
            
            // Add to current subtitle tracks
            val currentTracks = _subtitleTracks.value.toMutableList()
            currentTracks.add(externalSubtitle)
            _subtitleTracks.value = currentTracks
            
            // Create new media source with the external subtitle
            val player = exoPlayer ?: return
            val currentMediaItem = player.currentMediaItem ?: return
            
            // Create subtitle media source
            val subtitleMediaItem = MediaItem.SubtitleConfiguration.Builder(subtitleUri)
                .setMimeType(mimeType)
                .setLanguage(language)
                .setLabel(label)
                .build()
            
            val subtitleSource = SingleSampleMediaSource.Factory(
                DefaultDataSourceFactory(context, "VPlay")
            ).createMediaSource(
                subtitleMediaItem,
                C.TIME_UNSET
            )
            
            // Get current video source and merge with new subtitle
            val currentSource = player.currentMediaItem?.let { mediaItem ->
                ProgressiveMediaSource.Factory(
                    DefaultDataSourceFactory(context, "VPlay"),
                    DefaultExtractorsFactory()
                ).createMediaSource(mediaItem)
            }
            
            if (currentSource != null) {
                val mergedSource = MergingMediaSource(currentSource, subtitleSource)
                val currentPosition = player.currentPosition
                player.setMediaSource(mergedSource)
                player.seekTo(currentPosition)
                player.prepare()
            }
            
            AnalyticsManager.trackFeatureUsage("external_subtitle_loaded", mapOf(
                "language" to language,
                "mime_type" to mimeType
            ))
            
        } catch (e: Exception) {
            AnalyticsManager.trackError("external_subtitle_load_failed", e.message)
        }
    }
    
    // Audio track management
    fun getAvailableAudioTracks(): List<AudioTrack> = _audioTracks.value
    
    fun selectAudioTrack(trackId: String) {
        trackSelector?.let { selector ->
            val player = exoPlayer ?: return
            
            val tracks = player.currentTracks
            for (trackGroup in tracks.groups) {
                if (trackGroup.type == C.TRACK_TYPE_AUDIO) {
                    for (i in 0 until trackGroup.length) {
                        val format = trackGroup.getTrackFormat(i)
                        if (format.id == trackId) {
                            val override = TrackSelectionOverride(trackGroup.mediaTrackGroup, i)
                            selector.setParameters(
                                selector.buildUponParameters()
                                    .addOverride(override)
                            )
                            break
                        }
                    }
                }
            }
            
            AnalyticsManager.trackFeatureUsage("audio_track_selected", mapOf(
                "track_id" to trackId
            ))
        }
    }
    
    // Video quality management
    fun getAvailableVideoTracks(): List<VideoTrack> = _videoTracks.value
    
    fun selectVideoQuality(trackId: String?) {
        trackSelector?.let { selector ->
            if (trackId == null) {
                // Auto quality
                selector.setParameters(
                    selector.buildUponParameters()
                        .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                )
            } else {
                val player = exoPlayer ?: return
                val tracks = player.currentTracks
                
                for (trackGroup in tracks.groups) {
                    if (trackGroup.type == C.TRACK_TYPE_VIDEO) {
                        for (i in 0 until trackGroup.length) {
                            val format = trackGroup.getTrackFormat(i)
                            if (format.id == trackId) {
                                val override = TrackSelectionOverride(trackGroup.mediaTrackGroup, i)
                                selector.setParameters(
                                    selector.buildUponParameters()
                                        .addOverride(override)
                                )
                                break
                            }
                        }
                    }
                }
            }
            
            AnalyticsManager.trackFeatureUsage("video_quality_selected", mapOf(
                "track_id" to (trackId ?: "auto")
            ))
        }
    }
    
    // A-B Repeat functionality
    fun setABRepeatPointA() {
        val currentPosition = exoPlayer?.currentPosition ?: return
        val currentState = _abRepeatState.value
        
        _abRepeatState.value = currentState.copy(
            pointA = currentPosition,
            pointB = if (currentState.pointB != null && currentState.pointB <= currentPosition) null else currentState.pointB
        )
        
        AnalyticsManager.trackFeatureUsage("ab_repeat_point_a_set", mapOf(
            "position" to currentPosition.toString()
        ))
    }
    
    fun setABRepeatPointB() {
        val currentPosition = exoPlayer?.currentPosition ?: return
        val currentState = _abRepeatState.value
        
        if (currentState.pointA != null && currentPosition > currentState.pointA) {
            _abRepeatState.value = currentState.copy(
                pointB = currentPosition,
                isEnabled = true
            )
            
            AnalyticsManager.trackFeatureUsage("ab_repeat_point_b_set", mapOf(
                "position" to currentPosition.toString()
            ))
        }
    }
    
    fun clearABRepeat() {
        _abRepeatState.value = ABRepeatState()
        AnalyticsManager.trackFeatureUsage("ab_repeat_cleared")
    }
    
    fun toggleABRepeat() {
        val currentState = _abRepeatState.value
        if (currentState.pointA != null && currentState.pointB != null) {
            _abRepeatState.value = currentState.copy(isEnabled = !currentState.isEnabled)
            AnalyticsManager.trackFeatureUsage("ab_repeat_toggled", mapOf(
                "enabled" to currentState.isEnabled.toString()
            ))
        }
    }
    
    private fun checkABRepeat() {
        val state = _abRepeatState.value
        if (state.isEnabled && state.pointA != null && state.pointB != null) {
            val currentPosition = exoPlayer?.currentPosition ?: return
            
            if (currentPosition >= state.pointB) {
                exoPlayer?.seekTo(state.pointA)
            }
        }
    }
    
    // Playback speed control
    fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackParameters(
            PlaybackParameters(speed)
        )
        
        _currentSpeed.value = PlaybackSpeed.AVAILABLE_SPEEDS.find { it.speed == speed }
            ?: PlaybackSpeed(speed, "${speed}x")
        
        AnalyticsManager.trackFeatureUsage("playback_speed_changed", mapOf(
            "speed" to speed.toString()
        ))
    }
    
    fun getAvailablePlaybackSpeeds(): List<PlaybackSpeed> = PlaybackSpeed.AVAILABLE_SPEEDS
    
    // Chapter navigation
    fun getChapters(): List<ChapterInfo> = _chapters.value
    
    fun seekToChapter(chapterId: String) {
        val chapter = _chapters.value.find { it.id == chapterId } ?: return
        exoPlayer?.seekTo(chapter.startTimeMs)
        
        AnalyticsManager.trackFeatureUsage("chapter_selected", mapOf(
            "chapter_id" to chapterId,
            "chapter_title" to chapter.title
        ))
    }
    
    fun getCurrentChapter(): ChapterInfo? {
        val currentPosition = exoPlayer?.currentPosition ?: return null
        return _chapters.value.find { 
            currentPosition >= it.startTimeMs && currentPosition < it.endTimeMs 
        }
    }
    
    private fun loadChapters(mediaUri: Uri) {
        try {
            // Try to extract chapters from metadata first
            val chapters = extractChaptersFromMetadata(mediaUri)
            
            if (chapters.isNotEmpty()) {
                _chapters.value = chapters
                AnalyticsManager.trackFeatureUsage("chapters_loaded_from_metadata", mapOf(
                    "chapter_count" to chapters.size.toString()
                ))
                return
            }
            
            // Try to load from external chapter file
            val externalChapters = loadChaptersFromFile(mediaUri)
            if (externalChapters.isNotEmpty()) {
                _chapters.value = externalChapters
                AnalyticsManager.trackFeatureUsage("chapters_loaded_from_file", mapOf(
                    "chapter_count" to externalChapters.size.toString()
                ))
                return
            }
            
            // Fall back to sample chapters for demo purposes
            val sampleChapters = listOf(
                ChapterInfo("1", "Opening", 0, 300000),
                ChapterInfo("2", "Main Content", 300000, 1800000),
                ChapterInfo("3", "Conclusion", 1800000, Long.MAX_VALUE)
            )
            _chapters.value = sampleChapters
            
        } catch (e: Exception) {
            AnalyticsManager.trackError("chapter_loading_failed", e.message)
            _chapters.value = emptyList()
        }
    }
    
    private fun extractChaptersFromMetadata(mediaUri: Uri): List<ChapterInfo> {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, mediaUri)
            
            val chapters = mutableListOf<ChapterInfo>()
            
            // Try to extract chapter information using reflection (some formats support this)
            val chapterCount = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS)?.toIntOrNull() ?: 0
            
            for (i in 0 until chapterCount) {
                try {
                    // Extract chapter information - this is format-dependent
                    val chapterTitle = "Chapter ${i + 1}"
                    val startTime = i * 300000L // Default 5-minute chapters
                    val endTime = (i + 1) * 300000L
                    
                    chapters.add(ChapterInfo(
                        id = "meta_$i",
                        title = chapterTitle,
                        startTimeMs = startTime,
                        endTimeMs = endTime
                    ))
                } catch (e: Exception) {
                    // Continue if individual chapter extraction fails
                }
            }
            
            retriever.release()
            chapters
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun loadChaptersFromFile(mediaUri: Uri): List<ChapterInfo> {
        return try {
            val mediaPath = mediaUri.path ?: return emptyList()
            val baseName = File(mediaPath).nameWithoutExtension
            val parentDir = File(mediaPath).parent ?: return emptyList()
            
            // Look for chapter files with common naming patterns
            val chapterFilePatterns = listOf(
                "$baseName.chapters.txt",
                "$baseName.chapters",
                "$baseName.chp",
                "chapters.txt"
            )
            
            for (pattern in chapterFilePatterns) {
                val chapterFile = File(parentDir, pattern)
                if (chapterFile.exists()) {
                    return parseChapterFile(chapterFile)
                }
            }
            
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun parseChapterFile(chapterFile: File): List<ChapterInfo> {
        return try {
            val chapters = mutableListOf<ChapterInfo>()
            val lines = chapterFile.readLines()
            
            var chapterIndex = 1
            for (line in lines) {
                if (line.trim().isNotEmpty()) {
                    // Parse different chapter file formats
                    when {
                        // Format: 00:01:30 Chapter Title
                        line.matches(Regex("\\d{2}:\\d{2}:\\d{2}\\s+.*")) -> {
                            val parts = line.split("\\s+".toRegex(), 2)
                            if (parts.size >= 2) {
                                val timeMs = parseTimeToMs(parts[0])
                                val title = parts[1]
                                chapters.add(ChapterInfo(
                                    id = "file_$chapterIndex",
                                    title = title,
                                    startTimeMs = timeMs,
                                    endTimeMs = Long.MAX_VALUE // Will be updated
                                ))
                                chapterIndex++
                            }
                        }
                        // Format: Chapter Title (simple list)
                        else -> {
                            val timeMs = (chapterIndex - 1) * 300000L // Default 5-minute spacing
                            chapters.add(ChapterInfo(
                                id = "file_$chapterIndex",
                                title = line.trim(),
                                startTimeMs = timeMs,
                                endTimeMs = timeMs + 300000L
                            ))
                            chapterIndex++
                        }
                    }
                }
            }
            
            // Update end times
            for (i in 0 until chapters.size - 1) {
                chapters[i] = chapters[i].copy(endTimeMs = chapters[i + 1].startTimeMs)
            }
            
            chapters
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun parseTimeToMs(timeString: String): Long {
        return try {
            val parts = timeString.split(":")
            when (parts.size) {
                3 -> { // HH:MM:SS
                    val hours = parts[0].toLong()
                    val minutes = parts[1].toLong()
                    val seconds = parts[2].toLong()
                    (hours * 3600 + minutes * 60 + seconds) * 1000
                }
                2 -> { // MM:SS
                    val minutes = parts[0].toLong()
                    val seconds = parts[1].toLong()
                    (minutes * 60 + seconds) * 1000
                }
                else -> 0L
            }
        } catch (e: Exception) {
            0L
        }
    }
    
    // Audio processor integration
    private fun saveEqualizerSettings(preset: EqualizerPreset) {
        try {
            val prefs = context.getSharedPreferences("vplay_eq", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            
            // Save current preset ID
            editor.putString("current_preset_id", preset.id)
            editor.putString("current_preset_name", preset.name)
            
            // Save individual band levels
            preset.bands.forEachIndexed { index, band ->
                editor.putFloat("band_${index}_freq", band.frequency.toFloat())
                editor.putFloat("band_${index}_gain", band.gain)
                editor.putString("band_${index}_label", band.label)
            }
            
            editor.putInt("band_count", preset.bands.size)
            editor.apply()
            
        } catch (e: Exception) {
            AnalyticsManager.trackError("equalizer_settings_save_failed", e.message)
        }
    }
    
    private fun loadSavedEqualizerSettings() {
        try {
            val prefs = context.getSharedPreferences("vplay_eq", Context.MODE_PRIVATE)
            val presetId = prefs.getString("current_preset_id", "flat")
            val presetName = prefs.getString("current_preset_name", "Flat")
            val bandCount = prefs.getInt("band_count", 0)
            
            if (bandCount > 0) {
                val bands = mutableListOf<EqualizerBand>()
                for (i in 0 until bandCount) {
                    val frequency = prefs.getFloat("band_${i}_freq", 1000f).toInt()
                    val gain = prefs.getFloat("band_${i}_gain", 0f)
                    val label = prefs.getString("band_${i}_label", "${frequency}Hz") ?: "${frequency}Hz"
                    
                    bands.add(EqualizerBand(frequency, gain, label))
                }
                
                val savedPreset = EqualizerPreset(
                    id = presetId ?: "flat",
                    name = presetName ?: "Flat",
                    bands = bands,
                    isCustom = presetId?.startsWith("custom_") == true
                )
                
                _currentEqualizerPreset.value = savedPreset
            }
            
            // Load enabled state
            val isEnabled = prefs.getBoolean("equalizer_enabled", false)
            _isEqualizerEnabled.value = isEnabled
            
        } catch (e: Exception) {
            AnalyticsManager.trackError("equalizer_settings_load_failed", e.message)
        }
    }
    
    // Initialize audio processor when player is created
    private fun initializeAudioProcessor() {
        try {
            exoPlayer?.let { player ->
                // Attach equalizer to the player's audio session
                EqualizerController.attach(context)
                
                // Load saved settings
                loadSavedEqualizerSettings()
                
                // Apply current preset if available
                val currentPreset = _currentEqualizerPreset.value
                if (currentPreset?.bands?.isNotEmpty() == true) {
                    applyEqualizerPreset(currentPreset.id)
                }
                
                // Set enabled state
                EqualizerController.setEnabled(_isEqualizerEnabled.value)
            }
        } catch (e: Exception) {
            AnalyticsManager.trackError("audio_processor_init_failed", e.message)
        }
    }
    fun updateGestureSettings(settings: GestureSettings) {
        _gestureSettings.value = settings
        AnalyticsManager.trackFeatureUsage("gesture_settings_updated")
    }
    
    fun handleGesture(action: GestureAction, intensity: Float = 1.0f) {
        val settings = _gestureSettings.value
        val player = exoPlayer ?: return
        
        when (action) {
            GestureAction.SEEK_FORWARD -> {
                if (settings.doubleTapToSeek) {
                    val seekAmount = settings.seekInterval * intensity.toLong()
                    player.seekTo(player.currentPosition + seekAmount)
                    AnalyticsManager.trackFeatureUsage("gesture_seek_forward", mapOf(
                        "seek_amount" to seekAmount.toString()
                    ))
                }
            }
            GestureAction.SEEK_BACKWARD -> {
                if (settings.doubleTapToSeek) {
                    val seekAmount = settings.seekInterval * intensity.toLong()
                    player.seekTo(maxOf(0, player.currentPosition - seekAmount))
                    AnalyticsManager.trackFeatureUsage("gesture_seek_backward", mapOf(
                        "seek_amount" to seekAmount.toString()
                    ))
                }
            }
            GestureAction.PLAY_PAUSE -> {
                if (player.isPlaying) player.pause() else player.play()
                AnalyticsManager.trackFeatureUsage("gesture_play_pause")
            }
            else -> { /* Volume and brightness handled by UI layer */ }
        }
    }
    
    // Audio equalizer
    fun getEqualizerPresets(): List<EqualizerPreset> = _equalizerPresets.value
    
    fun applyEqualizerPreset(presetId: String) {
        val preset = _equalizerPresets.value.find { it.id == presetId } ?: return
        _currentEqualizerPreset.value = preset
        
        // Apply equalizer settings to audio processor
        try {
            EqualizerController.attach(context)
            
            // Apply each band setting
            preset.bands.forEachIndexed { index, band ->
                try {
                    // Convert dB to the range expected by Android Equalizer
                    val gainInMillibels = (band.gain * 100).toInt().toShort()
                    EqualizerController.setBandLevel(index.toShort(), gainInMillibels)
                } catch (e: Exception) {
                    // Continue with other bands if one fails
                }
            }
            
            // Save preset settings
            saveEqualizerSettings(preset)
            
        } catch (e: Exception) {
            AnalyticsManager.trackError("equalizer_preset_apply_failed", e.message)
        }
        
        AnalyticsManager.trackFeatureUsage("equalizer_preset_applied", mapOf(
            "preset_id" to presetId,
            "preset_name" to preset.name
        ))
    }
    
    fun createCustomEqualizerPreset(name: String, bands: List<EqualizerBand>): String {
        val presetId = "custom_${System.currentTimeMillis()}"
        val customPreset = EqualizerPreset(presetId, name, bands, true)
        
        val currentPresets = _equalizerPresets.value.toMutableList()
        currentPresets.add(customPreset)
        _equalizerPresets.value = currentPresets
        
        AnalyticsManager.trackFeatureUsage("custom_equalizer_preset_created", mapOf(
            "preset_name" to name
        ))
        
        return presetId
    }
    
    fun setEqualizerEnabled(enabled: Boolean) {
        _isEqualizerEnabled.value = enabled
        
        // Enable/disable equalizer in audio processor
        try {
            EqualizerController.attach(context)
            EqualizerController.setEnabled(enabled)
            
            // Save the enabled state
            val prefs = context.getSharedPreferences("vplay_eq", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("equalizer_enabled", enabled).apply()
            
        } catch (e: Exception) {
            AnalyticsManager.trackError("equalizer_toggle_failed", e.message)
        }
        
        AnalyticsManager.trackFeatureUsage("equalizer_toggled", mapOf(
            "enabled" to enabled.toString()
        ))
    }
    
    fun updateEqualizerBand(frequency: Int, gain: Float) {
        try {
            // Find the band index for the given frequency
            val currentPreset = _currentEqualizerPreset.value
            val bandIndex = currentPreset?.bands?.indexOfFirst { it.frequency == frequency } ?: -1
            
            if (bandIndex >= 0 && currentPreset != null) {
                // Update the current preset
                val updatedBands = currentPreset.bands.toMutableList()
                updatedBands[bandIndex] = updatedBands[bandIndex].copy(gain = gain)
                
                val updatedPreset = currentPreset.copy(bands = updatedBands)
                _currentEqualizerPreset.value = updatedPreset
                
                // Apply to audio processor
                EqualizerController.attach(context)
                val gainInMillibels = (gain * 100).toInt().toShort()
                EqualizerController.setBandLevel(bandIndex.toShort(), gainInMillibels)
                
                // Save settings
                saveEqualizerSettings(updatedPreset)
                
                // Update preset in the list if it's a custom preset
                if (updatedPreset.isCustom) {
                    val presets = _equalizerPresets.value.toMutableList()
                    val presetIndex = presets.indexOfFirst { it.id == updatedPreset.id }
                    if (presetIndex >= 0) {
                        presets[presetIndex] = updatedPreset
                        _equalizerPresets.value = presets
                    }
                }
            }
            
        } catch (e: Exception) {
            AnalyticsManager.trackError("equalizer_band_update_failed", e.message)
        }
        
        AnalyticsManager.trackFeatureUsage("equalizer_band_updated", mapOf(
            "frequency" to frequency.toString(),
            "gain" to gain.toString()
        ))
    }
    
    // Helper methods
    private fun updateAvailableTracks(tracks: Tracks) {
        val subtitles = mutableListOf<SubtitleTrack>()
        val audioTracks = mutableListOf<AudioTrack>()
        val videoTracks = mutableListOf<VideoTrack>()
        
        for (trackGroup in tracks.groups) {
            when (trackGroup.type) {
                C.TRACK_TYPE_TEXT -> {
                    for (i in 0 until trackGroup.length) {
                        val format = trackGroup.getTrackFormat(i)
                        subtitles.add(
                            SubtitleTrack(
                                id = format.id ?: "subtitle_$i",
                                language = format.language ?: "Unknown",
                                label = format.label ?: "Subtitle $i",
                                uri = Uri.EMPTY, // Would be populated from source
                                mimeType = format.sampleMimeType ?: "",
                                isSelected = trackGroup.isSelected
                            )
                        )
                    }
                }
                C.TRACK_TYPE_AUDIO -> {
                    for (i in 0 until trackGroup.length) {
                        val format = trackGroup.getTrackFormat(i)
                        audioTracks.add(
                            AudioTrack(
                                id = format.id ?: "audio_$i",
                                language = format.language ?: "Unknown",
                                label = format.label ?: "Audio Track $i",
                                channels = format.channelCount,
                                bitrate = format.bitrate,
                                isSelected = trackGroup.isSelected
                            )
                        )
                    }
                }
                C.TRACK_TYPE_VIDEO -> {
                    for (i in 0 until trackGroup.length) {
                        val format = trackGroup.getTrackFormat(i)
                        videoTracks.add(
                            VideoTrack(
                                id = format.id ?: "video_$i",
                                label = "${format.width}×${format.height}",
                                width = format.width,
                                height = format.height,
                                bitrate = format.bitrate,
                                frameRate = format.frameRate,
                                isSelected = trackGroup.isSelected
                            )
                        )
                    }
                }
            }
        }
        
        _subtitleTracks.value = subtitles
        _audioTracks.value = audioTracks
        _videoTracks.value = videoTracks
    }
    
    private fun getMimeTypeFromUri(uri: Uri): String {
        val path = uri.path?.lowercase() ?: ""
        return when {
            path.endsWith(".srt") -> MimeTypes.APPLICATION_SUBRIP
            path.endsWith(".vtt") -> MimeTypes.TEXT_VTT
            path.endsWith(".ass") || path.endsWith(".ssa") -> MimeTypes.APPLICATION_SS
            path.endsWith(".ttml") -> MimeTypes.APPLICATION_TTML
            else -> MimeTypes.APPLICATION_SUBRIP
        }
    }
    
    private fun getDefaultEqualizerPresets(): List<EqualizerPreset> {
        return listOf(
            EqualizerPreset("flat", "Flat", createFlatBands()),
            EqualizerPreset("rock", "Rock", createRockBands()),
            EqualizerPreset("pop", "Pop", createPopBands()),
            EqualizerPreset("jazz", "Jazz", createJazzBands()),
            EqualizerPreset("classical", "Classical", createClassicalBands()),
            EqualizerPreset("bass_boost", "Bass Boost", createBassBoostBands()),
            EqualizerPreset("treble_boost", "Treble Boost", createTrebleBoostBands()),
            EqualizerPreset("vocal", "Vocal", createVocalBands())
        )
    }
    
    private fun createFlatBands(): List<EqualizerBand> {
        val frequencies = listOf(60, 170, 310, 600, 1000, 3000, 6000, 12000, 14000, 16000)
        return frequencies.map { freq ->
            EqualizerBand(freq, 0.0f, "${freq}Hz")
        }
    }
    
    private fun createRockBands(): List<EqualizerBand> {
        val gains = listOf(4.0f, 3.0f, -2.0f, -3.0f, -1.0f, 2.0f, 5.0f, 6.0f, 6.0f, 6.0f)
        return createBandsWithGains(gains)
    }
    
    private fun createPopBands(): List<EqualizerBand> {
        val gains = listOf(-1.0f, 2.0f, 4.0f, 4.0f, 3.0f, 0.0f, -1.0f, -1.0f, -1.0f, -1.0f)
        return createBandsWithGains(gains)
    }
    
    private fun createJazzBands(): List<EqualizerBand> {
        val gains = listOf(2.0f, 1.0f, 0.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 2.0f, 3.0f)
        return createBandsWithGains(gains)
    }
    
    private fun createClassicalBands(): List<EqualizerBand> {
        val gains = listOf(3.0f, 2.0f, -1.0f, -1.0f, -1.0f, -1.0f, -2.0f, 1.0f, 2.0f, 3.0f)
        return createBandsWithGains(gains)
    }
    
    private fun createBassBoostBands(): List<EqualizerBand> {
        val gains = listOf(6.0f, 5.0f, 4.0f, 2.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
        return createBandsWithGains(gains)
    }
    
    private fun createTrebleBoostBands(): List<EqualizerBand> {
        val gains = listOf(0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 2.0f, 4.0f, 5.0f, 6.0f, 6.0f)
        return createBandsWithGains(gains)
    }
    
    private fun createVocalBands(): List<EqualizerBand> {
        val gains = listOf(-2.0f, -1.0f, 1.0f, 3.0f, 4.0f, 4.0f, 3.0f, 1.0f, 0.0f, -1.0f)
        return createBandsWithGains(gains)
    }
    
    private fun createBandsWithGains(gains: List<Float>): List<EqualizerBand> {
        val frequencies = listOf(60, 170, 310, 600, 1000, 3000, 6000, 12000, 14000, 16000)
        return frequencies.zip(gains) { freq, gain ->
            EqualizerBand(freq, gain, "${freq}Hz")
        }
    }
    
    // Cleanup
    fun release() {
        exoPlayer?.release()
        exoPlayer = null
        trackSelector = null
    }
}
