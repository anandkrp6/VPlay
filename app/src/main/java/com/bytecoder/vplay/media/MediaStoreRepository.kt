package com.bytecoder.vplay.media

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

class MediaStoreRepository(private val context: Context) {
    data class MediaEntry(
        val id: Long,
        val title: String,
        val artistOrAlbum: String?,
        val contentUri: Uri,
        val durationMs: Long,
        val isVideo: Boolean,
        val dateAddedSec: Long = 0L,
        val bucketDisplayName: String? = null
    )

    fun queryVideos(limit: Int = 500): List<MediaEntry> {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME
        )
        val sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC"

        context.contentResolver.query(collection, projection, null, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
            val list = ArrayList<MediaEntry>()
            var count = 0
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol) ?: "Untitled"
                val duration = cursor.getLong(durationCol)
                val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                val dateAdded = cursor.getLong(dateCol)
                val bucket = cursor.getString(bucketCol)
                list.add(MediaEntry(id, title, null, uri, duration, true, dateAdded, bucket))
                count++
                if (count >= limit) break
            }
            return list
        }
        return emptyList()
    }

    fun queryAudio(limit: Int = 1000): List<MediaEntry> {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DATE_ADDED
        )
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
        val sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC"

        context.contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val list = ArrayList<MediaEntry>()
            var count = 0
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol) ?: "Untitled"
                val artist = cursor.getString(artistCol)
                val duration = cursor.getLong(durationCol)
                val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                val album = cursor.getString(albumCol)
                val dateAdded = cursor.getLong(dateCol)
                val artistOrAlbum = artist ?: album
                list.add(MediaEntry(id, title, artistOrAlbum, uri, duration, false, dateAdded, null))
                count++
                if (count >= limit) break
            }
            return list
        }
        return emptyList()
    }

    fun getVideoBuckets(limit: Int = 500): List<String> {
        return queryVideos(limit).mapNotNull { it.bucketDisplayName }.distinct().sorted()
    }
}
