package com.bytecoder.vplay.frontend.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    // Mock history data
    val historyItems = remember {
        listOf(
            HistoryItem(
                title = "Movie.mp4",
                type = MediaType.VIDEO,
                duration = "2:15:30",
                watchedAt = System.currentTimeMillis() - 3600000, // 1 hour ago
                thumbnail = null,
                progress = 0.75f
            ),
            HistoryItem(
                title = "Song.mp3",
                type = MediaType.AUDIO,
                duration = "3:45",
                watchedAt = System.currentTimeMillis() - 7200000, // 2 hours ago
                thumbnail = null,
                progress = 1.0f
            ),
            HistoryItem(
                title = "Documentary.mkv",
                type = MediaType.VIDEO,
                duration = "1:32:15",
                watchedAt = System.currentTimeMillis() - 86400000, // 1 day ago
                thumbnail = null,
                progress = 0.45f
            ),
            HistoryItem(
                title = "Podcast.mp3",
                type = MediaType.AUDIO,
                duration = "45:20",
                watchedAt = System.currentTimeMillis() - 172800000, // 2 days ago
                thumbnail = null,
                progress = 0.60f
            ),
            HistoryItem(
                title = "Series S01E01.mp4",
                type = MediaType.VIDEO,
                duration = "42:18",
                watchedAt = System.currentTimeMillis() - 259200000, // 3 days ago
                thumbnail = null,
                progress = 1.0f
            )
        )
    }
    
    var showClearDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Watch History",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
            
            // Clear history button
            IconButton(onClick = { showClearDialog = true }) {
                Icon(
                    Icons.Default.ClearAll,
                    contentDescription = "Clear History",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        
        if (historyItems.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No watch history",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Your recently played media will appear here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            // History list
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(historyItems) { item ->
                    HistoryItemCard(
                        item = item,
                        onPlay = {
                            // TODO: Resume playback
                        },
                        onRemove = {
                            // TODO: Remove from history
                        }
                    )
                }
                
                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
    
    // Clear history confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Clear Watch History?") },
            text = { 
                Text("This will permanently remove all items from your watch history. This action cannot be undone.") 
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        // TODO: Clear history
                        showClearDialog = false 
                    }
                ) {
                    Text(
                        "Clear",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryItemCard(
    item: HistoryItem,
    onPlay: () -> Unit,
    onRemove: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        onClick = onPlay,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Media type icon
            Card(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = when (item.type) {
                        MediaType.VIDEO -> MaterialTheme.colorScheme.primaryContainer
                        MediaType.AUDIO -> MaterialTheme.colorScheme.secondaryContainer
                    }
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (item.type) {
                            MediaType.VIDEO -> Icons.Default.VideoFile
                            MediaType.AUDIO -> Icons.Default.AudioFile
                        },
                        contentDescription = null,
                        tint = when (item.type) {
                            MediaType.VIDEO -> MaterialTheme.colorScheme.onPrimaryContainer
                            MediaType.AUDIO -> MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.duration,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = dateFormat.format(Date(item.watchedAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Progress bar
                if (item.progress < 1.0f) {
                    LinearProgressIndicator(
                        progress = { item.progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Menu button
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Resume") },
                        onClick = {
                            showMenu = false
                            onPlay()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Remove from history") },
                        onClick = {
                            showMenu = false
                            onRemove()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    )
                }
            }
        }
    }
}

private data class HistoryItem(
    val title: String,
    val type: MediaType,
    val duration: String,
    val watchedAt: Long,
    val thumbnail: String?,
    val progress: Float // 0.0 to 1.0
)

private enum class MediaType {
    VIDEO, AUDIO
}