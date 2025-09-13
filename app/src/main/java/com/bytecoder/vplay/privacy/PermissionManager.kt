package com.bytecoder.vplay.privacy

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

data class PermissionInfo(
    val permission: String,
    val title: String,
    val description: String,
    val importance: PermissionImportance,
    val isGranted: Boolean,
    val isRequired: Boolean,
    val explanation: String,
    val impact: String
)

enum class PermissionImportance {
    CRITICAL,    // App won't work without it
    IMPORTANT,   // Core features won't work
    OPTIONAL     // Nice to have features
}

object PermissionManager {
    
    fun getAllPermissions(context: Context): List<PermissionInfo> {
        return listOf(
            PermissionInfo(
                permission = Manifest.permission.READ_EXTERNAL_STORAGE,
                title = "Storage Access",
                description = "Access your media files and documents",
                importance = PermissionImportance.CRITICAL,
                isGranted = isPermissionGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE),
                isRequired = true,
                explanation = "This permission is essential for the app to access and play your video and music files stored on your device.",
                impact = "Without this permission, the app cannot access any media files and most features will not work."
            ),
            
            PermissionInfo(
                permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.POST_NOTIFICATIONS else "notifications",
                title = "Notifications",
                description = "Show media playback controls and download progress",
                importance = PermissionImportance.IMPORTANT,
                isGranted = if (Build.VERSION.SDK_INT >= 33) 
                    isPermissionGranted(context, Manifest.permission.POST_NOTIFICATIONS) else true,
                isRequired = false,
                explanation = "Allows the app to show persistent media controls in the notification area and notify you about download progress.",
                impact = "Without this permission, you won't see media controls when the app is in the background, and download notifications won't appear."
            ),
            
            PermissionInfo(
                permission = Manifest.permission.WRITE_EXTERNAL_STORAGE,
                title = "Storage Modification",
                description = "Save downloaded files and create playlists",
                importance = PermissionImportance.IMPORTANT,
                isGranted = if (Build.VERSION.SDK_INT >= 29) true else 
                    isPermissionGranted(context, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                isRequired = false,
                explanation = "Enables the app to save downloaded media files and export playlists to your device storage.",
                impact = "Without this permission, you cannot download media files or export playlists to external storage."
            ),
            
            PermissionInfo(
                permission = Manifest.permission.CAMERA,
                title = "Camera Access",
                description = "Scan QR codes for streaming URLs",
                importance = PermissionImportance.OPTIONAL,
                isGranted = isPermissionGranted(context, Manifest.permission.CAMERA),
                isRequired = false,
                explanation = "Allows the app to use your camera to scan QR codes that contain streaming URLs or media links.",
                impact = "Without this permission, you cannot use the QR code scanner feature for quick URL input."
            ),
            
            PermissionInfo(
                permission = Manifest.permission.RECORD_AUDIO,
                title = "Microphone Access",
                description = "Voice commands and audio recording features",
                importance = PermissionImportance.OPTIONAL,
                isGranted = isPermissionGranted(context, Manifest.permission.RECORD_AUDIO),
                isRequired = false,
                explanation = "Enables voice commands for controlling playback and accessing audio recording features.",
                impact = "Without this permission, voice control and audio recording features will not be available."
            ),
            
            PermissionInfo(
                permission = Manifest.permission.ACCESS_FINE_LOCATION,
                title = "Location Access",
                description = "Location-based media recommendations",
                importance = PermissionImportance.OPTIONAL,
                isGranted = isPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION),
                isRequired = false,
                explanation = "Used for location-based features like finding nearby media servers or location-tagged content.",
                impact = "Without this permission, location-based features and recommendations will not be available."
            ),
            
            PermissionInfo(
                permission = "system_settings",
                title = "System Settings Access",
                description = "Modify system volume and brightness",
                importance = PermissionImportance.IMPORTANT,
                isGranted = Settings.System.canWrite(context),
                isRequired = false,
                explanation = "Allows the app to modify system settings like volume and brightness when using gesture controls.",
                impact = "Without this permission, gesture controls for volume and brightness will not work."
            )
        )
    }
    
    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    fun getCriticalPermissions(context: Context): List<PermissionInfo> {
        return getAllPermissions(context).filter { it.importance == PermissionImportance.CRITICAL }
    }
    
    fun getRequiredPermissions(context: Context): List<PermissionInfo> {
        return getAllPermissions(context).filter { it.isRequired }
    }
    
    fun getMissingPermissions(context: Context): List<PermissionInfo> {
        return getAllPermissions(context).filter { !it.isGranted }
    }
    
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }
    
    fun openSystemWriteSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    }
    
    fun getPermissionUsageStats(context: Context): PermissionUsageStats {
        val allPermissions = getAllPermissions(context)
        val granted = allPermissions.count { it.isGranted }
        val total = allPermissions.size
        val critical = allPermissions.filter { it.importance == PermissionImportance.CRITICAL }
        val criticalGranted = critical.count { it.isGranted }
        
        return PermissionUsageStats(
            totalPermissions = total,
            grantedPermissions = granted,
            criticalPermissions = critical.size,
            criticalGranted = criticalGranted,
            completionPercentage = (granted.toFloat() / total * 100).toInt()
        )
    }
}

data class PermissionUsageStats(
    val totalPermissions: Int,
    val grantedPermissions: Int,
    val criticalPermissions: Int,
    val criticalGranted: Int,
    val completionPercentage: Int
) {
    val hasAllCritical: Boolean = criticalGranted == criticalPermissions
    val hasAllOptional: Boolean = grantedPermissions == totalPermissions
}

object PrivacyManager {
    private const val PREFS_NAME = "privacy_settings"
    private const val KEY_ANALYTICS_ENABLED = "analytics_enabled"
    private const val KEY_CRASH_REPORTING = "crash_reporting"
    private const val KEY_USAGE_DATA = "usage_data"
    private const val KEY_PERSONALIZATION = "personalization"
    private const val KEY_DATA_SHARING = "data_sharing"
    
    fun isAnalyticsEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ANALYTICS_ENABLED, true)
    }
    
    fun setAnalyticsEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ANALYTICS_ENABLED, enabled)
            .apply()
    }
    
    fun isCrashReportingEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_CRASH_REPORTING, true)
    }
    
    fun setCrashReportingEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_CRASH_REPORTING, enabled)
            .apply()
    }
    
    fun isUsageDataCollectionEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_USAGE_DATA, false)
    }
    
    fun setUsageDataCollectionEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_USAGE_DATA, enabled)
            .apply()
    }
    
    fun isPersonalizationEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_PERSONALIZATION, true)
    }
    
    fun setPersonalizationEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_PERSONALIZATION, enabled)
            .apply()
    }
    
    fun isDataSharingEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_DATA_SHARING, false)
    }
    
    fun setDataSharingEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DATA_SHARING, enabled)
            .apply()
    }
    
    fun clearAllUserData(context: Context) {
        // Clear app databases
        val database = com.bytecoder.vplay.database.VPlayDatabase.getDatabase(context)
        
        // Clear all tables (implement in repository classes)
        // This should be done carefully and with user confirmation
        
        // Clear shared preferences except critical app settings
        val privacyPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        privacyPrefs.edit().clear().apply()
        
        // Clear other app data
        context.getSharedPreferences("vplay_settings", Context.MODE_PRIVATE)
            .edit().clear().apply()
    }
    
    fun exportUserData(context: Context): String {
        // Create a JSON export of user data for GDPR compliance
        val data = mutableMapOf<String, Any>()
        
        // Add privacy settings
        val privacyPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        data["privacy_settings"] = privacyPrefs.all
        
        // Add app settings
        val appSettings = context.getSharedPreferences("vplay_settings", Context.MODE_PRIVATE)
        data["app_settings"] = appSettings.all
        
        // Add other relevant data (playlists, watch history, etc.)
        // This would require async database queries in practice
        
        return "Data export functionality not yet implemented"
    }
    
    fun getDataUsageSummary(context: Context): DataUsageSummary {
        // Calculate approximate data usage by the app
        val database = com.bytecoder.vplay.database.VPlayDatabase.getDatabase(context)
        
        // In practice, these would be async database queries
        return DataUsageSummary(
            watchHistoryEntries = 0, // database.watchHistoryDao().getCount()
            playlistCount = 0, // database.playlistDao().getCount()
            downloadedFiles = 0, // database.downloadDao().getCompletedCount()
            analyticsEntries = 0, // database.analyticsDao().getCount()
            totalStorageUsed = 0L, // Calculate from downloads + cache
            lastDataCleanup = 0L // From preferences
        )
    }
}

data class DataUsageSummary(
    val watchHistoryEntries: Int,
    val playlistCount: Int,
    val downloadedFiles: Int,
    val analyticsEntries: Int,
    val totalStorageUsed: Long,
    val lastDataCleanup: Long
) {
    fun getFormattedStorageUsed(): String {
        val mb = totalStorageUsed / (1024 * 1024)
        return when {
            mb < 1 -> "${totalStorageUsed / 1024} KB"
            mb < 1024 -> "$mb MB"
            else -> "%.1f GB".format(mb / 1024.0)
        }
    }
}