import android.app.Application
import android.app.Activity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotoolkit.App
import com.example.spotoolkit.repositories.SpotifyRepository
import com.example.spotoolkit.util.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SpotifyRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState = _authState

    fun startSpotifyAuth(activity: Activity) {
        val intent = repo.buildAuthIntent()
        activity.startActivity(intent)
    }

    fun handleAuthCode(code: String) {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                val token = repo.exchangeCodeForToken(code)
                (getApplication() as App).spotifyToken = token
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value =
                    AuthState.Error(e.message ?: "Authorization failed")
            }
        }
    }
}
