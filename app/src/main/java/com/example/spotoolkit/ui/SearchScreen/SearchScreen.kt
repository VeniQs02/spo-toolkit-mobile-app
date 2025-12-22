package com.example.spotoolkit.ui.SearchScreen

import MainViewModel
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


@Composable
fun SearchScreen(vm: MainViewModel) {
    val query by vm.query.collectAsState()
    val results by vm.results.collectAsState()
    val loading by vm.loading.collectAsState()

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
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    if (artist.images.isNotEmpty()) {
                        AsyncImage(
                            model = artist.images.first().url,
                            contentDescription = artist.name,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(artist.name, style = MaterialTheme.typography.titleMedium)
                        Text("Popularity: ${artist.popularity}", style = MaterialTheme.typography.bodyMedium)
                        Text(artist.external_urls["spotify"] ?: "", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
