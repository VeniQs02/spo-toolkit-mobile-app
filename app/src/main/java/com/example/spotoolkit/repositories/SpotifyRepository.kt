package com.example.spotoolkit.repositories

import android.util.Base64
import com.example.spotoolkit.responses.Artist
import com.example.spotoolkit.responses.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class SpotifyRepository {

    private val client = OkHttpClient()

    private val CLIENT_ID = "d9d3978d2343416eaac48b4a361dc033"
    private val CLIENT_SECRET = "efd9683f6c7a417ebf9cc10514ec14b9"

    suspend fun fetchToken(): String = withContext(Dispatchers.IO) {
        val credentials = "$CLIENT_ID:$CLIENT_SECRET"
        val encoded = Base64.encodeToString(
            credentials.toByteArray(),
            Base64.NO_WRAP
        )

        val body = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .build()

        val request = Request.Builder()
            .url("https://accounts.spotify.com/api/token")
            .post(body)
            .addHeader("Authorization", "Basic $encoded")
            .build()

        val response = client.newCall(request).execute()
        val json = JSONObject(response.body?.string() ?: "")
        json.getString("access_token")
    }

    suspend fun searchArtist(token: String, query: String): List<Artist> =
        withContext(Dispatchers.IO) {
            val url = HttpUrl.Builder()
                .scheme("https")
                .host("api.spotify.com")
                .addPathSegment("v1")
                .addPathSegment("search")
                .addQueryParameter("q", query)
                .addQueryParameter("type", "artist")
                .addQueryParameter("limit", "10")
                .build()

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .build()

            val response = client.newCall(request).execute()
            val json = JSONObject(response.body?.string() ?: "")
            val artistsJson = json.getJSONObject("artists").getJSONArray("items")

            (0 until artistsJson.length()).map { i ->
                val a = artistsJson.getJSONObject(i)
                val imagesJson = a.getJSONArray("images")
                Artist(
                    id = a.getString("id"),
                    name = a.getString("name"),
                    popularity = a.getInt("popularity"),
                    external_urls = a.getJSONObject("external_urls").toMap(),
                    images = (0 until imagesJson.length()).map { j ->
                        val img = imagesJson.getJSONObject(j)
                        Image(
                            url = img.getString("url"),
                            height = img.optInt("height"),
                            width = img.optInt("width")
                        )
                    }
                )
            }
        }

}

fun JSONObject.toMap(): Map<String, String> =
    keys().asSequence().associateWith { getString(it) }
