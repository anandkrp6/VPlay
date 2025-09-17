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
    
    private val _downloadLocation = MutableStateFlow("Internal Storage/VPlay")
    val downloadLocation: StateFlow<String> = _downloadLocation.asStateFlow()
    
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
    
    private val _locationServicesEnabled = MutableStateFlow(false)
    val locationServicesEnabled: StateFlow<Boolean> = _locationServicesEnabled.asStateFlow()
    
    private val _historyTrackingEnabled = MutableStateFlow(true)
    val historyTrackingEnabled: StateFlow<Boolean> = _historyTrackingEnabled.asStateFlow()
    
    private val _usageAnalyticsEnabled = MutableStateFlow(true)
    val usageAnalyticsEnabled: StateFlow<Boolean> = _usageAnalyticsEnabled.asStateFlow()
    
    private val _debugModeEnabled = MutableStateFlow(false)
    val debugModeEnabled: StateFlow<Boolean> = _debugModeEnabled.asStateFlow()
    
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
            _downloadLocation.value = prefs.getString("download_location", "Internal Storage/VPlay") ?: "Internal Storage/VPlay"
            _locationServicesEnabled.value = prefs.getBoolean("location_services_enabled", false)
            _historyTrackingEnabled.value = prefs.getBoolean("history_tracking_enabled", true)
            _usageAnalyticsEnabled.value = prefs.getBoolean("usage_analytics_enabled", true)
            _debugModeEnabled.value = prefs.getBoolean("debug_mode_enabled", false)
            
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
    
    fun setDownloadLocation(location: String) {
        viewModelScope.launch {
            _downloadLocation.value = location
            val prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            prefs.edit().putString("download_location", location).apply()
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
    
    fun setLocationServices(enabled: Boolean) {
        viewModelScope.launch {
            _locationServicesEnabled.value = enabled
            val prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("location_services_enabled", enabled).apply()
        }
    }
    
    fun setHistoryTracking(enabled: Boolean) {
        viewModelScope.launch {
            _historyTrackingEnabled.value = enabled
            val prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("history_tracking_enabled", enabled).apply()
        }
    }
    
    fun setUsageAnalytics(enabled: Boolean) {
        viewModelScope.launch {
            _usageAnalyticsEnabled.value = enabled
            val prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("usage_analytics_enabled", enabled).apply()
        }
    }
    
    fun setDebugMode(enabled: Boolean) {
        viewModelScope.launch {
            _debugModeEnabled.value = enabled
            val prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("debug_mode_enabled", enabled).apply()
            
            // In a real app, you would configure logging levels here
            if (enabled) {
                // Enable verbose logging
                android.util.Log.d("SettingsViewModel", "Debug mode enabled")
            } else {
                // Disable verbose logging
                android.util.Log.d("SettingsViewModel", "Debug mode disabled")
            }
        }
    }
    
    fun checkForUpdates() {
        viewModelScope.launch {
            try {
                // In a real app, this would make an API call to check for updates
                // For now, we'll just simulate the check
                android.util.Log.d("SettingsViewModel", "Checking for updates...")
                
                // Simulate network delay
                kotlinx.coroutines.delay(1000)
                
                // For demo purposes, always show "up to date"
                android.util.Log.d("SettingsViewModel", "App is up to date")
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "Failed to check for updates", e)
            }
        }
    }
    
    fun submitBugReport(description: String, email: String) {
        viewModelScope.launch {
            try {
                // In a real app, this would send the bug report to your backend
                android.util.Log.d("SettingsViewModel", "Submitting bug report: $description")
                
                // Collect device info for bug report
                val deviceInfo = mapOf(
                    "model" to android.os.Build.MODEL,
                    "version" to android.os.Build.VERSION.RELEASE,
                    "sdk" to android.os.Build.VERSION.SDK_INT.toString(),
                    "app_version" to "3.0.0",
                    "description" to description,
                    "email" to email
                )
                
                // Simulate sending bug report
                kotlinx.coroutines.delay(1000)
                
                android.util.Log.d("SettingsViewModel", "Bug report submitted successfully: $deviceInfo")
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "Failed to submit bug report", e)
            }
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
    
    fun clearAllData() {
        viewModelScope.launch {
            // Clear all app data including preferences, cache, and database
            try {
                // Clear all shared preferences
                val uiPrefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE)
                uiPrefs.edit().clear().apply()
                
                val historyPrefs = context.getSharedPreferences("history_tracker", Context.MODE_PRIVATE)
                historyPrefs.edit().clear().apply()
                
                // Clear cache directory
                context.cacheDir.deleteRecursively()
                
                // Reset app settings
                AppSettings.resetAllSettings(context)
                
                // Reload default settings
                loadSettings()
            } catch (e: Exception) {
                // Handle error gracefully
                e.printStackTrace()
            }
        }
    }
}