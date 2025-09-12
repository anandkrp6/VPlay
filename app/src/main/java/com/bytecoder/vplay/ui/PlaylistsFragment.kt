package com.bytecoder.vplay.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytecoder.vplay.R
import com.bytecoder.vplay.playlists.Playlist
import com.bytecoder.vplay.playlists.PlaylistItem
import com.bytecoder.vplay.playlists.PlaylistRepository
import kotlinx.coroutines.launch

class PlaylistsFragment : Fragment() {
    private lateinit var repo: PlaylistRepository
    private lateinit var adapter: PlaylistsAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_playlists, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = PlaylistRepository(requireContext())
        val list = view.findViewById<RecyclerView>(R.id.playlists_list)
        adapter = PlaylistsAdapter(mutableListOf()) { playlist ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_container, PlaylistItemsFragment.newInstance(playlist.id, playlist.name))
                .addToBackStack("playlist_items")
                .commit()
        }
        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = adapter

        view.findViewById<Button>(R.id.btn_create_playlist).setOnClickListener {
            val name = view.findViewById<EditText>(R.id.input_playlist_name).text?.toString()?.trim().orEmpty()
            if (name.isNotEmpty()) {
                lifecycleScope.launch {
                    repo.createPlaylist(name, null, emptyList<PlaylistItem>())
                    load()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        viewLifecycleOwner.lifecycleScope.launch {
            val data = repo.listPlaylists()
            adapter.setItems(data)
        }
    }
}

class PlaylistsAdapter(
    private val items: MutableList<Playlist>,
    private val onClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistsVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistsVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_playlist, parent, false)
        return PlaylistsVH(v, onClick)
    }
    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: PlaylistsVH, position: Int) = holder.bind(items[position])
    fun setItems(newItems: List<Playlist>) {
        items.clear(); items.addAll(newItems); notifyDataSetChanged()
    }
}

class PlaylistsVH(itemView: View, private val onClick: (Playlist) -> Unit) : RecyclerView.ViewHolder(itemView) {
    private val title: android.widget.TextView = itemView.findViewById(R.id.playlist_title)
    init { itemView.setOnClickListener { val p = bindingAdapterPosition; if (p!=RecyclerView.NO_POSITION) onClick((itemView.tag as Playlist)) } }
    fun bind(item: Playlist) { itemView.tag = item; title.text = item.name }
}

class PlaylistItemsFragment : Fragment() {
    companion object {
        fun newInstance(id: Long, name: String): PlaylistItemsFragment {
            val f = PlaylistItemsFragment()
            val b = Bundle(); b.putLong("id", id); b.putString("name", name); f.arguments = b
            return f
        }
    }
    private lateinit var repo: PlaylistRepository
    private lateinit var adapter: PlaylistItemsAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_playlist_items, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = PlaylistRepository(requireContext())
        adapter = PlaylistItemsAdapter(mutableListOf())
        val list = view.findViewById<RecyclerView>(R.id.playlist_items_list)
        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = adapter
        view.findViewById<android.widget.TextView>(R.id.playlist_name).text = requireArguments().getString("name")
        load()
    }

    private fun load() {
        viewLifecycleOwner.lifecycleScope.launch {
            val id = requireArguments().getLong("id")
            val data = repo.getItems(id)
            adapter.setItems(data)
        }
    }
}

class PlaylistItemsAdapter(private val items: MutableList<PlaylistItem>) : RecyclerView.Adapter<PlaylistItemVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistItemVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_playlist_item, parent, false)
        return PlaylistItemVH(v)
    }
    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: PlaylistItemVH, position: Int) = holder.bind(items[position])
    fun setItems(newItems: List<PlaylistItem>) { items.clear(); items.addAll(newItems); notifyDataSetChanged() }
}

class PlaylistItemVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val title: android.widget.TextView = itemView.findViewById(R.id.playlist_item_title)
    fun bind(item: PlaylistItem) { title.text = item.title }
}
