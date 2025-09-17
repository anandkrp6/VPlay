package com.bytecoder.vplay.frontend.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bytecoder.vplay.frontend.viewmodels.PlaybackQueueViewModel
import com.bytecoder.vplay.backend.managers.PlayerManager
import com.bytecoder.vplay.backend.data.models.MediaItemModel

@Composable
fun MiniPlayer(
    queueViewModel: PlaybackQueueViewModel,
    onNavigateToQueue: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentIndex by queueViewModel.currentIndex.observeAsState(0)
    val queue by queueViewModel.queue.observeAsState(emptyList())
    val isPlaying by PlayerManager.isPlaying.observeAsState(false)
    val position by PlayerManager.position.observeAsState(0L)
    val duration by PlayerManager.duration.observeAsState(0L)
    
    val currentItem = if (currentIndex in queue.indices) queue[currentIndex] else null
    
    // Only show mini player when media is playing or paused
    AnimatedVisibility(
        visible = currentItem != null,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        Column(
            modifier = modifier.fillMaxWidth()
        ) {
            // Thin progress bar at top (2dp) - Plan specification
            if (duration > 0) {
                LinearProgressIndicator(
                    progress = { (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    trackColor = Color.Transparent
                )
            }
            
            // Main mini player content
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp) // Standard mini player height
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            if (dragAmount.y > 50) {
                                // Swipe down to close - pause playback + clear queue
                                if (PlayerManager.isPlaying.value == true) {
                                    PlayerManager.playPause() // This will pause if currently playing
                                }
                                queueViewModel.clearQueue()
                            } else if (dragAmount.y < -50) {
                                // Swipe up to open full player
                                onNavigateToQueue()
                            }
                        }
                    },
                color = MaterialTheme.colorScheme.surfaceContainer,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT SECTION (30%) - Small preview window
                    Box(
                        modifier = Modifier
                            .weight(0.3f)
                            .aspectRatio(1f)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { onNavigateToQueue() },
                        contentAlignment = Alignment.Center
                    ) {
                        currentItem?.let { item ->
                            if (item.isVideo && item.thumbnailPath?.isNotEmpty() == true) {
                                // Video: Live video preview (muted) - TODO: Implement live preview
                                AsyncImage(
                                    model = item.thumbnailPath,
                                    contentDescription = "Video preview",
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Play overlay for video preview
                                Icon(
                                    imageVector = Icons.Filled.PlayCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.White.copy(alpha = 0.8f)
                                )
                            } else if (!item.isVideo && item.thumbnailPath?.isNotEmpty() == true) {
                                // Audio: Album art thumbnail
                                AsyncImage(
                                    model = item.thumbnailPath,
                                    contentDescription = "Album art",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                // Fallback: Animated equalizer (colorful when playing, white when paused)
                                AnimatedEqualizer(isPlaying = isPlaying)
                            }
                        }
                    }
                    
                    // CENTER SECTION (40%) - Play/Pause button (Material 3 FAB style)
                    Box(
                        modifier = Modifier
                            .weight(0.4f)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FloatingActionButton(
                            onClick = { 
                                onRequestNotificationPermission()
                                PlayerManager.playPause() 
                            },
                            modifier = Modifier.size(48.dp),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    // RIGHT SECTION (30%) - Close (X) button
                    Box(
                        modifier = Modifier
                            .weight(0.3f)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { 
                                // Close - pause playback & clear queue
                                if (isPlaying) {
                                    PlayerManager.playPause() // This will pause if currently playing
                                }
                                queueViewModel.clearQueue()
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close player",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedEqualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val barCount = 5
    val barColors = if (isPlaying) {
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        )
    } else {
        List(barCount) { MaterialTheme.colorScheme.outline }
    }
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(barCount) { index ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(if (isPlaying) (12..24).random().dp else 16.dp)
                    .background(
                        color = barColors[index],
                        shape = RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}