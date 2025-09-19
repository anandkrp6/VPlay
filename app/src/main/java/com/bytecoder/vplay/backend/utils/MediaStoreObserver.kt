package com.bytecoder.vplay.backend.utils

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MediaStoreObserver(
    private val context: Context,
    private val onMediaChanged: (MediaType) -> Unit
) : ContentObserver(Handler(Looper.getMainLooper())) {
    
    enum class MediaType {
        AUDIO, VIDEO, BOTH
    }
    
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var refreshJob: Job? = null
    
    private val audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    
    fun startObserving() {
        context.contentResolver.registerContentObserver(audioUri, true, this)
        context.contentResolver.registerContentObserver(videoUri, true, this)
    }
    
    fun stopObserving() {
        context.contentResolver.unregisterContentObserver(this)
        refreshJob?.cancel()
    }
    
    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        
        // Debounce rapid changes - only refresh after changes stop for 2 seconds
        refreshJob?.cancel()
        refreshJob = scope.launch {
            delay(2000) // Wait 2 seconds after last change
            
            val mediaType = when {
                uri?.toString()?.contains("audio") == true -> MediaType.AUDIO
                uri?.toString()?.contains("video") == true -> MediaType.VIDEO
                else -> MediaType.BOTH
            }
            
            onMediaChanged(mediaType)
        }
    }
    
    override fun onChange(selfChange: Boolean) {
        onChange(selfChange, null)
    }
}


