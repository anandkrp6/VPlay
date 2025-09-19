package com.bytecoder.vplay.backend.managers

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bytecoder.vplay.backend.utils.HistoryTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * HistoryScreenManager manages playback history and recently played media.
 * Provides access to history tracking and management functionality.
 */
class HistoryScreenManager(application: Application) : AndroidViewModel(application) {
    
    private val context: Context = application.applicationContext
    private var historyTracker: HistoryTracker? = null
    
    init {
        initialize()
    }
    
    private fun initialize() {
        if (historyTracker == null) {
            historyTracker = HistoryTracker.getInstance(context)
        }
    }
    
    val historyItems: StateFlow<List<HistoryTracker.HistoryItem>>
        get() = historyTracker?.historyItems ?: MutableStateFlow(emptyList())
    
    /**
     * Remove specific item from history
     */
    fun removeFromHistory(itemId: String) {
        viewModelScope.launch {
            historyTracker?.removeFromHistory(itemId)
        }
    }
    
    /**
     * Clear all history
     */
    fun clearAllHistory() {
        viewModelScope.launch {
            historyTracker?.clearHistory()
        }
    }
    
    /**
     * Get recently played items
     */
    fun getRecentlyPlayed(limit: Int = 10): List<HistoryTracker.HistoryItem> {
        return historyTracker?.getRecentlyPlayed(limit) ?: emptyList()
    }
    
    /**
     * Get video history
     */
    fun getVideoHistory(limit: Int = 20): List<HistoryTracker.HistoryItem> {
        return historyTracker?.getByMediaType(isVideo = true, limit = limit) ?: emptyList()
    }
    
    /**
     * Get audio history
     */
    fun getAudioHistory(limit: Int = 20): List<HistoryTracker.HistoryItem> {
        return historyTracker?.getByMediaType(isVideo = false, limit = limit) ?: emptyList()
    }
    
    /**
     * Add item to history
     */
    fun addToHistory(
        itemId: String,
        title: String,
        artist: String = "",
        isVideo: Boolean = false,
        duration: Long = 0L,
        thumbnailPath: String? = null
    ) {
        viewModelScope.launch {
            historyTracker?.addToHistory(itemId, title, artist, isVideo, duration, thumbnailPath)
        }
    }
    
    /**
     * Get history by date range
     */
    fun getHistoryByDateRange(startTime: Long, endTime: Long): List<HistoryTracker.HistoryItem> {
        return historyTracker?.getHistoryByDateRange(startTime, endTime) ?: emptyList()
    }
    
    /**
     * Search history
     */
    fun searchHistory(query: String): List<HistoryTracker.HistoryItem> {
        return historyTracker?.searchHistory(query) ?: emptyList()
    }
}



