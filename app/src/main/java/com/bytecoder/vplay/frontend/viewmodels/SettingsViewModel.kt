package com.bytecoder.vplay.frontend.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.bytecoder.vplay.backend.utils.AppSettings

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context: Context = application.applicationContext
    
    private val _darkModeEnabled = MutableStateFlow(false)
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled.asStateFlow()
    
    private val _dynamicThemeEnabled = MutableStateFlow(true)
    val dynamicThemeEnabled: StateFlow<Boolean> = _dynamicThemeEnabled.asStateFlow()
    
    private val _autoPlayEnabled = MutableStateFlow(true)
    val autoPlayEnabled: StateFlow<Boolean> = _autoPlayEnabled.asStateFlow()
    
    private val _shuffleByDefault = MutableStateFlow(false)
    val shuffleByDefault: StateFlow<Boolean> = _shuffleByDefault.asStateFlow()
    
    private val _crossfadeEnabled = MutableStateFlow(true)
    val crossfadeEnabled: StateFlow<Boolean> = _crossfadeEnabled.asStateFlow()
    
    private val _highQualityEnabled = MutableStateFlow(true)
    val highQualityEnabled: StateFlow<Boolean> = _highQualityEnabled.asStateFlow()
    
    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()
    
    private val _backgroundPlayEnabled = MutableStateFlow(true)
    val backgroundPlayEnabled: StateFlow<Boolean> = _backgroundPlayEnabled.asStateFlow()
    
    private val _masterVolume = MutableStateFlow(1.0f)
    val masterVolume: StateFlow<Float> = _masterVolume.asStateFlow()
    
    private val _bufferSize = MutableStateFlow(8192)
    val bufferSize: StateFlow<Int> = _bufferSize.asStateFlow()
    
    private val _autoBrightness = MutableStateFlow(true)
    val autoBrightness: StateFlow<Boolean> = _autoBrightness.asStateFlow()
    
    // Additional settings
    private val _gridViewEnabled = MutableStateFlow(false)
    val gridViewEnabled: StateFlow<Boolean> = _gridViewEnabled.asStateFlow()
    
    private val _animationsEnabled = MutableStateFlow(true)
    val animationsEnabled: StateFlow<Boolean> = _animationsEnabled.asStateFlow()
    
    private val _immersiveModeEnabled = MutableStateFlow(false)
    val immersiveModeEnabled: StateFlow<Boolean> = _immersiveModeEnabled.asStateFlow()
    
    private val _headphoneDetectionEnabled = MutableStateFlow(true)
    val headphoneDetectionEnabled: StateFlow<Boolean> = _headphoneDetectionEnabled.asStateFlow()
    
    private val _smartSkipEnabled = MutableStateFlow(false)
    val smartSkipEnabled: StateFlow<Boolean> = _smartSkipEnabled.asStateFlow()
    
    private val _defaultPlaybackSpeed = MutableStateFlow(1.0f)
    val defaultPlaybackSpeed: StateFlow<Float> = _defaultPlaybackSpeed.asStateFlow()
    
    private val _autoDownloadEnabled = MutableStateFlow(false)
    val autoDownloadEnabled: StateFlow<Boolean> = _autoDownloadEnabled.asStateFlow()
    
    private val _analyticsEnabled = MutableStateFlow(true)
    val analyticsEnabled: StateFlow<Boolean> = _analyticsEnabled.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            // Load from AppSettings
            _autoPlayEnabled.value = AppSettings.isAutoplayNext(context)
            _defaultPlaybackSpeed.value = AppSettings.getDefaultSpeed(context)
            
            // Load from SharedPreferences
            val prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            _gridViewEnabled.value = prefs.getBoolean("grid_view", false)
            _animationsEnabled.value = prefs.getBoolean("animations_enabled", true)
            _immersiveModeEnabled.value = prefs.getBoolean("immersive_mode", false)
            _headphoneDetectionEnabled.value = prefs.getBoolean("headphone_detection", true)
            _smartSkipEnabled.value = prefs.getBoolean("smart_skip", false)
            _autoDownloadEnabled.value = prefs.getBoolean("auto_download", false)
            _analyticsEnabled.value = prefs.getBoolean("analytics_enabled", true)
            
            // Theme mode: 0=system, 1=light, 2=dark
            val themeMode = AppSettings.getThemeMode(context)
            _darkModeEnabled.value = (themeMode == 2)
        }
    }
    
    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            _darkModeEnabled.value = enabled
            // Set theme mode: 0=system, 1=light, 2=dark
            val themeMode = if (enabled) 2 else 1
            AppSettings.setThemeMode(context, themeMode)
        }
    }
    
    fun setDynamicTheme(enabled: Boolean) {
        viewModelScope.launch {
            _dynamicThemeEnabled.value = enabled
            val prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("dynamic_theme", enabled).apply()
        }
    }
    
    fun setAutoPlay(enabled: Boolean) {
        viewModelScope.launch {
            _autoPlayEnabled.value = enabled
            AppSettings.setAutoplayNext(context, enabled)
        }
    }
    
    fun setShuffleByDefault(enabled: Boolean) {
        viewModelScope.launch {
            _shuffleByDefault.value = enabled
            val prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("shuffle_by_default", enabled).apply()
        }
    }
    
    fun setCrossfade(enabled: Boolean) {
        viewModelScope.launch {
            _crossfadeEnabled.value = enabled
            val prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("crossfade_enabled", enabled).apply()
        }
    }
    
    fun setHighQuality(enabled: Boolean) {
        viewModelScope.launch {
            _highQualityEnabled.value = enabled
            val prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("high_quality", enabled).apply()
        }
    }
    
    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _notificationsEnabled.value = enabled
            val prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("notifications_enabled", enabled).apply()
        }
    }
    
    fun setBackgroundPlay(enabled: Boolean) {
        viewModelScope.launch {
            _backgroundPlayEnabled.value = enabled
            AppSettings.setBackgroundProcessing(context, enabled)
        }
    }
    
    fun setMasterVolume(volume: Float) {
        viewModelScope.launch {
            _masterVolume.value = volume.coerceIn(0f, 1f)
            val prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            prefs.edit().putFloat("master_volume", volume).apply()
        }
    }
    
    fun setBufferSize(size: Int) {
        viewModelScope.launch {
            _bufferSize.value = size
            AppSettings.setAudioBufferSize(context, size)
        }
    }
    
    fun setAutoBrightness(enabled: Boolean) {
        viewModelScope.launch {
            _autoBrightness.value = enabled
            val prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("auto_brightness", enabled).apply()
        }
    }
    
    fun setGridView(enabled: Boolean) {
        viewModelScope.launch {
            _gridViewEnabled.value = enabled
            val prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("grid_view", enabled).apply()
        }
    }
    
    fun setAnimations(enabled: Boolean) {
        viewModelScope.launch {
            _animationsEnabled.value = enabled
            val prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("animations_enabled", enabled).apply()
        }
    }
    
    fun setImmersiveMode(enabled: Boolean) {
        viewModelScope.launch {
            _immersiveModeEnabled.value = enabled
            val prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("immersive_mode", enabled).apply()
        }
    }
    
    fun setHeadphoneDetection(enabled: Boolean) {
        viewModelScope.launch {
            _headphoneDetectionEnabled.value = enabled
            val prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("headphone_detection", enabled).apply()
        }
    }
    
    fun setSmartSkip(enabled: Boolean) {
        viewModelScope.launch {
            _smartSkipEnabled.value = enabled
            val prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("smart_skip", enabled).apply()
        }
    }
    
    fun setDefaultPlaybackSpeed(speed: Float) {
        viewModelScope.launch {
            _defaultPlaybackSpeed.value = speed
            AppSettings.setDefaultSpeed(context, speed)
        }
    }
    
    fun setAutoDownload(enabled: Boolean) {
        viewModelScope.launch {
            _autoDownloadEnabled.value = enabled
            val prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("auto_download", enabled).apply()
        }
    }
    
    fun setAnalytics(enabled: Boolean) {
        viewModelScope.launch {
            _analyticsEnabled.value = enabled
            val prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("analytics_enabled", enabled).apply()
        }
    }
    
    fun setLanguage(language: String) {
        viewModelScope.launch {
            val prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            prefs.edit().putString("language", language).apply()
        }
    }
    
    fun setVideoQuality(quality: String) {
        viewModelScope.launch {
            val prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            prefs.edit().putString("video_quality", quality).apply()
        }
    }
    
    fun clearCache() {
        viewModelScope.launch {
            // Clear app cache - this would typically involve clearing temporary files
            val prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            prefs.edit().putLong("last_cache_clear", System.currentTimeMillis()).apply()
        }
    }
    
    fun resetAllSettings() {
        viewModelScope.launch {
            AppSettings.resetAllSettings(context)
            val prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            loadSettings()
        }
    }
}