package com.bytecoder.vplay.frontend.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bytecoder.vplay.frontend.managers.*
import com.bytecoder.vplay.backend.controllers.PlayerController
import com.bytecoder.vplay.backend.managers.AudioPlayerScreenManager
import com.bytecoder.vplay.backend.managers.EqualizerScreenManager
import com.bytecoder.vplay.backend.managers.FolderScreenManager
import com.bytecoder.vplay.backend.managers.MusicLibraryScreenManager
import com.bytecoder.vplay.backend.managers.PlaybackQueueManager
import com.bytecoder.vplay.backend.managers.PlaylistScreenManager
import com.bytecoder.vplay.backend.managers.SearchScreenManager
import com.bytecoder.vplay.backend.managers.SettingsScreenManager
import com.bytecoder.vplay.backend.managers.VideoLibraryScreenManager
import com.bytecoder.vplay.backend.managers.VideoPlayerScreenManager
import com.bytecoder.vplay.frontend.ui.screens.*
import com.bytecoder.vplay.frontend.ui.screens.player.*

/**
 * Main navigation graph for the VPlay application
 */
@Composable
fun VPlayNavigation(
    navController: NavHostController,
    playerController: PlayerController,
    startDestination: String = "music_library"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Main library screens
        composable("music_library") {
            val musicLibraryManager: MusicLibraryScreenManager = viewModel()
            val queueManager: PlaybackQueueManager = viewModel()
            
            MusicLibraryScreen(
                musicLibraryManager = musicLibraryManager,
                queueManager = queueManager,
                navController = navController,
                onNavigateToAudioPlayer = { track ->
                    queueManager.setQueue(listOf(track))
                    navController.navigate("audio_player")
                }
            )
        }
        
        composable("video_library") {
            val videoLibraryManager: VideoLibraryScreenManager = viewModel()
            val queueManager: PlaybackQueueManager = viewModel()
            
            VideoLibraryScreen(
                videoLibraryManager = videoLibraryManager,
                queueManager = queueManager,
                navController = navController,
                onNavigateToVideoPlayer = { video ->
                    navController.navigate("video_player/${video.id}")
                }
            )
        }
        
        composable("playlists") {
            val playlistManager: PlaylistScreenManager = viewModel()
            val queueManager: PlaybackQueueManager = viewModel()
            
            PlaylistScreen(
                playlistManager = playlistManager,
                queueManager = queueManager,
                navController = navController
            )
        }
        
        composable("search") {
            val searchManager: SearchScreenManager = viewModel()
            val musicLibraryManager: MusicLibraryScreenManager = viewModel()
            val videoLibraryManager: VideoLibraryScreenManager = viewModel()
            val queueManager: PlaybackQueueManager = viewModel()
            
            SearchScreen(
                searchManager = searchManager,
                musicLibraryManager = musicLibraryManager,
                videoLibraryManager = videoLibraryManager,
                queueManager = queueManager,
                navController = navController
            )
        }
        
        composable("settings") {
            val settingsManager: SettingsScreenManager = viewModel()
            
            SettingsScreen(
                settingsManager = settingsManager,
                navController = navController
            )
        }
        
        // Player screens
        composable("audio_player") {
            val audioPlayerManager: AudioPlayerScreenManager = viewModel()
            val queueManager: PlaybackQueueManager = viewModel()
            
            AudioPlayerScreen(
                audioPlayerManager = audioPlayerManager,
                queueManager = queueManager,
                playerController = playerController,
                navController = navController
            )
        }
        
        composable("video_player/{videoId}") { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString("videoId") ?: ""
            val videoPlayerManager: VideoPlayerScreenManager = viewModel()
            val videoLibraryManager: VideoLibraryScreenManager = viewModel()
            val queueManager: PlaybackQueueManager = viewModel()
            
            VideoPlayerScreen(
                videoId = videoId,
                queueManager = queueManager,
                navController = navController,
                videoPlayerScreenManager = videoPlayerManager,
                videoLibraryScreenManager = videoLibraryManager
            )
        }
        
        composable("queue") {
            val queueManager: PlaybackQueueManager = viewModel()
            
            QueueScreen(
                queueManager = queueManager,
                playerController = playerController,
                navController = navController
            )
        }
        
        // Additional screens
        composable("folders") {
            val folderManager: FolderScreenManager = viewModel()
            
            FolderScreen(
                folderManager = folderManager,
                navController = navController
            )
        }
        
        composable("equalizer") {
            val equalizerManager: EqualizerScreenManager = viewModel()
            
            EqualizerScreen(
                equalizerManager = equalizerManager,
                playerController = playerController,
                navController = navController
            )
        }
    }
}

/**
 * Navigation routes constants
 */
object NavigationRoutes {
    const val MUSIC_LIBRARY = "music_library"
    const val VIDEO_LIBRARY = "video_library"
    const val PLAYLISTS = "playlists"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val AUDIO_PLAYER = "audio_player"
    const val VIDEO_PLAYER = "video_player/{videoId}"
    const val QUEUE = "queue"
    const val FOLDERS = "folders"
    const val EQUALIZER = "equalizer"
    
    fun videoPlayer(videoId: String): String = "video_player/$videoId"
}


