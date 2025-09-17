package com.bytecoder.vplay.backend.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaStyleNotificationHelper
import com.bytecoder.vplay.backend.managers.PlayerManager
import androidx.media.session.MediaButtonReceiver
import com.bytecoder.vplay.MainActivity
import com.bytecoder.vplay.R
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.AudioManager
import android.media.AudioFocusRequest
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.Size
import java.util.concurrent.Executors
import java.io.ByteArrayOutputStream
import android.content.BroadcastReceiver
import android.content.IntentFilter

@UnstableApi
class PlaybackService : Service() {
    private var mediaSession: MediaSession? = null
    private var playerListener: Player.Listener? = null
    private val artworkExecutor = Executors.newSingleThreadExecutor()
    @Volatile private var lastArtBitmap: Bitmap? = null
    @Volatile private var lastItemUri: Uri? = null
    // Simple in-memory artwork cache to avoid recomputation
    private val artworkCache = object : android.util.LruCache<String, Bitmap>(24) {
        override fun sizeOf(key: String, value: Bitmap): Int = 1 // count-based
    }

    // Audio focus handling
    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    @Volatile private var resumeOnFocusGain: Boolean = false
    @Volatile private var ducked: Boolean = false
    @Volatile private var normalVolume: Float = 1.0f
    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                // Pause playback when audio becomes noisy (e.g., headphones unplugged)
                val p = PlayerManager.getOrCreate(this@PlaybackService)
                if (p.isPlaying) p.playWhenReady = false
            }
        }
    }
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var pendingNotifUpdate: Runnable? = null
    @Volatile private var appInForeground: Boolean = true
    @Volatile private var isForegroundService: Boolean = false
    private fun debounceUpdateNotification(delayMs: Long = 200L) {
        val r = Runnable { updateNotification() }
        pendingNotifUpdate?.let { mainHandler.removeCallbacks(it) }
        pendingNotifUpdate = r
        mainHandler.postDelayed(r, delayMs)
    }

    override fun onCreate() {
        super.onCreate()
        val player = PlayerManager.getOrCreate(this)
        // Let us manage audio focus ourselves (second arg = handleAudioFocus)
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                .build(),
            false
        )
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mediaSession = MediaSession.Builder(this, player).build()
        // Update notification on player changes
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) ensureAudioFocus()
                debounceUpdateNotification()
                updateForegroundState()
            }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // Clear last art and attempt to load for new item
                lastArtBitmap = null
                debounceUpdateNotification()
                mediaItem?.let { maybeLoadArtwork(it) }
                updateForegroundState()
            }
        }
        player.addListener(listener)
        playerListener = listener
        createNotificationChannel()
        // Initial artwork load
        player.currentMediaItem?.let { maybeLoadArtwork(it) }
        // Register becoming noisy receiver
        registerReceiver(noisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
        // Evaluate initial foreground state
        updateForegroundState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> PlayerManager.playPause()
            ACTION_NEXT -> PlayerManager.next()
            ACTION_PREV -> PlayerManager.previous()
            // ACTION_MEDIA_BUTTON is routed by the manifest-declared MediaButtonReceiver
            ACTION_APP_FOREGROUND -> {
                appInForeground = true
                updateForegroundState()
            }
            ACTION_APP_BACKGROUND -> {
                appInForeground = false
                updateForegroundState()
            }
        }
        updateForegroundState()
        return START_STICKY
    }

    override fun onDestroy() {
        val p = PlayerManager.getOrCreate(this)
        playerListener?.let { p.removeListener(it) }
        playerListener = null
        mediaSession?.release()
        abandonAudioFocus()
        unregisterReceiver(noisyReceiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val session = mediaSession ?: throw IllegalStateException("No session")
        val player = PlayerManager.getOrCreate(this)

        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val prevPending = PendingIntent.getService(
            this, 1, Intent(this, PlaybackService::class.java).setAction(ACTION_PREV),
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        val playPausePending = PendingIntent.getService(
            this, 2, Intent(this, PlaybackService::class.java).setAction(ACTION_PLAY_PAUSE),
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        val nextPending = PendingIntent.getService(
            this, 3, Intent(this, PlaybackService::class.java).setAction(ACTION_NEXT),
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // Media button intent to support headset/BT controls
        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON).setClass(this, androidx.media.session.MediaButtonReceiver::class.java)
        val mediaButtonPending = PendingIntent.getBroadcast(
            this, 4, mediaButtonIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val isPlaying = player.isPlaying
        val playIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play

        val currentItem: MediaItem? = if (player.mediaItemCount > 0) player.currentMediaItem else null
        val title = currentItem?.mediaMetadata?.title ?: "vPlay"
        val text = if (currentItem != null) "Playing" else ""

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .addAction(NotificationCompat.Action(android.R.drawable.ic_media_previous, "Prev", prevPending))
            .addAction(NotificationCompat.Action(playIcon, if (isPlaying) "Pause" else "Play", playPausePending))
            .addAction(NotificationCompat.Action(android.R.drawable.ic_media_next, "Next", nextPending))
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(session.sessionCompatToken)
                    .setShowActionsInCompactView(0, 1, 2)
                    .setMediaSession(session.sessionCompatToken)
            )
            .setDeleteIntent(mediaButtonPending)

        lastArtBitmap?.let { builder.setLargeIcon(it) }

        return builder.build()
    }

    private fun updateNotification() {
        val notification = buildNotification()
        if (isForegroundService) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1, notification)
            } else {
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(1, notification)
            }
        } else {
            // When not in foreground mode, remove the notification entirely
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(1)
        }
    }

    private fun updateForegroundState() {
        val player = PlayerManager.getOrCreate(this)
        val shouldShow = player.isPlaying || player.playWhenReady || player.mediaItemCount > 0
        if (!appInForeground && shouldShow) {
            val notif = buildNotification()
            if (!isForegroundService) {
                startForeground(1, notif)
                isForegroundService = true
            } else {
                // Update existing foreground notification
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) startForeground(1, notif)
                else (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(1, notif)
            }
        } else {
            if (isForegroundService) {
                // Remove notification when app returns to foreground or playback not active
                if (Build.VERSION.SDK_INT >= 24) stopForeground(Service.STOP_FOREGROUND_REMOVE) else stopForeground(true)
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(1)
                isForegroundService = false
            }
        }
    }

    private fun maybeLoadArtwork(mediaItem: MediaItem) {
        val uri = mediaItem.localConfiguration?.uri ?: return
        if (uri == lastItemUri) return
        lastItemUri = uri
        artworkExecutor.execute {
            try {
                val key = uri.toString()
                val cached = artworkCache.get(key)
                val bmp = if (cached != null) cached else {
                    val mime = contentResolver.getType(uri)
                    loadArtworkBestEffort(uri, mime)?.also { artworkCache.put(key, it) }
                }
                if (bmp != null) {
                    lastArtBitmap = bmp
                    // Push artwork into current MediaItem's metadata so MediaSession surfaces can use it
                    try {
                        val player = PlayerManager.getOrCreate(this@PlaybackService)
                        val index = player.currentMediaItemIndex
                        val current = player.currentMediaItem
                        if (current != null && index != C.INDEX_UNSET) {
                            val baos = ByteArrayOutputStream()
                            bmp.compress(Bitmap.CompressFormat.PNG, 100, baos)
                            val bytes = baos.toByteArray()
                            val newMeta = current.mediaMetadata.buildUpon()
                                .setArtworkData(bytes, null)
                                .build()
                            val newItem = current.buildUpon().setMediaMetadata(newMeta).build()
                            player.replaceMediaItem(index, newItem)
                        }
                    } catch (_: Exception) { }
                    debounceUpdateNotification()
                }
            } catch (_: Exception) {
                // Ignore artwork failures
            }
        }
    }

    private fun loadArtworkBestEffort(uri: Uri, mime: String?): Bitmap? {
        // Prefer system-generated thumbnail when available (Q+)
        if (VERSION.SDK_INT >= VERSION_CODES.Q) {
            try {
                contentResolver.loadThumbnail(uri, Size(256, 256), null)?.let { return it }
            } catch (_: Exception) { /* ignore */ }
        }
        // Fall back based on mime hint
        return when {
            mime?.startsWith("audio/") == true -> loadAudioArtwork(uri)
            else -> loadVideoThumbnail(uri)
        }
    }

    private fun loadVideoThumbnail(uri: Uri): Bitmap? {
        return try {
            if (VERSION.SDK_INT >= VERSION_CODES.Q) {
                contentResolver.loadThumbnail(uri, Size(256, 256), null)
            } else {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(this, uri)
                val bmp = retriever.getFrameAtTime(0)
                retriever.release()
                bmp
            }
        } catch (_: Exception) { null }
    }

    private fun loadAudioArtwork(uri: Uri): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(this, uri)
            val art = retriever.embeddedPicture
            var bmp = if (art != null) BitmapFactory.decodeByteArray(art, 0, art.size) else null
            retriever.release()
            if (bmp == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Try album art via thumbnails on Q+
                try { contentResolver.loadThumbnail(uri, Size(256, 256), null) } catch (_: Exception) { null }
            } else bmp
        } catch (_: Exception) { null }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(CHANNEL_ID, "Playback", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "vplay_playback"
        const val ACTION_PLAY_PAUSE = "com.bytecoder.vplay.action.PLAY_PAUSE"
        const val ACTION_NEXT = "com.bytecoder.vplay.action.NEXT"
        const val ACTION_PREV = "com.bytecoder.vplay.action.PREV"
        const val ACTION_APP_FOREGROUND = "com.bytecoder.vplay.action.APP_FOREGROUND"
        const val ACTION_APP_BACKGROUND = "com.bytecoder.vplay.action.APP_BACKGROUND"
    }

    // region Audio Focus
    private fun ensureAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (audioFocusRequest == null) {
                    val afr = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setOnAudioFocusChangeListener { change -> handleFocusChange(change) }
                        .setAudioAttributes(
                            android.media.AudioAttributes.Builder()
                                .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        .setWillPauseWhenDucked(true)
                        .build()
                    audioFocusRequest = afr
                }
                audioFocusRequest?.let { audioManager.requestAudioFocus(it) }
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(
                    { change -> handleFocusChange(change) },
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                )
            }
        } catch (_: Exception) { }
    }

    private fun abandonAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(null)
            }
        } catch (_: Exception) { }
    }

    private fun handleFocusChange(change: Int) {
        val player = try { PlayerManager.getOrCreate(this) } catch (_: Exception) { null } ?: return
        when (change) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (ducked) {
                    player.volume = normalVolume
                    ducked = false
                }
                if (resumeOnFocusGain) {
                    player.playWhenReady = true
                    resumeOnFocusGain = false
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (!ducked) {
                    normalVolume = player.volume
                    player.volume = 0.2f
                    ducked = true
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                resumeOnFocusGain = player.playWhenReady || player.isPlaying
                player.playWhenReady = false
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                resumeOnFocusGain = false
                player.playWhenReady = false
            }
        }
    }
    // endregion
}
