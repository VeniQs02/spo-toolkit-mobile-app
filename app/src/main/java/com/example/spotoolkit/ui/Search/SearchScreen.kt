package com.example.spotoolkit.ui.Search

import MainViewModel
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext


@Composable
fun SearchScreen(vm: MainViewModel) {
    val query by vm.query.collectAsState()
    val results by vm.results.collectAsState()
    val loading by vm.loading.collectAsState()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        OutlinedTextField(
            value = query,
            onValueChange = { vm.query.value = it },
            label = { Text("Artist name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { vm.searchArtist() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (loading) {
            CircularProgressIndicator()
        }

        LazyColumn {
            items(results) { artist ->
                val spotifyUrl = artist.external_urls["spotify"]

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable(enabled = spotifyUrl != null) {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(spotifyUrl)
                            )
                            context.startActivity(intent)
                        }
                ) {

                    if (artist.images.isNotEmpty()) {
                        AsyncImage(
                            model = artist.images.first().url,
                            contentDescription = artist.name,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            artist.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Followers: ${formatFollowerNumber(artist.followers.total)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            artist.genres.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

fun formatFollowerNumber(n: Int): String =
    when {
        n > 999999 -> {
            val millions = n / 1000000
            val decimal = (n % 1000000) / 100000
            "$millions.$decimal M"
        }
        n > 999 -> {
            val thousands = n / 1000
            val rest = n % 1000
            "$thousands,${rest.toString().padStart(3, '0')}"
        }
        else -> n.toString()
    }

