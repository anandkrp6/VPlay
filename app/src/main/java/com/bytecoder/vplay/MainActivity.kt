package com.bytecoder.vplay

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bytecoder.vplay.media.PlaybackQueueViewModel
import com.bytecoder.vplay.media.PlayerManager
import com.bytecoder.vplay.ui.MusicFragment
import com.bytecoder.vplay.ui.OnlineFragment
import com.bytecoder.vplay.ui.OptionsFragment
import com.bytecoder.vplay.ui.PlaylistsFragment
import com.bytecoder.vplay.ui.QueueFragment
import com.bytecoder.vplay.ui.VideosFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.app.RemoteAction
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.lifecycle.Observer
import android.Manifest
import android.content.pm.PackageManager
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi

class MainActivity : AppCompatActivity() {

    private lateinit var queueViewModel: PlaybackQueueViewModel
    private var pipPlayObserver: Observer<Boolean>? = null
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
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        queueViewModel = ViewModelProvider(this)[PlaybackQueueViewModel::class.java]

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_videos
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_container, VideosFragment())
                .commit()
        }
        bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_videos -> VideosFragment()
                R.id.nav_music -> MusicFragment()
                R.id.nav_playlists -> PlaylistsFragment()
                R.id.nav_online -> OnlineFragment()
                R.id.nav_options -> OptionsFragment()
                else -> null
            }
            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_container, it)
                    .commit()
                true
            } ?: false
        }

        initMiniPlayer()
    }

    @OptIn(UnstableApi::class)
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Auto-enter PiP if playback is active
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && PlayerManager.hasActivePlayback()) {
            // On Android 13+, prompt for notifications so media controls can appear consistently
            requestNotificationPermissionIfNeeded(trigger="leave")
            // Inform service app is going background to show notification controls
            startService(Intent(this, com.bytecoder.vplay.media.PlaybackService::class.java).setAction(com.bytecoder.vplay.media.PlaybackService.ACTION_APP_BACKGROUND))
            val params = android.app.PictureInPictureParams.Builder()
                .setAspectRatio(android.util.Rational(16,9))
                .apply {
                    if (android.os.Build.VERSION.SDK_INT >= 26) {
                        val playPauseIntent = PendingIntent.getService(
                            this@MainActivity, 201,
                            Intent(this@MainActivity, com.bytecoder.vplay.media.PlaybackService::class.java).setAction(com.bytecoder.vplay.media.PlaybackService.ACTION_PLAY_PAUSE),
                            PendingIntent.FLAG_UPDATE_CURRENT or (if (android.os.Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
                        )
                        val nextIntent = PendingIntent.getService(
                            this@MainActivity, 202,
                            Intent(this@MainActivity, com.bytecoder.vplay.media.PlaybackService::class.java).setAction(com.bytecoder.vplay.media.PlaybackService.ACTION_NEXT),
                            PendingIntent.FLAG_UPDATE_CURRENT or (if (android.os.Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
                        )
                        val prevIntent = PendingIntent.getService(
                            this@MainActivity, 203,
                            Intent(this@MainActivity, com.bytecoder.vplay.media.PlaybackService::class.java).setAction(com.bytecoder.vplay.media.PlaybackService.ACTION_PREV),
                            PendingIntent.FLAG_UPDATE_CURRENT or (if (android.os.Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
                        )
                        val playPause = RemoteAction(Icon.createWithResource(this@MainActivity, android.R.drawable.ic_media_play), "Play/Pause", "Play/Pause", playPauseIntent)
                        val next = RemoteAction(Icon.createWithResource(this@MainActivity, android.R.drawable.ic_media_next), "Next", "Next", nextIntent)
                        val prev = RemoteAction(Icon.createWithResource(this@MainActivity, android.R.drawable.ic_media_previous), "Prev", "Prev", prevIntent)
                        if (android.os.Build.VERSION.SDK_INT >= 26) setActions(listOf(prev, playPause, next))
                    }
                }
                .build()
            enterPictureInPictureMode(params)
        }
    }

    @OptIn(UnstableApi::class)
    override fun onResume() {
        super.onResume()
        // Inform service app is foreground; hide persistent notification
        startService(Intent(this, com.bytecoder.vplay.media.PlaybackService::class.java).setAction(com.bytecoder.vplay.media.PlaybackService.ACTION_APP_FOREGROUND))
    }

    private fun initMiniPlayer() {
    val playPause = findViewById<ImageButton>(R.id.mini_play_pause)
        val next = findViewById<ImageButton>(R.id.mini_next)
        val prev = findViewById<ImageButton>(R.id.mini_prev)
    val title = findViewById<TextView>(R.id.mini_title)
    val sub = findViewById<TextView>(R.id.mini_sub)
    val progress = findViewById<ProgressBar>(R.id.mini_progress)

        // Placeholder observers; will integrate with real player later.
        queueViewModel.currentIndex.observe(this) { idx ->
            val queue = queueViewModel.queue.value.orEmpty()
            if (idx in queue.indices) {
                val item = queue[idx]
                title.text = item.title
                sub.text = item.subtitle ?: ""
            }
        }
        playPause.setOnClickListener {
            // If user explicitly taps play/pause, we can also opportunistically request notifications (API 33+)
            requestNotificationPermissionIfNeeded(trigger="play")
            PlayerManager.playPause()
        }
        next.setOnClickListener { PlayerManager.next() }
        prev.setOnClickListener { PlayerManager.previous() }

        PlayerManager.isPlaying.observe(this) { isPlaying ->
            playPause.setImageResource(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            )
        }
        PlayerManager.position.observe(this) { pos ->
            val dur = PlayerManager.duration.value ?: 0
            if (dur > 0) {
                progress.max = 1000
                progress.progress = ((pos.toDouble() / dur) * 1000).toInt()
            } else {
                progress.progress = 0
            }
        }

        // Open queue management when tapping title area
        val card = findViewById<android.view.View>(R.id.mini_player_bar)
        card.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_container, QueueFragment())
                .addToBackStack("queue")
                .commit()
        }
    }

    private fun requestNotificationPermissionIfNeeded(trigger: String) {
        if (android.os.Build.VERSION.SDK_INT < 33) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) return
        val prefs = getSharedPreferences("vplay_prefs", MODE_PRIVATE)
        val asked = prefs.getBoolean("asked_notif_perm", false)
        // Only auto-prompt on user leave once; allow prompt on explicit play tap regardless of asked flag
        if (trigger == "leave" && asked) return
        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        val mini = findViewById<android.view.View>(R.id.mini_player_bar)
        val bottom = findViewById<BottomNavigationView>(R.id.bottom_nav)
        if (isInPictureInPictureMode) {
            mini.visibility = android.view.View.GONE
            bottom.visibility = android.view.View.GONE
            // While in PiP, reflect current play/pause action icon when state changes
            if (pipPlayObserver == null) {
                pipPlayObserver = Observer<Boolean> {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && isInPictureInPictureMode) {
                        setPictureInPictureParams(buildPipParams())
                    }
                }
                PlayerManager.isPlaying.observe(this, pipPlayObserver!!)
            }
        } else {
            mini.visibility = android.view.View.VISIBLE
            bottom.visibility = android.view.View.VISIBLE
            pipPlayObserver?.let { PlayerManager.isPlaying.removeObserver(it) }
            pipPlayObserver = null
        }
    }

    @OptIn(UnstableApi::class)
    private fun buildPipParams(): android.app.PictureInPictureParams {
        val playPauseIntent = PendingIntent.getService(
            this, 201,
            Intent(this, com.bytecoder.vplay.media.PlaybackService::class.java).setAction(com.bytecoder.vplay.media.PlaybackService.ACTION_PLAY_PAUSE),
            PendingIntent.FLAG_UPDATE_CURRENT or (if (android.os.Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        val nextIntent = PendingIntent.getService(
            this, 202,
            Intent(this, com.bytecoder.vplay.media.PlaybackService::class.java).setAction(com.bytecoder.vplay.media.PlaybackService.ACTION_NEXT),
            PendingIntent.FLAG_UPDATE_CURRENT or (if (android.os.Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        val prevIntent = PendingIntent.getService(
            this, 203,
            Intent(this, com.bytecoder.vplay.media.PlaybackService::class.java).setAction(com.bytecoder.vplay.media.PlaybackService.ACTION_PREV),
            PendingIntent.FLAG_UPDATE_CURRENT or (if (android.os.Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        val isPlaying = PlayerManager.isPlaying.value == true
        val playIconRes = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPause = RemoteAction(Icon.createWithResource(this, playIconRes), if (isPlaying) "Pause" else "Play", "Play/Pause", playPauseIntent)
        val next = RemoteAction(Icon.createWithResource(this, android.R.drawable.ic_media_next), "Next", "Next", nextIntent)
        val prev = RemoteAction(Icon.createWithResource(this, android.R.drawable.ic_media_previous), "Prev", "Prev", prevIntent)
        return android.app.PictureInPictureParams.Builder()
            .setAspectRatio(android.util.Rational(16,9))
            .setActions(listOf(prev, playPause, next))
            .build()
    }
}