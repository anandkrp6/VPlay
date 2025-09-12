package com.bytecoder.vplay.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.media3.common.util.UnstableApi
import com.bytecoder.vplay.R
import com.bytecoder.vplay.media.MediaItemModel
import com.bytecoder.vplay.media.PlaybackQueueViewModel
import com.bytecoder.vplay.media.PlayerManager
import com.bytecoder.vplay.settings.AppSettings

class OptionsFragment : Fragment() {
    private val queueViewModel: PlaybackQueueViewModel by activityViewModels()
    private var lastPicked: Uri? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            requireContext().contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            lastPicked = it
            view?.findViewById<TextView>(R.id.last_picked)?.text = it.toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_options, container, false)

    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.btn_pick_media).setOnClickListener {
            pickMedia.launch(arrayOf("video/*", "audio/*"))
        }
        view.findViewById<Button>(R.id.btn_play_picked).setOnClickListener {
            lastPicked?.let { u ->
                val item = MediaItemModel(u.toString(), u.lastPathSegment ?: "Media", uri = u.toString(), isVideo = true)
                queueViewModel.setQueue(listOf(item), 0)
                PlayerManager.setQueue(requireContext(), listOf(item), 0)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_container, VideoPlayerFragment())
                    .addToBackStack("player")
                    .commit()
            } ?: android.widget.Toast.makeText(requireContext(), "Pick a media file first", android.widget.Toast.LENGTH_SHORT).show()
        }
        view.findViewById<Button>(R.id.btn_add_queue_picked).setOnClickListener {
            lastPicked?.let { u ->
                val item = MediaItemModel(u.toString(), u.lastPathSegment ?: "Media", uri = u.toString(), isVideo = true)
                queueViewModel.addToQueue(item)
                PlayerManager.addToQueue(requireContext(), item)
            } ?: android.widget.Toast.makeText(requireContext(), "Pick a media file first", android.widget.Toast.LENGTH_SHORT).show()
        }

        // Gesture sensitivity sliders
        val seek = view.findViewById<android.widget.SeekBar>(R.id.seek_sens)
        val vol = view.findViewById<android.widget.SeekBar>(R.id.vol_sens)
        val bright = view.findViewById<android.widget.SeekBar>(R.id.bright_sens)
        fun initBar(bar: android.widget.SeekBar, factor: Float, onChange: (Float)->Unit) {
            bar.progress = (factor * 100).toInt()
            bar.setOnSeekBarChangeListener(object: android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) { if (fromUser) onChange(progress/100f) }
                override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
            })
        }
        initBar(seek, AppSettings.getSeekSensitivity(requireContext())) { AppSettings.setSeekSensitivity(requireContext(), it) }
        initBar(vol, AppSettings.getVolumeSensitivity(requireContext())) { AppSettings.setVolumeSensitivity(requireContext(), it) }
        initBar(bright, AppSettings.getBrightnessSensitivity(requireContext())) { AppSettings.setBrightnessSensitivity(requireContext(), it) }
    }
}
