package com.bytecoder.vplay.backend.managers

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.bytecoder.vplay.backend.utils.AppSettings

/**
 * SettingsScreenManager handles all application settings and preferences.
 * Manages user configurations for theme, playback, audio quality, and more.
 */
class SettingsScreenManager(application: Application) : AndroidViewModel(application) {
    
    private val context: Context = application.applicationContext
    private val appSettings = AppSettings(context)
    
    // Theme settings
    private val _darkModeEnabled = MutableStateFlow(false)
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled.asStateFlow()
    
    private val _dynamicThemeEnabled = MutableStateFlow(true)
    val dynamicThemeEnabled: StateFlow<Boolean> = _dynamicThemeEnabled.asStateFlow()
    
    private val _immersiveModeEnabled = MutableStateFlow(false)
    val immersiveModeEnabled: StateFlow<Boolean> = _immersiveModeEnabled.asStateFlow()
    
    private val _animationsEnabled = MutableStateFlow(true)
    val animationsEnabled: StateFlow<Boolean> = _animationsEnabled.asStateFlow()
    
    // Playback settings
    private val _autoPlayEnabled = MutableStateFlow(true)
    val autoPlayEnabled: StateFlow<Boolean> = _autoPlayEnabled.asStateFlow()
    
    private val _shuffleByDefault = MutableStateFlow(false)
    val shuffleByDefault: StateFlow<Boolean> = _shuffleByDefault.asStateFlow()
    
    private val _crossfadeEnabled = MutableStateFlow(true)
    val crossfadeEnabled: StateFlow<Boolean> = _crossfadeEnabled.asStateFlow()
    
    private val _backgroundPlayEnabled = MutableStateFlow(true)
    val backgroundPlayEnabled: StateFlow<Boolean> = _backgroundPlayEnabled.asStateFlow()
    
    private val _defaultPlaybackSpeed = MutableStateFlow(1.0f)
    val defaultPlaybackSpeed: StateFlow<Float> = _defaultPlaybackSpeed.asStateFlow()
    
    private val _smartSkipEnabled = MutableStateFlow(false)
    val smartSkipEnabled: StateFlow<Boolean> = _smartSkipEnabled.asStateFlow()
    
    // Audio settings
    private val _highQualityEnabled = MutableStateFlow(true)
    val highQualityEnabled: StateFlow<Boolean> = _highQualityEnabled.asStateFlow()
    
    private val _masterVolume = MutableStateFlow(1.0f)
    val masterVolume: StateFlow<Float> = _masterVolume.asStateFlow()
    
    private val _bufferSize = MutableStateFlow(8192)
    val bufferSize: StateFlow<Int> = _bufferSize.asStateFlow()
    
    private val _headphoneDetectionEnabled = MutableStateFlow(true)
    val headphoneDetectionEnabled: StateFlow<Boolean> = _headphoneDetectionEnabled.asStateFlow()
    
    // Video settings
    private val _autoBrightness = MutableStateFlow(true)
    val autoBrightness: StateFlow<Boolean> = _autoBrightness.asStateFlow()
    
    private val _hardwareAccelerationEnabled = MutableStateFlow(true)
    val hardwareAccelerationEnabled: StateFlow<Boolean> = _hardwareAccelerationEnabled.asStateFlow()
    
    private val _gestureControlsEnabled = MutableStateFlow(true)
    val gestureControlsEnabled: StateFlow<Boolean> = _gestureControlsEnabled.asStateFlow()
    
    // Library settings
    private val _gridViewEnabled = MutableStateFlow(false)
    val gridViewEnabled: StateFlow<Boolean> = _gridViewEnabled.asStateFlow()
    
    private val _autoRefreshLibrary = MutableStateFlow(true)
    val autoRefreshLibrary: StateFlow<Boolean> = _autoRefreshLibrary.asStateFlow()
    
    private val _includeSystemFolders = MutableStateFlow(false)
    val includeSystemFolders: StateFlow<Boolean> = _includeSystemFolders.asStateFlow()
    
    // Download settings
    private val _autoDownloadEnabled = MutableStateFlow(false)
    val autoDownloadEnabled: StateFlow<Boolean> = _autoDownloadEnabled.asStateFlow()
    
    private val _downloadLocation = MutableStateFlow("Internal Storage/VPlay")
    val downloadLocation: StateFlow<String> = _downloadLocation.asStateFlow()
    
    private val _downloadQuality = MutableStateFlow("High")
    val downloadQuality: StateFlow<String> = _downloadQuality.asStateFlow()
    
    // Notification settings
    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()
    
    private val _lockScreenControlsEnabled = MutableStateFlow(true)
    val lockScreenControlsEnabled: StateFlow<Boolean> = _lockScreenControlsEnabled.asStateFlow()
    
    // Privacy settings
    private val _analyticsEnabled = MutableStateFlow(true)
    val analyticsEnabled: StateFlow<Boolean> = _analyticsEnabled.asStateFlow()
    
    private val _locationServicesEnabled = MutableStateFlow(false)
    val locationServicesEnabled: StateFlow<Boolean> = _locationServicesEnabled.asStateFlow()
    
    private val _crashReportingEnabled = MutableStateFlow(true)
    val crashReportingEnabled: StateFlow<Boolean> = _crashReportingEnabled.asStateFlow()
    
    init {
        loadSettings()
    }

    /**
     * Load all settings from persistent storage
     */
    private fun loadSettings() {
        viewModelScope.launch {
            _darkModeEnabled.value = appSettings.getDarkModeEnabled()
            _dynamicThemeEnabled.value = appSettings.getDynamicThemeEnabled()
            _immersiveModeEnabled.value = appSettings.getImmersiveModeEnabled()
            _animationsEnabled.value = appSettings.getAnimationsEnabled()
            
            _autoPlayEnabled.value = appSettings.getAutoPlayEnabled()
            _shuffleByDefault.value = appSettings.getShuffleByDefault()
            _crossfadeEnabled.value = appSettings.getCrossfadeEnabled()
            _backgroundPlayEnabled.value = appSettings.getBackgroundPlayEnabled()
            _defaultPlaybackSpeed.value = appSettings.getDefaultPlaybackSpeed()
            _smartSkipEnabled.value = appSettings.getSmartSkipEnabled()
            
            _highQualityEnabled.value = appSettings.getHighQualityEnabled()
            _masterVolume.value = appSettings.getMasterVolume()
            _bufferSize.value = appSettings.getBufferSize()
            _headphoneDetectionEnabled.value = appSettings.getHeadphoneDetectionEnabled()
            
            _autoBrightness.value = appSettings.getAutoBrightness()
            _hardwareAccelerationEnabled.value = appSettings.getHardwareAccelerationEnabled()
            _gestureControlsEnabled.value = appSettings.getGestureControlsEnabled()
            
            _gridViewEnabled.value = appSettings.getGridViewEnabled()
            _autoRefreshLibrary.value = appSettings.getAutoRefreshLibrary()
            _includeSystemFolders.value = appSettings.getIncludeSystemFolders()
            
            _autoDownloadEnabled.value = appSettings.getAutoDownloadEnabled()
            _downloadLocation.value = appSettings.getDownloadLocation()
            _downloadQuality.value = appSettings.getDownloadQuality()
            
            _notificationsEnabled.value = appSettings.getNotificationsEnabled()
            _lockScreenControlsEnabled.value = appSettings.getLockScreenControlsEnabled()
            
            _analyticsEnabled.value = appSettings.getAnalyticsEnabled()
            _locationServicesEnabled.value = appSettings.getLocationServicesEnabled()
            _crashReportingEnabled.value = appSettings.getCrashReportingEnabled()
        }
    }

    // Theme settings
    fun toggleDarkMode() {
        viewModelScope.launch {
            val newValue = !_darkModeEnabled.value
            _darkModeEnabled.value = newValue
            appSettings.setDarkModeEnabled(newValue)
        }
    }

    fun toggleDynamicTheme() {
        viewModelScope.launch {
            val newValue = !_dynamicThemeEnabled.value
            _dynamicThemeEnabled.value = newValue
            appSettings.setDynamicThemeEnabled(newValue)
        }
    }

    fun toggleImmersiveMode() {
        viewModelScope.launch {
            val newValue = !_immersiveModeEnabled.value
            _immersiveModeEnabled.value = newValue
            appSettings.setImmersiveModeEnabled(newValue)
        }
    }

    fun toggleAnimations() {
        viewModelScope.launch {
            val newValue = !_animationsEnabled.value
            _animationsEnabled.value = newValue
            appSettings.setAnimationsEnabled(newValue)
        }
    }

    // Playback settings
    fun toggleAutoPlay() {
        viewModelScope.launch {
            val newValue = !_autoPlayEnabled.value
            _autoPlayEnabled.value = newValue
            appSettings.setAutoPlayEnabled(newValue)
        }
    }

    fun toggleShuffleByDefault() {
        viewModelScope.launch {
            val newValue = !_shuffleByDefault.value
            _shuffleByDefault.value = newValue
            appSettings.setShuffleByDefault(newValue)
        }
    }

    fun toggleCrossfade() {
        viewModelScope.launch {
            val newValue = !_crossfadeEnabled.value
            _crossfadeEnabled.value = newValue
            appSettings.setCrossfadeEnabled(newValue)
        }
    }

    fun toggleBackgroundPlay() {
        viewModelScope.launch {
            val newValue = !_backgroundPlayEnabled.value
            _backgroundPlayEnabled.value = newValue
            appSettings.setBackgroundPlayEnabled(newValue)
        }
    }

    fun setDefaultPlaybackSpeed(speed: Float) {
        viewModelScope.launch {
            _defaultPlaybackSpeed.value = speed
            appSettings.setDefaultPlaybackSpeed(speed)
        }
    }

    fun toggleSmartSkip() {
        viewModelScope.launch {
            val newValue = !_smartSkipEnabled.value
            _smartSkipEnabled.value = newValue
            appSettings.setSmartSkipEnabled(newValue)
        }
    }

    // Audio settings
    fun toggleHighQuality() {
        viewModelScope.launch {
            val newValue = !_highQualityEnabled.value
            _highQualityEnabled.value = newValue
            appSettings.setHighQualityEnabled(newValue)
        }
    }

    fun setMasterVolume(volume: Float) {
        viewModelScope.launch {
            _masterVolume.value = volume.coerceIn(0f, 1f)
            appSettings.setMasterVolume(_masterVolume.value)
        }
    }

    fun setBufferSize(size: Int) {
        viewModelScope.launch {
            _bufferSize.value = size
            appSettings.setBufferSize(size)
        }
    }

    fun toggleHeadphoneDetection() {
        viewModelScope.launch {
            val newValue = !_headphoneDetectionEnabled.value
            _headphoneDetectionEnabled.value = newValue
            appSettings.setHeadphoneDetectionEnabled(newValue)
        }
    }

    // Video settings
    fun toggleAutoBrightness() {
        viewModelScope.launch {
            val newValue = !_autoBrightness.value
            _autoBrightness.value = newValue
            appSettings.setAutoBrightness(newValue)
        }
    }

    fun toggleHardwareAcceleration() {
        viewModelScope.launch {
            val newValue = !_hardwareAccelerationEnabled.value
            _hardwareAccelerationEnabled.value = newValue
            appSettings.setHardwareAccelerationEnabled(newValue)
        }
    }

    fun toggleGestureControls() {
        viewModelScope.launch {
            val newValue = !_gestureControlsEnabled.value
            _gestureControlsEnabled.value = newValue
            appSettings.setGestureControlsEnabled(newValue)
        }
    }

    // Library settings
    fun toggleGridView() {
        viewModelScope.launch {
            val newValue = !_gridViewEnabled.value
            _gridViewEnabled.value = newValue
            appSettings.setGridViewEnabled(newValue)
        }
    }

    fun toggleAutoRefreshLibrary() {
        viewModelScope.launch {
            val newValue = !_autoRefreshLibrary.value
            _autoRefreshLibrary.value = newValue
            appSettings.setAutoRefreshLibrary(newValue)
        }
    }

    fun toggleIncludeSystemFolders() {
        viewModelScope.launch {
            val newValue = !_includeSystemFolders.value
            _includeSystemFolders.value = newValue
            appSettings.setIncludeSystemFolders(newValue)
        }
    }

    // Download settings
    fun toggleAutoDownload() {
        viewModelScope.launch {
            val newValue = !_autoDownloadEnabled.value
            _autoDownloadEnabled.value = newValue
            appSettings.setAutoDownloadEnabled(newValue)
        }
    }

    fun setDownloadLocation(location: String) {
        viewModelScope.launch {
            _downloadLocation.value = location
            appSettings.setDownloadLocation(location)
        }
    }

    fun setDownloadQuality(quality: String) {
        viewModelScope.launch {
            _downloadQuality.value = quality
            appSettings.setDownloadQuality(quality)
        }
    }

    // Notification settings
    fun toggleNotifications() {
        viewModelScope.launch {
            val newValue = !_notificationsEnabled.value
            _notificationsEnabled.value = newValue
            appSettings.setNotificationsEnabled(newValue)
        }
    }

    fun toggleLockScreenControls() {
        viewModelScope.launch {
            val newValue = !_lockScreenControlsEnabled.value
            _lockScreenControlsEnabled.value = newValue
            appSettings.setLockScreenControlsEnabled(newValue)
        }
    }

    // Privacy settings
    fun toggleAnalytics() {
        viewModelScope.launch {
            val newValue = !_analyticsEnabled.value
            _analyticsEnabled.value = newValue
            appSettings.setAnalyticsEnabled(newValue)
        }
    }

    fun toggleLocationServices() {
        viewModelScope.launch {
            val newValue = !_locationServicesEnabled.value
            _locationServicesEnabled.value = newValue
            appSettings.setLocationServicesEnabled(newValue)
        }
    }

    fun toggleCrashReporting() {
        viewModelScope.launch {
            val newValue = !_crashReportingEnabled.value
            _crashReportingEnabled.value = newValue
            appSettings.setCrashReportingEnabled(newValue)
        }
    }

    /**
     * Reset all settings to default values
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            appSettings.resetToDefaults()
            loadSettings()
        }
    }

    /**
     * Export settings to file
     */
    fun exportSettings(): String {
        return appSettings.exportSettings()
    }

    /**
     * Import settings from file
     */
    fun importSettings(settingsJson: String): Boolean {
        return try {
            appSettings.importSettings(settingsJson)
            loadSettings()
            true
        } catch (e: Exception) {
            false
        }
    }
}



