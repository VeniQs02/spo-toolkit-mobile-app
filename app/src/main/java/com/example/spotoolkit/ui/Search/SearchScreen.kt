package com.example.spotoolkit.ui.Search

import Album
import Artist
import Audiobook
import Episode
import MainViewModel
import Playlist
import Show
import Track
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext


@Composable
fun SearchScreen(vm: MainViewModel) {
    val query by vm.query.collectAsState()
    val results by vm.searchResults.collectAsState()
    val loading by vm.loading.collectAsState()
    val context = LocalContext.current

    val searchType by vm.searchType.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            OutlinedTextField(value = query,
                onValueChange = { vm.query.value = it },
                label = { Text("Search") },
                modifier = Modifier.weight(1f)
            )

            Box(modifier = Modifier
                .width(120.dp)
                .wrapContentSize(Alignment.TopStart)
                .clickable { expanded = true }) {
                OutlinedTextField(value = searchType.name,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("Type") },
                    trailingIcon = {
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = LocalContentColor.current,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledTrailingIconColor = LocalContentColor.current
                    )
                )

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    SearchType.values().forEach { type ->
                        DropdownMenuItem(text = { Text(type.name) }, onClick = {
                            vm.searchType.value = type
                            expanded = false
                        })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { vm.search() }, modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        LazyColumn {
            items(results) { item ->
                when (item) {
                    is SearchResultItem.ArtistItem -> ArtistRow(item.artist, context)
                    is SearchResultItem.TrackItem -> TrackRow(item.track, context)
                    is SearchResultItem.AlbumItem -> AlbumRow(item.album, context)
                    is SearchResultItem.PlaylistItem -> PlaylistRow(item.playlist, context)
                    is SearchResultItem.ShowItem -> ShowRow(item.show, context)
                    is SearchResultItem.EpisodeItem -> EpisodeRow(item.episode, context)
                    else -> {
                        SomethingWentWrongView()
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistRow(artist: Artist?, context: Context) {
    val spotifyUrl = artist?.externalUrls?.get("spotify")
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)
        .clickable(enabled = spotifyUrl != null) {
            spotifyUrl?.let { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
        }) {
        val imageUrl = artist?.images?.firstOrNull()?.url
        if (!imageUrl.isNullOrEmpty()) {
            AsyncImage(model = imageUrl, contentDescription = artist?.name, modifier = Modifier.size(64.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.Gray)
            )
        }

        Spacer(Modifier.width(8.dp))
        Column {
            Text(artist?.name ?: "Unknown", style = MaterialTheme.typography.titleMedium)
            Text(
                "Followers: ${artist?.followers?.total ?: 0}", style = MaterialTheme.typography.bodyMedium
            )
            Text(
                artist?.genres?.joinToString(", ") ?: "Unknown",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun TrackRow(track: Track?, context: Context) {
    val spotifyUrl = track?.externalUrls?.get("spotify")
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)
        .clickable(enabled = spotifyUrl != null) {
            spotifyUrl?.let { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
        }) {
        val imageUrl = track?.album?.images?.firstOrNull()?.url
        if (!imageUrl.isNullOrEmpty()) {
            AsyncImage(model = imageUrl, contentDescription = track?.name, modifier = Modifier.size(64.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.Gray)
            )
        }

        Spacer(Modifier.width(8.dp))
        Column {
            Text(track?.name ?: "Unknown", style = MaterialTheme.typography.titleMedium)
            Text("Artists: ${track?.artists?.joinToString(", ") { it.name ?: "Unknown" } ?: "Unknown"}",
                style = MaterialTheme.typography.bodyMedium)
            val duration = track?.durationMs ?: 0
            Text(
                "Duration: ${duration / 1000 / 60}:${(duration / 1000) % 60}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun AlbumRow(album: Album?, context: Context) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)
        .clickable {
            val url = album?.externalUrls?.get("spotify") ?: return@clickable
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }) {
        val imageUrl = album?.images?.firstOrNull()?.url
        if (!imageUrl.isNullOrEmpty()) {
            AsyncImage(model = imageUrl, contentDescription = album?.name, modifier = Modifier.size(64.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.Gray)
            )
        }

        Spacer(Modifier.width(8.dp))
        Column {
            Text(album?.name ?: "Unknown", style = MaterialTheme.typography.titleMedium)
            Text("Artists: ${album?.artists?.joinToString(", ") { it.name ?: "Unknown" } ?: "Unknown"}",
                style = MaterialTheme.typography.bodyMedium)
            Text(
                "Type: ${album?.albumType ?: "Unknown"}, Tracks: ${album?.totalTracks ?: 0}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun PlaylistRow(playlist: Playlist, context: Context) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)
        .clickable {
            val url = playlist.externalUrls?.get("spotify") ?: return@clickable
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }) {
        val imageUrl = playlist.images?.firstOrNull()?.url
        if (!imageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = imageUrl, contentDescription = playlist.name, modifier = Modifier.size(64.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.Gray)
            )
        }

        Spacer(Modifier.width(8.dp))
        Column {
            Text(playlist.name ?: "Unknown", style = MaterialTheme.typography.titleMedium)
            Text(
                "Owner: ${playlist.owner?.displayName ?: playlist.owner?.id ?: "Unknown"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Tracks: ${playlist.tracks?.total ?: 0}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun ShowRow(show: Show?, context: Context) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)
        .clickable {
            val url = show?.externalUrls?.get("spotify") ?: return@clickable
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }) {
        val imageUrl = show?.images?.firstOrNull()?.url
        if (!imageUrl.isNullOrEmpty()) {
            AsyncImage(model = imageUrl, contentDescription = show?.name, modifier = Modifier.size(64.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.Gray)
            )
        }

        Spacer(Modifier.width(8.dp))
        Column {
            Text(show?.name ?: "Unknown", style = MaterialTheme.typography.titleMedium)
            Text("Publisher: ${show?.publisher ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Episodes: ${show?.totalEpisodes ?: 0}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun EpisodeRow(episode: Episode?, context: Context) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)
        .clickable {
            val url = episode?.externalUrls?.get("spotify") ?: return@clickable
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }) {
        val imageUrl = episode?.images?.firstOrNull()?.url
        if (!imageUrl.isNullOrEmpty()) {
            AsyncImage(model = imageUrl, contentDescription = episode?.name, modifier = Modifier.size(64.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.Gray)
            )
        }

        Spacer(Modifier.width(8.dp))
        Column {
            Text(episode?.name ?: "Unknown", style = MaterialTheme.typography.titleMedium)
            val duration = episode?.durationMs ?: 0
            Text(
                "Duration: ${duration / 1000 / 60}:${(duration / 1000) % 60}",
                style = MaterialTheme.typography.bodyMedium
            )
            episode?.description?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SomethingWentWrongView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp), contentAlignment = Alignment.Center
    ) {
        Text("Something went wrong")
    }
}

fun formatFollowerNumber(n: Int): String = when {
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

