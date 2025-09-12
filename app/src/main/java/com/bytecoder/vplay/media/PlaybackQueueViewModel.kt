package com.bytecoder.vplay.media

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlaybackQueueViewModel : ViewModel() {
    private val _queue = MutableLiveData<List<MediaItemModel>>(emptyList())
    val queue: LiveData<List<MediaItemModel>> = _queue

    private val _currentIndex = MutableLiveData<Int>(-1)
    val currentIndex: LiveData<Int> = _currentIndex

    fun setQueue(items: List<MediaItemModel>, startIndex: Int = 0) {
        _queue.value = items
        _currentIndex.value = startIndex.coerceIn(items.indices)
    }

    fun addToQueue(item: MediaItemModel) {
        val list = _queue.value?.toMutableList() ?: mutableListOf()
        list.add(item)
        _queue.value = list
        if ((_currentIndex.value ?: -1) == -1) _currentIndex.value = 0
    }

    fun addAllToQueue(items: List<MediaItemModel>) {
        val list = _queue.value?.toMutableList() ?: mutableListOf()
        list.addAll(items)
        _queue.value = list
        if ((_currentIndex.value ?: -1) == -1 && list.isNotEmpty()) _currentIndex.value = 0
    }

    fun setCurrentIndex(index: Int) {
        val list = _queue.value ?: return
        if (index in list.indices) _currentIndex.value = index
    }

    fun moveItem(from: Int, to: Int) {
        val list = _queue.value?.toMutableList() ?: return
        if (from !in list.indices || to !in list.indices) return
        val item = list.removeAt(from)
        list.add(to, item)
        _queue.value = list
        // Adjust current index if needed
        val ci = _currentIndex.value ?: return
        _currentIndex.value = when {
            from == ci -> to
            from < ci && to >= ci -> ci - 1
            from > ci && to <= ci -> ci + 1
            else -> ci
        }
    }

    fun skipNext() {
        val list = _queue.value ?: return
        val ci = _currentIndex.value ?: return
        if (ci + 1 < list.size) _currentIndex.value = ci + 1
    }

    fun skipPrevious() {
        val ci = _currentIndex.value ?: return
        if (ci - 1 >= 0) _currentIndex.value = ci - 1
    }
}
