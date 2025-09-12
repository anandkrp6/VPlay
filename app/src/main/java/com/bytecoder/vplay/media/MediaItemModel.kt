package com.bytecoder.vplay.media

data class MediaItemModel(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val uri: String,
    val isVideo: Boolean = false,
    val durationMs: Long = 0L
)