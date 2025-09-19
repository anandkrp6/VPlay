package com.bytecoder.vplay.backend.managers

import android.net.Uri

/**
 * Represents a subtitle track for video content.
 * Contains information about subtitle language, source, and display properties.
 */
data class SubtitleTrack(
    val id: String,
    val language: String,
    val uri: Uri? = null,
    val label: String = language,
    val mimeType: String = "text/vtt", // Default to WebVTT
    val isDefault: Boolean = false,
    val isForced: Boolean = false,
    val isAutoSelect: Boolean = false
) {
    // Helper properties
    val displayName: String get() = label.ifBlank { language }
    val isExternal: Boolean get() = uri != null
    
    companion object {
        // Common subtitle mime types
        const val MIME_TYPE_VTT = "text/vtt"
        const val MIME_TYPE_SRT = "application/x-subrip"
        const val MIME_TYPE_ASS = "text/x-ass"
        const val MIME_TYPE_SSA = "text/x-ssa"
        
        // Create subtitle track for different formats
        fun createVTT(id: String, language: String, uri: Uri? = null): SubtitleTrack {
            return SubtitleTrack(id, language, uri, mimeType = MIME_TYPE_VTT)
        }
        
        fun createSRT(id: String, language: String, uri: Uri? = null): SubtitleTrack {
            return SubtitleTrack(id, language, uri, mimeType = MIME_TYPE_SRT)
        }
        
        fun createASS(id: String, language: String, uri: Uri? = null): SubtitleTrack {
            return SubtitleTrack(id, language, uri, mimeType = MIME_TYPE_ASS)
        }
        
        // Disabled/None subtitle track
        fun none(): SubtitleTrack {
            return SubtitleTrack("none", "None", null, "None")
        }
    }
}

/**
 * Extension functions for subtitle track collections
 */
fun List<SubtitleTrack>.sortedByLanguage(): List<SubtitleTrack> = sortedBy { it.language }
fun List<SubtitleTrack>.filterByLanguage(language: String): List<SubtitleTrack> = 
    filter { it.language.equals(language, ignoreCase = true) }
fun List<SubtitleTrack>.getDefault(): SubtitleTrack? = find { it.isDefault }
fun List<SubtitleTrack>.getByLanguage(language: String): SubtitleTrack? = 
    find { it.language.equals(language, ignoreCase = true) }
