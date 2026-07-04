import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension
import prayit.simplebudget.App

fun main() = application {
    val sharedIsDark = mutableStateOf(true)

    Window(
        title = "SimpleBudget — Phone",
        state = rememberWindowState(width = 400.dp, height = 850.dp),
        onCloseRequest = ::exitApplication,
    ) {
        window.minimumSize = Dimension(400, 600)
        Box(Modifier.fillMaxSize()) {
            App(
                isDark = sharedIsDark.value,
                darkThemeToggle = {
                    ThemeToggleButton(
                        isDark = sharedIsDark.value,
                        onToggle = { sharedIsDark.value = !sharedIsDark.value },
                    )
                },
            )
        }
    }
    /*Window(
        title = "SimpleBudget — Tablet",
        state = rememberWindowState(width = 1024.dp, height = 768.dp),
        onCloseRequest = ::exitApplication,
    ) {
        window.minimumSize = Dimension(768, 600)
        Box(Modifier.fillMaxSize()) {
            App(
                isDark = sharedIsDark.value,
                darkThemeToggle = {
                    ThemeToggleButton(
                        isDark = sharedIsDark.value,
                        onToggle = { sharedIsDark.value = !sharedIsDark.value },
                    )
                },
            )
        }
    }*/
}

@Composable
private fun ThemeToggleButton(isDark: Boolean, onToggle: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopEnd) {
        Button(onClick = onToggle) {
            Text(if (isDark) "☀️" else "🌙", fontSize = 16.sp)
        }
    }
}
