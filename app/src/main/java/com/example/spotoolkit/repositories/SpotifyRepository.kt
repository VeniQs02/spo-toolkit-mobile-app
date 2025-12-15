package com.example.spotoolkit.repositories

import android.content.Intent
import android.net.Uri
import com.example.spotoolkit.util.PKCEUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class SpotifyRepository {
    private val redirectUri = "spotoolkit://callback"

    private val codeVerifier = PKCEUtil.generateCodeVerifier()
    private val codeChallenge = PKCEUtil.codeChallenge(codeVerifier)

    private val CLIENT_ID = "caa41d3c67044e5eba99cf688e12d094"

    fun buildAuthIntent(): Intent {
        val uri = Uri.Builder()
            .scheme("https")
            .authority("accounts.spotify.com")
            .appendPath("authorize")
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("scope", "user-read-private playlist-read-private")
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("code_challenge", codeChallenge)
            .build()

        return Intent(Intent.ACTION_VIEW, uri)
    }

    suspend fun exchangeCodeForToken(code: String): String =
        withContext(Dispatchers.IO) {
            val body = FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", redirectUri)
                .add("client_id", CLIENT_ID)
                .add("code_verifier", codeVerifier)
                .build()

            val request = Request.Builder()
                .url("https://accounts.spotify.com/api/token")
                .post(body)
                .build()

            val response = OkHttpClient().newCall(request).execute()
            val json = JSONObject(response.body?.string() ?: "")
            json.getString("access_token")
        }
}