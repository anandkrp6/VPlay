package com.bytecoder.vplay.frontend.ui.screens.navscreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@Composable
fun OptionsScreen(
    navController: NavController
) {
    val optionItems = remember {
        listOf(
            OptionItem(
                title = "History",
                description = "View recently played media",
                icon = Icons.Default.History,
                route = "history"
            ),
            OptionItem(
                title = "Downloads",
                description = "Manage downloaded content",
                icon = Icons.Default.Download,
                route = "downloads"
            ),
            OptionItem(
                title = "Permissions",
                description = "App permissions and privacy",
                icon = Icons.Default.Security,
                route = "permissions"
            ),
            OptionItem(
                title = "File Explorer",
                description = "Browse device storage",
                icon = Icons.Default.Folder,
                route = "file_explorer"
            ),
            OptionItem(
                title = "Settings",
                description = "App preferences and configuration",
                icon = Icons.Default.Settings,
                route = "settings"
            ),
            OptionItem(
                title = "Feedback",
                description = "Send feedback and report bugs",
                icon = Icons.Default.Feedback,
                route = "feedback"
            ),
            OptionItem(
                title = "About",
                description = "App information and credits",
                icon = Icons.Default.Info,
                route = "about"
            ),
            OptionItem(
                title = "Tips",
                description = "Learn how to use vPlay",
                icon = Icons.Default.Lightbulb,
                route = "tips"
            )
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Options",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Options list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(optionItems) { item ->
                OptionCard(
                    option = item,
                    onClick = {
                        // Navigate to specific option screen
                        when (item.route) {
                            "settings" -> navController.navigate("settings")
                            "feedback" -> navController.navigate("feedback")
                            "about" -> navController.navigate("about")
                            "tips" -> navController.navigate("tips")
                            "history" -> navController.navigate("history")
                            "downloads" -> navController.navigate("downloads")
                            "permissions" -> navController.navigate("permissions")
                            "file_explorer" -> navController.navigate("file_explorer")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptionCard(
    option: OptionItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Card(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = option.title,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            // Arrow indicator
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private data class OptionItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String
)


