package prayit.simplebudget.di

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import prayit.simplebudget.core.components.navigation.SnackbarType
import prayit.simplebudget.core.utils.Log

@SingleIn(AppScope::class)
@Inject
class SnackbarViewModel {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val snackbarHostState = SnackbarHostState()
    var currentType by mutableStateOf(SnackbarType.SUCCESS)
        private set

    fun showSnackbar(
        message: String,
        type: SnackbarType = SnackbarType.SUCCESS,
        duration: SnackbarDuration = SnackbarDuration.Short,
    ) {
        Log.d("SnackbarViewModel") { "showSnackbar: $message (type=$type)" }
        currentType = type
        scope.launch {
            snackbarHostState.showSnackbar(message, duration = duration)
        }
    }
}
