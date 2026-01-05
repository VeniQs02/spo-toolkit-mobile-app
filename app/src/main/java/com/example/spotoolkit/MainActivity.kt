package com.example.spotoolkit

import MainViewModel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels



class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntent(intent)
        setContent { AppRoot(vm) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val uri = intent.data
        if (uri != null && uri.toString().startsWith("spotoolkit://callback")) {
            val code = uri.getQueryParameter("code")
            val error = uri.getQueryParameter("error")
            Log.d("PKCE", "Error: $error")
            code?.let { vm.handleAuthCode(it) }
        }
    }
}
