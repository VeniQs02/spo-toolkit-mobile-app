package com.example.spotoolkit.ui.main

import MainViewModel
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.example.spotoolkit.ui.BottomDest
import com.example.spotoolkit.ui.Playlists.PlaylistsScreen
import com.example.spotoolkit.ui.Profile.ProfileScreen
import com.example.spotoolkit.ui.Search.SearchScreen


@Composable
fun MainScreen(vm: MainViewModel) {
    val navController = rememberNavController()

    val authState by vm.authState.collectAsState()

    LaunchedEffect(authState) { Log.d("PKCE", "Auth state changed: $authState") }

    val items = listOf(
        BottomDest.Search,
        BottomDest.Playlists,
        BottomDest.Profile
    )

    Scaffold(
        bottomBar = {
            Box(modifier = Modifier.height(56.dp)) {
                NavigationBar {
                    val backStack by navController.currentBackStackEntryAsState()
                    val currentRoute = backStack?.destination?.route

                    items.forEach { dest ->
                        NavigationBarItem(
                            selected = currentRoute == dest.route,
                            onClick = { navController.navigate(dest.route) { launchSingleTop = true } },
                            icon = { Icon(dest.icon, contentDescription = null) },
                            label = { Text(stringResource(dest.label)) },
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomDest.Search.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomDest.Search.route) { SearchScreen(vm) }
            composable(BottomDest.Playlists.route) { PlaylistsScreen() }
            composable(BottomDest.Profile.route) { ProfileScreen(vm) }
        }
    }
}