package prayit.simplebudget

import androidx.compose.runtime.Composable

@Composable
fun App(
    darkThemeToggle: @Composable (() -> Unit)? = null,
    onThemeChanged: @Composable ((isDark: Boolean) -> Unit) = {},
    isDark: Boolean? = null,
) {
    NavHost(
        isDark = isDark,
        darkThemeToggle = darkThemeToggle,
        onThemeChanged = onThemeChanged,
    )
}
