package com.bytecoder.vplay.frontend.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Adb
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bytecoder.vplay.backend.managers.SettingsScreenManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    SettingsScreenManager: SettingsScreenManager = viewModel()
) {
    val context = LocalContext.current
    
    // Settings state
    val darkModeEnabled by SettingsScreenManager.darkModeEnabled.collectAsState()
    val dynamicThemeEnabled by SettingsScreenManager.dynamicThemeEnabled.collectAsState()
    val autoPlayEnabled by SettingsScreenManager.autoPlayEnabled.collectAsState()
    val shuffleByDefault by SettingsScreenManager.shuffleByDefault.collectAsState()
    val crossfadeEnabled by SettingsScreenManager.crossfadeEnabled.collectAsState()
    val highQualityEnabled by SettingsScreenManager.highQualityEnabled.collectAsState()
    val notificationsEnabled by SettingsScreenManager.notificationsEnabled.collectAsState()
    val backgroundPlayEnabled by SettingsScreenManager.backgroundPlayEnabled.collectAsState()
    val gridViewEnabled by SettingsScreenManager.gridViewEnabled.collectAsState()
    val animationsEnabled by SettingsScreenManager.animationsEnabled.collectAsState()
    val immersiveModeEnabled by SettingsScreenManager.immersiveModeEnabled.collectAsState()
    val headphoneDetectionEnabled by SettingsScreenManager.headphoneDetectionEnabled.collectAsState()
    val smartSkipEnabled by SettingsScreenManager.smartSkipEnabled.collectAsState()
    val defaultPlaybackSpeed by SettingsScreenManager.defaultPlaybackSpeed.collectAsState()
    val autoDownloadEnabled by SettingsScreenManager.autoDownloadEnabled.collectAsState()
    val analyticsEnabled by SettingsScreenManager.analyticsEnabled.collectAsState()
    val downloadLocation by SettingsScreenManager.downloadLocation.collectAsState()
    val locationServicesEnabled by SettingsScreenManager.locationServicesEnabled.collectAsState()
    val historyTrackingEnabled by SettingsScreenManager.historyTrackingEnabled.collectAsState()
    val usageAnalyticsEnabled by SettingsScreenManager.usageAnalyticsEnabled.collectAsState()
    val debugModeEnabled by SettingsScreenManager.debugModeEnabled.collectAsState()
    
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showAudioQualityDialog by remember { mutableStateOf(false) }
    var showVideoQualityDialog by remember { mutableStateOf(false) }
    var showDownloadLocationDialog by remember { mutableStateOf(false) }
    var showBugReportDialog by remember { mutableStateOf(false) }
    
    var audioVolume by remember { mutableFloatStateOf(0.8f) }
    var crossfadeDuration by remember { mutableFloatStateOf(3f) }
    var bufferSize by remember { mutableFloatStateOf(50f) }

    // Folder picker for download location
    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            // Convert URI to a readable path for display
            val displayPath = it.lastPathSegment?.replace("primary:", "Internal Storage/") ?: "Internal Storage/VPlay"
            SettingsScreenManager.setDownloadLocation(displayPath)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Enhanced UI/Display Section
            item {
                SettingsSection(title = "UI & Display") {
                    SettingsItem(
                        icon = Icons.Default.Palette,
                        title = "Theme",
                        subtitle = if (darkModeEnabled) "Dark" else "Light",
                        onClick = { showThemeDialog = true }
                    )
                    
                    SettingsToggleItem(
                        icon = Icons.Default.ColorLens,
                        title = "Dynamic Colors",
                        subtitle = "Use system colors (Material You)",
                        checked = dynamicThemeEnabled,
                        onCheckedChange = { SettingsScreenManager.setDynamicTheme(it) }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Language,
                        title = "Language",
                        subtitle = "English",
                        onClick = { showLanguageDialog = true }
                    )
                    
                    // New UI options
                    SettingsToggleItem(
                        icon = Icons.Default.GridView,
                        title = "Grid View",
                        subtitle = "Show albums and playlists in grid layout",
                        checked = gridViewEnabled,
                        onCheckedChange = { SettingsScreenManager.setGridView(it) }
                    )
                    
                    SettingsToggleItem(
                        icon = Icons.Default.Animation,
                        title = "Animations",
                        subtitle = "Enable smooth animations and transitions",
                        checked = animationsEnabled,
                        onCheckedChange = { SettingsScreenManager.setAnimations(it) }
                    )
                    
                    var showFontSizeDialog by remember { mutableStateOf(false) }
                    
                    SettingsItem(
                        icon = Icons.Default.TextFormat,
                        title = "Font Size",
                        subtitle = "Medium",
                        onClick = { showFontSizeDialog = true }
                    )
                    
                    if (showFontSizeDialog) {
                        FontSizeDialog(
                            onDismiss = { showFontSizeDialog = false },
                            onSizeSelected = { size ->
                                // Handle font size selection
                                showFontSizeDialog = false
                            }
                        )
                    }
                    
                    SettingsToggleItem(
                        icon = Icons.Default.FullscreenExit,
                        title = "Immersive Mode",
                        subtitle = "Hide status bar during playback",
                        checked = immersiveModeEnabled,
                        onCheckedChange = { SettingsScreenManager.setImmersiveMode(it) }
                    )
                }
            }
            
            // Enhanced Playback Section with more options
            item {
                SettingsSection(title = "Playback Settings") {
                    SettingsToggleItem(
                        icon = Icons.Default.PlayArrow,
                        title = "Auto Play",
                        subtitle = "Continue playing after current song ends",
                        checked = autoPlayEnabled,
                        onCheckedChange = { SettingsScreenManager.setAutoPlay(it) }
                    )
                    
                    SettingsToggleItem(
                        icon = Icons.Default.Sync,
                        title = "Shuffle by Default",
                        subtitle = "Start playback in shuffle mode",
                        checked = shuffleByDefault,
                        onCheckedChange = { SettingsScreenManager.setShuffleByDefault(it) }
                    )
                    
                    SettingsToggleItem(
                        icon = Icons.Default.MusicNote,
                        title = "Crossfade",
                        subtitle = "Smooth transition between tracks",
                        checked = crossfadeEnabled,
                        onCheckedChange = { SettingsScreenManager.setCrossfade(it) }
                    )
                    
                    if (crossfadeEnabled) {
                        SettingsSliderItem(
                            title = "Crossfade Duration",
                            subtitle = "${crossfadeDuration.toInt()} seconds",
                            value = crossfadeDuration,
                            onValueChange = { crossfadeDuration = it },
                            valueRange = 1f..10f,
                            steps = 8
                        )
                    }
                    
                    SettingsToggleItem(
                        icon = Icons.Default.Lock,
                        title = "Background Play",
                        subtitle = "Continue playing when app is minimized",
                        checked = backgroundPlayEnabled,
                        onCheckedChange = { SettingsScreenManager.setBackgroundPlay(it) }
                    )
                    
                    // New advanced playback options
                    SettingsToggleItem(
                        icon = Icons.Default.Headset,
                        title = "Headphone Detection",
                        subtitle = "Pause when headphones are disconnected",
                        checked = headphoneDetectionEnabled,
                        onCheckedChange = { SettingsScreenManager.setHeadphoneDetection(it) }
                    )
                    
                    var showPlaybackSpeedDialog by remember { mutableStateOf(false) }
                    
                    SettingsItem(
                        icon = Icons.Default.Speed,
                        title = "Playback Speed",
                        subtitle = "${String.format("%.1f", defaultPlaybackSpeed)}x",
                        onClick = { showPlaybackSpeedDialog = true }
                    )
                    
                    if (showPlaybackSpeedDialog) {
                        PlaybackSpeedDialog(
                            currentSpeed = defaultPlaybackSpeed,
                            onDismiss = { showPlaybackSpeedDialog = false },
                            onSpeedSelected = { speed ->
                                SettingsScreenManager.setDefaultPlaybackSpeed(speed)
                                showPlaybackSpeedDialog = false
                            }
                        )
                    }
                    
                    SettingsToggleItem(
                        icon = Icons.Default.SkipNext,
                        title = "Smart Skip",
                        subtitle = "Skip silent parts in audio",
                        checked = smartSkipEnabled,
                        onCheckedChange = { SettingsScreenManager.setSmartSkip(it) }
                    )
                }
            }
            
            // Audio Section
            item {
                SettingsSection(title = "Audio") {
                    SettingsItem(
                        icon = Icons.Default.HighQuality,
                        title = "Audio Quality",
                        subtitle = if (highQualityEnabled) "High Quality" else "Standard",
                        onClick = { showAudioQualityDialog = true }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Equalizer,
                        title = "Equalizer",
                        subtitle = "Adjust sound settings",
                        onClick = { 
                            navController.navigate("equalizer")
                        }
                    )
                    
                    SettingsSliderItem(
                        title = "Master Volume",
                        subtitle = "${(audioVolume * 100).toInt()}%",
                        value = audioVolume,
                        onValueChange = { 
                            audioVolume = it
                            SettingsScreenManager.setMasterVolume(it)
                        },
                        valueRange = 0f..1f
                    )
                    
                    SettingsSliderItem(
                        title = "Buffer Size",
                        subtitle = "${bufferSize.toInt()} MB",
                        value = bufferSize,
                        onValueChange = { 
                            bufferSize = it
                            SettingsScreenManager.setBufferSize(it.toInt())
                        },
                        valueRange = 10f..200f,
                        steps = 18
                    )
                }
            }
            
            // Video Section
            item {
                SettingsSection(title = "Video") {
                    SettingsItem(
                        icon = Icons.Default.VideoFile,
                        title = "Video Quality",
                        subtitle = "Auto (Best Available)",
                        onClick = { showVideoQualityDialog = true }
                    )
                    
                    SettingsToggleItem(
                        icon = Icons.Default.Brightness6,
                        title = "Auto Brightness",
                        subtitle = "Adjust brightness based on content",
                        checked = true,
                        onCheckedChange = { SettingsScreenManager.setAutoBrightness(it) }
                    )
                }
            }
            
            // Notifications Section
            item {
                SettingsSection(title = "Notifications") {
                    SettingsToggleItem(
                        icon = Icons.Default.Notifications,
                        title = "Enable Notifications",
                        subtitle = "Show playback controls in notification",
                        checked = notificationsEnabled,
                        onCheckedChange = { SettingsScreenManager.setNotifications(it) }
                    )
                }
            }
            
            // Enhanced Storage Section
            item {
                SettingsSection(title = "Storage & Data") {
                    SettingsItem(
                        icon = Icons.Default.Storage,
                        title = "Storage Usage",
                        subtitle = "App: 2.3 GB • Cache: 450 MB",
                        onClick = { 
                            navController.navigate("storage_details")
                        }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.DeleteSweep,
                        title = "Clear Cache",
                        subtitle = "Free up storage space",
                        onClick = { SettingsScreenManager.clearCache() }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.CloudSync,
                        title = "Backup & Sync",
                        subtitle = "Sync playlists and preferences",
                        onClick = { 
                            navController.navigate("backup_settings")
                        }
                    )
                    
                    SettingsToggleItem(
                        icon = Icons.Default.Download,
                        title = "Auto Download",
                        subtitle = "Download songs when on Wi-Fi",
                        checked = autoDownloadEnabled,
                        onCheckedChange = { SettingsScreenManager.setAutoDownload(it) }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.FolderOpen,
                        title = "Download Location",
                        subtitle = downloadLocation,
                        onClick = { showDownloadLocationDialog = true }
                    )
                    
                    SettingsSliderItem(
                        title = "Max Cache Size",
                        subtitle = "${bufferSize.toInt()} MB",
                        value = bufferSize,
                        onValueChange = { 
                            bufferSize = it
                            SettingsScreenManager.setBufferSize(it.toInt())
                        },
                        valueRange = 10f..500f,
                        steps = 48
                    )
                }
            }
            
            // Enhanced Privacy & Security Section
            item {
                SettingsSection(title = "Privacy & Security") {
                    SettingsItem(
                        icon = Icons.Default.Security,
                        title = "Privacy Settings",
                        subtitle = "Manage your privacy preferences",
                        onClick = { 
                            navController.navigate("privacy_settings")
                        }
                    )
                    
                    SettingsToggleItem(
                        icon = Icons.Default.Analytics,
                        title = "Usage Analytics",
                        subtitle = "Help improve VPlay by sharing usage data",
                        checked = analyticsEnabled,
                        onCheckedChange = { SettingsScreenManager.setAnalytics(it) }
                    )
                    
                    SettingsToggleItem(
                        icon = Icons.Default.History,
                        title = "Listening History",
                        subtitle = "Track what you listen to",
                        checked = true,
                        onCheckedChange = { /* TODO: Implement history toggle */ }
                    )
                    
                    var showClearDataDialog by remember { mutableStateOf(false) }
                    
                    SettingsItem(
                        icon = Icons.Default.DeleteForever,
                        title = "Clear All Data",
                        subtitle = "Reset app to factory defaults",
                        onClick = { showClearDataDialog = true }
                    )
                    
                    if (showClearDataDialog) {
                        ClearDataDialog(
                            onDismiss = { showClearDataDialog = false },
                            onConfirm = {
                                // Handle clearing all data
                                SettingsScreenManager.clearAllData()
                                showClearDataDialog = false
                            }
                        )
                    }
                    
                }
            }
            
            // Privacy & Data Section
            item {
                SettingsSection(title = "Privacy & Data") {
                    SettingsToggleItem(
                        icon = Icons.Default.LocationOn,
                        title = "Location Services",
                        subtitle = "For local music discovery",
                        checked = locationServicesEnabled,
                        onCheckedChange = { SettingsScreenManager.setLocationServices(it) }
                    )
                    
                    SettingsToggleItem(
                        icon = Icons.Default.History,
                        title = "History Tracking",
                        subtitle = "Save playback history and recommendations",
                        checked = historyTrackingEnabled,
                        onCheckedChange = { SettingsScreenManager.setHistoryTracking(it) }
                    )
                    
                    SettingsToggleItem(
                        icon = Icons.Default.Analytics,
                        title = "Usage Analytics",
                        subtitle = "Help improve the app with anonymous usage data",
                        checked = usageAnalyticsEnabled,
                        onCheckedChange = { SettingsScreenManager.setUsageAnalytics(it) }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Policy,
                        title = "Privacy Policy",
                        subtitle = "View our privacy policy",
                        onClick = { navController.navigate("privacy_policy") }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Security,
                        title = "Data & Privacy Settings",
                        subtitle = "Manage your data preferences",
                        onClick = { navController.navigate("data_privacy") }
                    )
                }
            }
            
            // Enhanced About Section
            item {
                SettingsSection(title = "About & Support") {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "About VPlay",
                        subtitle = "Version 3.0.0 (Build 2025.09.17)",
                        onClick = { showAboutDialog = true }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Update,
                        title = "Check for Updates",
                        subtitle = "You're up to date",
                        onClick = { SettingsScreenManager.checkForUpdates() }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.BugReport,
                        title = "Report a Bug",
                        subtitle = "Help us improve VPlay",
                        onClick = { showBugReportDialog = true }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Feedback,
                        title = "Send Feedback",
                        subtitle = "Share your thoughts and suggestions",
                        onClick = { 
                            navController.navigate("feedback")
                        }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Article,
                        title = "Terms of Service",
                        subtitle = "Read our terms and conditions",
                        onClick = { 
                            navController.navigate("about")
                        }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Policy,
                        title = "Privacy Policy",
                        subtitle = "Learn how we protect your data",
                        onClick = { navController.navigate("privacy_policy") }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Code,
                        title = "Open Source Licenses",
                        subtitle = "View third-party licenses",
                        onClick = { navController.navigate("licenses") }
                    )
                }
            }
            
            // Developer Options Section (hidden by default)
            item {
                SettingsSection(title = "Developer Options") {
                    SettingsToggleItem(
                        icon = Icons.Default.DeveloperMode,
                        title = "Debug Mode",
                        subtitle = "Enable detailed logging",
                        checked = debugModeEnabled,
                        onCheckedChange = { SettingsScreenManager.setDebugMode(it) }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Memory,
                        title = "Performance Monitor",
                        subtitle = "View app performance metrics",
                        onClick = { /* TODO: Performance monitor */ }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Adb,
                        title = "Export Logs",
                        subtitle = "Share debug information",
                        onClick = { /* TODO: Export logs */ }
                    )
                    
                    SettingsToggleItem(
                        icon = Icons.Default.Science,
                        title = "Experimental Features",
                        subtitle = "Enable beta features (may be unstable)",
                        checked = false,
                        onCheckedChange = { /* TODO: Experimental features */ }
                    )
                }
            }
        }
    }
    
    // Theme Selection Dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = if (darkModeEnabled) "Dark" else "Light",
            onThemeSelected = { theme ->
                SettingsScreenManager.setDarkMode(theme == "Dark")
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
    
    // Language Selection Dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = "English",
            onLanguageSelected = { language ->
                SettingsScreenManager.setLanguage(language)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
    
    // Audio Quality Dialog
    if (showAudioQualityDialog) {
        AudioQualityDialog(
            currentQuality = if (highQualityEnabled) "High Quality" else "Standard",
            onQualitySelected = { quality ->
                SettingsScreenManager.setHighQuality(quality == "High Quality")
                showAudioQualityDialog = false
            },
            onDismiss = { showAudioQualityDialog = false }
        )
    }
    
    // Video Quality Dialog
    if (showVideoQualityDialog) {
        VideoQualityDialog(
            currentQuality = "Auto",
            onQualitySelected = { quality ->
                SettingsScreenManager.setVideoQuality(quality)
                showVideoQualityDialog = false
            },
            onDismiss = { showVideoQualityDialog = false }
        )
    }
    
    // About Dialog
    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }
    
    // Download Location Dialog
    if (showDownloadLocationDialog) {
        DownloadLocationDialog(
            currentLocation = downloadLocation,
            onLocationSelected = { location ->
                SettingsScreenManager.setDownloadLocation(location)
                showDownloadLocationDialog = false
            },
            onBrowseClicked = {
                folderPicker.launch(null)
                showDownloadLocationDialog = false
            },
            onDismiss = { showDownloadLocationDialog = false }
        )
    }
    
    // Bug Report Dialog
    if (showBugReportDialog) {
        BugReportDialog(
            onSubmit = { description, email ->
                SettingsScreenManager.submitBugReport(description, email)
                showBugReportDialog = false
            },
            onDismiss = { showBugReportDialog = false }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = false,
                onClick = onClick,
                role = Role.Button
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(20.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(20.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsSliderItem(
    title: String,
    subtitle: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: String,
    onThemeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val themes = listOf(
        "Light" to Icons.Default.LightMode,
        "Dark" to Icons.Default.DarkMode,
        "Auto" to Icons.Default.BrightnessAuto
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Theme") },
        text = {
            Column(
                modifier = Modifier.selectableGroup()
            ) {
                themes.forEach { (theme, icon) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = currentTheme == theme,
                                onClick = { onThemeSelected(theme) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentTheme == theme,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = theme,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val languages = listOf("English", "Spanish", "French", "German", "Italian", "Portuguese")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Language") },
        text = {
            Column(
                modifier = Modifier.selectableGroup()
            ) {
                languages.forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = currentLanguage == language,
                                onClick = { onLanguageSelected(language) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentLanguage == language,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = language,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AudioQualityDialog(
    currentQuality: String,
    onQualitySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val qualities = listOf(
        "Standard" to "128 kbps",
        "High Quality" to "320 kbps",
        "Lossless" to "FLAC/WAV"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Audio Quality") },
        text = {
            Column(
                modifier = Modifier.selectableGroup()
            ) {
                qualities.forEach { (quality, description) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = currentQuality == quality,
                                onClick = { onQualitySelected(quality) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentQuality == quality,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = quality,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun VideoQualityDialog(
    currentQuality: String,
    onQualitySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val qualities = listOf(
        "Auto" to "Best available quality",
        "4K" to "2160p",
        "1080p" to "Full HD",
        "720p" to "HD",
        "480p" to "Standard"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Video Quality") },
        text = {
            Column(
                modifier = Modifier.selectableGroup()
            ) {
                qualities.forEach { (quality, description) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = currentQuality == quality,
                                onClick = { onQualitySelected(quality) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentQuality == quality,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = quality,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("About VPlay") },
        text = {
            Column {
                Text(
                    text = "VPlay Media Player",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Version 3.0.0",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "A modern, Material 3 media player built with Jetpack Compose. Enjoy your music and videos with a beautiful, intuitive interface.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "© 2025 ByteCoder. All rights reserved.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
private fun FontSizeDialog(
    onDismiss: () -> Unit,
    onSizeSelected: (String) -> Unit
) {
    val fontSizes = listOf("Small", "Medium", "Large", "Extra Large")
    var selectedSize by remember { mutableStateOf("Medium") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Font Size")
        },
        text = {
            Column {
                fontSizes.forEach { size ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSize == size,
                            onClick = { selectedSize = size }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = size,
                            style = when (size) {
                                "Small" -> MaterialTheme.typography.bodySmall
                                "Medium" -> MaterialTheme.typography.bodyMedium
                                "Large" -> MaterialTheme.typography.bodyLarge
                                "Extra Large" -> MaterialTheme.typography.headlineSmall
                                else -> MaterialTheme.typography.bodyMedium
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSizeSelected(selectedSize) }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PlaybackSpeedDialog(
    currentSpeed: Float,
    onDismiss: () -> Unit,
    onSpeedSelected: (Float) -> Unit
) {
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
    var selectedSpeed by remember { mutableStateOf(currentSpeed) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Playback Speed")
        },
        text = {
            Column {
                speeds.forEach { speed ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSpeed == speed,
                            onClick = { selectedSpeed = speed }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${String.format("%.2f", speed)}x",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (speed == 1.0f) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(Normal)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSpeedSelected(selectedSpeed) }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ClearDataDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Clear All Data")
        },
        text = {
            Column {
                Text(
                    text = "This will permanently delete:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                listOf(
                    "• All playlists and favorites",
                    "• Listening history",
                    "• Downloaded content",
                    "• App settings and preferences",
                    "• Cache and temporary files"
                ).forEach { item ->
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DownloadLocationDialog(
    currentLocation: String,
    onLocationSelected: (String) -> Unit,
    onBrowseClicked: () -> Unit,
    onDismiss: () -> Unit
) {
    val predefinedLocations = listOf(
        "Internal Storage/VPlay",
        "Internal Storage/Music",
        "Internal Storage/Downloads",
        "SD Card/VPlay" // If available
    )
    
    var selectedLocation by remember { mutableStateOf(currentLocation) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Download Location",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Choose where to save downloaded media files:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Predefined locations
                predefinedLocations.forEach { location ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedLocation == location,
                                onClick = { selectedLocation = location },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedLocation == location,
                            onClick = { selectedLocation = location }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = location.substringAfterLast('/'),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Browse for custom location
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onBrowseClicked,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Browse for custom location...")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onLocationSelected(selectedLocation) }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun BugReportDialog(
    onSubmit: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var description by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Report a Bug",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Help us improve VPlay by reporting any issues you've encountered:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Describe the issue") },
                    placeholder = { Text("Please provide details about the bug...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (optional)") },
                    placeholder = { Text("your.email@example.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "We'll automatically include device and app information to help diagnose the issue.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    if (description.isNotBlank()) {
                        onSubmit(description, email)
                    }
                },
                enabled = description.isNotBlank()
            ) {
                Text("Submit Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


