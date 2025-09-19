package com.bytecoder.vplay.backend.utils

import android.content.Context
import android.content.SharedPreferences
import com.bytecoder.vplay.backend.data.models.MediaItemModel
import com.bytecoder.vplay.backend.data.models.OnlineContentModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

class HistoryTracker private constructor(private val context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: HistoryTracker? = null
        
        fun getInstance(context: Context): HistoryTracker {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HistoryTracker(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        private const val PREFS_NAME = "history_tracker"
        private const val KEY_HISTORY_JSON = "history_json"
        private const val KEY_PLAYBACK_POSITIONS = "playback_positions"
        private const val MAX_HISTORY_ITEMS = 100
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val _historyItems = MutableStateFlow<List<HistoryItem>>(emptyList())
    val historyItems: StateFlow<List<HistoryItem>> = _historyItems.asStateFlow()
    
    // Store current playback positions for tracking progress
    private val currentPositions = ConcurrentHashMap<String, Long>()
    private val currentDurations = ConcurrentHashMap<String, Long>()
    
    data class HistoryItem(
        val id: String,
        val title: String,
        val subtitle: String = "",
        val uri: String,
        val isVideo: Boolean,
        val durationMs: Long,
        val lastPlayedTime: Long,
        val playbackPosition: Long = 0L,
        val playCount: Int = 1,
        val thumbnail: String? = null
    ) {
        val progressPercentage: Float
            get() = if (durationMs > 0) (playbackPosition.toFloat() / durationMs.toFloat()) else 0f
            
        val isCompleted: Boolean
            get() = progressPercentage >= 0.9f // Consider 90%+ as completed
    }
    
    init {
        loadHistoryFromPrefs()
    }
    
    fun recordPlayback(mediaItem: MediaItemModel, positionMs: Long = 0L) {
        val historyItem = HistoryItem(
            id = mediaItem.id,
            title = mediaItem.title,
            subtitle = mediaItem.subtitle ?: "",
            uri = mediaItem.uri,
            isVideo = mediaItem.isVideo,
            durationMs = mediaItem.durationMs,
            lastPlayedTime = System.currentTimeMillis(),
            playbackPosition = positionMs,
            thumbnail = mediaItem.thumbnailPath
        )
        addOrUpdateHistoryItem(historyItem)
    }
    
    fun recordPlayback(onlineContent: OnlineContentModel, positionMs: Long = 0L) {
        val historyItem = HistoryItem(
            id = onlineContent.id,
            title = onlineContent.title,
            subtitle = onlineContent.channel,
            uri = onlineContent.streamUrl,
            isVideo = onlineContent.type == "video" || onlineContent.type == "live",
            durationMs = parseDurationToMs(onlineContent.duration),
            lastPlayedTime = System.currentTimeMillis(),
            playbackPosition = positionMs,
            thumbnail = onlineContent.thumbnailUrl
        )
        addOrUpdateHistoryItem(historyItem)
    }
    
    private fun parseDurationToMs(duration: String): Long {
        return try {
            when {
                duration.equals("Live", ignoreCase = true) -> 0L
                duration.contains(":") -> {
                    val parts = duration.split(":")
                    when (parts.size) {
                        2 -> { // mm:ss
                            val minutes = parts[0].toLong()
                            val seconds = parts[1].toLong()
                            (minutes * 60 + seconds) * 1000
                        }
                        3 -> { // hh:mm:ss
                            val hours = parts[0].toLong()
                            val minutes = parts[1].toLong()
                            val seconds = parts[2].toLong()
                            (hours * 3600 + minutes * 60 + seconds) * 1000
                        }
                        else -> 0L
                    }
                }
                else -> 0L
            }
        } catch (e: Exception) {
            0L
        }
    }
    
    fun updatePlaybackPosition(itemId: String, positionMs: Long, durationMs: Long = 0L) {
        currentPositions[itemId] = positionMs
        if (durationMs > 0) {
            currentDurations[itemId] = durationMs
        }
        
        // Update history item if it exists
        val currentHistory = _historyItems.value.toMutableList()
        val existingIndex = currentHistory.indexOfFirst { it.id == itemId }
        if (existingIndex != -1) {
            val existingItem = currentHistory[existingIndex]
            val updatedItem = existingItem.copy(
                playbackPosition = positionMs,
                durationMs = if (durationMs > 0) durationMs else existingItem.durationMs,
                lastPlayedTime = System.currentTimeMillis()
            )
            currentHistory[existingIndex] = updatedItem
            _historyItems.value = currentHistory
            saveHistoryToPrefs()
        }
    }
    
    private fun addOrUpdateHistoryItem(newItem: HistoryItem) {
        val currentHistory = _historyItems.value.toMutableList()
        
        // Check if item already exists
        val existingIndex = currentHistory.indexOfFirst { it.id == newItem.id }
        
        if (existingIndex != -1) {
            // Update existing item
            val existingItem = currentHistory[existingIndex]
            val updatedItem = newItem.copy(
                playCount = existingItem.playCount + 1,
                playbackPosition = currentPositions[newItem.id] ?: newItem.playbackPosition
            )
            currentHistory.removeAt(existingIndex)
            currentHistory.add(0, updatedItem) // Move to top
        } else {
            // Add new item at the beginning
            currentHistory.add(0, newItem)
        }
        
        // Limit history size
        if (currentHistory.size > MAX_HISTORY_ITEMS) {
            currentHistory.removeAt(currentHistory.size - 1)
        }
        
        _historyItems.value = currentHistory
        saveHistoryToPrefs()
    }
    
    fun removeFromHistory(itemId: String) {
        val currentHistory = _historyItems.value.toMutableList()
        currentHistory.removeAll { it.id == itemId }
        _historyItems.value = currentHistory
        saveHistoryToPrefs()
        
        // Also remove from current positions
        currentPositions.remove(itemId)
        currentDurations.remove(itemId)
    }
    
    fun clearHistory() {
        _historyItems.value = emptyList()
        currentPositions.clear()
        currentDurations.clear()
        saveHistoryToPrefs()
    }
    
    fun getPlaybackPosition(itemId: String): Long {
        return currentPositions[itemId] ?: 0L
    }
    
    fun getHistoryItem(itemId: String): HistoryItem? {
        return _historyItems.value.find { it.id == itemId }
    }
    
    fun getRecentlyPlayed(limit: Int = 10): List<HistoryItem> {
        return _historyItems.value.take(limit)
    }
    
    fun getByMediaType(isVideo: Boolean, limit: Int = 20): List<HistoryItem> {
        return _historyItems.value
            .filter { it.isVideo == isVideo }
            .take(limit)
    }
    
    private fun saveHistoryToPrefs() {
        try {
            val historyJson = _historyItems.value.joinToString(separator = "|||") { item ->
                "${item.id}|${item.title}|${item.subtitle}|${item.uri}|${item.isVideo}|${item.durationMs}|${item.lastPlayedTime}|${item.playbackPosition}|${item.playCount}|${item.thumbnail ?: ""}"
            }
            prefs.edit().putString(KEY_HISTORY_JSON, historyJson).apply()
        } catch (e: Exception) {
            // Handle save error silently
        }
    }
    
    private fun loadHistoryFromPrefs() {
        try {
            val historyJson = prefs.getString(KEY_HISTORY_JSON, "") ?: ""
            if (historyJson.isNotEmpty()) {
                val items = historyJson.split("|||").mapNotNull { itemString ->
                    try {
                        val parts = itemString.split("|")
                        if (parts.size >= 9) {
                            HistoryItem(
                                id = parts[0],
                                title = parts[1],
                                subtitle = parts[2],
                                uri = parts[3],
                                isVideo = parts[4].toBoolean(),
                                durationMs = parts[5].toLong(),
                                lastPlayedTime = parts[6].toLong(),
                                playbackPosition = parts[7].toLong(),
                                playCount = parts[8].toInt(),
                                thumbnail = if (parts.size > 9 && parts[9].isNotEmpty()) parts[9] else null
                            )
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }
                _historyItems.value = items
            }
        } catch (e: Exception) {
            // Handle load error silently, start with empty history
            _historyItems.value = emptyList()
        }
    }
}


