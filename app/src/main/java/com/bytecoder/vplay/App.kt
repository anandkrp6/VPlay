package com.bytecoder.vplay

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.bytecoder.vplay.frontend.viewmodels.PlaybackQueueViewModel
import com.bytecoder.vplay.frontend.ui.screens.*
import com.bytecoder.vplay.frontend.ui.player.*

// Tab mode for content tabs
enum class TabMode {
    LIBRARY,
    PLAYLISTS
}

// Comprehensive navigation for restored feature set
sealed class VPlayScreen(val route: String, val title: String, val icon: ImageVector) {
    object Video : VPlayScreen("videos", "Videos", Icons.Filled.VideoLibrary)
    object Music : VPlayScreen("music", "Music", Icons.Filled.LibraryMusic)
    object Online : VPlayScreen("online", "Online", Icons.Filled.CloudQueue)
    object Options : VPlayScreen("tools", "Tools", Icons.Filled.Build)
    object Settings : VPlayScreen("settings", "Settings", Icons.Filled.Settings)
    
    // Detailed screens
    object AudioPlayer : VPlayScreen("audio_player", "Now Playing", Icons.Filled.MusicNote)
    object VideoPlayer : VPlayScreen("video_player/{videoId}", "Video Player", Icons.Filled.PlayCircle)
    object Queue : VPlayScreen("queue", "Queue", Icons.Filled.QueueMusic)
    object Downloads : VPlayScreen("downloads", "Downloads", Icons.Filled.Download)
    object FileExplorer : VPlayScreen("file_explorer", "Files", Icons.Filled.Folder)
    object PrivacyManager : VPlayScreen("privacy_manager", "Privacy", Icons.Filled.Security)
    object MediaTools : VPlayScreen("media_tools", "Media Tools", Icons.Filled.AudioFile)
    
    // New option screens
    object Feedback : VPlayScreen("feedback", "Feedback", Icons.Filled.Feedback)
    object About : VPlayScreen("about", "About", Icons.Filled.Info)
    object Tips : VPlayScreen("tips", "Tips", Icons.Filled.Lightbulb)
    object History : VPlayScreen("history", "History", Icons.Filled.History)
    object Permissions : VPlayScreen("permissions", "Permissions", Icons.Filled.Security)
}

@OptIn(ExperimentalMaterial3Api::class)
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

    // Persistent state for last used tab (default: Music)
    val sharedPrefs = remember { 
        context.getSharedPreferences("vplay_prefs", android.content.Context.MODE_PRIVATE) 
    }
    val lastUsedTab = remember { 
        sharedPrefs.getString("last_used_tab", VPlayScreen.Music.route) ?: VPlayScreen.Music.route 
    }

    // Get current tab name for top bar
    val currentTabName = remember(currentDestination?.route) {
        screens.find { it.route == currentDestination?.route }?.title ?: "vPlay"
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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentTabName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    // App logo on the left (24dp height as per plan)
                    Icon(
                        imageVector = Icons.Filled.MusicNote, // TODO: Replace with actual vPlay logo
                        contentDescription = "vPlay",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    // Search icon
                    IconButton(onClick = { /* TODO: Implement search */ }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // 3-dot overflow menu
                    IconButton(onClick = { /* TODO: Implement overflow menu */ }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                imageVector = screen.icon, 
                                contentDescription = screen.title,
                                tint = if (currentDestination?.hierarchy?.any { it.route == screen.route } == true) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            ) 
                        },
                        label = { 
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelMedium
                            ) 
                        },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
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
                    OptionsScreen(navController = navController)
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
            composable(VPlayScreen.Queue.route) {
                QueueScreen(queueViewModel = queueViewModel, navController = navController)
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
            
            // New option screens
            composable(VPlayScreen.Feedback.route) {
                FeedbackScreen(navController = navController)
            }
            composable(VPlayScreen.About.route) {
                AboutScreen(navController = navController)
            }
            composable(VPlayScreen.Tips.route) {
                TipsScreen(navController = navController)
            }
            composable(VPlayScreen.History.route) {
                HistoryScreen(navController = navController)
            }
            composable(VPlayScreen.Permissions.route) {
                PermissionsScreen(navController = navController)
            }
        }
        
        // Mini-player overlay
        MiniPlayer(
            queueViewModel = queueViewModel,
            onNavigateToQueue = { 
                // Navigate to dedicated queue screen
                navController.navigate(VPlayScreen.Queue.route)
            },
            onRequestNotificationPermission = { /* TODO: Handle permissions */ },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        }
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
}
