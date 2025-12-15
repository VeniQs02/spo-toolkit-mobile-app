package com.example.spotoolkit.util

sealed class AuthState {
    data object Unauthenticated : AuthState()
    data object Loading : AuthState()
    data object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
