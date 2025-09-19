package com.bytecoder.vplay.backend.managers

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.bytecoder.vplay.backend.data.models.MediaFile
import com.bytecoder.vplay.backend.managers.MusicLibraryManager
import com.bytecoder.vplay.backend.managers.VideoLibraryManager
import java.io.File

/**
 * FolderScreenManager handles folder-based navigation and file system browsing
 * for media files. Provides folder structure exploration and media discovery.
 */
class FolderScreenManager(application: Application) : AndroidViewModel(application) {
    
    private val context: Context = application.applicationContext
    private val musicLibraryManager = MusicLibraryManager(context)
    private val videoLibraryManager = VideoLibraryManager(context)
    
    // Folder navigation state
    private val _currentPath = MutableStateFlow<String>("/storage/emulated/0")
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()
    
    private val _folderContents = MutableStateFlow<List<FolderItem>>(emptyList())
    val folderContents: StateFlow<List<FolderItem>> = _folderContents.asStateFlow()
    
    private val _navigationHistory = MutableStateFlow<List<String>>(emptyList())
    val navigationHistory: StateFlow<List<String>> = _navigationHistory.asStateFlow()
    
    // Media files in current folder
    private val _mediaFiles = MutableStateFlow<List<MediaFile>>(emptyList())
    val mediaFiles: StateFlow<List<MediaFile>> = _mediaFiles.asStateFlow()
    
    private val _audioFiles = MutableStateFlow<List<MediaFile>>(emptyList())
    val audioFiles: StateFlow<List<MediaFile>> = _audioFiles.asStateFlow()
    
    private val _videoFiles = MutableStateFlow<List<MediaFile>>(emptyList())
    val videoFiles: StateFlow<List<MediaFile>> = _videoFiles.asStateFlow()
    
    // UI state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _viewMode = MutableStateFlow(FolderViewMode.LIST)
    val viewMode: StateFlow<FolderViewMode> = _viewMode.asStateFlow()
    
    private val _sortBy = MutableStateFlow(FolderSortBy.NAME_ASC)
    val sortBy: StateFlow<FolderSortBy> = _sortBy.asStateFlow()
    
    private val _showHiddenFiles = MutableStateFlow(false)
    val showHiddenFiles: StateFlow<Boolean> = _showHiddenFiles.asStateFlow()
    
    private val _filterType = MutableStateFlow(MediaFilterType.ALL)
    val filterType: StateFlow<MediaFilterType> = _filterType.asStateFlow()

    enum class FolderViewMode {
        LIST, GRID
    }

    enum class FolderSortBy {
        NAME_ASC, NAME_DESC, SIZE_ASC, SIZE_DESC, DATE_ASC, DATE_DESC, TYPE_ASC, TYPE_DESC
    }

    enum class MediaFilterType {
        ALL, AUDIO_ONLY, VIDEO_ONLY, FOLDERS_ONLY
    }

    data class FolderItem(
        val name: String,
        val path: String,
        val isDirectory: Boolean,
        val size: Long,
        val lastModified: Long,
        val mediaFile: MediaFile? = null
    )
    
    init {
        loadFolderContents(_currentPath.value)
    }

    /**
     * Navigate to a specific folder
     */
    fun navigateToFolder(folderPath: String) {
        viewModelScope.launch {
            val currentPath = _currentPath.value
            
            // Add current path to navigation history
            val history = _navigationHistory.value.toMutableList()
            if (history.isEmpty() || history.last() != currentPath) {
                history.add(currentPath)
                // Limit history size
                if (history.size > 20) {
                    history.removeAt(0)
                }
                _navigationHistory.value = history
            }
            
            _currentPath.value = folderPath
            loadFolderContents(folderPath)
        }
    }

    /**
     * Navigate back to parent directory
     */
    fun navigateUp() {
        val currentPath = _currentPath.value
        val parentPath = File(currentPath).parent
        
        if (parentPath != null && parentPath != currentPath) {
            navigateToFolder(parentPath)
        }
    }

    /**
     * Navigate back in history
     */
    fun navigateBack(): Boolean {
        val history = _navigationHistory.value
        
        return if (history.isNotEmpty()) {
            val previousPath = history.last()
            val newHistory = history.dropLast(1)
            _navigationHistory.value = newHistory
            
            _currentPath.value = previousPath
            loadFolderContents(previousPath)
            true
        } else {
            false
        }
    }

    /**
     * Load contents of specified folder
     */
    private fun loadFolderContents(folderPath: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val folder = File(folderPath)
                
                if (!folder.exists() || !folder.isDirectory) {
                    _error.value = "Folder does not exist or is not accessible"
                    return@launch
                }
                
                val contents = folder.listFiles()?.toList() ?: emptyList()
                
                // Filter hidden files if needed
                val filteredContents = if (_showHiddenFiles.value) {
                    contents
                } else {
                    contents.filter { !it.name.startsWith(".") }
                }
                
                // Convert to FolderItem and get media info
                val folderItems = filteredContents.map { file ->
                    val mediaFile = if (!file.isDirectory && isMediaFile(file)) {
                        getMediaFileInfo(file)
                    } else null
                    
                    FolderItem(
                        name = file.name,
                        path = file.absolutePath,
                        isDirectory = file.isDirectory,
                        size = if (file.isDirectory) 0L else file.length(),
                        lastModified = file.lastModified(),
                        mediaFile = mediaFile
                    )
                }
                
                // Apply filtering
                val filtered = applyFilter(folderItems)
                
                // Apply sorting
                val sorted = applySorting(filtered)
                
                _folderContents.value = sorted
                
                // Separate media files
                val mediaFiles = sorted.mapNotNull { it.mediaFile }
                _mediaFiles.value = mediaFiles
                _audioFiles.value = mediaFiles.filter { it.type.startsWith("audio") }
                _videoFiles.value = mediaFiles.filter { it.type.startsWith("video") }
                
            } catch (e: SecurityException) {
                _error.value = "Permission denied accessing folder"
            } catch (e: Exception) {
                _error.value = "Error loading folder: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Check if file is a media file
     */
    private fun isMediaFile(file: File): Boolean {
        val extension = file.extension.lowercase()
        return isAudioFile(extension) || isVideoFile(extension)
    }

    /**
     * Check if file is an audio file
     */
    private fun isAudioFile(extension: String): Boolean {
        return extension in listOf("mp3", "wav", "flac", "aac", "ogg", "m4a", "wma", "opus")
    }

    /**
     * Check if file is a video file
     */
    private fun isVideoFile(extension: String): Boolean {
        return extension in listOf("mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "m4v", "3gp")
    }

    /**
     * Get media file information
     */
    private fun getMediaFileInfo(file: File): MediaFile? {
        return try {
            val extension = file.extension.lowercase()
            val type = if (isAudioFile(extension)) "audio" else "video"
            
            MediaFile(
                id = file.absolutePath,
                title = file.nameWithoutExtension,
                artist = "Unknown Artist",
                album = "Unknown Album",
                duration = 0L, // Would need MediaMetadataRetriever to get actual duration
                filePath = file.absolutePath,
                type = type,
                size = file.length(),
                dateAdded = file.lastModified()
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Apply current filter to folder items
     */
    private fun applyFilter(items: List<FolderItem>): List<FolderItem> {
        return when (_filterType.value) {
            MediaFilterType.ALL -> items
            MediaFilterType.AUDIO_ONLY -> items.filter { 
                it.isDirectory || (it.mediaFile?.type?.startsWith("audio") == true) 
            }
            MediaFilterType.VIDEO_ONLY -> items.filter { 
                it.isDirectory || (it.mediaFile?.type?.startsWith("video") == true) 
            }
            MediaFilterType.FOLDERS_ONLY -> items.filter { it.isDirectory }
        }
    }

    /**
     * Apply current sorting to folder items
     */
    private fun applySorting(items: List<FolderItem>): List<FolderItem> {
        // Always show directories first, then files
        val directories = items.filter { it.isDirectory }
        val files = items.filter { !it.isDirectory }
        
        val sortedDirectories = when (_sortBy.value) {
            FolderSortBy.NAME_ASC -> directories.sortedBy { it.name }
            FolderSortBy.NAME_DESC -> directories.sortedByDescending { it.name }
            FolderSortBy.SIZE_ASC -> directories.sortedBy { it.size }
            FolderSortBy.SIZE_DESC -> directories.sortedByDescending { it.size }
            FolderSortBy.DATE_ASC -> directories.sortedBy { it.lastModified }
            FolderSortBy.DATE_DESC -> directories.sortedByDescending { it.lastModified }
            FolderSortBy.TYPE_ASC -> directories.sortedBy { it.name }
            FolderSortBy.TYPE_DESC -> directories.sortedByDescending { it.name }
        }
        
        val sortedFiles = when (_sortBy.value) {
            FolderSortBy.NAME_ASC -> files.sortedBy { it.name }
            FolderSortBy.NAME_DESC -> files.sortedByDescending { it.name }
            FolderSortBy.SIZE_ASC -> files.sortedBy { it.size }
            FolderSortBy.SIZE_DESC -> files.sortedByDescending { it.size }
            FolderSortBy.DATE_ASC -> files.sortedBy { it.lastModified }
            FolderSortBy.DATE_DESC -> files.sortedByDescending { it.lastModified }
            FolderSortBy.TYPE_ASC -> files.sortedBy { it.mediaFile?.type ?: "" }
            FolderSortBy.TYPE_DESC -> files.sortedByDescending { it.mediaFile?.type ?: "" }
        }
        
        return sortedDirectories + sortedFiles
    }

    /**
     * Set view mode
     */
    fun setViewMode(mode: FolderViewMode) {
        _viewMode.value = mode
    }

    /**
     * Set sort option
     */
    fun setSortBy(sortBy: FolderSortBy) {
        _sortBy.value = sortBy
        loadFolderContents(_currentPath.value)
    }

    /**
     * Toggle hidden files visibility
     */
    fun toggleHiddenFiles() {
        _showHiddenFiles.value = !_showHiddenFiles.value
        loadFolderContents(_currentPath.value)
    }

    /**
     * Set media filter type
     */
    fun setFilterType(filterType: MediaFilterType) {
        _filterType.value = filterType
        loadFolderContents(_currentPath.value)
    }

    /**
     * Refresh current folder
     */
    fun refresh() {
        loadFolderContents(_currentPath.value)
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Get current folder name
     */
    fun getCurrentFolderName(): String {
        return File(_currentPath.value).name.ifEmpty { "Root" }
    }

    /**
     * Check if can navigate up
     */
    fun canNavigateUp(): Boolean {
        val currentPath = _currentPath.value
        val parentPath = File(currentPath).parent
        return parentPath != null && parentPath != currentPath
    }

    /**
     * Check if can navigate back in history
     */
    fun canNavigateBack(): Boolean {
        return _navigationHistory.value.isNotEmpty()
    }
}



