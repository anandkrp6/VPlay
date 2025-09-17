package com.bytecoder.vplay.frontend.ui.player

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bytecoder.vplay.backend.managers.SubtitleTrack
import com.bytecoder.vplay.frontend.viewmodels.PlaybackQueueViewModel
import com.bytecoder.vplay.frontend.viewmodels.VideoPlayerViewModel
import kotlin.math.abs

@Composable
fun VideoPlayerScreen(
    videoId: String,
    queueViewModel: PlaybackQueueViewModel,
    navController: NavController,
    videoPlayerViewModel: VideoPlayerViewModel = viewModel()
) {
    val context = LocalContext.current
    var controlsVisible by remember { mutableStateOf(true) }
    var showBrightnessOverlay by remember { mutableStateOf(false) }
    var showVolumeOverlay by remember { mutableStateOf(false) }
    var showSeekOverlay by remember { mutableStateOf(false) }
    var seekDirection by remember { mutableStateOf("") }
    
    // Collect video player state
    val currentMedia by videoPlayerViewModel.currentMedia.collectAsState()
    val isPlaying by videoPlayerViewModel.isPlaying.collectAsState()
    val currentPosition by videoPlayerViewModel.currentPosition.collectAsState()
    val duration by videoPlayerViewModel.duration.collectAsState()
    val volume by videoPlayerViewModel.volume.collectAsState()
    val brightness by videoPlayerViewModel.brightness.collectAsState()
    val isMuted by videoPlayerViewModel.isMuted.collectAsState()
    val isFullscreen by videoPlayerViewModel.isFullscreen.collectAsState()
    
    // Collect subtitle-related state
    val availableSubtitles by videoPlayerViewModel.availableSubtitles.collectAsState()
    val selectedSubtitleTrack by videoPlayerViewModel.selectedSubtitleTrack.collectAsState()
    val subtitlesEnabled by videoPlayerViewModel.subtitlesEnabled.collectAsState()
    val showSubtitleDialog by videoPlayerViewModel.showSubtitleDialog.collectAsState()
    
    // Helper function to format time
    fun formatTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }
    
    // Calculate progress
    val progress = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()) else 0f
    
    // File picker for external subtitles
    val subtitleFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // For demo purposes, we'll use "English" as default language
            // In a real app, you might want to let the user select the language
            videoPlayerViewModel.loadExternalSubtitle(it, "English")
        }
    }
    
    // Auto-hide controls after 3 seconds
    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            kotlinx.coroutines.delay(3000)
            controlsVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Main video player area with gesture detection
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            val screenWidth = size.width
                            val screenHeight = size.height
                            
                            when {
                                // Double tap left side - rewind 10 seconds
                                offset.x < screenWidth * 0.3f -> {
                                    seekDirection = "Rewind 10s"
                                    showSeekOverlay = true
                                    val newPosition = (currentPosition - 10000).coerceAtLeast(0)
                                    videoPlayerViewModel.seekTo(newPosition)
                                }
                                // Double tap right side - forward 10 seconds  
                                offset.x > screenWidth * 0.7f -> {
                                    seekDirection = "Forward 10s"
                                    showSeekOverlay = true
                                    val newPosition = (currentPosition + 10000).coerceAtMost(duration)
                                    videoPlayerViewModel.seekTo(newPosition)
                                }
                                // Single tap center - toggle controls
                                else -> {
                                    controlsVisible = !controlsVisible
                                }
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            showBrightnessOverlay = false
                            showVolumeOverlay = false
                        }
                    ) { _, dragAmount ->
                        val screenWidth = size.width.toFloat()
                        
                        if (abs(dragAmount.y) > abs(dragAmount.x)) {
                            // Vertical swipe detected
                            if (dragAmount.x < screenWidth / 2) {
                                // Left side - brightness control
                                val newBrightness = (brightness - dragAmount.y / 1000f).coerceIn(0f, 1f)
                                videoPlayerViewModel.setBrightness(newBrightness)
                                showBrightnessOverlay = true
                            } else {
                                // Right side - volume control
                                val newVolume = (volume - dragAmount.y / 1000f).coerceIn(0f, 1f)
                                videoPlayerViewModel.setVolume(newVolume)
                                showVolumeOverlay = true
                            }
                        } else if (abs(dragAmount.x) > 50) {
                            // Horizontal swipe - seek forward/backward
                            val seekAmount = (dragAmount.x / screenWidth) * duration * 0.1f // 10% of duration per full swipe
                            val newPosition = (currentPosition + seekAmount.toLong()).coerceIn(0, duration)
                            videoPlayerViewModel.seekTo(newPosition)
                            showSeekOverlay = true
                            seekDirection = if (dragAmount.x > 0) "Seeking Forward" else "Seeking Backward"
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Video player placeholder
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "Video Player (ID: $videoId)",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "• Single tap: Show/hide controls\n• Double tap left: Rewind 10s\n• Double tap right: Forward 10s\n• Swipe left edge: Brightness\n• Swipe right edge: Volume",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Top controls bar (visible when controlsVisible is true)
        AnimatedVisibility(
            visible = controlsVisible,
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            TopVideoControls(
                onBackClick = { navController.navigateUp() },
                onQueueClick = { navController.navigate("queue") },
                title = currentMedia?.title ?: "Video Player"
            )
        }

        // Bottom controls bar (visible when controlsVisible is true)
        AnimatedVisibility(
            visible = controlsVisible,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomVideoControls(
                onPreviousClick = { videoPlayerViewModel.skipToPrevious() },
                onPlayPauseClick = { 
                    if (isPlaying) videoPlayerViewModel.pause() else videoPlayerViewModel.play()
                },
                onNextClick = { videoPlayerViewModel.skipToNext() },
                isPlaying = isPlaying,
                currentTime = formatTime(currentPosition),
                duration = formatTime(duration),
                progress = progress,
                onSeek = { newProgress ->
                    val newPosition = (newProgress * duration).toLong()
                    videoPlayerViewModel.seekTo(newPosition)
                },
                subtitlesEnabled = subtitlesEnabled,
                hasSubtitles = availableSubtitles.isNotEmpty(),
                onSubtitleClick = { videoPlayerViewModel.showSubtitleDialog() },
                onSubtitleToggle = { videoPlayerViewModel.toggleSubtitles() }
            )
        }

        // Brightness overlay (left side)
        if (showBrightnessOverlay) {
            BrightnessVolumeOverlay(
                modifier = Modifier.align(Alignment.CenterStart),
                icon = Icons.Default.Brightness6,
                value = brightness,
                label = "Brightness"
            )
        }

        // Volume overlay (right side)
        if (showVolumeOverlay) {
            BrightnessVolumeOverlay(
                modifier = Modifier.align(Alignment.CenterEnd),
                icon = Icons.Default.VolumeUp,
                value = volume,
                label = "Volume"
            )
        }

        // Seek overlay (center)
        if (showSeekOverlay) {
            SeekOverlay(
                modifier = Modifier.align(Alignment.Center),
                direction = seekDirection
            )
            
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(1000)
                showSeekOverlay = false
            }
        }
    }
    
    // Subtitle Selection Dialog
    if (showSubtitleDialog) {
        SubtitleSelectionDialog(
            availableSubtitles = availableSubtitles,
            selectedSubtitle = selectedSubtitleTrack,
            onSubtitleSelected = { track ->
                videoPlayerViewModel.selectSubtitleTrack(track)
                videoPlayerViewModel.hideSubtitleDialog()
            },
            onLoadExternalSubtitle = {
                subtitleFilePicker.launch("*/*")
            },
            onDismiss = { videoPlayerViewModel.hideSubtitleDialog() }
        )
    }
}

@Composable
private fun TopVideoControls(
    onBackClick: () -> Unit,
    onQueueClick: () -> Unit,
    title: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button (left)
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            
            // Title (center)
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Rotation toggle button (right side)
            IconButton(
                onClick = { 
                    // Toggle screen orientation
                    val activity = context as? androidx.activity.ComponentActivity
                    activity?.let {
                        val currentOrientation = it.requestedOrientation
                        it.requestedOrientation = when (currentOrientation) {
                            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> {
                                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            }
                            else -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        }
                    }
                }
            ) {
                Icon(
                    Icons.Default.ScreenRotation,
                    contentDescription = "Toggle rotation",
                    tint = Color.White
                )
            }
            
            // Queue list button (right)
            IconButton(onClick = onQueueClick) {
                Icon(
                    Icons.Default.QueueMusic,
                    contentDescription = "Queue",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun BottomVideoControls(
    onPreviousClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    isPlaying: Boolean,
    currentTime: String,
    duration: String,
    progress: Float,
    onSeek: (Float) -> Unit = {},
    subtitlesEnabled: Boolean,
    hasSubtitles: Boolean,
    onSubtitleClick: () -> Unit,
    onSubtitleToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Timeline scrubber
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
                
                Slider(
                    value = progress,
                    onValueChange = onSeek,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                Text(
                    text = duration,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
            
            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousClick) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                IconButton(onClick = onNextClick) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            // Secondary controls row with subtitle controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Subtitle toggle button (when subtitles are available)
                if (hasSubtitles) {
                    IconButton(onClick = onSubtitleToggle) {
                        Icon(
                            imageVector = if (subtitlesEnabled) Icons.Default.ClosedCaption else Icons.Default.ClosedCaptionDisabled,
                            contentDescription = if (subtitlesEnabled) "Disable Subtitles" else "Enable Subtitles",
                            tint = if (subtitlesEnabled) MaterialTheme.colorScheme.primary else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
                
                // Subtitle selection button
                IconButton(onClick = onSubtitleClick) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Subtitle Options",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Placeholder for additional controls
                Spacer(modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.size(48.dp))
            }
        }
    }
}

@Composable
private fun BrightnessVolumeOverlay(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: Float,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.padding(32.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.Black.copy(alpha = 0.8f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { value },
                modifier = Modifier
                    .height(4.dp)
                    .width(80.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.White.copy(alpha = 0.3f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
        }
    }
}

@Composable
private fun SeekOverlay(
    direction: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.Black.copy(alpha = 0.8f)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (direction.contains("Forward")) Icons.Default.FastForward else Icons.Default.FastRewind,
                contentDescription = direction,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = direction,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
    }
}

@Composable
private fun SubtitleSelectionDialog(
    availableSubtitles: List<SubtitleTrack>,
    selectedSubtitle: SubtitleTrack?,
    onSubtitleSelected: (SubtitleTrack?) -> Unit,
    onLoadExternalSubtitle: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Subtitle Options",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Option to disable subtitles
                item {
                    SubtitleOptionItem(
                        title = "Off",
                        subtitle = "Disable subtitles",
                        isSelected = selectedSubtitle == null,
                        onClick = { onSubtitleSelected(null) }
                    )
                }
                
                // Available subtitle tracks
                items(availableSubtitles) { track ->
                    SubtitleOptionItem(
                        title = track.label,
                        subtitle = track.language,
                        isSelected = selectedSubtitle?.id == track.id,
                        onClick = { onSubtitleSelected(track) }
                    )
                }
                
                // Option to load external subtitle
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        onClick = {
                            onLoadExternalSubtitle()
                            onDismiss()
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Load External Subtitle",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Load subtitle file (SRT, VTT, ASS)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun SubtitleOptionItem(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
            else 
                MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}