package com.bytecoder.vplay.frontend.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipsScreen(navController: NavController) {
    val listState = rememberLazyListState()
    
    val tipSections = remember {
        listOf(
            TipSection(
                title = "Getting Started",
                icon = Icons.Default.PlayArrow,
                tips = listOf(
                    Tip(
                        title = "Navigate Tabs",
                        description = "Use the bottom navigation to switch between Video, Music, Online, and Options.",
                        icon = Icons.Default.Tab
                    ),
                    Tip(
                        title = "Browse Media",
                        description = "Tap on any video or audio file to start playback immediately.",
                        icon = Icons.Default.TouchApp
                    ),
                    Tip(
                        title = "Search Content",
                        description = "Use the search button to quickly find specific media files.",
                        icon = Icons.Default.Search
                    )
                )
            ),
            TipSection(
                title = "Video Player Controls",
                icon = Icons.Default.VideoLibrary,
                tips = listOf(
                    Tip(
                        title = "Gesture Controls",
                        description = "Swipe up/down on the left side to adjust brightness, right side for volume.",
                        icon = Icons.Default.SwipeUp
                    ),
                    Tip(
                        title = "Seek Control",
                        description = "Swipe left/right anywhere on the screen to seek backward/forward.",
                        icon = Icons.Default.FastForward
                    ),
                    Tip(
                        title = "Double Tap",
                        description = "Double tap left/right sides of the screen for quick 10-second seek.",
                        icon = Icons.Default.TouchApp
                    ),
                    Tip(
                        title = "Subtitles",
                        description = "Tap the subtitle button in the video player to select subtitle tracks.",
                        icon = Icons.Default.Subtitles
                    ),
                    Tip(
                        title = "Picture-in-Picture",
                        description = "Use the PiP button to continue watching while using other apps.",
                        icon = Icons.Default.PictureInPicture
                    )
                )
            ),
            TipSection(
                title = "Music Player Features",
                icon = Icons.Default.MusicNote,
                tips = listOf(
                    Tip(
                        title = "Queue Management",
                        description = "Add songs to queue by tapping the '+' button next to any track.",
                        icon = Icons.Default.QueueMusic
                    ),
                    Tip(
                        title = "Repeat & Shuffle",
                        description = "Toggle repeat and shuffle modes using the controls in the music player.",
                        icon = Icons.Default.Shuffle
                    ),
                    Tip(
                        title = "Favorites",
                        description = "Tap the heart icon to add songs to your favorites playlist.",
                        icon = Icons.Default.Favorite
                    ),
                    Tip(
                        title = "Background Playback",
                        description = "Music continues playing when you navigate to other apps or lock your phone.",
                        icon = Icons.Default.MusicOff
                    )
                )
            ),
            TipSection(
                title = "Online Streaming",
                icon = Icons.Default.CloudDownload,
                tips = listOf(
                    Tip(
                        title = "Stream URLs",
                        description = "Paste any video or audio URL to stream content directly.",
                        icon = Icons.Default.Link
                    ),
                    Tip(
                        title = "Download Media",
                        description = "Tap the download button to save content for offline viewing.",
                        icon = Icons.Default.Download
                    ),
                    Tip(
                        title = "Manage Downloads",
                        description = "Check download progress and manage files in the Downloads section.",
                        icon = Icons.Default.FolderOpen
                    )
                )
            ),
            TipSection(
                title = "Customization",
                icon = Icons.Default.Settings,
                tips = listOf(
                    Tip(
                        title = "Theme Selection",
                        description = "Switch between light, dark, and system themes in Settings.",
                        icon = Icons.Default.Palette
                    ),
                    Tip(
                        title = "Playback Settings",
                        description = "Adjust video quality, audio preferences, and subtitle settings.",
                        icon = Icons.Default.Tune
                    ),
                    Tip(
                        title = "Storage Management",
                        description = "Configure download locations and manage storage usage.",
                        icon = Icons.Default.Storage
                    )
                )
            ),
            TipSection(
                title = "Pro Tips",
                icon = Icons.Default.Lightbulb,
                tips = listOf(
                    Tip(
                        title = "Keyboard Shortcuts",
                        description = "Use space bar to play/pause, arrow keys to seek, and M to mute.",
                        icon = Icons.Default.Keyboard
                    ),
                    Tip(
                        title = "File Formats",
                        description = "vPlay supports most video and audio formats including MP4, MKV, MP3, FLAC.",
                        icon = Icons.Default.FilePresent
                    ),
                    Tip(
                        title = "Performance",
                        description = "Enable hardware acceleration in settings for smoother playback.",
                        icon = Icons.Default.Speed
                    ),
                    Tip(
                        title = "Sharing",
                        description = "Share your favorite content with friends using the share button.",
                        icon = Icons.Default.Share
                    )
                )
            )
        )
    }
    
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
                text = "Tips & Tutorials",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        // Welcome message
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Learn vPlay",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Master all features and get the most out of your media player",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tips sections
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tipSections) { section ->
                TipSectionCard(section = section)
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TipSectionCard(section: TipSection) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Section header
            Card(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = section.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // Expandable content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    section.tips.forEachIndexed { index, tip ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        TipCard(tip = tip)
                    }
                }
            }
        }
    }
}

@Composable
private fun TipCard(tip: Tip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Card(
                modifier = Modifier.size(36.dp),
                shape = MaterialTheme.shapes.small,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tip.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tip.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
                )
            }
        }
    }
}

private data class TipSection(
    val title: String,
    val icon: ImageVector,
    val tips: List<Tip>
)

private data class Tip(
    val title: String,
    val description: String,
    val icon: ImageVector
)


