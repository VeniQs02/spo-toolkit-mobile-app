import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotoolkit.repositories.SpotifyRepository
import com.example.spotoolkit.ui.Search.SearchType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import com.example.spotoolkit.ui.UserProfile.User
import com.example.spotoolkit.util.AuthState


class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = SpotifyRepository(application)

    val authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val token = MutableStateFlow<String?>(null)
    val query = MutableStateFlow("")
    val userResults = MutableStateFlow<User?>(null)
    val loading = MutableStateFlow(false)

    val searchResults = MutableStateFlow<List<SearchResultItem>>(emptyList())
    val searchType = MutableStateFlow(SearchType.Artist)

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

    fun search() {
        val t = token.value ?: return
        val q = query.value.ifEmpty { return }

        viewModelScope.launch {
            loading.value = true
            try {
                searchResults.value = repo.search(t, q, searchType.value)
            } catch (e: Exception) {
                searchResults.value = emptyList()
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

