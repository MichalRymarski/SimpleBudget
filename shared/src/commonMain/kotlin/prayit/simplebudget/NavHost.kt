package prayit.simplebudget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import prayit.simplebudget.core.components.navigation.LocalSnackbarHostState
import prayit.simplebudget.core.components.navigation.LocalSnackbarType
import prayit.simplebudget.core.components.navigation.SnackbarType
import prayit.simplebudget.core.components.theme.MParafiaTheme
import prayit.simplebudget.di.Graph
import prayit.simplebudget.feature.budgetitem.ui.BudgetItemScreen
import prayit.simplebudget.feature.home.ui.HomeScreen
import prayit.simplebudget.feature.settings.ui.SettingsScreen
import prayit.simplebudget.navigation.BudgetItemRoute
import prayit.simplebudget.navigation.HomeRoute
import prayit.simplebudget.navigation.SettingsRoute
import prayit.simplebudget.navigation.navConfig
import prayit.simplebudget.navigation.rememberDeviceClass

@Composable
fun NavHost(
    darkThemeToggle: @Composable (() -> Unit)? = null,
    onThemeChanged: @Composable ((isDark: Boolean) -> Unit) = {},
    isDark: Boolean? = null,
) {
    val backStack = rememberNavBackStack(navConfig, HomeRoute.Main)
    val deviceClass = rememberDeviceClass()
    val defaultOnBack: () -> Unit = { backStack.removeLastOrNull() }

    val homeViewModel = Graph.app.homeViewModel
    val budgetItemViewModel = Graph.app.budgetItemViewModel
    val settingsViewModel = Graph.app.settingsViewModel
    val snackbarViewModel = Graph.app.snackbarViewModel
    val snackbarHostState = remember { snackbarViewModel.snackbarHostState }

    MParafiaTheme(
        isDark = isDark,
        onThemeChanged = onThemeChanged,
        darkThemeToggle = darkThemeToggle,
    ) {
        CompositionLocalProvider(
            LocalSnackbarHostState provides snackbarHostState,
            LocalSnackbarType provides snackbarViewModel.currentType,
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
                            onExpenseClick = { id -> backStack.add(BudgetItemRoute(id)) },
                            onOpenSettings = { backStack.add(SettingsRoute) },
                            onSnackbarMessage = { snackbarViewModel.showSnackbar(it.message, it.type) },
                        )
                    }
                    entry<BudgetItemRoute> { route ->
                        BudgetItemScreen(
                            viewModel = budgetItemViewModel,
                            id = route.id,
                            deviceClass = deviceClass,
                            onBack = defaultOnBack,
                        )
                    }
                entry<SettingsRoute> {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        deviceClass = deviceClass,
                        onBack = defaultOnBack,
                        onSnackbarMessage = { snackbarViewModel.showSnackbar(it.message, it.type) },
                    )
                }
                }
            )
        }
    }
}
