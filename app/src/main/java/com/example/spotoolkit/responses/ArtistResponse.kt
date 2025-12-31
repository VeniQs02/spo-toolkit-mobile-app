package com.example.spotoolkit.responses

data class ArtistResponse(
    val artists: Artists
)

data class Artists(
    val items: List<Artist>
)

data class Artist(
    val id: String,
    val name: String,
    val images: List<Image>,
    val popularity: Int,
    val followers: Followers,
    val genres: List<String>,
    val external_urls: Map<String, String>,
)

data class Image(
    val url: String,
    val height: Int?,
    val width: Int?,
)

data class Followers(
    val total: Int
)
