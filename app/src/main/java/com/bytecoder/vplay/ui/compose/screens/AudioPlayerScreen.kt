package com.bytecoder.vplay.ui.compose.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bytecoder.vplay.media.MediaItemModel
import com.bytecoder.vplay.media.PlaybackQueueViewModel
import com.bytecoder.vplay.ui.theme.VPlayTheme
import com.bytecoder.vplay.ui.viewmodels.AudioPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerScreen(
    queueViewModel: PlaybackQueueViewModel,
    navController: NavController,
    audioPlayerViewModel: AudioPlayerViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentMedia by queueViewModel.currentMedia.observeAsState()
    val isPlaying by queueViewModel.isPlaying.observeAsState(false)
    val currentPosition by queueViewModel.currentPosition.observeAsState(0L)
    val duration by queueViewModel.duration.observeAsState(0L)
    val queue by queueViewModel.queue.observeAsState(emptyList())
    val currentIndex by queueViewModel.currentIndex.observeAsState(0)
    
    var volume by remember { mutableFloatStateOf(0.8f) }
    var isShuffleEnabled by remember { mutableStateOf(false) }
    var repeatMode by remember { mutableStateOf(0) } // 0: none, 1: all, 2: one
    var isFavorite by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }
    
    val scaffoldState = rememberBottomSheetScaffoldState()
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    VPlayTheme {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = "Now Playing",
                            color = MaterialTheme.colorScheme.onSurface
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack, 
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showQueue = !showQueue }) {
                            Icon(
                                Icons.AutoMirrored.Filled.QueueMusic, 
                                contentDescription = "Queue",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { /* TODO: More options */ }) {
                            Icon(
                                Icons.Default.MoreVert, 
                                contentDescription = "More",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            },
            sheetContent = {
                if (showQueue) {
                    QueueBottomSheet(
                        queue = queue,
                        currentIndex = currentIndex,
                        onTrackSelected = { index ->
                            queueViewModel.seekToQueueItem(index)
                        },
                        modifier = Modifier.height(400.dp)
                    )
                }
            },
            modifier = Modifier.statusBarsPadding()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                currentMedia?.let { media ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Album Art with rotation animation when playing
                        Box(
                            modifier = Modifier
                                .size(280.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .rotate(if (isPlaying) rotation else 0f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (media.thumbnailPath?.isNotEmpty() == true) {
                                AsyncImage(
                                    model = media.thumbnailPath,
                                    contentDescription = media.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                        contentDescription = null,
                                        modifier = Modifier.size(80.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            // Gradient overlay for better text visibility
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.1f)
                                            ),
                                            startY = 0f,
                                            endY = 1000f
                                        )
                                    )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Track Info
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = media.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            media.subtitle?.let { artist ->
                                Text(
                                    text = artist,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Progress Bar
                        Column {
                            LinearProgressIndicator(
                                progress = { if (duration > 0) (currentPosition.toFloat() / duration.toFloat()) else 0f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Time labels
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatTime(currentPosition),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatTime(duration),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Main Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Shuffle
                            IconButton(
                                onClick = { 
                                    isShuffleEnabled = !isShuffleEnabled
                                    queueViewModel.setShuffleMode(isShuffleEnabled)
                                }
                            ) {
                                Icon(
                                    Icons.Default.Shuffle, 
                                    contentDescription = "Shuffle",
                                    tint = if (isShuffleEnabled) MaterialTheme.colorScheme.primary 
                                           else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            // Previous
                            IconButton(
                                onClick = { queueViewModel.skipToPrevious() },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.SkipPrevious, 
                                    contentDescription = "Previous",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            
                            // Play/Pause
                            FilledIconButton(
                                onClick = { 
                                    if (isPlaying) queueViewModel.pause() 
                                    else queueViewModel.play()
                                },
                                modifier = Modifier.size(72.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            
                            // Next
                            IconButton(
                                onClick = { queueViewModel.skipToNext() },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.SkipNext, 
                                    contentDescription = "Next",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            
                            // Repeat
                            IconButton(
                                onClick = { 
                                    repeatMode = (repeatMode + 1) % 3
                                    queueViewModel.setRepeatMode(repeatMode)
                                }
                            ) {
                                Icon(
                                    when (repeatMode) {
                                        1 -> Icons.Default.Repeat
                                        2 -> Icons.Default.RepeatOne
                                        else -> Icons.Default.Repeat
                                    },
                                    contentDescription = "Repeat",
                                    tint = if (repeatMode > 0) MaterialTheme.colorScheme.primary 
                                           else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Secondary Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Favorite
                            IconButton(
                                onClick = { 
                                    isFavorite = !isFavorite
                                    // TODO: Update favorite status in database
                                }
                            ) {
                                Icon(
                                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            // Volume Control
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Slider(
                                    value = volume,
                                    onValueChange = { 
                                        volume = it
                                        audioPlayerViewModel.setVolume(it)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } ?: run {
                    // No media selected state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.QueueMusic,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "No music selected",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                            Text(
                                text = "Select a song from your library",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QueueBottomSheet(
    queue: List<MediaItemModel>,
    currentIndex: Int,
    onTrackSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    LaunchedEffect(currentIndex) {
        if (currentIndex >= 0 && currentIndex < queue.size) {
            listState.animateScrollToItem(currentIndex)
        }
    }
    
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            text = "Up Next",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(queue.size) { index ->
                val media = queue[index]
                Card(
                    onClick = { onTrackSelected(index) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (index == currentIndex) 
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (index == currentIndex) 4.dp else 1.dp
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Track number or now playing indicator
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (index == currentIndex) 
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (index == currentIndex) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Now Playing",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        // Track info
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = media.title,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = if (index == currentIndex) FontWeight.Bold else FontWeight.Normal
                            )
                            media.subtitle?.let { artist ->
                                Text(
                                    text = artist,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}