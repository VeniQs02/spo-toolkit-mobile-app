package com.example.spotoolkit.ui.Login

import MainViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.spotoolkit.R
import com.example.spotoolkit.data.AuthState
import androidx.compose.ui.platform.LocalContext

@Composable
fun LoginScreen(vm: MainViewModel) {
    val context = LocalContext.current

    val authState by vm.authState.collectAsState()
    val enabled = authState != AuthState.Loading

    val text = when (authState) {
        AuthState.Authenticated -> R.string.already_logged_in
        AuthState.Loading -> R.string.logging_in
        else -> R.string.login_with_spotify
    }

    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Button(enabled = enabled, onClick = {
            val intent = vm.buildSpotifyAuthIntent()
            context.startActivity(intent)
        }) {
            Text(stringResource(text))
        }
    }
}

