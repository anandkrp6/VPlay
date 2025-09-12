package com.bytecoder.vplay.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytecoder.vplay.R
import com.bytecoder.vplay.media.MediaItemModel
import com.bytecoder.vplay.media.MediaStoreRepository
import com.bytecoder.vplay.media.PlayerManager
import coil.load

class VideosFragment : Fragment() {
    private lateinit var repo: MediaStoreRepository
    private lateinit var recycler: RecyclerView
    private var emptyView: TextView? = null
    private val items = mutableListOf<MediaStoreRepository.MediaEntry>()
    private val allItems = mutableListOf<MediaStoreRepository.MediaEntry>()
    private val adapter = MediaAdapter()
    private var sortMode: SortMode = SortMode.DATE_DESC
    private var currentBucket: String? = null

    private enum class SortMode { DATE_DESC, TITLE_ASC, DURATION_DESC }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grantMap ->
        val granted = grantMap.values.any { it }
        if (granted) loadMedia()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_videos, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = MediaStoreRepository(requireContext())
    recycler = view.findViewById(R.id.media_list)
    emptyView = view.findViewById(R.id.empty_view)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter
        recycler.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        view.findViewById<View>(R.id.btn_sort)?.setOnClickListener { v ->
            showSortMenu(v)
        }
        val spinner = view.findViewById<Spinner>(R.id.spinner_folder)
        val buckets = listOf("All") + repo.getVideoBuckets()
        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, buckets)
        spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, v: View?, position: Int, id: Long) {
                currentBucket = if (position == 0) null else buckets[position]
                applyFilterAndSort(view.findViewById(R.id.input_search))
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) { }
        }
        val search = view.findViewById<android.widget.EditText>(R.id.input_search)
        search?.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) { applyFilterAndSort(search) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        ensurePermissionAndLoad()
    }

    private fun ensurePermissionAndLoad() {
        val perms = if (Build.VERSION.SDK_INT >= 33) {
            arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        val has = perms.all { ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED }
        if (has) loadMedia() else permissionLauncher.launch(perms)
    }

    private fun loadMedia() {
        allItems.clear()
        allItems.addAll(repo.queryVideos())
        applyFilterAndSort(view?.findViewById(R.id.input_search))
    }

    private fun applyFilterAndSort(searchView: View?) {
        val q = (searchView as? android.widget.EditText)?.text?.toString()?.trim()?.lowercase().orEmpty()
        var list = allItems.asSequence()
        if (q.isNotEmpty()) list = list.filter { it.title.lowercase().contains(q) || (it.artistOrAlbum?.lowercase()?.contains(q) == true) }
        if (currentBucket != null) list = list.filter { it.bucketDisplayName == currentBucket }
        val sorted = when (sortMode) {
            SortMode.DATE_DESC -> list.sortedByDescending { it.dateAddedSec }
            SortMode.TITLE_ASC -> list.sortedBy { it.title.lowercase() }
            SortMode.DURATION_DESC -> list.sortedByDescending { it.durationMs }
        }.toList()
        items.clear(); items.addAll(sorted); adapter.notifyDataSetChanged()
        emptyView?.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showSortMenu(anchor: View) {
        val popup = android.widget.PopupMenu(requireContext(), anchor)
        popup.menu.add(0, 1, 0, getString(R.string.sort_date))
        popup.menu.add(0, 2, 1, getString(R.string.sort_title))
        popup.menu.add(0, 3, 2, getString(R.string.sort_duration))
        popup.setOnMenuItemClickListener { item ->
            sortMode = when (item.itemId) {
                1 -> SortMode.DATE_DESC
                2 -> SortMode.TITLE_ASC
                3 -> SortMode.DURATION_DESC
                else -> sortMode
            }
            applyFilterAndSort(view?.findViewById(R.id.input_search))
            true
        }
        popup.show()
    }

    private inner class MediaAdapter : RecyclerView.Adapter<MediaVH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaVH {
            val v = layoutInflater.inflate(R.layout.item_media_row, parent, false)
            return MediaVH(v)
        }
        override fun getItemCount() = items.size
        override fun onBindViewHolder(holder: MediaVH, position: Int) = holder.bind(items[position])
    }

    private inner class MediaVH(view: View) : RecyclerView.ViewHolder(view) {
        private val thumb: ImageView = view.findViewById(R.id.thumb)
        private val title: TextView = view.findViewById(R.id.title)
        private val subtitle: TextView = view.findViewById(R.id.subtitle)
    private val badge: TextView = view.findViewById(R.id.progress_badge)
        private val btnPlay: ImageButton = view.findViewById(R.id.btn_play_now)
        private val btnAdd: ImageButton = view.findViewById(R.id.btn_add_queue)

        @OptIn(UnstableApi::class)
        fun bind(entry: MediaStoreRepository.MediaEntry) {
            title.text = entry.title
            subtitle.text = entry.artistOrAlbum ?: formatDuration(entry.durationMs)
            val mediaId = "video:${entry.id}"
            val progress = com.bytecoder.vplay.media.PlayerManager.getPerItemProgress(requireContext(), mediaId)
            if (progress > 0 && entry.durationMs > 0) {
                val pct = (100 * progress / entry.durationMs).toInt().coerceIn(1, 99)
                badge.visibility = View.VISIBLE
                badge.text = "Continue • ${pct}%"
            } else badge.visibility = View.GONE
            // Load video frame thumbnail from content Uri (requires coil-video)
            thumb.load(entry.contentUri) {
                crossfade(true)
                placeholder(R.drawable.ic_launcher_foreground)
                error(R.drawable.ic_launcher_foreground)
            }
            btnPlay.setOnClickListener {
                val model = MediaItemModel(
                    id = "video:${entry.id}",
                    title = entry.title,
                    uri = entry.contentUri.toString(),
                    isVideo = true,
                    durationMs = entry.durationMs
                )
                PlayerManager.setQueue(requireContext(), listOf(model), 0)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_container, VideoPlayerFragment())
                    .addToBackStack("player")
                    .commit()
            }
            btnAdd.setOnClickListener {
                val model = MediaItemModel(
                    id = "video:${entry.id}",
                    title = entry.title,
                    uri = entry.contentUri.toString(),
                    isVideo = true,
                    durationMs = entry.durationMs
                )
                PlayerManager.addToQueue(requireContext(), model)
            }
        }
    }

    private fun formatDuration(ms: Long): String {
        if (ms <= 0) return ""
        val totalSec = ms / 1000
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return if (h > 0) String.format("%d:%02d:%02d", h, m, s) else String.format("%d:%02d", m, s)
    }
}
