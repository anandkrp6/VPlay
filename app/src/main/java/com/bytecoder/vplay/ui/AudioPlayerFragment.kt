package com.bytecoder.vplay.ui

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bytecoder.vplay.R
import com.bytecoder.vplay.media.PlayerManager
import coil.load

class AudioPlayerFragment : Fragment() {
    private lateinit var art: ImageView
    private lateinit var bg: ImageView
    private lateinit var title: TextView
    private lateinit var subtitle: TextView
    private lateinit var btnPlay: ImageButton
    private lateinit var btnPrev: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var seek: SeekBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_audio_player, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        art = view.findViewById(R.id.art)
        bg = view.findViewById(R.id.bg)
        title = view.findViewById(R.id.title)
        subtitle = view.findViewById(R.id.subtitle)
        btnPlay = view.findViewById(R.id.btn_play)
        btnPrev = view.findViewById(R.id.btn_prev)
        btnNext = view.findViewById(R.id.btn_next)
        seek = view.findViewById(R.id.seek)

        val player = PlayerManager.getOrCreate(requireContext())
        updateMeta(player.currentMediaItem?.mediaMetadata?.title?.toString(), player.currentMediaItem?.mediaMetadata?.artist?.toString())
        val uri = player.currentMediaItem?.localConfiguration?.uri
        if (uri != null) {
            art.load(uri) { crossfade(true); placeholder(R.drawable.ic_music_note) }
            bg.load(uri) { crossfade(true) }
            if (Build.VERSION.SDK_INT >= 31) {
                bg.setRenderEffect(RenderEffect.createBlurEffect(24f, 24f, Shader.TileMode.CLAMP))
            } else {
                bg.alpha = 0.6f
            }
        }
        btnPlay.setOnClickListener { PlayerManager.playPause() }
        btnPrev.setOnClickListener { PlayerManager.previous() }
        btnNext.setOnClickListener { PlayerManager.next() }

        PlayerManager.isPlaying.observe(viewLifecycleOwner, Observer { playing ->
            btnPlay.setImageResource(if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)
        })
        PlayerManager.duration.observe(viewLifecycleOwner, Observer { d ->
            seek.max = (d / 1000).toInt()
        })
        PlayerManager.position.observe(viewLifecycleOwner, Observer { p ->
            val sec = (p / 1000).toInt()
            if (kotlin.math.abs(seek.progress - sec) > 1) seek.progress = sec
        })
        seek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) player.seekTo(progress * 1000L)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updateMeta(t: String?, s: String?) {
        title.text = t ?: ""
        subtitle.text = s ?: ""
    }
}
