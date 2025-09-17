package com.bytecoder.vplay.backend.data.models

data class MediaItemModel(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val uri: String,
    val isVideo: Boolean = false,
    val durationMs: Long = 0L,
    val thumbnailPath: String? = null
)