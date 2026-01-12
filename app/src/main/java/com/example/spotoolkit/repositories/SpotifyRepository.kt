package com.example.spotoolkit.repositories

import SearchResponse
import SearchResultItem
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.spotoolkit.ui.Search.SearchType
import com.example.spotoolkit.ui.UserProfile.User
import com.example.spotoolkit.util.PKCEUtil
import com.google.gson.Gson
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

    private val baseUrl: HttpUrl =
        HttpUrl.Builder().scheme("https").host("api.spotify.com").addPathSegment("v1").build()


    fun buildAuthIntent(): Intent {
        val verifier = PKCEUtil.generateCodeVerifier()
        saveVerifier(verifier)

        val challenge = PKCEUtil.codeChallenge(verifier)

        val uri = Uri.Builder().scheme("https").authority("accounts.spotify.com").appendPath("authorize")
            .appendQueryParameter("client_id", CLIENT_ID).appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", REDIRECT_URI).appendQueryParameter(
                "scope", "user-read-private playlist-read-private"
            ).appendQueryParameter("code_challenge_method", "S256").appendQueryParameter("code_challenge", challenge)
            .build()

        return Intent(Intent.ACTION_VIEW, uri)
    }

    fun getVerifier(): String = prefs.getString(KEY_VERIFIER, null) ?: error("PKCE verifier missing — auth flow broken")

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
        val verifier = getVerifier()

        val body = FormBody.Builder().add("grant_type", "authorization_code").add("code", code)
            .add("redirect_uri", REDIRECT_URI).add("client_id", CLIENT_ID).add("code_verifier", verifier).build()

        val request = Request.Builder().url("https://accounts.spotify.com/api/token").post(body).build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        Log.d("PKCE", "Token response: $responseBody")

        if (!response.isSuccessful) {
            throw Exception("Token request failed: ${response.message} — $responseBody")
        }

        clearVerifier()
        JSONObject(responseBody).getString("access_token")
    }

    suspend fun search(
        token: String, query: String, type: SearchType
    ): List<SearchResultItem> = withContext(Dispatchers.IO) {
        val url = baseUrl.newBuilder().addPathSegment("search").addQueryParameter("q", query)
            .addQueryParameter("type", type.string).addQueryParameter("limit", "10").build()

        val request = Request.Builder().url(url).addHeader("Authorization", "Bearer $token").build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return@withContext emptyList()

        if (!response.isSuccessful) {
            Log.e("API", "Search failed ${response.code}: $body")
            return@withContext emptyList()
        }

        val gson = Gson()
        val searchResponse = gson.fromJson(body, SearchResponse::class.java)

        when (type) {
            SearchType.Artist -> searchResponse.artists?.items?.map { SearchResultItem.ArtistItem(it) }
            SearchType.Track -> searchResponse.tracks?.items?.map { SearchResultItem.TrackItem(it) }
            SearchType.Album -> searchResponse.albums?.items?.map { SearchResultItem.AlbumItem(it) }
            SearchType.Playlist -> searchResponse.playlists?.items?.mapNotNull {
                it?.let {
                    SearchResultItem.PlaylistItem(
                        it
                    )
                }
            } ?: emptyList()

            SearchType.Show -> searchResponse.shows?.items?.map { SearchResultItem.ShowItem(it) }
            SearchType.Episode -> searchResponse.episodes?.items?.map { SearchResultItem.EpisodeItem(it) }
        } ?: emptyList()
    }


    suspend fun fetchUserData(token: String): User = withContext(Dispatchers.IO) {
        val url = baseUrl.newBuilder().addPathSegment("me").build()

        val request = Request.Builder().url(url).addHeader("Authorization", "Bearer $token").build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: error("Empty response")

        if (!response.isSuccessful) {
            throw Exception("User data request failed: ${response.code} $body")
        }

        val json = JSONObject(body)

        val displayName = json.optString("display_name", "Unknown")
        val country = json.optString("country", "")
        val followers = json.getJSONObject("followers").optInt("total", 0)
        val images = json.optJSONArray("images")
        val imageUrl = if (images != null && images.length() > 0) {
            images.getJSONObject(0).optString("url")
        } else null

        User(displayName, country, followers, imageUrl)
    }
}


fun JSONObject.toMap(): Map<String, String> = keys().asSequence().associateWith { getString(it) }
