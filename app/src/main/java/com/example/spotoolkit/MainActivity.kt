package com.example.spotoolkit

import MainViewModel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.spotoolkit.ui.main.MainScreen


class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        setContent { MainScreen(vm) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val uri = intent.data
        if (uri != null && uri.toString().startsWith("spotoolkit://callback")) {
            Log.d("PKCE", "Redirect received: $uri")
            val code = uri.getQueryParameter("code")
            val error = uri.getQueryParameter("error")
            Log.d("PKCE", "Authorization code: $code")
            Log.d("PKCE", "Error: $error")
            code?.let { vm.handleAuthCode(it) }
        }
    }
}
