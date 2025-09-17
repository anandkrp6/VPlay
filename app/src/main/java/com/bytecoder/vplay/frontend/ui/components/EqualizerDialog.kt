package com.bytecoder.vplay.frontend.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

data class EqualizerBand(
    val frequency: String,
    val gain: Float // -15 to 15 dB
)

@Composable
fun EqualizerDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    // Standard 10-band equalizer frequencies
    var bands by remember {
        mutableStateOf(
            listOf(
                EqualizerBand("32", 0f),
                EqualizerBand("64", 0f),
                EqualizerBand("125", 0f),
                EqualizerBand("250", 0f),
                EqualizerBand("500", 0f),
                EqualizerBand("1K", 0f),
                EqualizerBand("2K", 0f),
                EqualizerBand("4K", 0f),
                EqualizerBand("8K", 0f),
                EqualizerBand("16K", 0f)
            )
        )
    }
    
    var selectedPreset by remember { mutableStateOf("Custom") }
    
    val presets = mapOf(
        "Flat" to listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
        "Bass Boost" to listOf(6f, 4f, 2f, 0f, -1f, -2f, -1f, 0f, 1f, 2f),
        "Treble Boost" to listOf(-2f, -1f, 0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f),
        "Vocal" to listOf(-2f, -1f, 1f, 3f, 4f, 4f, 3f, 1f, 0f, -1f),
        "Rock" to listOf(3f, 2f, -1f, -2f, 1f, 2f, 3f, 3f, 2f, 2f),
        "Pop" to listOf(1f, 2f, 3f, 2f, 0f, -1f, -1f, 1f, 2f, 3f),
        "Classical" to listOf(3f, 2f, 1f, 0f, -1f, -1f, 0f, 1f, 2f, 3f),
        "Jazz" to listOf(2f, 1f, 0f, 1f, 2f, 2f, 1f, 1f, 2f, 3f)
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Equalizer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Equalizer")
            }
        },
        text = {
            Column {
                // Preset selection
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
                            text = "Presets",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            presets.keys.forEach { preset ->
                                FilterChip(
                                    selected = selectedPreset == preset,
                                    onClick = {
                                        selectedPreset = preset
                                        val gains = presets[preset] ?: return@FilterChip
                                        bands = bands.mapIndexed { index, band ->
                                            band.copy(gain = gains[index])
                                        }
                                    },
                                    label = { Text(preset, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Equalizer bands
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
                            text = "Frequency Bands",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Gain indicators
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("+15", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            Text("0", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            Text("-15", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Equalizer sliders
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            bands.forEachIndexed { index, band ->
                                EqualizerSlider(
                                    frequency = band.frequency,
                                    gain = band.gain,
                                    onGainChange = { newGain ->
                                        bands = bands.toMutableList().apply {
                                            this[index] = band.copy(gain = newGain)
                                        }
                                        selectedPreset = "Custom"
                                    }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Reset button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OutlinedButton(
                                onClick = {
                                    bands = bands.map { it.copy(gain = 0f) }
                                    selectedPreset = "Flat"
                                }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // TODO: Apply equalizer settings to audio engine
                    onDismiss()
                }
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
private fun EqualizerSlider(
    frequency: String,
    gain: Float,
    onGainChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(24.dp)
    ) {
        // Gain value
        Text(
            text = "${gain.roundToInt()}",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Vertical slider
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(120.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            val sliderHeight = 120.dp
            val normalizedValue = (gain + 15f) / 30f // Convert -15 to 15 range to 0 to 1
            val thumbPosition = (1f - normalizedValue) * 120 // Invert for top-to-bottom
            
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            val currentY = thumbPosition + dragAmount.y
                            val normalizedY = (currentY / 120f).coerceIn(0f, 1f)
                            val newGain = (1f - normalizedY) * 30f - 15f
                            onGainChange(newGain.coerceIn(-15f, 15f))
                        }
                    }
            ) {
                // Draw slider track
                drawLine(
                    color = Color.Gray,
                    start = Offset(size.width / 2, 0f),
                    end = Offset(size.width / 2, size.height),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
                
                // Draw active track
                val activeEnd = thumbPosition.coerceIn(0f, 120f) * density
                drawLine(
                    color = Color.Blue,
                    start = Offset(size.width / 2, size.height / 2),
                    end = Offset(size.width / 2, activeEnd),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
                
                // Draw thumb
                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx(),
                    center = Offset(size.width / 2, activeEnd)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Frequency label
        Text(
            text = frequency,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    // Simple implementation for demo - would normally use FlowRow from Accompanist
    Column(modifier = modifier) {
        content()
    }
}