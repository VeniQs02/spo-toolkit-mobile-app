package com.example.spotoolkit

import MainViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spotoolkit.ui.MainScreen.MainScreen
import com.example.spotoolkit.ui.SearchScreen.SearchScreen


class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm.loadToken()

        setContent {
            val vm: MainViewModel = viewModel()
            SearchScreen(vm)
        }
    }
}
