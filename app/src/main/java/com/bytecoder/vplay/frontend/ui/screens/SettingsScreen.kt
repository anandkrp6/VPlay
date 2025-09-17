package com.bytecoder.vplay.frontend.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.bytecoder.vplay.frontend.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // Settings state
    val darkModeEnabled by settingsViewModel.darkModeEnabled.collectAsState()
    val dynamicThemeEnabled by settingsViewModel.dynamicThemeEnabled.collectAsState()
    val autoPlayEnabled by settingsViewModel.autoPlayEnabled.collectAsState()
    val shuffleByDefault by settingsViewModel.shuffleByDefault.collectAsState()
    val crossfadeEnabled by settingsViewModel.crossfadeEnabled.collectAsState()
    val highQualityEnabled by settingsViewModel.highQualityEnabled.collectAsState()
    val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsState()
    val backgroundPlayEnabled by settingsViewModel.backgroundPlayEnabled.collectAsState()
    
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showAudioQualityDialog by remember { mutableStateOf(false) }
    var showVideoQualityDialog by remember { mutableStateOf(false) }
    
    var audioVolume by remember { mutableFloatStateOf(0.8f) }
    var crossfadeDuration by remember { mutableFloatStateOf(3f) }
    var bufferSize by remember { mutableFloatStateOf(50f) }

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
            // Appearance Section
            item {
                SettingsSection(title = "Appearance") {
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
                        onCheckedChange = { settingsViewModel.setDynamicTheme(it) }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Language,
                        title = "Language",
                        subtitle = "English",
                        onClick = { showLanguageDialog = true }
                    )
                }
            }
            
            // Playback Section
            item {
                SettingsSection(title = "Playback") {
                    SettingsToggleItem(
                        icon = Icons.Default.PlayArrow,
                        title = "Auto Play",
                        subtitle = "Continue playing after current song ends",
                        checked = autoPlayEnabled,
                        onCheckedChange = { settingsViewModel.setAutoPlay(it) }
                    )
                    
                    SettingsToggleItem(
                        icon = Icons.Default.Sync,
                        title = "Shuffle by Default",
                        subtitle = "Start playback in shuffle mode",
                        checked = shuffleByDefault,
                        onCheckedChange = { settingsViewModel.setShuffleByDefault(it) }
                    )
                    
                    SettingsToggleItem(
                        icon = Icons.Default.MusicNote,
                        title = "Crossfade",
                        subtitle = "Smooth transition between tracks",
                        checked = crossfadeEnabled,
                        onCheckedChange = { settingsViewModel.setCrossfade(it) }
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
                        onCheckedChange = { settingsViewModel.setBackgroundPlay(it) }
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
                        onClick = { /* TODO: Navigate to equalizer */ }
                    )
                    
                    SettingsSliderItem(
                        title = "Master Volume",
                        subtitle = "${(audioVolume * 100).toInt()}%",
                        value = audioVolume,
                        onValueChange = { 
                            audioVolume = it
                            settingsViewModel.setMasterVolume(it)
                        },
                        valueRange = 0f..1f
                    )
                    
                    SettingsSliderItem(
                        title = "Buffer Size",
                        subtitle = "${bufferSize.toInt()} MB",
                        value = bufferSize,
                        onValueChange = { 
                            bufferSize = it
                            settingsViewModel.setBufferSize(it.toInt())
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
                        onCheckedChange = { settingsViewModel.setAutoBrightness(it) }
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
                        onCheckedChange = { settingsViewModel.setNotifications(it) }
                    )
                }
            }
            
            // Storage Section
            item {
                SettingsSection(title = "Storage & Cache") {
                    SettingsItem(
                        icon = Icons.Default.Storage,
                        title = "Clear Cache",
                        subtitle = "Free up storage space",
                        onClick = { settingsViewModel.clearCache() }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Storage,
                        title = "Storage Usage",
                        subtitle = "View app storage details",
                        onClick = { /* TODO: Navigate to storage details */ }
                    )
                }
            }
            
            // Privacy & Security Section
            item {
                SettingsSection(title = "Privacy & Security") {
                    SettingsItem(
                        icon = Icons.Default.Security,
                        title = "Privacy Settings",
                        subtitle = "Manage your privacy preferences",
                        onClick = { /* TODO: Navigate to privacy settings */ }
                    )
                }
            }
            
            // About Section
            item {
                SettingsSection(title = "About") {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "About VPlay",
                        subtitle = "Version 3.0.0",
                        onClick = { showAboutDialog = true }
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
                settingsViewModel.setDarkMode(theme == "Dark")
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
                settingsViewModel.setLanguage(language)
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
                settingsViewModel.setHighQuality(quality == "High Quality")
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
                settingsViewModel.setVideoQuality(quality)
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
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
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
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
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