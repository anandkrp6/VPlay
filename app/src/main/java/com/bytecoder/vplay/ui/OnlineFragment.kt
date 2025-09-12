package com.bytecoder.vplay.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bytecoder.vplay.R
import com.bytecoder.vplay.media.MediaItemModel
import com.bytecoder.vplay.media.PlaybackQueueViewModel
import com.bytecoder.vplay.media.PlayerManager

class OnlineFragment : Fragment() {
    private val queueViewModel: PlaybackQueueViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_online, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val input = view.findViewById<EditText>(R.id.input_url)
        view.findViewById<Button>(R.id.btn_play_now).setOnClickListener {
            val url = input.text?.toString()?.trim().orEmpty()
            if (url.isNotEmpty()) {
                val item = MediaItemModel(url, url, uri = url, isVideo = true)
                queueViewModel.setQueue(listOf(item), 0)
                PlayerManager.setQueue(requireContext(), listOf(item), 0)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_container, VideoPlayerFragment())
                    .addToBackStack("player")
                    .commit()
            } else android.widget.Toast.makeText(requireContext(), "Enter a valid URL", android.widget.Toast.LENGTH_SHORT).show()
        }
        view.findViewById<Button>(R.id.btn_add_queue).setOnClickListener {
            val url = input.text?.toString()?.trim().orEmpty()
            if (url.isNotEmpty()) {
                val item = MediaItemModel(url, url, uri = url, isVideo = true)
                queueViewModel.addToQueue(item)
                PlayerManager.addToQueue(requireContext(), item)
            } else android.widget.Toast.makeText(requireContext(), "Enter a valid URL", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
