package com.example.spotoolkit.ui.Profile

import MainViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.spotoolkit.R
import com.example.spotoolkit.util.AuthState

@Composable
fun ProfileScreen(vm: MainViewModel) {
    val authStatus = vm.authState.collectAsState().value
    when(authStatus){
        AuthState.Authenticated -> {
            UserDataList()
        }
        else -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.login_with_spotify))
            }
        }
    }
}

@Composable
fun UserDataList(){

}
