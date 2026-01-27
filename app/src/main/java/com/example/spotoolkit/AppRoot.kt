package com.example.spotoolkit

import MainViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.colorResource
import com.example.spotoolkit.ui.Login.LoginScreen
import com.example.spotoolkit.ui.main.MainScreen
import com.example.spotoolkit.data.AuthState

@Composable
fun AppRoot(vm: MainViewModel) {
    val DarkColorScheme = darkColorScheme(
        primary = colorResource(R.color.primary),
        secondary = colorResource(R.color.secondary),
        background = colorResource(R.color.background),
        surface = colorResource(R.color.surface),
        onPrimary = colorResource(R.color.on_primary),
        onSecondary = colorResource(R.color.on_secondary),
        onBackground = colorResource(R.color.on_primary),
        onSurface = colorResource(R.color.on_primary),
        error = colorResource(R.color.error),
    )

    @Composable
    fun SpoToolkitTheme(content: @Composable () -> Unit) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            content = content
        )
    }

    SpoToolkitTheme {
        val authState by vm.authState.collectAsState()

    when (authState) {
        AuthState.Unauthenticated, AuthState.Loading -> LoginScreen(vm)

            AuthState.Authenticated -> MainScreen(vm)

            is AuthState.Error -> LoginScreen(vm)
        }
    }
}

