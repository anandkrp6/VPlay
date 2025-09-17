package com.bytecoder.vplay.frontend.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytecoder.vplay.backend.utils.HistoryTracker
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {
    private var historyTracker: HistoryTracker? = null
    
    fun initialize(context: Context) {
        if (historyTracker == null) {
            historyTracker = HistoryTracker.getInstance(context)
        }
    }
    
    val historyItems: StateFlow<List<HistoryTracker.HistoryItem>>
        get() = historyTracker?.historyItems ?: kotlinx.coroutines.flow.MutableStateFlow(emptyList())
    
    fun removeFromHistory(itemId: String) {
        viewModelScope.launch {
            historyTracker?.removeFromHistory(itemId)
        }
    }
    
    fun clearAllHistory() {
        viewModelScope.launch {
            historyTracker?.clearHistory()
        }
    }
    
    fun getRecentlyPlayed(limit: Int = 10): List<HistoryTracker.HistoryItem> {
        return historyTracker?.getRecentlyPlayed(limit) ?: emptyList()
    }
    
    fun getVideoHistory(limit: Int = 20): List<HistoryTracker.HistoryItem> {
        return historyTracker?.getByMediaType(isVideo = true, limit = limit) ?: emptyList()
    }
    
    fun getAudioHistory(limit: Int = 20): List<HistoryTracker.HistoryItem> {
        return historyTracker?.getByMediaType(isVideo = false, limit = limit) ?: emptyList()
    }
}