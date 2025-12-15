package com.example.spotoolkit


import MainViewModel
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spotoolkit.ui.ErrorScreen.ErrorScreen
import com.example.spotoolkit.ui.LoadingScreen.LoadingScreen
import com.example.spotoolkit.ui.LoginScreen.LoginScreen
import com.example.spotoolkit.ui.MainScreen.MainScreen
import com.example.spotoolkit.util.AuthState

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val vm: MainViewModel = viewModel()
            val authState by vm.authState.collectAsState()

            when (authState) {
                is AuthState.Unauthenticated -> LoginScreen { vm.startSpotifyAuth(this) }
                is AuthState.Loading -> LoadingScreen()
                is AuthState.Authenticated -> MainScreen()
                is AuthState.Error -> ErrorScreen((authState as AuthState.Error).message)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val code = intent.data?.getQueryParameter("code") ?: return

        vm.handleAuthCode(code)
    }
}
