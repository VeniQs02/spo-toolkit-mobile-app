import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotoolkit.repositories.SpotifyRepository
import com.example.spotoolkit.responses.Artist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.example.spotoolkit.ui.UserProfile.User
import com.example.spotoolkit.util.AuthState


class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = SpotifyRepository(application)

    val authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val token = MutableStateFlow<String?>(null)
    val query = MutableStateFlow("")
    val artistsResults = MutableStateFlow<List<Artist>>(emptyList())
    val userResults = MutableStateFlow<User?>(null)
    val loading = MutableStateFlow(false)

    fun buildSpotifyAuthIntent(): Intent {
        return repo.buildAuthIntent()
    }


    fun handleAuthCode(code: String) {
        authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val newToken = repo.exchangeCodeForToken(code)
                token.value = newToken
                authState.value = AuthState.Authenticated
                repo.clearVerifier()
                fetchUserData()
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
                artistsResults.value = repo.searchArtist(t, q)
            } catch (e: Exception) {
                artistsResults.value = emptyList()
            }
            loading.value = false
        }
    }

    fun fetchUserData() {
        val t = token.value ?: return

        viewModelScope.launch {
            loading.value = true
            try {
                userResults.value = repo.fetchUserData(t)
            } catch (e: Exception) {
                userResults.value = null
            }
            loading.value = false
        }
    }
}

