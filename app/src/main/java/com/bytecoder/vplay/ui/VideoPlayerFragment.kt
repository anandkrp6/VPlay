package com.bytecoder.vplay.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bytecoder.vplay.R
import com.bytecoder.vplay.media.MediaItemModel
import com.bytecoder.vplay.media.PlaybackQueueViewModel
import com.bytecoder.vplay.media.PlayerManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout
import android.app.PictureInPictureParams
import android.os.Build
import android.util.Rational
import android.media.AudioManager
import android.provider.Settings
import android.view.WindowManager
import android.widget.TextView
import com.bytecoder.vplay.settings.AppSettings
import androidx.media3.common.util.UnstableApi

@UnstableApi
class VideoPlayerFragment : Fragment() {

    private val queueViewModel: PlaybackQueueViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_video_player, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnQueue = view.findViewById<ImageButton>(R.id.btn_open_queue)
        val playerView = view.findViewById<PlayerView>(R.id.player_view)
        val gestureOverlay = view.findViewById<View>(R.id.gesture_overlay)
        val btnPip = view.findViewById<ImageButton>(R.id.btn_pip)
        val btnAspect = view.findViewById<ImageButton>(R.id.btn_aspect)
        val btnOrient = view.findViewById<ImageButton>(R.id.btn_orient_lock)
        val hud = view.findViewById<TextView>(R.id.hud_text)

        if (queueViewModel.queue.value.isNullOrEmpty()) {
            queueViewModel.setQueue(
                listOf(
                    MediaItemModel("1", "Video A", uri = "sample://videoA", isVideo = true),
                    MediaItemModel("2", "Video B", uri = "sample://videoB", isVideo = true),
                    MediaItemModel("3", "Video C", uri = "sample://videoC", isVideo = true)
                )
            )
        }
        val player = PlayerManager.getOrCreate(requireContext())
        playerView.player = player
        PlayerManager.setQueue(requireContext(), queueViewModel.queue.value.orEmpty(),
            queueViewModel.currentIndex.value ?: 0)

        btnQueue.setOnClickListener { showQueueSheet() }

        // Gesture detectors
        val tapDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean { return true }
            override fun onDoubleTap(e: MotionEvent): Boolean {
                PlayerManager.playPause(); return true
            }
        })
        var downX = 0f
        var downY = 0f
        val seekFactor = AppSettings.getSeekSensitivity(requireContext()) // 1.0 default
        val volFactor = AppSettings.getVolumeSensitivity(requireContext())
        val brightFactor = AppSettings.getBrightnessSensitivity(requireContext())
        val audio = requireContext().getSystemService(AudioManager::class.java)
        val maxVol = audio?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
        val scaleDetector = ScaleGestureDetector(requireContext(), object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                // Toggle between FIT and ZOOM based on scale direction
                playerView.resizeMode = if (detector.scaleFactor > 1f) AspectRatioFrameLayout.RESIZE_MODE_ZOOM else AspectRatioFrameLayout.RESIZE_MODE_FIT
                return true
            }
        })
        gestureOverlay.setOnTouchListener { _, ev ->
            tapDetector.onTouchEvent(ev)
            scaleDetector.onTouchEvent(ev)
            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> { downX = ev.x; downY = ev.y }
                MotionEvent.ACTION_MOVE -> {
                    val dx = ev.x - downX
                    val dy = ev.y - downY
                    val width = gestureOverlay.width
                    val height = gestureOverlay.height
                    if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                        // Seek: horizontal swipe; base ~10 sec per quarter width scaled by sensitivity
                        val secs = (dx / (width / 4f) * 10_000 * seekFactor).toLong()
                        val pos = player.currentPosition + secs
                        player.seekTo(pos.coerceAtLeast(0))
                        hud.visibility = View.VISIBLE
                        hud.text = if (secs >= 0) "+${secs/1000}s" else "${secs/1000}s"
                    } else {
                        // Vertical swipe: left = brightness, right = volume
                        val isRight = ev.x > width/2f
                        if (isRight) {
                            val current = audio?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
                            val delta = (-dy / height * maxVol * volFactor).toInt()
                            val newVol = (current + delta).coerceIn(0, maxVol)
                            audio?.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                            hud.visibility = View.VISIBLE
                            hud.text = "Vol ${((newVol.toFloat()/maxVol)*100).toInt()}%"
                        } else {
                            val lp = requireActivity().window.attributes
                            var b = lp.screenBrightness
                            if (b < 0f) b = 0.5f
                            b = (b - (dy / height) * brightFactor).coerceIn(0.05f, 1f)
                            lp.screenBrightness = b
                            requireActivity().window.attributes = lp
                            hud.visibility = View.VISIBLE
                            hud.text = "Bright ${ (b*100).toInt()}%"
                        }
                    }
                    downX = ev.x; downY = ev.y
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    hud.visibility = View.GONE
                }
            }
            true
        }

        btnAspect.setOnClickListener {
            val mode = playerView.resizeMode
            val next = when (mode) {
                AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                AspectRatioFrameLayout.RESIZE_MODE_FILL -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
            playerView.resizeMode = next
        }
        var locked = false
        btnOrient.setOnClickListener {
            locked = !locked
            requireActivity().requestedOrientation = if (locked) android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LOCKED else android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        btnPip.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16,9))
                    .build()
                requireActivity().enterPictureInPictureMode(params)
            }
        }
    }

    private fun showQueueSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val v = layoutInflater.inflate(R.layout.bottom_sheet_queue, null)
        dialog.setContentView(v)

        val recycler = v.findViewById<RecyclerView>(R.id.queue_list)
        val adapter = QueueAdapter(mutableListOf())
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.bindingAdapterPosition
                val to = target.bindingAdapterPosition
                adapter.swap(from, to)
                queueViewModel.moveItem(from, to)
                com.bytecoder.vplay.media.PlayerManager.moveItemInPlayer(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
        touchHelper.attachToRecyclerView(recycler)

        queueViewModel.queue.observe(viewLifecycleOwner) { list ->
            adapter.setItems(list)
        }

        dialog.show()
    }
}
