package com.bytecoder.vplay.backend.data.repositories

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import com.bytecoder.vplay.backend.utils.AppSettings

class DownloadRepository(private val context: Context) {
    private val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    data class DownloadInfo(
        val id: Long,
        val title: String?,
        val status: Int,
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val localUri: String?,
        val reason: Int?
    )

    private fun prefs() = context.getSharedPreferences("vplay_downloads", Context.MODE_PRIVATE)

    fun enqueue(url: String, title: String = "Media") : Long {
        val req = DownloadManager.Request(Uri.parse(url))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(title)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, guessFileName(url))
            .setAllowedOverMetered(!AppSettings.isWifiOnly(context))
            .setAllowedOverRoaming(!AppSettings.isWifiOnly(context))
        if (AppSettings.isWifiOnly(context)) {
            req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
        }
        val id = dm.enqueue(req)
        trackId(id)
        return id
    }

    fun status(id: Long): Int? {
        val q = DownloadManager.Query().setFilterById(id)
        dm.query(q)?.use { c ->
            if (c.moveToFirst()) {
                val idx = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
                if (idx >= 0) return c.getInt(idx)
            }
        }
        return null
    }

    fun cancel(id: Long): Boolean {
        return try {
            dm.remove(id)
            untrackId(id)
            true
        } catch (_: Exception) { false }
    }

    fun getUri(id: Long): Uri? = try { dm.getUriForDownloadedFile(id) } catch (_: Exception) { null }

    fun open(context: Context, id: Long): Boolean {
        val uri = getUri(id) ?: return false
        val mime = try { dm.getMimeTypeForDownloadedFile(id) } catch (_: Exception) { null }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return try { context.startActivity(intent); true } catch (_: Exception) { false }
    }

    fun getTrackedIds(): List<Long> {
        val json = prefs().getString("ids", null) ?: return emptyList()
        return try {
            Json.parseToJsonElement(json).jsonArray.mapNotNull { it.jsonPrimitive.longOrNull }
        } catch (_: Exception) { emptyList() }
    }

    private fun trackId(id: Long) {
        val cur = getTrackedIds().toMutableList()
        if (!cur.contains(id)) cur.add(0, id)
        saveIds(cur)
    }

    private fun untrackId(id: Long) {
        val cur = getTrackedIds().toMutableList()
        cur.remove(id)
        saveIds(cur)
    }

    private fun saveIds(list: List<Long>) {
        val arr = buildJsonArray { list.forEach { add(JsonPrimitive(it)) } }
        prefs().edit().putString("ids", Json.encodeToString(JsonArray.serializer(), arr)).apply()
    }

    fun queryInfos(ids: List<Long>): List<DownloadInfo> {
        if (ids.isEmpty()) return emptyList()
        val q = DownloadManager.Query().setFilterById(*ids.toLongArray())
        val result = mutableListOf<DownloadInfo>()
        dm.query(q)?.use { c ->
            val idxId = c.getColumnIndex(DownloadManager.COLUMN_ID)
            val idxTitle = c.getColumnIndex(DownloadManager.COLUMN_TITLE)
            val idxStatus = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val idxBytes = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val idxTotal = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            val idxUri = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
            val idxReason = c.getColumnIndex(DownloadManager.COLUMN_REASON)
            while (c.moveToNext()) {
                result.add(
                    DownloadInfo(
                        id = if (idxId >= 0) c.getLong(idxId) else -1L,
                        title = if (idxTitle >= 0) c.getString(idxTitle) else null,
                        status = if (idxStatus >= 0) c.getInt(idxStatus) else -1,
                        bytesDownloaded = if (idxBytes >= 0) c.getLong(idxBytes) else 0L,
                        totalBytes = if (idxTotal >= 0) c.getLong(idxTotal) else -1L,
                        localUri = if (idxUri >= 0) c.getString(idxUri) else null,
                        reason = if (idxReason >= 0) c.getInt(idxReason) else null
                    )
                )
            }
        }
        // Ensure all ids are represented (e.g., removed entries)
        val present = result.map { it.id }.toSet()
        val missing = ids.filterNot { present.contains(it) }
        // If missing and status cannot be queried, untrack them
        if (missing.isNotEmpty()) {
            val cur = getTrackedIds().toMutableList(); cur.removeAll(missing); saveIds(cur)
        }
        return result.sortedBy { -it.id }
    }

    private fun guessFileName(url: String): String {
        val path = Uri.parse(url).lastPathSegment ?: "download.bin"
        return path
    }
}


