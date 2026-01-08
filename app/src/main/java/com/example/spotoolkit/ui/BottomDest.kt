package com.example.spotoolkit.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.spotoolkit.R

sealed class BottomDest(
    val route: String,
    val label: Int,
    val icon: ImageVector
) {
    object Search : BottomDest("search", R.string.title_search, Icons.Default.Search)
    object Playlists : BottomDest("playlists", R.string.title_playlists, Icons.Default.Star)
    object UserProfile : BottomDest("userProfile", R.string.title_user_profile, Icons.Default.Create)
}