package com.bytecoder.vplay.backend.models

data class Playlist(
    val id: String,
    val name: String,
    val description: String = "",
    val trackCount: Int = 0,
    val duration: Long = 0L,
    val coverImagePath: String? = null,
    val isSystemPlaylist: Boolean = false,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val createdDate: Long = System.currentTimeMillis(),
    val modifiedDate: Long = System.currentTimeMillis()
)

