package prayit.simplebudget.core.components.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.compositionLocalOf

val LocalSnackbarHostState = compositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided. Wrap your content with ProvideSnackbarHostState.")
}

enum class SnackbarType {
    SUCCESS,
    ERROR,
}

data class SnackbarMessage(
    val message: String,
    val type: SnackbarType = SnackbarType.SUCCESS,
)

val LocalSnackbarType = compositionLocalOf { SnackbarType.SUCCESS }
