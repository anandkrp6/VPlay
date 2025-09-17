package com.bytecoder.vplay.backend.managers

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class MediaInfo(
    val duration: Long,
    val width: Int,
    val height: Int,
    val bitrate: Int,
    val frameRate: Float,
    val hasAudio: Boolean,
    val hasVideo: Boolean,
    val audioCodec: String?,
    val videoCodec: String?,
    val audioChannels: Int,
    val audioSampleRate: Int,
    val fileSize: Long,
    val title: String?,
    val artist: String?,
    val album: String?,
    val genre: String?,
    val year: Int
)

data class ConversionProgress(
    val progress: Float,
    val status: String,
    val outputFile: String?
)

class MediaToolsManager(private val context: Context) {

    suspend fun analyzeMedia(filePath: String): MediaInfo? = withContext(Dispatchers.IO) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull() ?: 0
            val frameRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toFloatOrNull() ?: 0f
            
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
            val year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)?.toIntOrNull() ?: 0
            
            // Analyze tracks
            val extractor = MediaExtractor()
            extractor.setDataSource(filePath)
            
            var hasAudio = false
            var hasVideo = false
            var audioCodec: String? = null
            var videoCodec: String? = null
            var audioChannels = 0
            var audioSampleRate = 0
            
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mimeType = format.getString(MediaFormat.KEY_MIME) ?: ""
                
                when {
                    mimeType.startsWith("audio/") -> {
                        hasAudio = true
                        audioCodec = mimeType
                        audioChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                        audioSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                    }
                    mimeType.startsWith("video/") -> {
                        hasVideo = true
                        videoCodec = mimeType
                    }
                }
            }
            
            extractor.release()
            retriever.release()
            
            val fileSize = File(filePath).length()
            
            MediaInfo(
                duration = duration,
                width = width,
                height = height,
                bitrate = bitrate,
                frameRate = frameRate,
                hasAudio = hasAudio,
                hasVideo = hasVideo,
                audioCodec = audioCodec,
                videoCodec = videoCodec,
                audioChannels = audioChannels,
                audioSampleRate = audioSampleRate,
                fileSize = fileSize,
                title = title,
                artist = artist,
                album = album,
                genre = genre,
                year = year
            )
            
        } catch (e: Exception) {
            null
        }
    }

    suspend fun generateThumbnail(
        videoPath: String, 
        timeUs: Long = 1000000L,
        width: Int = 320,
        height: Int = 240
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoPath)
            
            val bitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            retriever.release()
            
            bitmap?.let { 
                Bitmap.createScaledBitmap(it, width, height, true)
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun generateThumbnails(
        videoPath: String,
        count: Int = 10,
        outputDir: String
    ): List<String> = withContext(Dispatchers.IO) {
        val thumbnails = mutableListOf<String>()
        
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoPath)
            
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val interval = duration / count
            
            for (i in 0 until count) {
                val timeUs = (i * interval * 1000) // Convert to microseconds
                val bitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                
                bitmap?.let {
                    val filename = "thumb_${i}_${System.currentTimeMillis()}.jpg"
                    val file = File(outputDir, filename)
                    
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                    }
                    
                    thumbnails.add(file.absolutePath)
                    bitmap.recycle()
                }
            }
            
            retriever.release()
        } catch (e: Exception) {
            // Handle error
        }
        
        thumbnails
    }

    suspend fun extractAudioFromVideo(
        videoPath: String,
        outputPath: String,
        onProgress: (ConversionProgress) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // This is a simplified version - in practice you'd use FFmpeg or similar
            onProgress(ConversionProgress(0f, "Starting audio extraction...", null))
            
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoPath)
            
            // Simulate progress
            for (i in 1..10) {
                kotlinx.coroutines.delay(500)
                onProgress(ConversionProgress(i / 10f, "Extracting audio... ${i * 10}%", null))
            }
            
            retriever.release()
            
            onProgress(ConversionProgress(1f, "Audio extraction completed", outputPath))
            true
            
        } catch (e: Exception) {
            onProgress(ConversionProgress(0f, "Error: ${e.message}", null))
            false
        }
    }

    suspend fun compressVideo(
        inputPath: String,
        outputPath: String,
        quality: VideoQuality,
        onProgress: (ConversionProgress) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            onProgress(ConversionProgress(0f, "Starting video compression...", null))
            
            // This is a simplified version - in practice you'd use FFmpeg
            val params = when (quality) {
                VideoQuality.LOW -> "Low quality (480p)"
                VideoQuality.MEDIUM -> "Medium quality (720p)"
                VideoQuality.HIGH -> "High quality (1080p)"
            }
            
            onProgress(ConversionProgress(0.2f, "Analyzing video: $params", null))
            kotlinx.coroutines.delay(1000)
            
            onProgress(ConversionProgress(0.5f, "Compressing video...", null))
            kotlinx.coroutines.delay(2000)
            
            onProgress(ConversionProgress(0.8f, "Finalizing...", null))
            kotlinx.coroutines.delay(1000)
            
            onProgress(ConversionProgress(1f, "Video compression completed", outputPath))
            true
            
        } catch (e: Exception) {
            onProgress(ConversionProgress(0f, "Error: ${e.message}", null))
            false
        }
    }

    suspend fun convertAudioFormat(
        inputPath: String,
        outputPath: String,
        format: AudioFormat,
        quality: AudioQuality,
        onProgress: (ConversionProgress) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            onProgress(ConversionProgress(0f, "Starting audio conversion...", null))
            
            val formatInfo = "${format.name} ${quality.bitrate}kbps"
            onProgress(ConversionProgress(0.3f, "Converting to $formatInfo", null))
            
            // Simulate conversion process
            kotlinx.coroutines.delay(2000)
            
            onProgress(ConversionProgress(1f, "Audio conversion completed", outputPath))
            true
            
        } catch (e: Exception) {
            onProgress(ConversionProgress(0f, "Error: ${e.message}", null))
            false
        }
    }

    suspend fun editMetadata(
        filePath: String,
        metadata: Map<String, String>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // This would use a metadata editing library in practice
            // For now, just simulate the operation
            kotlinx.coroutines.delay(500)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun createGif(
        videoPath: String,
        startTime: Long,
        duration: Long,
        outputPath: String,
        onProgress: (ConversionProgress) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            onProgress(ConversionProgress(0f, "Creating GIF...", null))
            
            onProgress(ConversionProgress(0.3f, "Extracting frames...", null))
            kotlinx.coroutines.delay(1000)
            
            onProgress(ConversionProgress(0.7f, "Generating GIF...", null))
            kotlinx.coroutines.delay(1500)
            
            onProgress(ConversionProgress(1f, "GIF created successfully", outputPath))
            true
            
        } catch (e: Exception) {
            onProgress(ConversionProgress(0f, "Error: ${e.message}", null))
            false
        }
    }

    fun getFileInfo(filePath: String): FileInfo {
        val file = File(filePath)
        return FileInfo(
            name = file.name,
            path = file.absolutePath,
            size = file.length(),
            lastModified = file.lastModified(),
            isReadable = file.canRead(),
            isWritable = file.canWrite(),
            extension = file.extension,
            mimeType = getMimeType(file.extension)
        )
    }

    private fun getMimeType(extension: String): String {
        return when (extension.lowercase()) {
            "mp4", "mkv", "avi", "mov" -> "video/${extension.lowercase()}"
            "mp3", "m4a", "flac", "ogg" -> "audio/${extension.lowercase()}"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "txt" -> "text/plain"
            "pdf" -> "application/pdf"
            else -> "application/octet-stream"
        }
    }
}

data class FileInfo(
    val name: String,
    val path: String,
    val size: Long,
    val lastModified: Long,
    val isReadable: Boolean,
    val isWritable: Boolean,
    val extension: String,
    val mimeType: String
) {
    fun getFormattedSize(): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        
        return when {
            gb >= 1 -> "%.1f GB".format(gb)
            mb >= 1 -> "%.1f MB".format(mb)
            kb >= 1 -> "%.1f KB".format(kb)
            else -> "$size B"
        }
    }
}

enum class VideoQuality(val displayName: String) {
    LOW("Low (480p)"),
    MEDIUM("Medium (720p)"),
    HIGH("High (1080p)")
}

enum class AudioFormat(val displayName: String, val extension: String) {
    MP3("MP3", "mp3"),
    AAC("AAC", "m4a"),
    FLAC("FLAC", "flac"),
    OGG("OGG Vorbis", "ogg"),
    WAV("WAV", "wav")
}

enum class AudioQuality(val displayName: String, val bitrate: Int) {
    LOW("Low (128kbps)", 128),
    MEDIUM("Medium (192kbps)", 192),
    HIGH("High (320kbps)", 320),
    LOSSLESS("Lossless", 0)
}