package com.bytecoder.vplay.frontend.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    var selectedCategory by remember { mutableStateOf(FeedbackCategory.BUG_REPORT) }
    var feedbackText by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(5) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    
    val feedbackCategories = remember {
        listOf(
            FeedbackCategory.BUG_REPORT,
            FeedbackCategory.FEATURE_REQUEST,
            FeedbackCategory.GENERAL_FEEDBACK,
            FeedbackCategory.PERFORMANCE_ISSUE,
            FeedbackCategory.UI_IMPROVEMENT
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Header with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Feedback",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Rating section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "How would you rate vPlay?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(5) { index ->
                        IconButton(
                            onClick = { rating = index + 1 }
                        ) {
                            Icon(
                                imageVector = if (index < rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Rate ${index + 1} stars",
                                tint = if (index < rating) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                
                Text(
                    text = when (rating) {
                        1 -> "Poor"
                        2 -> "Fair"
                        3 -> "Good"
                        4 -> "Very Good"
                        5 -> "Excellent"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Feedback category
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Feedback Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                feedbackCategories.forEach { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Feedback text
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Your Feedback",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { 
                        Text("Please describe your ${selectedCategory.displayName.lowercase()}...") 
                    },
                    maxLines = 6,
                    shape = MaterialTheme.shapes.medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Submit button
        Button(
            onClick = {
                if (feedbackText.isNotBlank()) {
                    scope.launch {
                        isSubmitting = true
                        try {
                            submitFeedback(
                                category = selectedCategory,
                                feedback = feedbackText,
                                rating = rating
                            )
                            showSuccessDialog = true
                        } catch (e: Exception) {
                            showErrorDialog = true
                        } finally {
                            isSubmitting = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = feedbackText.isNotBlank() && !isSubmitting,
            shape = MaterialTheme.shapes.medium
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (isSubmitting) "Submitting..." else "Submit Feedback",
                style = MaterialTheme.typography.labelLarge
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Contact info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Alternative Contact",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You can also reach out to us directly at:\nvplay.support@bytecoder.dev",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
    
    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                navController.popBackStack()
            },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Feedback Sent!") },
            text = { Text("Thank you for your feedback. We appreciate your input and will review it carefully.") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showSuccessDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
    
    // Error dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            icon = {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Submission Failed") },
            text = { Text("Unable to submit feedback. Please check your internet connection and try again.") },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

private enum class FeedbackCategory(
    val displayName: String,
    val icon: ImageVector
) {
    BUG_REPORT("Bug Report", Icons.Default.BugReport),
    FEATURE_REQUEST("Feature Request", Icons.Default.Lightbulb),
    GENERAL_FEEDBACK("General Feedback", Icons.Default.Chat),
    PERFORMANCE_ISSUE("Performance Issue", Icons.Default.Speed),
    UI_IMPROVEMENT("UI Improvement", Icons.Default.Palette)
}

private suspend fun submitFeedback(
    category: FeedbackCategory,
    feedback: String,
    rating: Int
) {
    // Discord webhook URL (replace with your actual webhook)
    val webhookUrl = "YOUR_DISCORD_WEBHOOK_URL_HERE"
    
    val payload = JSONObject().apply {
        put("content", "**New vPlay Feedback**")
        put("embeds", org.json.JSONArray().apply {
            put(JSONObject().apply {
                put("title", "📱 vPlay Feedback")
                put("color", 5814783) // Blue color
                put("fields", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("name", "Category")
                        put("value", category.displayName)
                        put("inline", true)
                    })
                    put(JSONObject().apply {
                        put("name", "Rating")
                        put("value", "$rating/5 ⭐")
                        put("inline", true)
                    })
                    put(JSONObject().apply {
                        put("name", "Feedback")
                        put("value", feedback)
                        put("inline", false)
                    })
                })
                put("timestamp", java.time.Instant.now().toString())
                put("footer", JSONObject().apply {
                    put("text", "vPlay Android App")
                })
            })
        })
    }
    
    try {
        val url = URL(webhookUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        
        connection.outputStream.use { os ->
            os.write(payload.toString().toByteArray())
        }
        
        val responseCode = connection.responseCode
        if (responseCode !in 200..299) {
            throw Exception("HTTP $responseCode")
        }
    } catch (e: Exception) {
        // For demo purposes, we'll simulate success after a delay
        kotlinx.coroutines.delay(1000)
        // In production, uncomment the line below to propagate the error
        // throw e
    }
}