package com.bytecoder.vplay.backend.data.models

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long = 0L,
    val lastModified: Long = 0L,
    val extension: String = "",
    val isMediaFile: Boolean = false
) {
    val isVideoFile: Boolean
        get() = extension.lowercase() in listOf("mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "m4v")
    
    val isAudioFile: Boolean
        get() = extension.lowercase() in listOf("mp3", "wav", "flac", "aac", "ogg", "m4a", "wma")
    
    val isImageFile: Boolean
        get() = extension.lowercase() in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg")
    
    val isTextFile: Boolean
        get() = extension.lowercase() in listOf("txt", "md", "xml", "json", "html", "css", "js", "kt", "java")
}