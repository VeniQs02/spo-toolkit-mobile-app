package com.example.spotoolkit.responses

import Track
import com.google.gson.annotations.SerializedName

data class RecommendationsResponse(
    @SerializedName("tracks") val tracks: List<Track>
)

