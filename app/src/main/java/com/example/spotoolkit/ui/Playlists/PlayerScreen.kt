package com.example.spotoolkit.ui.Playlists

import MainViewModel
import Track
import albumArtUrl
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.spotoolkit.R
import primaryArtistName

@Composable
fun PlayerScreen(vm: MainViewModel) {

    val track by vm.currentTrack.collectAsState()
    val recommendations by vm.recommendations.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        track?.albumArtUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = track?.name ?: "Unknown track",
                modifier = Modifier.size(200.dp).clip(RoundedCornerShape(16.dp)).align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Crop
            )
        } ?: Box(
            modifier = Modifier.size(200.dp).background(Color.Gray).align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = track?.name ?: stringResource(R.string.could_find_no_song),
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = track?.primaryArtistName ?: "",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = vm::togglePlay) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Clear else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Text("Recommended", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (recommendations.isNotEmpty()) {
                items(recommendations) { recTrack -> RecommendationItem(recTrack) }
            } else {
                item {
                    Text("No recommendations available", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun RecommendationItem(track: Track) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable {},
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = track.albumArtUrl,
            contentDescription = track.name,
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = track.name ?: "",
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}



