package com.bytecoder.vplay.frontend.ui.screens.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bytecoder.vplay.backend.managers.PlaybackQueueManager
import com.bytecoder.vplay.backend.controllers.PlayerController

/**
 * MiniPlayerScreen provides a compact player interface that appears at the bottom
 * of the screen when media is playing. Allows basic playback controls and navigation
 * to the full player screen.
 */
@Composable
fun MiniPlayerScreen(
    playerController: PlayerController,
    queueManager: PlaybackQueueManager,
    navController: NavController,
    onNavigateToFullPlayer: () -> Unit,
    onNavigateToQueue: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Collect player state
    val currentMedia by playerController.currentMedia.collectAsState()
    val isPlaying by playerController.isPlaying.collectAsState()
    val currentPosition by playerController.currentPosition.collectAsState()
    val duration by playerController.duration.collectAsState()
    val currentPlayerType by playerController.currentPlayerType.collectAsState()
    
    // Collect queue state
    val currentQueue by queueManager.currentQueue.collectAsState()
    val currentIndex by queueManager.currentIndex.collectAsState()
    
    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    
    // Helper function to format time
    fun formatTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }
    
    // Calculate progress
    val progress = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()) else 0f
    
    // Only show mini player when media is playing
    AnimatedVisibility(
        visible = currentMedia != null && !isDragging,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = {
                            // If dragged down significantly, hide mini player
                            if (dragOffset > 100) {
                                // Could implement swipe to dismiss
                            }
                            isDragging = false
                            dragOffset = 0f
                        }
                    ) { _, dragAmount ->
                        dragOffset += dragAmount.y
                    }
                }
                .clickable { onNavigateToFullPlayer() },
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column {
                // Progress indicator
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Album art/thumbnail
                    Card(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        AsyncImage(
                            model = currentMedia?.thumbnailPath ?: currentMedia?.albumArtPath,
                            contentDescription = "Album Art",
                            modifier = Modifier.fillMaxSize(),
                            fallback = androidx.compose.ui.res.painterResource(
                                if (currentPlayerType == PlayerController.PlayerType.VIDEO) {
                                    android.R.drawable.ic_media_play // Video icon
                                } else {
                                    android.R.drawable.ic_media_play // Music icon
                                }
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Media info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = currentMedia?.title ?: "Unknown Title",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentMedia?.artist ?: "Unknown Artist",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Time display
                    Text(
                        text = "${formatTime(currentPosition)} / ${formatTime(duration)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Control buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous button
                        IconButton(
                            onClick = { playerController.playPrevious() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // Play/Pause button
                        FilledIconButton(
                            onClick = { playerController.togglePlayPause() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // Next button
                        IconButton(
                            onClick = { playerController.playNext() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // Queue button
                        IconButton(
                            onClick = onNavigateToQueue,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Badge(
                                modifier = Modifier.offset(x = 8.dp, y = (-8).dp)
                            ) {
                                Text(
                                    text = currentQueue.size.toString(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.QueueMusic,
                                contentDescription = "Queue",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}


