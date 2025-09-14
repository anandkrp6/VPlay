package com.bytecoder.vplay.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.bytecoder.vplay.media.PlaybackQueueViewModel
import com.bytecoder.vplay.media.MediaItemModel
import com.bytecoder.vplay.ui.compose.screens.*
import com.bytecoder.vplay.ui.compose.components.MiniPlayer

// Tab mode for content tabs
enum class TabMode {
    LIBRARY,
    PLAYLISTS
}

// Comprehensive navigation for restored feature set
sealed class VPlayScreen(val route: String, val title: String, val icon: ImageVector) {
    object Video : VPlayScreen("videos", "Video", Icons.Default.VideoFile)
    object Music : VPlayScreen("music", "Music", Icons.Default.MusicNote)
    object Online : VPlayScreen("online", "Online", Icons.Default.Language)
    object Options : VPlayScreen("tools", "Options", Icons.Default.Build)
    object Settings : VPlayScreen("settings", "Settings", Icons.Default.Settings)
    
    // Detailed screens
    object AudioPlayer : VPlayScreen("audio_player", "Audio Player", Icons.Default.MusicNote)
    object VideoPlayer : VPlayScreen("video_player/{videoId}", "Video Player", Icons.Default.PlayArrow)
    object Downloads : VPlayScreen("downloads", "Downloads", Icons.Default.Download)
    object FileExplorer : VPlayScreen("file_explorer", "File Explorer", Icons.Default.Folder)
    object PrivacyManager : VPlayScreen("privacy_manager", "Privacy", Icons.Default.Security)
    object MediaTools : VPlayScreen("media_tools", "Media Tools", Icons.Default.AudioFile)
}

@Composable
fun VPlayApp(
    queueViewModel: PlaybackQueueViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Define screens list first
    val screens = listOf(
        VPlayScreen.Video,
        VPlayScreen.Music,
        VPlayScreen.Online,
        VPlayScreen.Options
    )

    // Persistent state for last used tab (default: Video)
    val sharedPrefs = remember { 
        context.getSharedPreferences("vplay_prefs", android.content.Context.MODE_PRIVATE) 
    }
    val lastUsedTab = remember { 
        sharedPrefs.getString("last_used_tab", VPlayScreen.Video.route) ?: VPlayScreen.Video.route 
    }

    // Save current tab when navigation changes
    LaunchedEffect(currentDestination?.route) {
        currentDestination?.route?.let { route ->
            if (screens.any { it.route == route }) {
                sharedPrefs.edit().putString("last_used_tab", route).apply()
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = lastUsedTab,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(VPlayScreen.Video.route) {
                    VideosScreen(queueViewModel = queueViewModel, navController = navController)
                }
                composable(VPlayScreen.Music.route) {
                    MusicScreen(queueViewModel = queueViewModel, navController = navController)
                }
                composable(VPlayScreen.Online.route) {
                    OnlineScreen(queueViewModel = queueViewModel, navController = navController)
                }
                composable(VPlayScreen.Options.route) {
                    ToolsScreen(queueViewModel = queueViewModel, navController = navController)
                }
            composable(VPlayScreen.Settings.route) {
                SettingsScreen(navController = navController)
            }
            
            // Detailed screens
            composable(VPlayScreen.AudioPlayer.route) {
                AudioPlayerScreen(queueViewModel = queueViewModel, navController = navController)
            }
            composable(
                VPlayScreen.VideoPlayer.route,
                arguments = listOf(navArgument("videoId") { type = NavType.StringType })
            ) { backStackEntry ->
                val videoId = backStackEntry.arguments?.getString("videoId") ?: ""
                VideoPlayerScreen(videoId = videoId, queueViewModel = queueViewModel, navController = navController)
            }
            composable(VPlayScreen.Downloads.route) {
                DownloadsScreen(queueViewModel = queueViewModel, navController = navController)
            }
            composable(VPlayScreen.FileExplorer.route) {
                FileExplorerScreen(queueViewModel = queueViewModel, navController = navController)
            }
            composable(VPlayScreen.PrivacyManager.route) {
                PrivacyManagerScreen(queueViewModel = queueViewModel, navController = navController)
            }
            composable(VPlayScreen.MediaTools.route) {
                MediaToolsScreen(queueViewModel = queueViewModel, navController = navController)
            }
        }
        
        // Mini-player overlay
        MiniPlayer(
            queueViewModel = queueViewModel,
            onNavigateToQueue = { 
                // Navigate to audio player since we removed separate queue tab
                navController.navigate("audio_player")
            },
            onRequestNotificationPermission = { /* TODO: Handle permissions */ },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.MusicNote,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "VPlay",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Successfully migrated to Jetpack Compose + Material 3!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Migration Complete ✅",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• XML layouts → Jetpack Compose\n• View-based UI → Declarative UI\n• Material Design → Material 3\n• Legacy components → Modern architecture",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}}
