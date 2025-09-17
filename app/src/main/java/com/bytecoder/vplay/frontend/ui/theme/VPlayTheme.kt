package com.bytecoder.vplay.frontend.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun VPlayTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        content = content
    )
}