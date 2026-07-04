package prayit.simplebudget

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import prayit.simplebudget.core.components.theme.MParafiaTheme
import prayit.simplebudget.di.Graph
import prayit.simplebudget.feature.home.ui.HomeScreen
import prayit.simplebudget.navigation.HomeRoute
import prayit.simplebudget.navigation.navConfig
import prayit.simplebudget.navigation.rememberDeviceClass

@Composable
fun NavHost(
    isDark: Boolean? = null,
    darkThemeToggle: @Composable (() -> Unit)? = null,
    onThemeChanged: @Composable ((isDark: Boolean) -> Unit) = {},
) {
    val backStack = rememberNavBackStack(navConfig, HomeRoute.Main)
    val deviceClass = rememberDeviceClass()
    val defaultOnBack: () -> Unit = { backStack.removeLastOrNull() }

    val homeViewModel = Graph.app.homeViewModel

    MParafiaTheme(
        isDark = isDark,
        onThemeChanged = onThemeChanged,
        darkThemeToggle = darkThemeToggle,
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = defaultOnBack,
            entryProvider = entryProvider {
                entry<HomeRoute.Main> {
                    HomeScreen(
                        viewModel = homeViewModel,
                        deviceClass = deviceClass,
                        onBack = defaultOnBack,
                    )
                }
            }
        )
    }
}
