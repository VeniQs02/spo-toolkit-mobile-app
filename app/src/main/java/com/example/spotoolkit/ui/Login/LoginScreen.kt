package com.example.spotoolkit.ui.Login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    buttonText: String,
    isEnabled: Boolean
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = onLoginClick, enabled = isEnabled) {
            Text(text = buttonText)
        }
    }
}
