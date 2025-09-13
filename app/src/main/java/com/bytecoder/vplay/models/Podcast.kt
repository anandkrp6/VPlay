package com.bytecoder.vplay.models

data class Podcast(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val author: String,
    val category: String,
    val language: String = "en",
    val episodeCount: Int = 0,
    val isSubscribed: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis(),
    val rssUrl: String = "",
    val website: String = ""
)