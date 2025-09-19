package com.bytecoder.vplay.backend.models

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Video represents a video file in the VPlay system.
 * Contains video-specific properties and metadata.
 */
@Entity(tableName = "videos")
data class Video(
    @PrimaryKey
    val id: String,
    val title: String,
    val path: String,
    val duration: Long = 0L,
    val size: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis(),
    val dateModified: Long = System.currentTimeMillis(),
    val thumbnailPath: String? = null,
    val resolution: String? = null, // "1920x1080"
    val frameRate: Float? = null,
    val bitrate: Int? = null,
    val codec: String? = null,
    val mimeType: String? = null,
    val orientation: Int = 0, // 0, 90, 180, 270
    val hasAudio: Boolean = true,
    val hasSubtitles: Boolean = false,
    val folderPath: String? = null,
    val fileName: String = "",
    val fileExtension: String = ""
) {
    // Helper properties
    val uri: Uri get() = Uri.parse(path)
    val displayTitle: String get() = title.ifBlank { fileName.ifBlank { "Unknown Video" } }
    
    // Duration formatting
    fun getFormattedDuration(): String {
        val seconds = duration / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        
        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes % 60, seconds % 60)
            else -> String.format("%d:%02d", minutes, seconds % 60)
        }
    }
    
    // File size formatting
    fun getFormattedSize(): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        
        return when {
            gb >= 1 -> String.format("%.1f GB", gb)
            mb >= 1 -> String.format("%.1f MB", mb)
            else -> String.format("%.0f KB", kb)
        }
    }
    
    // Resolution display
    fun getDisplayResolution(): String {
        return resolution ?: "Unknown"
    }
    
    // Check if it's HD, Full HD, 4K etc.
    fun getQualityLabel(): String {
        return when (resolution) {
            "1920x1080" -> "Full HD"
            "1280x720" -> "HD"
            "3840x2160" -> "4K"
            "2560x1440" -> "QHD"
            else -> resolution ?: "Unknown"
        }
    }
    
    // Check if video is in landscape or portrait
    val isLandscape: Boolean get() {
        if (resolution == null) return true
        val parts = resolution.split("x")
        if (parts.size != 2) return true
        val width = parts[0].toIntOrNull() ?: 0
        val height = parts[1].toIntOrNull() ?: 0
        return width > height
    }
}

/**
 * Extension functions for collections of Video
 */
fun List<Video>.sortedByTitle(): List<Video> = sortedBy { it.title.lowercase() }
fun List<Video>.sortedByDateAdded(): List<Video> = sortedByDescending { it.dateAdded }
fun List<Video>.sortedByDuration(): List<Video> = sortedByDescending { it.duration }
fun List<Video>.sortedBySize(): List<Video> = sortedByDescending { it.size }
fun List<Video>.filterByResolution(minWidth: Int): List<Video> = filter { video ->
    video.resolution?.let { res ->
        val width = res.split("x").firstOrNull()?.toIntOrNull() ?: 0
        width >= minWidth
    } ?: false
}
fun List<Video>.filterHD(): List<Video> = filterByResolution(1280)
fun List<Video>.filterFullHD(): List<Video> = filterByResolution(1920)
fun List<Video>.filter4K(): List<Video> = filterByResolution(3840)
