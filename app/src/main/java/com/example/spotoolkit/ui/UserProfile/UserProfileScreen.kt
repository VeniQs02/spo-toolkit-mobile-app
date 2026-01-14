package com.example.spotoolkit.ui.UserProfile

import MainViewModel
import android.widget.Button
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.spotoolkit.R
import com.example.spotoolkit.ui.Search.formatFollowerNumber
import com.example.spotoolkit.util.AuthState

@Composable
fun UserProfileScreen(vm: MainViewModel) {
    val authStatus = vm.authState.collectAsState().value
    val userResults by vm.userResults.collectAsState()

    when {
        authStatus != AuthState.Authenticated -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.login_with_spotify))
            }
        }

        userResults != null -> {
            UserDataList(userResults!!)
        }

        else -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun UserDataList(userResults: User) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Image
        if (!userResults.imageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = userResults.imageUrl,
                contentDescription = userResults.displayName,
                modifier = Modifier.size(128.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display name
        Text(
            text = userResults.displayName, style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            val flagEmoji = countryCodeToEmoji(userResults.country)
            if (flagEmoji.isNotEmpty()) {
                Text(flagEmoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(userResults.country)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Followers
        Text(
            text = "Followers: ${formatFollowerNumber(userResults.followers)}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {}){
            Text(stringResource(R.string.log_out))
        }
    }
}

@Composable
fun countryCodeToEmoji(countryCode: String): String {
    if (countryCode.length != 2) return ""
    val first = Character.codePointAt(countryCode.uppercase(), 0) - 0x41 + 0x1F1E6
    val second = Character.codePointAt(countryCode.uppercase(), 1) - 0x41 + 0x1F1E6
    return String(Character.toChars(first)) + String(Character.toChars(second))
}
