package com.bytecoder.vplay.data

data class OnlineContentModel(
    val id: String = "",
    val title: String = "",
    val channel: String = "",
    val description: String = "",
    val thumbnailUrl: String = "",
    val streamUrl: String = "",
    val duration: String = "",
    val type: String = "", // "video", "audio", "podcast", "radio", "live"
    val category: String = "",
    val tags: List<String> = emptyList(),
    val isLive: Boolean = false,
    val publishedDate: String = "",
    val viewCount: String = "",
    val quality: String = "",
    val language: String = "",
    val subtitle: String = "",
    val isSubscribed: Boolean = false,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayedTime: Long = 0L
)

data class OnlineCategory(
    val id: String,
    val name: String,
    val icon: String,
    val description: String,
    val contentCount: Int
)

data class RadioStation(
    val id: String,
    val name: String,
    val genre: String,
    val country: String,
    val language: String,
    val streamUrl: String,
    val logoUrl: String,
    val description: String,
    val bitrate: String,
    val isActive: Boolean
)

data class Podcast(
    val id: String,
    val title: String,
    val author: String,
    val description: String,
    val artworkUrl: String,
    val rssUrl: String,
    val website: String,
    val language: String,
    val category: String,
    val episodes: List<PodcastEpisode> = emptyList(),
    val isSubscribed: Boolean = false,
    val lastUpdated: String = ""
)

data class PodcastEpisode(
    val id: String,
    val title: String,
    val description: String,
    val audioUrl: String,
    val duration: String,
    val publishedDate: String,
    val episodeNumber: Int = 0,
    val seasonNumber: Int = 0,
    val isPlayed: Boolean = false,
    val playProgress: Float = 0f
)

data class LiveStream(
    val id: String,
    val title: String,
    val channel: String,
    val description: String,
    val thumbnailUrl: String,
    val streamUrl: String,
    val category: String,
    val language: String,
    val viewerCount: String,
    val startTime: String,
    val isActive: Boolean
)

data class StreamQuality(
    val label: String,
    val url: String,
    val resolution: String,
    val bitrate: String
)

data class OnlinePlaylist(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val channel: String,
    val videoCount: Int,
    val duration: String,
    val videos: List<OnlineContentModel> = emptyList(),
    val isPublic: Boolean = true,
    val createdDate: String = "",
    val updatedDate: String = ""
)