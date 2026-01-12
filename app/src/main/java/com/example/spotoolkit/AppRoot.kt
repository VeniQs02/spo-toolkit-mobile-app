package com.example.spotoolkit

import MainViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.spotoolkit.ui.Login.LoginScreen
import com.example.spotoolkit.ui.main.MainScreen
import com.example.spotoolkit.util.AuthState

@Composable
fun AppRoot(vm: MainViewModel) {
    val authState by vm.authState.collectAsState()

    when (authState) {
        AuthState.Unauthenticated, AuthState.Loading -> LoginScreen(vm)

        AuthState.Authenticated -> MainScreen(vm)

        is AuthState.Error -> LoginScreen(vm)
    }
}