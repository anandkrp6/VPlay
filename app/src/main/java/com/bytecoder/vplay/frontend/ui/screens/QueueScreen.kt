package com.bytecoder.vplay.frontend.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bytecoder.vplay.backend.data.models.MediaItemModel
import com.bytecoder.vplay.frontend.viewmodels.PlaybackQueueViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun QueueScreen(
    queueViewModel: PlaybackQueueViewModel,
    navController: NavController
) {
    val queue by queueViewModel.queue.observeAsState(emptyList())
    val currentIndex by queueViewModel.currentIndex.observeAsState(0)
    val isPlaying by queueViewModel.isPlaying.observeAsState(false)
    
    var showClearDialog by remember { mutableStateOf(false) }
    var showSavePlaylistDialog by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()
    
    // Auto-scroll to current playing item
    LaunchedEffect(currentIndex) {
        if (currentIndex in queue.indices) {
            listState.animateScrollToItem(currentIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "Queue",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (queue.isNotEmpty()) {
                            Text(
                                text = "${queue.size} songs",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Shuffle queue
                    IconButton(
                        onClick = { queueViewModel.setShuffleMode(true) }
                    ) {
                        Icon(
                            Icons.Default.Shuffle,
                            contentDescription = "Shuffle Queue"
                        )
                    }
                    
                    // Save as playlist
                    IconButton(
                        onClick = { showSavePlaylistDialog = true }
                    ) {
                        Icon(
                            Icons.Default.PlaylistAdd,
                            contentDescription = "Save as Playlist"
                        )
                    }
                    
                    // Clear queue
                    IconButton(
                        onClick = { showClearDialog = true }
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear Queue"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (queue.isEmpty()) {
            // Empty queue state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                EmptyQueueContent()
            }
        } else {
            // Queue content
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = queue,
                    key = { _, item -> item.id }
                ) { index, item ->
                    QueueItem(
                        item = item,
                        index = index,
                        isCurrentPlaying = index == currentIndex,
                        isPlaying = isPlaying && index == currentIndex,
                        onItemClick = { queueViewModel.seekToQueueItem(index) },
                        onDeleteClick = { queueViewModel.removeFromQueue(index) },
                        onMoveItem = { from, to -> queueViewModel.moveItem(from, to) },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
                
                // Add some bottom padding for the mini player
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
    
    // Clear queue confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Queue") },
            text = { Text("Are you sure you want to clear the entire queue?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        queueViewModel.clearQueue()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Save playlist dialog
    if (showSavePlaylistDialog) {
        var playlistName by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showSavePlaylistDialog = false },
            title = { Text("Save as Playlist") },
            text = {
                Column {
                    Text("Enter a name for your playlist:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        label = { Text("Playlist name") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (playlistName.isNotBlank()) {
                            queueViewModel.saveQueueAsPlaylist(playlistName)
                            showSavePlaylistDialog = false
                            playlistName = ""
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showSavePlaylistDialog = false
                    playlistName = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun QueueItem(
    item: MediaItemModel,
    index: Int,
    isCurrentPlaying: Boolean,
    isPlaying: Boolean,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMoveItem: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(0f) }
    var isDeleting by remember { mutableStateOf(false) }
    
    val density = LocalDensity.current
    val itemHeight = 80.dp
    val itemHeightPx = with(density) { itemHeight.toPx() }
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isCurrentPlaying -> MaterialTheme.colorScheme.primaryContainer
            isDragging -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(300),
        label = "background_color"
    )
    
    val elevation = if (isDragging) 8.dp else 2.dp

    Card(
        onClick = onItemClick,
        modifier = modifier
            .fillMaxWidth()
            .offset(y = with(density) { dragOffset.toDp() })
            .shadow(elevation)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { 
                        isDragging = false
                        dragOffset = 0f
                    }
                ) { _, dragAmount ->
                    dragOffset += dragAmount.y
                    
                    // Simple drag-to-reorder logic
                    val targetIndex = ((dragOffset + itemHeightPx / 2) / itemHeightPx).toInt()
                    val newIndex = (index + targetIndex).coerceIn(0, Int.MAX_VALUE)
                    
                    if (newIndex != index && abs(dragOffset) > itemHeightPx / 2) {
                        onMoveItem(index, newIndex)
                        dragOffset = 0f
                    }
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag handle
            Icon(
                Icons.Default.DragHandle,
                contentDescription = "Drag to reorder",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Album art or placeholder
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (item.thumbnailPath?.isNotEmpty() == true) {
                    AsyncImage(
                        model = item.thumbnailPath,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Track info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isCurrentPlaying) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isCurrentPlaying) 
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
                )
                
                item.subtitle?.let { artist ->
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCurrentPlaying)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            
            // Current playing indicator
            if (isCurrentPlaying) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Playing" else "Paused",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            // Delete button
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove from queue",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyQueueContent() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.AutoMirrored.Filled.PlaylistPlay,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Queue is Empty",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Add songs from your library to see them here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedButton(
                onClick = { /* TODO: Navigate to music library */ }
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Browse Music")
            }
        }
    }
}