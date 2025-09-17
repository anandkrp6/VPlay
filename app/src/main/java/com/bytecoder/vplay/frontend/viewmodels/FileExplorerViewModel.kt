package com.bytecoder.vplay.frontend.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class FileExplorerViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _currentPath = MutableStateFlow("")
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()
    
    private val _files = MutableStateFlow<List<FileItem>>(emptyList())
    val files: StateFlow<List<FileItem>> = _files.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _pathHistory = mutableListOf<String>()
    
    var searchQuery by mutableStateOf("")
        private set
    
    init {
        navigateToRoot()
    }
    
    fun navigateToRoot() {
        val rootPath = android.os.Environment.getExternalStorageDirectory().absolutePath
        navigateToPath(rootPath)
    }
    
    fun navigateToPath(path: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val directory = File(path)
                if (directory.exists() && directory.isDirectory) {
                    _pathHistory.add(_currentPath.value)
                    _currentPath.value = path
                    loadFiles(directory)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun navigateBack(): Boolean {
        return if (_pathHistory.isNotEmpty()) {
            val previousPath = _pathHistory.removeLastOrNull()
            if (previousPath != null) {
                viewModelScope.launch {
                    _currentPath.value = previousPath
                    loadFiles(File(previousPath))
                }
                true
            } else {
                false
            }
        } else {
            val currentFile = File(_currentPath.value)
            val parent = currentFile.parent
            if (parent != null) {
                navigateToPath(parent)
                true
            } else {
                false
            }
        }
    }
    
    private fun loadFiles(directory: File) {
        viewModelScope.launch {
            try {
                val fileItems = directory.listFiles()?.map { file ->
                    FileItem(
                        name = file.name,
                        path = file.absolutePath,
                        isDirectory = file.isDirectory,
                        size = if (file.isFile) file.length() else 0L,
                        lastModified = file.lastModified(),
                        isMediaFile = isMediaFile(file)
                    )
                }?.sortedWith(compareBy<FileItem> { !it.isDirectory }.thenBy { it.name }) ?: emptyList()
                
                _files.value = fileItems
            } catch (e: Exception) {
                e.printStackTrace()
                _files.value = emptyList()
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        searchQuery = query
    }
    
    fun sortFiles(sortType: String) {
        viewModelScope.launch {
            val currentFiles = _files.value
            val sortedFiles = when (sortType) {
                "name" -> currentFiles.sortedWith(compareBy<FileItem> { !it.isDirectory }.thenBy { it.name })
                "size" -> currentFiles.sortedWith(compareBy<FileItem> { !it.isDirectory }.thenByDescending { it.size })
                "date" -> currentFiles.sortedWith(compareBy<FileItem> { !it.isDirectory }.thenByDescending { it.lastModified })
                "type" -> currentFiles.sortedWith(compareBy<FileItem> { !it.isDirectory }.thenBy { getFileExtension(it.name) })
                else -> currentFiles
            }
            _files.value = sortedFiles
        }
    }
    
    private fun isMediaFile(file: File): Boolean {
        if (file.isDirectory) return false
        val extension = file.extension.lowercase()
        return extension in listOf(
            "mp3", "wav", "flac", "aac", "ogg", "m4a", "wma",
            "mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "3gp"
        )
    }
    
    private fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "")
    }
    
    fun refreshCurrentDirectory() {
        viewModelScope.launch {
            loadFiles(File(_currentPath.value))
        }
    }
}

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val isMediaFile: Boolean
)