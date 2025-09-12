package com.bytecoder.vplay.media

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import androidx.annotation.OptIn
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

object PlayerManager {
    private var player: ExoPlayer? = null
    private var appContext: Context? = null
    private val prefsName = "vplay_player"
    private const val KEY_QUEUE = "queue_json"
    private const val KEY_INDEX = "index"
    private const val KEY_POSITION = "position"
    private var restoring = false
    private const val KEY_POS_PREFIX = "pos_" // per-item position by mediaId

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _duration = MutableLiveData<Long>(0)
    val duration: LiveData<Long> = _duration

    private val _position = MutableLiveData<Long>(0)
    val position: LiveData<Long> = _position

    @OptIn(UnstableApi::class)
    fun getOrCreate(context: Context): ExoPlayer {
        val existing = player
        if (existing != null) return existing
        appContext = context.applicationContext
        val p = ExoPlayer.Builder(context).build()
        p.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.postValue(isPlaying)
            }
            override fun onEvents(player: Player, events: Player.Events) {
                _duration.postValue(player.duration.coerceAtLeast(0))
                _position.postValue(player.currentPosition.coerceAtLeast(0))
                if (!restoring) saveState()
                // Save per-item progress occasionally
                if (!restoring && player.currentMediaItem != null && player.duration > 0) {
                    savePerItemProgress(player.currentMediaItem!!.mediaId, player.currentPosition)
                }
            }
        })
        player = p
        // Ensure service is running for background controls
        context.startService(Intent(context, PlaybackService::class.java))
        // Attempt to restore previous session
        restoreState()
        return p
    }

    fun setQueue(context: Context, items: List<MediaItemModel>, startIndex: Int) {
        val p = getOrCreate(context)
        p.clearMediaItems()
        val mediaItems = items.map { model ->
            MediaItem.Builder()
                .setUri(model.uri)
                .setMediaId(model.id)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(model.title)
                        .setArtist(model.subtitle)
                        .build()
                )
                .build()
        }
        p.setMediaItems(mediaItems, startIndex, 0L)
        p.prepare()
        p.playWhenReady = true
        saveState()
    }

    fun addToQueue(context: Context, item: MediaItemModel) {
        val p = getOrCreate(context)
        p.addMediaItem(
            MediaItem.Builder()
                .setUri(item.uri)
                .setMediaId(item.id)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(item.title)
                        .setArtist(item.subtitle)
                        .build()
                )
                .build()
        )
        if (p.mediaItemCount == 1) {
            p.prepare()
            p.playWhenReady = true
        }
        saveState()
    }

    fun rebuildFromQueue(context: Context, items: List<MediaItemModel>, currentIndex: Int) {
        setQueue(context, items, currentIndex)
    }

    fun moveItemInPlayer(from: Int, to: Int) {
        val p = player ?: return
        if (from == to) return
        val item = p.getMediaItemAt(from)
        p.removeMediaItem(from)
        p.addMediaItem(to, item)
        saveState()
    }

    fun playPause() {
        val p = player ?: return
        p.playWhenReady = !p.playWhenReady
    }

    fun next() { player?.seekToNext() }
    fun previous() { player?.seekToPrevious() }

    fun hasActivePlayback(): Boolean {
        val p = player
        return p != null && p.mediaItemCount > 0
    }

    fun release() {
        player?.release()
        player = null
    }

    // region Persistence
    private fun saveState() {
        val context = appContext ?: return
        val p = player ?: return
        try {
            val list = (0 until p.mediaItemCount).map { idx ->
                val item = p.getMediaItemAt(idx)
                mapOf(
                    "id" to (item.mediaId ?: ""),
                    "title" to (item.mediaMetadata.title?.toString() ?: ""),
                    "subtitle" to (item.mediaMetadata.artist?.toString() ?: ""),
                    "uri" to (item.localConfiguration?.uri?.toString() ?: ""),
                    "isVideo" to (item.mediaMetadata.genre == "video")
                )
            }
            val jsonArr = kotlinx.serialization.json.buildJsonArray {
                list.forEach { m ->
                    add(
                        kotlinx.serialization.json.buildJsonObject {
                            m.forEach { (k, v) ->
                                put(k, when (v) {
                                    is Boolean -> kotlinx.serialization.json.JsonPrimitive(v)
                                    is Number -> kotlinx.serialization.json.JsonPrimitive(v)
                                    else -> kotlinx.serialization.json.JsonPrimitive(v.toString())
                                })
                            }
                        }
                    )
                }
            }
            val json = kotlinx.serialization.json.Json.encodeToString(
                kotlinx.serialization.json.JsonArray.serializer(),
                jsonArr
            )
            val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_QUEUE, json)
                .putInt(KEY_INDEX, p.currentMediaItemIndex)
                .putLong(KEY_POSITION, p.currentPosition)
                .apply()
        } catch (_: Exception) { }
    }

    private fun restoreState() {
        val context = appContext ?: return
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_QUEUE, null) ?: return
        try {
            val arr: JsonArray = Json.parseToJsonElement(json).jsonArray
            val models = arr.map { el ->
                val obj = el.jsonObject
                MediaItemModel(
                    id = obj["id"]?.jsonPrimitive?.content ?: "",
                    title = obj["title"]?.jsonPrimitive?.content ?: "",
                    subtitle = obj["subtitle"]?.jsonPrimitive?.contentOrNull,
                    uri = obj["uri"]?.jsonPrimitive?.content ?: "",
                    isVideo = obj["isVideo"]?.jsonPrimitive?.booleanOrNull ?: false
                )
            }
            if (models.isEmpty()) return
            val startIndex = prefs.getInt(KEY_INDEX, 0).coerceIn(0, models.lastIndex)
            val position = prefs.getLong(KEY_POSITION, 0L).coerceAtLeast(0L)
            restoring = true
            setQueue(context, models, startIndex)
            val p = player
            if (p != null && position > 0) {
                p.seekTo(position)
                p.playWhenReady = false
            }
        } catch (_: Exception) {
        } finally {
            restoring = false
        }
    }

    private fun savePerItemProgress(mediaId: String, positionMs: Long) {
        val context = appContext ?: return
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_POS_PREFIX + mediaId, positionMs).apply()
    }

    fun getPerItemProgress(context: Context, mediaId: String): Long {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_POS_PREFIX + mediaId, 0L)
    }
    // endregion
}
