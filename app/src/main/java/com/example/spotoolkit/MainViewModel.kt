import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotoolkit.repositories.SpotifyRepository
import com.example.spotoolkit.responses.Artist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = SpotifyRepository()

    val token = MutableStateFlow<String?>(null)
    val query = MutableStateFlow("")
    val results = MutableStateFlow<List<Artist>>(emptyList())
    val loading = MutableStateFlow(false)

    fun loadToken() {
        viewModelScope.launch {
            token.value = repo.fetchToken()
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

