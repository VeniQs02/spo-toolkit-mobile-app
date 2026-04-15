import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotoolkit.repositories.SpotifyRepository
import com.example.spotoolkit.data.SearchType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import com.example.spotoolkit.data.User
import com.example.spotoolkit.data.AuthState
import com.example.spotoolkit.data.TokenBundle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive


class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = SpotifyRepository(application)

    val authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val token = MutableStateFlow<TokenBundle?>(null)
    val query = MutableStateFlow("")
    val userResults = MutableStateFlow<User?>(null)
    val loading = MutableStateFlow(false)

    val searchResults = MutableStateFlow<List<SearchResultItem>>(emptyList())
    val searchType = MutableStateFlow(SearchType.Artist)

    val currentTrack = MutableStateFlow<Track?>(null)
    val recommendations = MutableStateFlow<List<Track>>(emptyList())
    val isPlaying = MutableStateFlow(false)

    init {
        restoreSession()
    }

    fun buildSpotifyAuthIntent(): Intent {
        return repo.buildAuthIntent()
    }


    fun handleAuthCode(code: String) {
        authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val newToken = repo.exchangeCodeForToken(code)
                repo.saveToken(newToken)
                token.value = newToken
                authState.value = AuthState.Authenticated

                repo.clearVerifier()
                fetchUserData()
                startPlaybackObserver()
            } catch (e: Exception) {
                authState.value = AuthState.Error(e.message ?: "Authorization failed")
            }
        }
    }

    fun restoreSession() {
        viewModelScope.launch {
            val savedToken = repo.loadToken()
            if (savedToken != null) {
                token.value = savedToken
                authState.value = AuthState.Authenticated
                if (userResults.value == null) {
                    fetchUserData()
                    startPlaybackObserver()
                }
            } else {
                authState.value = AuthState.Unauthenticated
            }
        }
    }



    fun fetchUserData() {
        viewModelScope.launch {
            loading.value = true
            try {
                userResults.value = repo.fetchUserData()
            } catch (e: Exception) {
                userResults.value = null
            }
            loading.value = false
        }
    }


    fun logout() {
        repo.clearToken()
        token.value = null
        userResults.value = null
        searchResults.value = emptyList()
        query.value = ""
        authState.value = AuthState.Unauthenticated
        Log.d("PKCE", "Auth state changed: ${authState.value}")
    }

    fun search() {
        val q = query.value.ifEmpty { return }

        viewModelScope.launch {
            loading.value = true
            try {
                searchResults.value = repo.search(q, searchType.value) ?: emptyList()
            } catch (e: Exception) {
                searchResults.value = emptyList()
            }
            loading.value = false
        }
    }

    fun togglePlay() {
        // Optimistically update UI
        isPlaying.value = !isPlaying.value

        viewModelScope.launch {
            try {
                if (isPlaying.value) repo.play() else repo.pause()
            } catch (e: Exception) {
                // Revert on failure
                isPlaying.value = !isPlaying.value
            }
        }
    }

    private var lastSeedTrackId: String? = null

    private fun loadRecommendations(track: Track) {
        val seedId = track.id ?: return
        if (seedId == lastSeedTrackId) return
        lastSeedTrackId = seedId

        viewModelScope.launch {
            val recs = repo.getRecommendations(seedId, track.id)
            Log.d("RECS", "Got ${recs?.size ?: 0} recommendations")
            recommendations.value = recs ?: emptyList()
        }
    }


    private var pollingJob: Job? = null

    fun startPlaybackObserver() {
        pollingJob?.cancel()

        pollingJob = viewModelScope.launch {
            while (isActive) {
                val state = repo.getCurrentlyPlaying()
                if (state != null && state.track != null) {
                    currentTrack.value = state.track
                    isPlaying.value = state.isPlaying

                    loadRecommendations(state.track)
                }
                delay(3_000)
            }
        }
    }
}


