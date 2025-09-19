package com.bytecoder.vplay.frontend.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bytecoder.vplay.backend.services.SleepTimerService

@Composable
fun SleepTimerDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isRunning by SleepTimerService.isRunning.collectAsStateWithLifecycle()
    val remainingTime by SleepTimerService.remainingTime.collectAsStateWithLifecycle()
    
    var selectedDuration by remember { mutableStateOf(30) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Bedtime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sleep Timer")
            }
        },
        text = {
            Column {
                if (isRunning) {
                    // Show current timer status
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Timer Active",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            val minutes = (remainingTime / 1000 / 60).toInt()
                            val seconds = ((remainingTime / 1000) % 60).toInt()
                            Text(
                                text = String.format("%d:%02d remaining", minutes, seconds),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        SleepTimerService.extendSleepTimer(context, 15)
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("15 min")
                                }
                                
                                OutlinedButton(
                                    onClick = {
                                        SleepTimerService.stopSleepTimer(context)
                                        onDismiss()
                                    }
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Stop")
                                }
                            }
                        }
                    }
                } else {
                    // Show timer setup options
                    Text(
                        text = "Music will stop playing after the selected time:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    val durations = listOf(15, 30, 45, 60, 90, 120)
                    
                    durations.forEach { duration ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedDuration == duration,
                                    onClick = { selectedDuration = duration },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedDuration == duration,
                                onClick = null
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = when (duration) {
                                    15 -> "15 minutes"
                                    30 -> "30 minutes"
                                    45 -> "45 minutes"
                                    60 -> "1 hour"
                                    90 -> "1 hour 30 minutes"
                                    120 -> "2 hours"
                                    else -> "$duration minutes"
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isRunning) {
                TextButton(
                    onClick = {
                        SleepTimerService.startSleepTimer(context, selectedDuration)
                        onDismiss()
                    }
                ) {
                    Text("Start Timer")
                }
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
fun SleepTimerButton(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isRunning by SleepTimerService.isRunning.collectAsStateWithLifecycle()
    val remainingTime by SleepTimerService.remainingTime.collectAsStateWithLifecycle()
    
    var showDialog by remember { mutableStateOf(false) }
    
    IconButton(
        onClick = { showDialog = true },
        modifier = modifier
    ) {
        if (isRunning) {
            Badge(
                contentColor = MaterialTheme.colorScheme.onError,
                containerColor = MaterialTheme.colorScheme.error
            ) {
                Icon(
                    Icons.Default.Bedtime,
                    contentDescription = "Sleep Timer Active",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            Icon(
                Icons.Default.Bedtime,
                contentDescription = "Sleep Timer",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
    
    if (showDialog) {
        SleepTimerDialog(
            onDismiss = { showDialog = false }
        )
    }
}


