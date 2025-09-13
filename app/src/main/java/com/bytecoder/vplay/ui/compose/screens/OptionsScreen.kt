package com.bytecoder.vplay.ui.compose.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun OptionsScreen(
    navController: NavController
) {
    // Options screen is essentially the settings screen
    SettingsScreen(navController = navController)
}