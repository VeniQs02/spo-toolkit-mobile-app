package com.example.spotoolkit.repositories

import SearchResponse
import SearchResultItem
import Track
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.spotoolkit.data.CurrentlyPlaying
import com.example.spotoolkit.data.SearchType
import com.example.spotoolkit.data.TokenBundle
import com.example.spotoolkit.data.User
import com.example.spotoolkit.responses.RecommendationsResponse
import com.example.spotoolkit.util.PKCEUtil
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class SpotifyRepository(context: Context) {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val client = OkHttpClient()

    private val CLIENT_ID = "1da3f8b0be664a498f875417a7960b24"
    private val REDIRECT_URI = "spotoolkit://callback"

    private val baseUrl: HttpUrl =
        HttpUrl.Builder().scheme("https").host("api.spotify.com").addPathSegment("v1").build()

    fun buildAuthIntent(): Intent {
        val verifier = PKCEUtil.generateCodeVerifier()
        saveVerifier(verifier)

        val challenge = PKCEUtil.codeChallenge(verifier)

        val scopes = listOf(
            "user-read-private",
            "playlist-read-private",
            "user-read-playback-state",
            "user-modify-playback-state",
            "user-read-currently-playing",
            "user-personalized"
        ).joinToString(" ")

        val uri = Uri.Builder().scheme("https").authority("accounts.spotify.com").appendPath("authorize")
            .appendQueryParameter("client_id", CLIENT_ID).appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", REDIRECT_URI).appendQueryParameter("scope", scopes)
            .appendQueryParameter("code_challenge_method", "S256").appendQueryParameter("code_challenge", challenge)
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

    fun clearToken() {
        prefs.edit().remove(KEY_ACCESS).remove(KEY_REFRESH).remove(KEY_EXPIRES_AT).apply()
    }

    fun saveToken(token: TokenBundle) {
        prefs.edit().putString(KEY_ACCESS, token.accessToken).putString(KEY_REFRESH, token.refreshToken)
            .putLong(KEY_EXPIRES_AT, token.expiresAt).apply()
    }

    fun loadToken(): TokenBundle? {
        val access = prefs.getString(KEY_ACCESS, null) ?: return null
        val refresh = prefs.getString(KEY_REFRESH, null) ?: return null
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        if (expiresAt == 0L) return null

        return TokenBundle(access, refresh, expiresAt)
    }

    companion object {
        private const val KEY_VERIFIER = "pkce_verifier"
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_EXPIRES_AT = "expires_at"
    }

    suspend fun exchangeCodeForToken(code: String): TokenBundle = withContext(Dispatchers.IO) {
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
        val json = JSONObject(responseBody)

        TokenBundle(
            accessToken = json.getString("access_token"),
            refreshToken = json.optString("refresh_token") ?: throw Exception("Missing refresh_token: $responseBody"),
            expiresAt = System.currentTimeMillis() + json.getLong("expires_in") * 1000
        )
    }

    suspend fun refreshToken(refreshToken: String): TokenBundle = withContext(Dispatchers.IO) {

        val body = FormBody.Builder().add("grant_type", "refresh_token").add("refresh_token", refreshToken)
            .add("client_id", CLIENT_ID).build()

        val request = Request.Builder().url("https://accounts.spotify.com/api/token").post(body).build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        Log.e("PKCE", "Refresh response: $responseBody")

        if (!response.isSuccessful) {
            throw Exception("Refresh failed: ${response.code} $responseBody")
        }

        val json = JSONObject(responseBody)

        val accessToken = json.optString("access_token", null)
        if (accessToken == null) {
            throw Exception("No access_token in response: $responseBody")
        }

        return@withContext TokenBundle(
            accessToken = accessToken,
            refreshToken = json.optString("refresh_token", refreshToken),
            expiresAt = System.currentTimeMillis() + json.getLong("expires_in") * 1000
        )
    }

    suspend fun getValidAccessToken(): String? {
        val token = loadToken() ?: return null

        if (System.currentTimeMillis() < token.expiresAt - 60_000) {
            return token.accessToken
        }

        val refreshed = try {
            Log.d("PKCE", "Attempting refresh with token: ${token.refreshToken.take(10)}...")
            refreshToken(token.refreshToken)
        } catch (e: Exception) {
            Log.e("PKCE", "Refresh failed, clearing token", e)
            clearToken()
            return null
        }

        saveToken(refreshed)
        Log.d("PKCE", "Refreshed token: ${refreshed.accessToken}")
        return refreshed.accessToken
    }

    suspend fun search(query: String, type: SearchType): List<SearchResultItem>? = withContext(Dispatchers.IO) {
        val accessToken = getValidAccessToken() ?: return@withContext null

        val url = baseUrl.newBuilder().addPathSegment("search").addQueryParameter("q", query)
            .addQueryParameter("type", type.string).addQueryParameter("limit", "10").build()

        val request = Request.Builder().url(url).addHeader("Authorization", "Bearer ${accessToken}").build()

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


    suspend fun fetchUserData(): User? = withContext(Dispatchers.IO) {
        val accessToken = getValidAccessToken() ?: return@withContext null
        val url = baseUrl.newBuilder().addPathSegment("me").build()

        val request = Request.Builder().url(url).addHeader("Authorization", "Bearer ${accessToken}").build()

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

    suspend fun getCurrentlyPlaying(): CurrentlyPlaying? = withContext(Dispatchers.IO) {
        val token = getValidAccessToken() ?: return@withContext null

        val request = Request.Builder().url("https://api.spotify.com/v1/me/player/currently-playing")
            .addHeader("Authorization", "Bearer $token").build()

        val response = client.newCall(request).execute()

        if (response.code == 204) return@withContext null // 204 = nothing playing
        if (!response.isSuccessful) return@withContext null

        val json = JSONObject(response.body!!.string())
        val isPlaying = json.getBoolean("is_playing")
        val item = json.optJSONObject("item") ?: return@withContext null

        val track = Gson().fromJson(item.toString(), Track::class.java)

        CurrentlyPlaying(isPlaying, track)
    }

    suspend fun play() = withContext(Dispatchers.IO) {
        val token = getValidAccessToken() ?: return@withContext null

        val request = Request.Builder().url("https://api.spotify.com/v1/me/player/play")
            .put(RequestBody.create(null, ByteArray(0))).addHeader("Authorization", "Bearer $token").build()

        client.newCall(request).execute()
    }

    suspend fun pause() = withContext(Dispatchers.IO) {
        val token = getValidAccessToken() ?: return@withContext null

        val request = Request.Builder().url("https://api.spotify.com/v1/me/player/pause")
            .put(RequestBody.create(null, ByteArray(0))).addHeader("Authorization", "Bearer $token").build()

        client.newCall(request).execute()
    }

    suspend fun getRecommendations(seedTrackId: String, seedArtistId: String? = null): List<Track>? =
        withContext(Dispatchers.IO) {

            val accessToken = getValidAccessToken() ?: return@withContext null

            val builder =
                baseUrl.newBuilder().addPathSegment("recommendations").addQueryParameter("seed_tracks", seedTrackId)
                    .addQueryParameter("limit", "10")

            if (!seedArtistId.isNullOrEmpty()) {
                builder.addQueryParameter("seed_artists", seedArtistId)
            }

            val url = builder.build()

            val request = Request.Builder().url(url).addHeader("Authorization", "Bearer $accessToken").build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext emptyList()

            if (!response.isSuccessful) return@withContext emptyList()

            Gson().fromJson(body, RecommendationsResponse::class.java).tracks
        }


}


fun JSONObject.toMap(): Map<String, String> = keys().asSequence().associateWith { getString(it) }
