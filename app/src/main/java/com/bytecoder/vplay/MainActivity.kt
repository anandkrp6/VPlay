package com.bytecoder.vplay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.core.view.WindowCompat
import androidx.media3.common.util.UnstableApi
import com.bytecoder.vplay.backend.managers.PlaybackQueueManager
import com.bytecoder.vplay.frontend.ui.theme.VPlayTheme
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.bytecoder.vplay.backend.services.PlaybackService

class MainActivity : ComponentActivity() {

    private val queueManager: PlaybackQueueManager by viewModels()
    
    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // Remember that we've asked once to avoid re-prompt loops
        getSharedPreferences("vplay_prefs", MODE_PRIVATE).edit()
            .putBoolean("asked_notif_perm", true).apply()
        // No immediate action required regardless of grant result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Request notification permission if needed
        requestNotificationPermissionIfNeeded()
        
        setContent {
            VPlayTheme {
                VPlayApp(
                    queueManager = queueViewModel
                )
            }
        }
    }

    @OptIn(UnstableApi::class)
    override fun onResume() {
        super.onResume()
        // Inform service app is foreground; hide persistent notification
        startService(android.content.Intent(this, PlaybackService::class.java).setAction(PlaybackService.ACTION_APP_FOREGROUND))
    }

    @OptIn(UnstableApi::class)
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Handle Picture-in-Picture mode for video playback
        val player = PlayerManager.getOrCreate(this)
        if (player.isPlaying && player.videoFormat != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                enterPictureInPictureMode(buildPipParams())
            }
        }
        // Request notification permission when leaving app
        requestNotificationPermissionIfNeeded()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT < 33) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) return
        val prefs = getSharedPreferences("vplay_prefs", MODE_PRIVATE)
        val asked = prefs.getBoolean("asked_notif_perm", false)
        if (!asked) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    @OptIn(UnstableApi::class)
    private fun buildPipParams(): android.app.PictureInPictureParams {
        val playPauseIntent = android.app.PendingIntent.getService(
            this, 201,
            android.content.Intent(this, PlaybackService::class.java).setAction(PlaybackService.ACTION_PLAY_PAUSE),
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or (if (android.os.Build.VERSION.SDK_INT >= 23) android.app.PendingIntent.FLAG_IMMUTABLE else 0)
        )
        val nextIntent = android.app.PendingIntent.getService(
            this, 202,
            android.content.Intent(this, PlaybackService::class.java).setAction(PlaybackService.ACTION_NEXT),
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or (if (android.os.Build.VERSION.SDK_INT >= 23) android.app.PendingIntent.FLAG_IMMUTABLE else 0)
        )
        val prevIntent = android.app.PendingIntent.getService(
            this, 203,
            android.content.Intent(this, PlaybackService::class.java).setAction(PlaybackService.ACTION_PREV),
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or (if (android.os.Build.VERSION.SDK_INT >= 23) android.app.PendingIntent.FLAG_IMMUTABLE else 0)
        )
        val isPlaying = PlayerManager.isPlaying.value == true
        val playIconRes = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPause = android.app.RemoteAction(
            android.graphics.drawable.Icon.createWithResource(this, playIconRes), 
            if (isPlaying) "Pause" else "Play", 
            "Play/Pause", 
            playPauseIntent
        )
        val next = android.app.RemoteAction(
            android.graphics.drawable.Icon.createWithResource(this, android.R.drawable.ic_media_next), 
            "Next", 
            "Next", 
            nextIntent
        )
        val prev = android.app.RemoteAction(
            android.graphics.drawable.Icon.createWithResource(this, android.R.drawable.ic_media_previous), 
            "Prev", 
            "Prev", 
            prevIntent
        )
        return android.app.PictureInPictureParams.Builder()
            .setAspectRatio(android.util.Rational(16,9))
            .setActions(listOf(prev, playPause, next))
            .build()
    }
}


