package com.bytecoder.vplay.backend.managers

import android.content.Context
import com.bytecoder.vplay.backend.data.database.AnalyticsEntry
import com.bytecoder.vplay.backend.data.database.VPlayDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

object AnalyticsManager {
    private var database: VPlayDatabase? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private var currentSessionId = UUID.randomUUID().toString()

    fun initialize(context: Context) {
        database = VPlayDatabase.getDatabase(context)
    }

    fun startNewSession() {
        currentSessionId = UUID.randomUUID().toString()
    }

    fun trackMediaPlay(mediaId: String, position: Long = 0) {
        trackEvent("play", mediaId, position = position)
    }

    fun trackMediaPause(mediaId: String, position: Long = 0, duration: Long = 0) {
        trackEvent("pause", mediaId, position = position, duration = duration)
    }

    fun trackMediaSeek(mediaId: String, fromPosition: Long, toPosition: Long) {
        trackEvent("seek", mediaId, position = toPosition, 
                  metadata = """{"from":$fromPosition,"to":$toPosition}""")
    }

    fun trackMediaComplete(mediaId: String, totalDuration: Long) {
        trackEvent("complete", mediaId, duration = totalDuration)
    }

    fun trackMediaSkip(mediaId: String, position: Long, reason: String = "user") {
        trackEvent("skip", mediaId, position = position, 
                  metadata = """{"reason":"$reason"}""")
    }

    fun trackFeatureUsage(feature: String, metadata: Map<String, Any> = emptyMap()) {
        val metadataJson = if (metadata.isEmpty()) "" else {
            metadata.entries.joinToString(",", "{", "}") { (k, v) -> 
                "\"$k\":\"$v\"" 
            }
        }
        trackEvent("feature_use", feature, metadata = metadataJson)
    }

    fun trackError(error: String, context: String? = null) {
        val metadata = context?.let { """{"context":"$it"}""" } ?: ""
        trackEvent("error", error, metadata = metadata)
    }

    private fun trackEvent(
        action: String, 
        mediaId: String, 
        position: Long = 0, 
        duration: Long = 0,
        metadata: String = ""
    ) {
        scope.launch {
            database?.analyticsDao()?.insertAnalytics(
                AnalyticsEntry(
                    mediaId = mediaId,
                    action = action,
                    sessionId = currentSessionId,
                    position = position,
                    duration = duration,
                    metadata = metadata
                )
            )
        }
    }

    suspend fun getMostPlayedMedia(limit: Int = 20) = 
        database?.analyticsDao()?.getMostPlayedMedia(limit) ?: emptyList()

    suspend fun getPlayCount(mediaId: String) = 
        database?.analyticsDao()?.getPlayCount(mediaId) ?: 0

    suspend fun getTotalPlayTime(mediaId: String) = 
        database?.analyticsDao()?.getTotalPlayTime(mediaId) ?: 0L

    suspend fun getActionStats(sinceDays: Int = 7) = 
        database?.analyticsDao()?.getActionStats(
            System.currentTimeMillis() - (sinceDays * 24 * 60 * 60 * 1000L)
        ) ?: emptyList()

    fun cleanupOldData(olderThanDays: Int = 90) {
        scope.launch {
            val cutoff = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
            database?.analyticsDao()?.deleteOldAnalytics(cutoff)
        }
    }
}


