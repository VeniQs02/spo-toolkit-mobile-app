package com.example.spotoolkit.data

data class TokenBundle(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long
)
