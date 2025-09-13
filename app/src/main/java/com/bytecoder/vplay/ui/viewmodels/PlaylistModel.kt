package com.bytecoder.vplay.ui.viewmodels

data class PlaylistModel(
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
    val lastModifiedDate: Long = System.currentTimeMillis()
)