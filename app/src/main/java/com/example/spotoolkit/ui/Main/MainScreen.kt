package com.example.spotoolkit.ui.main

import MainViewModel
import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.spotoolkit.R
import com.example.spotoolkit.ui.BottomDest
import com.example.spotoolkit.ui.Login.LoginScreen
import com.example.spotoolkit.ui.Playlists.PlaylistsScreen
import com.example.spotoolkit.ui.Profile.ProfileScreen
import com.example.spotoolkit.ui.Search.SearchScreen
import com.example.spotoolkit.util.AuthState


@Composable
fun MainScreen(vm: MainViewModel) {
    val navController = rememberNavController()
    vm.loadToken()

    val authState by vm.authState.collectAsState()

    LaunchedEffect(authState) { Log.d("PKCE", "Auth state changed: $authState") }

    val items = listOf(
        BottomDest.Login,
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
            startDestination = BottomDest.Login.route,
            modifier = Modifier.padding(padding)
        ) {

            val buttonText = if(authState == AuthState.Authenticated){
                R.string.logged_in_button
            } else {
                R.string.login_button
            }

            composable(BottomDest.Login.route) {
                val activity = LocalContext.current as? Activity
                if (activity != null) {
                    LoginScreen(
                        {vm.startSpotifyAuth(activity)},
                        stringResource(buttonText),
                        authState != AuthState.Authenticated
                    )
                }
            }
            composable(BottomDest.Search.route) { SearchScreen(vm) }
            composable(BottomDest.Playlists.route) { PlaylistsScreen() }
            composable(BottomDest.Profile.route) { ProfileScreen() }
        }
    }
}