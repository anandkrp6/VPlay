package com.bytecoder.vplay.frontend.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun OptionsScreen(
    navController: NavController
) {
    // Options screen is essentially the settings screen
    SettingsScreen(navController = navController)
}