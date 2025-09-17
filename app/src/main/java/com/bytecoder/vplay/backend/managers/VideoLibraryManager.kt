package com.bytecoder.vplay.backend.managers

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.Manifest
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

data class VideoFile(
    val id: String,
    val title: String,
    val filePath: String,
    val duration: Long,
    val size: Long,
    val width: Int,
    val height: Int,
    val dateAdded: Long,
    val dateModified: Long,
    val mimeType: String,
    val resolution: String = "${width}x${height}",
    val displayName: String = title,
    val thumbnailPath: String? = null
)

sealed class VideoScanResult {
    data class Success(val videos: List<VideoFile>) : VideoScanResult()
    data class Error(val message: String, val cause: Throwable? = null) : VideoScanResult()
    object PermissionDenied : VideoScanResult()
}

class VideoLibraryManager(private val context: Context) {

    suspend fun scanVideosFromDevice(): VideoScanResult = withContext(Dispatchers.IO) {
        // Check for storage permissions
        if (!hasStoragePermission()) {
            return@withContext VideoScanResult.PermissionDenied
        }

        try {
            val videos = performVideoScan()
            VideoScanResult.Success(videos)
        } catch (e: SecurityException) {
            VideoScanResult.Error("Storage permission denied", e)
        } catch (e: Exception) {
            VideoScanResult.Error("Failed to scan videos: ${e.message}", e)
        }
    }

    private fun hasStoragePermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_VIDEO
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Below Android 13 uses READ_EXTERNAL_STORAGE
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private suspend fun performVideoScan(): List<VideoFile> = withContext(Dispatchers.IO) {
        val videos = mutableListOf<VideoFile>()
        
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.MIME_TYPE
        )
        
        val selection = "${MediaStore.Video.Media.MIME_TYPE} LIKE 'video/%'"
        val sortOrder = "${MediaStore.Video.Media.TITLE} ASC"
        
        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
            
            while (cursor.moveToNext()) {
                try {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown Video"
                    val displayName = cursor.getString(displayNameColumn) ?: title
                    val filePath = cursor.getString(dataColumn) ?: continue
                    val duration = cursor.getLong(durationColumn)
                    val size = cursor.getLong(sizeColumn)
                    val width = cursor.getInt(widthColumn)
                    val height = cursor.getInt(heightColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn) * 1000
                    val dateModified = cursor.getLong(dateModifiedColumn) * 1000
                    val mimeType = cursor.getString(mimeTypeColumn) ?: "video/mp4"
                    
                    // Validate file exists and is readable
                    val file = File(filePath)
                    if (!file.exists() || !file.canRead()) continue
                    
                    // Skip corrupted or very small files
                    if (size < 1024) continue // Skip files smaller than 1KB
                    
                    // Generate thumbnail
                    val thumbnailPath = generateVideoThumbnail(filePath, id.toString())
                    
                    val video = VideoFile(
                        id = id.toString(),
                        title = title,
                        filePath = filePath,
                        duration = duration,
                        size = size,
                        width = width,
                        height = height,
                        dateAdded = dateAdded,
                        dateModified = dateModified,
                        mimeType = mimeType,
                        thumbnailPath = thumbnailPath
                    )
                    
                    videos.add(video)
                } catch (e: Exception) {
                    // Log individual file errors but continue processing
                    continue
                }
            }
        }
        
        return@withContext videos
    }
    
    fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        val hours = (durationMs / (1000 * 60 * 60))
        
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }
    
    fun formatFileSize(bytes: Long): String {
        val kilobyte = 1024
        val megabyte = kilobyte * 1024
        val gigabyte = megabyte * 1024
        
        return when {
            bytes >= gigabyte -> String.format("%.1f GB", bytes.toDouble() / gigabyte)
            bytes >= megabyte -> String.format("%.1f MB", bytes.toDouble() / megabyte)
            bytes >= kilobyte -> String.format("%.1f KB", bytes.toDouble() / kilobyte)
            else -> "$bytes bytes"
        }
    }
    
    private suspend fun generateVideoThumbnail(videoPath: String, videoId: String): String? = withContext(Dispatchers.IO) {
        try {
            val thumbnailsDir = File(context.cacheDir, "video_thumbnails")
            if (!thumbnailsDir.exists()) {
                thumbnailsDir.mkdirs()
            }
            
            val thumbnailFile = File(thumbnailsDir, "thumb_$videoId.jpg")
            
            // Return existing thumbnail if it exists
            if (thumbnailFile.exists()) {
                return@withContext thumbnailFile.absolutePath
            }
            
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(videoPath)
                
                // Get thumbnail at 1 second into the video (or start if video is shorter)
                val timeUs = 1_000_000L // 1 second in microseconds
                val bitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                
                if (bitmap != null) {
                    // Scale down the bitmap to save space
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 160, 120, true)
                    
                    val outputStream = FileOutputStream(thumbnailFile)
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                    outputStream.close()
                    
                    bitmap.recycle()
                    scaledBitmap.recycle()
                    
                    return@withContext thumbnailFile.absolutePath
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    retriever.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return@withContext null
    }
    
    fun clearThumbnailCache() {
        try {
            val thumbnailsDir = File(context.cacheDir, "video_thumbnails")
            if (thumbnailsDir.exists()) {
                thumbnailsDir.listFiles()?.forEach { file ->
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}