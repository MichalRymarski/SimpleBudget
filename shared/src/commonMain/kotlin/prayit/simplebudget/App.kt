package prayit.simplebudget

import androidx.compose.runtime.Composable

@Composable
fun App(
    isDark: Boolean? = null,
    darkThemeToggle: @Composable (() -> Unit)? = null,
    onThemeChanged: @Composable ((isDark: Boolean) -> Unit) = {},
) {
    NavHost(
        isDark = isDark,
        darkThemeToggle = darkThemeToggle,
        onThemeChanged = onThemeChanged,
    )
}
