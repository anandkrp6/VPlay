package com.bytecoder.vplay.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytecoder.vplay.R
import com.bytecoder.vplay.media.MediaItemModel
import com.bytecoder.vplay.media.PlaybackQueueViewModel

class QueueFragment : Fragment() {

    private val queueViewModel: PlaybackQueueViewModel by activityViewModels()
    private lateinit var adapter: QueueAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_queue, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recycler = view.findViewById<RecyclerView>(R.id.queue_list)
        adapter = QueueAdapter(mutableListOf())
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

        if (queueViewModel.queue.value.isNullOrEmpty()) {
            queueViewModel.setQueue(
                listOf(
                    MediaItemModel("1", "Sample Video 1", uri = "sample://video1", isVideo = true),
                    MediaItemModel("2", "Sample Video 2", uri = "sample://video2", isVideo = true),
                    MediaItemModel("3", "Sample Song 1", uri = "sample://audio1", isVideo = false)
                )
            )
        }
    }
}

class QueueAdapter(private val items: MutableList<MediaItemModel>) : RecyclerView.Adapter<QueueViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_queue, parent, false)
        return QueueViewHolder(v)
    }

    override fun getItemCount(): Int = items.size
    override fun onBindViewHolder(holder: QueueViewHolder, position: Int) = holder.bind(items[position])

    fun setItems(newItems: List<MediaItemModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun swap(from: Int, to: Int) {
        if (from !in items.indices || to !in items.indices) return
        val item = items.removeAt(from)
        items.add(to, item)
        notifyItemMoved(from, to)
    }
}

class QueueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val title: android.widget.TextView = itemView.findViewById(R.id.queue_item_title)
    private val subtitle: android.widget.TextView = itemView.findViewById(R.id.queue_item_sub)
    fun bind(item: MediaItemModel) {
        title.text = item.title
        subtitle.text = if (item.isVideo) "Video" else "Audio"
    }
}
