import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotoolkit.repositories.SpotifyRepository
import com.example.spotoolkit.responses.Artist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.example.spotoolkit.App
import com.example.spotoolkit.util.AuthState


class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = SpotifyRepository()

    val authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val token = MutableStateFlow<String?>(null)
    val query = MutableStateFlow("")
    val results = MutableStateFlow<List<Artist>>(emptyList())
    val loading = MutableStateFlow(false)

    fun loadToken() {
        viewModelScope.launch {
            token.value = repo.fetchToken()
        }
    }

    fun startSpotifyAuth(activity: Activity) {
        val intent = repo.buildAuthIntent()
        activity.startActivity(intent)
    }

    fun handleAuthCode(code: String) {
        authState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                val newToken = repo.exchangeCodeForToken(code)
                token.value = newToken
                (getApplication() as App).spotifyToken = newToken
                authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                authState.value = AuthState.Error(e.message ?: "Authorization failed")
            }
        }
    }

    fun searchArtist() {
        val t = token.value ?: return
        val q = query.value.ifEmpty { return }

        viewModelScope.launch {
            loading.value = true
            try {
                results.value = repo.searchArtist(t, q)
            } catch (_: Exception) { results.value = emptyList() }
            loading.value = false
        }
    }
}

