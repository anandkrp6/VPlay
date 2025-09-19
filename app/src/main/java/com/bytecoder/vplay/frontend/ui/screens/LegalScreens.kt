package com.bytecoder.vplay.frontend.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "VPlay Privacy Policy",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            item {
                Text(
                    text = "Last updated: September 17, 2025",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            item {
                PrivacySection(
                    title = "Information We Collect",
                    content = """
                        • Usage analytics to improve app performance
                        • Crash reports for debugging purposes
                        • Device information for compatibility
                        • Media files accessed only locally on your device
                        • Optional location data for local music discovery
                    """.trimIndent()
                )
            }
            
            item {
                PrivacySection(
                    title = "How We Use Your Information",
                    content = """
                        • Improve app functionality and user experience
                        • Provide personalized music recommendations
                        • Fix bugs and optimize performance
                        • Ensure app compatibility across devices
                    """.trimIndent()
                )
            }
            
            item {
                PrivacySection(
                    title = "Data Storage and Security",
                    content = """
                        • All media files remain on your device
                        • Settings and preferences stored locally
                        • Optional cloud sync with your consent
                        • Industry-standard encryption for data protection
                    """.trimIndent()
                )
            }
            
            item {
                PrivacySection(
                    title = "Third-Party Services",
                    content = """
                        • Analytics providers (Google Analytics)
                        • Crash reporting services (Firebase Crashlytics)
                        • Music metadata services (MusicBrainz, Last.fm)
                        • All services comply with privacy regulations
                    """.trimIndent()
                )
            }
            
            item {
                PrivacySection(
                    title = "Your Rights",
                    content = """
                        • Request data deletion at any time
                        • Opt-out of analytics and tracking
                        • Control location data usage
                        • Export your data and settings
                    """.trimIndent()
                )
            }
            
            item {
                PrivacySection(
                    title = "Contact Us",
                    content = """
                        If you have questions about this privacy policy, please contact us at:
                        
                        Email: privacy@bytecoder.com
                        Website: https://bytecoder.com/vplay/privacy
                        
                        ByteCoder Technologies
                        Privacy Officer
                    """.trimIndent()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataPrivacyScreen(navController: NavController) {
    var analyticsEnabled by remember { mutableStateOf(true) }
    var crashReportsEnabled by remember { mutableStateOf(true) }
    var locationDataEnabled by remember { mutableStateOf(false) }
    var personalizationEnabled by remember { mutableStateOf(true) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data & Privacy Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Manage Your Privacy",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Control how VPlay handles your data and protects your privacy.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Analytics & Usage Data",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        PrivacyToggleItem(
                            title = "Usage Analytics",
                            description = "Help improve VPlay by sharing anonymous usage statistics",
                            checked = analyticsEnabled,
                            onCheckedChange = { analyticsEnabled = it }
                        )
                        
                        PrivacyToggleItem(
                            title = "Crash Reports",
                            description = "Automatically send crash reports to help fix bugs",
                            checked = crashReportsEnabled,
                            onCheckedChange = { crashReportsEnabled = it }
                        )
                    }
                }
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Location & Personalization",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        PrivacyToggleItem(
                            title = "Location Data",
                            description = "Use location for local music discovery and recommendations",
                            checked = locationDataEnabled,
                            onCheckedChange = { locationDataEnabled = it }
                        )
                        
                        PrivacyToggleItem(
                            title = "Personalization",
                            description = "Customize recommendations based on your listening habits",
                            checked = personalizationEnabled,
                            onCheckedChange = { personalizationEnabled = it }
                        )
                    }
                }
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Data Management",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { /* TODO: Export data */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Export My Data")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedButton(
                            onClick = { /* TODO: Delete data */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Delete All My Data")
                        }
                    }
                }
            }
            
            item {
                Text(
                    text = "Changes to these settings take effect immediately. For more information, see our Privacy Policy.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(navController: NavController) {
    val licenses = remember {
        listOf(
            LicenseInfo(
                "ExoPlayer",
                "Google Inc.",
                "Apache License 2.0",
                "Media playback library for Android"
            ),
            LicenseInfo(
                "Jetpack Compose",
                "Google Inc.",
                "Apache License 2.0",
                "Modern UI toolkit for Android"
            ),
            LicenseInfo(
                "Material Design 3",
                "Google Inc.",
                "Apache License 2.0",
                "Material Design components and theming"
            ),
            LicenseInfo(
                "OkHttp",
                "Square, Inc.",
                "Apache License 2.0",
                "HTTP client for networking"
            ),
            LicenseInfo(
                "Retrofit",
                "Square, Inc.",
                "Apache License 2.0",
                "Type-safe HTTP client for Android"
            ),
            LicenseInfo(
                "Glide",
                "Bump Technologies",
                "BSD, part MIT and Apache 2.0",
                "Image loading and caching library"
            ),
            LicenseInfo(
                "Room Database",
                "Google Inc.",
                "Apache License 2.0",
                "SQLite object mapping library"
            ),
            LicenseInfo(
                "Kotlin Coroutines",
                "JetBrains",
                "Apache License 2.0",
                "Asynchronous programming library"
            )
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Open Source Licenses") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "VPlay uses the following open source libraries:",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            items(licenses) { license ->
                LicenseCard(license = license)
            }
            
            item {
                Text(
                    text = "We thank all the open source contributors who make VPlay possible.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun PrivacySection(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

@Composable
private fun PrivacyToggleItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
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
private fun LicenseCard(license: LicenseInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = license.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "by ${license.author}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = license.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = license.license,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private data class LicenseInfo(
    val name: String,
    val author: String,
    val license: String,
    val description: String
)


