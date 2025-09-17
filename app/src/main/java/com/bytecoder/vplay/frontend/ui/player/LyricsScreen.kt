package com.bytecoder.vplay.frontend.ui.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bytecoder.vplay.backend.managers.PlayerManager
import com.bytecoder.vplay.frontend.viewmodels.PlaybackQueueViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsScreen(
    mediaId: String,
    navController: NavController,
    queueViewModel: PlaybackQueueViewModel
) {
    val context = LocalContext.current
    val position by PlayerManager.position.observeAsState(0L)
    val duration by PlayerManager.duration.observeAsState(0L)
    val isPlaying by PlayerManager.isPlaying.observeAsState(false)
    
    // Sample lyrics data - in a real app, this would be loaded from the media metadata or external service
    val lyrics = remember {
        listOf(
            LyricLine(0L, "Welcome to the rhythm of the night"),
            LyricLine(3000L, "Where the music takes you higher"),
            LyricLine(6000L, "Dancing under neon lights"),
            LyricLine(9000L, "Feel the beat that never tires"),
            LyricLine(12000L, ""),
            LyricLine(15000L, "Verse one begins with gentle grace"),
            LyricLine(18000L, "Melodies that touch your soul"),
            LyricLine(21000L, "Every note finds its place"),
            LyricLine(24000L, "In this story we control"),
            LyricLine(27000L, ""),
            LyricLine(30000L, "Chorus time to sing along"),
            LyricLine(33000L, "Raise your voice up to the sky"),
            LyricLine(36000L, "This is where we all belong"),
            LyricLine(39000L, "Let the music amplify"),
            LyricLine(42000L, ""),
            LyricLine(45000L, "Bridge the gap between the worlds"),
            LyricLine(48000L, "Where reality meets dreams"),
            LyricLine(51000L, "See how every note unfurls"),
            LyricLine(54000L, "Into something more it seems"),
            LyricLine(57000L, ""),
            LyricLine(60000L, "Final chorus brings us home"),
            LyricLine(63000L, "To the place where hearts unite"),
            LyricLine(66000L, "No more need to search or roam"),
            LyricLine(69000L, "We have found our guiding light"),
            LyricLine(72000L, ""),
            LyricLine(75000L, "Outro fades but memories stay"),
            LyricLine(78000L, "Of this moment shared in song"),
            LyricLine(81000L, "Until we meet another day"),
            LyricLine(84000L, "The music will carry on...")
        )
    }
    
    val listState = rememberLazyListState()
    
    // Auto-scroll to current lyric
    LaunchedEffect(position) {
        val currentIndex = lyrics.indexOfLast { it.timestamp <= position }
        if (currentIndex >= 0 && currentIndex < lyrics.size) {
            listState.animateScrollToItem(
                index = maxOf(0, currentIndex - 2), // Show a bit of context
                scrollOffset = -100
            )
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lyrics") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Toggle auto-scroll */ }) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Auto-scroll"
                        )
                    }
                    IconButton(onClick = { /* TODO: Text size adjustment */ }) {
                        Icon(
                            imageVector = Icons.Default.TextFormat,
                            contentDescription = "Text size"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    // Current song info
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = null, // TODO: Get album art from media
                                contentDescription = "Album Art",
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Current Song", // TODO: Get from media
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Artist Name", // TODO: Get from media
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                itemsIndexed(lyrics) { index, lyric ->
                    LyricLineItem(
                        lyric = lyric,
                        isActive = position >= lyric.timestamp && 
                                  (index == lyrics.lastIndex || position < lyrics[index + 1].timestamp),
                        isPlaying = isPlaying
                    )
                }
                
                item {
                    // Bottom padding for better scrolling
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
            
            // Mini playback controls overlay
            if (isPlaying || position > 0) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        // Progress bar
                        LinearProgressIndicator(
                            progress = if (duration > 0) position.toFloat() / duration.toFloat() else 0f,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatTime(position),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Row {
                                IconButton(
                                    onClick = { PlayerManager.previous() }
                                ) {
                                    Icon(
                                        Icons.Default.SkipPrevious,
                                        contentDescription = "Previous"
                                    )
                                }
                                
                                IconButton(
                                    onClick = { PlayerManager.playPause() }
                                ) {
                                    Icon(
                                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play"
                                    )
                                }
                                
                                IconButton(
                                    onClick = { PlayerManager.next() }
                                ) {
                                    Icon(
                                        Icons.Default.SkipNext,
                                        contentDescription = "Next"
                                    )
                                }
                            }
                            
                            Text(
                                text = formatTime(duration),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LyricLineItem(
    lyric: LyricLine,
    isActive: Boolean,
    isPlaying: Boolean
) {
    val alpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.6f,
        animationSpec = tween(300),
        label = "lyric_alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.05f else 1f,
        animationSpec = tween(300),
        label = "lyric_scale"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        if (lyric.text.isBlank()) {
            // Empty line for spacing
            Spacer(modifier = Modifier.height(20.dp))
        } else {
            Text(
                text = lyric.text,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = if (isActive) 24.sp else 20.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(alpha)
            )
            
            // Subtle highlight background for active lyric
            if (isActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .alpha(0.5f)
                )
            }
        }
    }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

private data class LyricLine(
    val timestamp: Long, // in milliseconds
    val text: String
)