package com.example.spotoolkit.repositories

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.spotoolkit.responses.Artist
import com.example.spotoolkit.responses.Followers
import com.example.spotoolkit.responses.Image
import com.example.spotoolkit.util.PKCEUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class SpotifyRepository(context: Context) {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val client = OkHttpClient()

    private val CLIENT_ID = "d9d3978d2343416eaac48b4a361dc033"
    private val REDIRECT_URI = "spotoolkit://callback"

    fun buildAuthIntent(): Intent {
        val verifier = PKCEUtil.generateCodeVerifier()
        saveVerifier(verifier)

        val challenge = PKCEUtil.codeChallenge(verifier)

        val uri = Uri.Builder()
            .scheme("https")
            .authority("accounts.spotify.com")
            .appendPath("authorize")
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter(
                "scope",
                "user-read-private playlist-read-private"
            )
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("code_challenge", challenge)
            .build()

        return Intent(Intent.ACTION_VIEW, uri)
    }

    fun getVerifier(): String =
        prefs.getString(KEY_VERIFIER, null)
            ?: error("PKCE verifier missing — auth flow broken")

    fun clearVerifier() {
        prefs.edit().remove(KEY_VERIFIER).apply()
    }

    private fun saveVerifier(verifier: String) {
        prefs.edit().putString(KEY_VERIFIER, verifier).apply()
    }

    companion object {
        private const val KEY_VERIFIER = "pkce_verifier"
    }

    suspend fun exchangeCodeForToken(code: String): String = withContext(Dispatchers.IO) {
        val verifier = getVerifier() // <-- use the saved verifier

        val body = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("code", code)
            .add("redirect_uri", REDIRECT_URI)
            .add("client_id", CLIENT_ID)
            .add("code_verifier", verifier)
            .build()

        val request = Request.Builder()
            .url("https://accounts.spotify.com/api/token")
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        Log.d("PKCE", "Token response: $responseBody")

        if (!response.isSuccessful) {
            throw Exception("Token request failed: ${response.message} — $responseBody")
        }

        clearVerifier()
        JSONObject(responseBody).getString("access_token")
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
            val body = response.body?.string() ?: return@withContext emptyList()

            if (!response.isSuccessful) {
                Log.e("API", "Search failed ${response.code}: $body")
                return@withContext emptyList()
            }

            val artistsJson = JSONObject(body)
                .getJSONObject("artists")
                .getJSONArray("items")

            (0 until artistsJson.length()).map { i ->
                val a = artistsJson.getJSONObject(i)

                val imagesJson = a.getJSONArray("images")
                val genresJson = a.getJSONArray("genres")
                val followersJson = a.getJSONObject("followers")

                Artist(
                    id = a.getString("id"),
                    name = a.getString("name"),
                    popularity = a.getInt("popularity"),
                    external_urls = a.getJSONObject("external_urls").toMap(),
                    followers = Followers(
                        total = followersJson.getInt("total")
                    ),
                    genres = (0 until genresJson.length()).map { j ->
                        genresJson.getString(j)
                    },
                    images = (0 until imagesJson.length()).map { j ->
                        val img = imagesJson.getJSONObject(j)
                        Image(
                            url = img.getString("url"),
                            height = img.optInt("height").takeIf { it != 0 },
                            width = img.optInt("width").takeIf { it != 0 }
                        )
                    }
                )
            }
        }
}


fun JSONObject.toMap(): Map<String, String> =
    keys().asSequence().associateWith { getString(it) }
