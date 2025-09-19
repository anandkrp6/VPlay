package com.bytecoder.vplay.backend.utils

import android.content.Context
import android.content.SharedPreferences

object AppSettings {
    private const val PREFS = "vplay_settings"
    private const val KEY_SEEK_SENS = "seek_sens" // factor *100
    private const val KEY_VOL_SENS = "vol_sens"
    private const val KEY_BRIGHT_SENS = "bright_sens"
    private const val KEY_DEFAULT_SPEED = "default_speed" // float *100
    private const val KEY_AUTOPLAY_NEXT = "autoplay_next" // bool
    private const val KEY_REMEMBER_POS = "remember_pos" // bool
    private const val KEY_SEEK_INTERVAL_MS = "seek_interval_ms" // int ms
    private const val KEY_MAX_VIDEO_BITRATE = "max_video_bitrate" // int bps (-1 auto)
    private const val KEY_WIFI_ONLY = "wifi_only_streaming" // bool
    private const val KEY_CONNECT_TIMEOUT = "connect_timeout_ms" // int
    private const val KEY_READ_TIMEOUT = "read_timeout_ms" // int
    private const val KEY_RETRY_COUNT = "retry_count" // int
    private const val KEY_SUB_SIZE_PCT = "sub_size_pct" // int percent (50-200)
    private const val KEY_SUB_COLOR = "sub_color" // int ARGB

    private fun prefs(ctx: Context): SharedPreferences = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getSeekSensitivity(ctx: Context): Float = (prefs(ctx).getInt(KEY_SEEK_SENS, 100)) / 100f
    fun getVolumeSensitivity(ctx: Context): Float = (prefs(ctx).getInt(KEY_VOL_SENS, 100)) / 100f
    fun getBrightnessSensitivity(ctx: Context): Float = (prefs(ctx).getInt(KEY_BRIGHT_SENS, 100)) / 100f

    fun setSeekSensitivity(ctx: Context, factor: Float) {
        prefs(ctx).edit().putInt(KEY_SEEK_SENS, (factor * 100).toInt().coerceIn(20, 200)).apply()
    }
    fun setVolumeSensitivity(ctx: Context, factor: Float) {
        prefs(ctx).edit().putInt(KEY_VOL_SENS, (factor * 100).toInt().coerceIn(20, 200)).apply()
    }
    fun setBrightnessSensitivity(ctx: Context, factor: Float) {
        prefs(ctx).edit().putInt(KEY_BRIGHT_SENS, (factor * 100).toInt().coerceIn(20, 200)).apply()
    }

    // Simple theme setting: 0 = System, 1 = Light, 2 = Dark
    fun getThemeMode(ctx: Context): Int = prefs(ctx).getInt("theme_mode", 0)
    fun setThemeMode(ctx: Context, v: Int) { prefs(ctx).edit().putInt("theme_mode", v).apply() }

    // Favorites set stored as CSV of media IDs (lightweight without Room join)
    fun isFavorite(ctx: Context, id: String): Boolean = prefs(ctx).getString("favorites", "")
        ?.split(',')?.filter { it.isNotBlank() }?.toSet()?.contains(id) == true

    fun toggleFavorite(ctx: Context, id: String): Boolean {
        val set = prefs(ctx).getString("favorites", "")
            ?.split(',')?.filter { it.isNotBlank() }?.toMutableSet() ?: mutableSetOf()
        val added: Boolean
        if (set.contains(id)) { set.remove(id); added = false } else { set.add(id); added = true }
        prefs(ctx).edit().putString("favorites", set.joinToString(",")).apply()
        return added
    }

    fun getDefaultSpeed(ctx: Context): Float = (prefs(ctx).getInt(KEY_DEFAULT_SPEED, 100)).coerceIn(25, 300) / 100f
    fun setDefaultSpeed(ctx: Context, speed: Float) {
        val clamped = (speed * 100).toInt().coerceIn(25, 300)
        prefs(ctx).edit().putInt(KEY_DEFAULT_SPEED, clamped).apply()
    }

    fun isAutoplayNext(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_AUTOPLAY_NEXT, true)
    fun setAutoplayNext(ctx: Context, enabled: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_AUTOPLAY_NEXT, enabled).apply()
    }

    fun isRememberPosition(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_REMEMBER_POS, true)
    fun setRememberPosition(ctx: Context, enabled: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_REMEMBER_POS, enabled).apply()
    }

    // Gesture seek interval
    fun getSeekIntervalMs(ctx: Context): Int = prefs(ctx).getInt(KEY_SEEK_INTERVAL_MS, 10_000).coerceIn(1_000, 120_000)
    fun setSeekIntervalMs(ctx: Context, ms: Int) {
        val clamped = ms.coerceIn(1_000, 120_000)
        prefs(ctx).edit().putInt(KEY_SEEK_INTERVAL_MS, clamped).apply()
    }

    // Preferred streaming quality (max bitrate). -1 = auto
    fun getMaxVideoBitrate(ctx: Context): Int = prefs(ctx).getInt(KEY_MAX_VIDEO_BITRATE, -1)
    fun setMaxVideoBitrate(ctx: Context, bps: Int) {
        prefs(ctx).edit().putInt(KEY_MAX_VIDEO_BITRATE, bps).apply()
    }

    // Network
    fun isWifiOnly(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_WIFI_ONLY, false)
    fun setWifiOnly(ctx: Context, enabled: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_WIFI_ONLY, enabled).apply()
    }
    fun getConnectTimeoutMs(ctx: Context): Int = prefs(ctx).getInt(KEY_CONNECT_TIMEOUT, 8000).coerceIn(1000, 60000)
    fun setConnectTimeoutMs(ctx: Context, ms: Int) {
        prefs(ctx).edit().putInt(KEY_CONNECT_TIMEOUT, ms.coerceIn(1000, 60000)).apply()
    }
    fun getReadTimeoutMs(ctx: Context): Int = prefs(ctx).getInt(KEY_READ_TIMEOUT, 15000).coerceIn(1000, 120000)
    fun setReadTimeoutMs(ctx: Context, ms: Int) {
        prefs(ctx).edit().putInt(KEY_READ_TIMEOUT, ms.coerceIn(1000, 120000)).apply()
    }
    fun getRetryCount(ctx: Context): Int = prefs(ctx).getInt(KEY_RETRY_COUNT, 2).coerceIn(0, 10)
    fun setRetryCount(ctx: Context, count: Int) {
        prefs(ctx).edit().putInt(KEY_RETRY_COUNT, count.coerceIn(0, 10)).apply()
    }

    // Subtitles appearance
    fun getSubtitleSizePercent(ctx: Context): Int = prefs(ctx).getInt(KEY_SUB_SIZE_PCT, 100).coerceIn(50, 200)
    fun setSubtitleSizePercent(ctx: Context, pct: Int) {
        prefs(ctx).edit().putInt(KEY_SUB_SIZE_PCT, pct.coerceIn(50, 200)).apply()
    }
    fun getSubtitleColor(ctx: Context): Int = prefs(ctx).getInt(KEY_SUB_COLOR, 0xFFFFFFFF.toInt())
    fun setSubtitleColor(ctx: Context, argb: Int) {
        prefs(ctx).edit().putInt(KEY_SUB_COLOR, argb).apply()
    }

    // Advanced Settings for Enhanced Options Integration
    private const val KEY_SHOW_HIDDEN_FILES = "show_hidden_files"
    private const val KEY_ENABLE_EXPERIMENTAL = "enable_experimental_features"
    private const val KEY_HARDWARE_ACCELERATION = "hardware_acceleration"
    private const val KEY_BACKGROUND_PROCESSING = "background_processing"
    private const val KEY_AUDIO_BUFFER_SIZE = "audio_buffer_size"
    private const val KEY_VIDEO_DECODER_PREFERENCE = "video_decoder_preference"
    private const val KEY_BUFFER_AHEAD_MS = "buffer_ahead_ms"
    private const val KEY_AI_RECOMMENDATIONS = "ai_recommendations"
    private const val KEY_ADVANCED_GESTURES = "advanced_gestures"
    private const val KEY_DEBUG_LOGGING = "debug_logging"

    // File Explorer Settings
    fun getShowHiddenFiles(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_SHOW_HIDDEN_FILES, false)
    fun setShowHiddenFiles(ctx: Context, enabled: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_SHOW_HIDDEN_FILES, enabled).apply()
    }

    // Experimental Features
    fun getEnableExperimentalFeatures(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_ENABLE_EXPERIMENTAL, false)
    fun setEnableExperimentalFeatures(ctx: Context, enabled: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_ENABLE_EXPERIMENTAL, enabled).apply()
    }

    // Performance Settings
    fun getHardwareAcceleration(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_HARDWARE_ACCELERATION, true)
    fun setHardwareAcceleration(ctx: Context, enabled: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_HARDWARE_ACCELERATION, enabled).apply()
    }

    fun getBackgroundProcessing(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_BACKGROUND_PROCESSING, false)
    fun setBackgroundProcessing(ctx: Context, enabled: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_BACKGROUND_PROCESSING, enabled).apply()
    }

    // Audio/Video Settings
    fun getAudioBufferSize(ctx: Context): Int = prefs(ctx).getInt(KEY_AUDIO_BUFFER_SIZE, 1000).coerceIn(100, 5000)
    fun setAudioBufferSize(ctx: Context, size: Int) {
        prefs(ctx).edit().putInt(KEY_AUDIO_BUFFER_SIZE, size.coerceIn(100, 5000)).apply()
    }

    fun getVideoDecoderPreference(ctx: Context): Int = prefs(ctx).getInt(KEY_VIDEO_DECODER_PREFERENCE, 0).coerceIn(0, 2)
    fun setVideoDecoderPreference(ctx: Context, preference: Int) {
        prefs(ctx).edit().putInt(KEY_VIDEO_DECODER_PREFERENCE, preference.coerceIn(0, 2)).apply()
    }

    // Network Settings
    fun getBufferAheadMs(ctx: Context): Int = prefs(ctx).getInt(KEY_BUFFER_AHEAD_MS, 15000).coerceIn(5000, 60000)
    fun setBufferAheadMs(ctx: Context, ms: Int) {
        prefs(ctx).edit().putInt(KEY_BUFFER_AHEAD_MS, ms.coerceIn(5000, 60000)).apply()
    }

    // Experimental Features
    fun getAIRecommendations(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_AI_RECOMMENDATIONS, false)
    fun setAIRecommendations(ctx: Context, enabled: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_AI_RECOMMENDATIONS, enabled).apply()
    }

    fun getAdvancedGestures(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_ADVANCED_GESTURES, false)
    fun setAdvancedGestures(ctx: Context, enabled: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_ADVANCED_GESTURES, enabled).apply()
    }

    // Developer Options
    fun getDebugLogging(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_DEBUG_LOGGING, false)
    fun setDebugLogging(ctx: Context, enabled: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_DEBUG_LOGGING, enabled).apply()
    }

    // Reset all settings to defaults
    fun resetAllSettings(ctx: Context) {
        prefs(ctx).edit().clear().apply()
    }
}


