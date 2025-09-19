package com.bytecoder.vplay.backend.data.models

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * MediaFile represents a generic media file in the VPlay system.
 * This can be either an audio or video file with common properties.
 */
@Entity(tableName = "media_files")
data class MediaFile(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String? = null,
    val album: String? = null,
    val duration: Long = 0L,
    val filePath: String,
    val size: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis(),
    val albumArtPath: String? = null,
    val thumbnailPath: String? = null,
    val type: MediaType = MediaType.AUDIO,
    val mimeType: String? = null,
    val bitrate: Int? = null,
    val sampleRate: Int? = null,
    val resolution: String? = null, // For videos: "1920x1080"
    val frameRate: Float? = null,   // For videos
    val isVideo: Boolean = false
) {
    // Helper properties
    val uri: Uri get() = Uri.parse(filePath)
    val isAudio: Boolean get() = type == MediaType.AUDIO
    val displayTitle: String get() = title.ifBlank { "Unknown Title" }
    val displayArtist: String get() = artist?.ifBlank { "Unknown Artist" } ?: "Unknown Artist"
    val displayAlbum: String get() = album?.ifBlank { "Unknown Album" } ?: "Unknown Album"
    
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
}

/**
 * Enum representing the type of media file
 */
enum class MediaType {
    AUDIO,
    VIDEO
}

/**
 * Extension functions for collections of MediaFile
 */
fun List<MediaFile>.filterAudio(): List<MediaFile> = filter { it.type == MediaType.AUDIO }
fun List<MediaFile>.filterVideo(): List<MediaFile> = filter { it.type == MediaType.VIDEO }
fun List<MediaFile>.sortedByTitle(): List<MediaFile> = sortedBy { it.title.lowercase() }
fun List<MediaFile>.sortedByArtist(): List<MediaFile> = sortedBy { it.artist?.lowercase() ?: "" }
fun List<MediaFile>.sortedByDateAdded(): List<MediaFile> = sortedByDescending { it.dateAdded }
fun List<MediaFile>.sortedByDuration(): List<MediaFile> = sortedByDescending { it.duration }
