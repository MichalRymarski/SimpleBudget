package prayit.simplebudget.di

import dev.zacsweers.metro.DependencyGraph
import io.ktor.client.HttpClient
import prayit.simplebudget.core.domain.repository.ExpenseRepository
import prayit.simplebudget.core.domain.repository.ExportRepository
import prayit.simplebudget.core.domain.repository.PendingExpenseRepository
import prayit.simplebudget.core.domain.repository.SettingsRepository
import prayit.simplebudget.feature.budgetitem.state.BudgetItemViewModel
import prayit.simplebudget.feature.home.state.HomeViewModel
import prayit.simplebudget.feature.settings.state.SettingsViewModel

@DependencyGraph(AppScope::class)
interface AppGraph {
    val httpClient: HttpClient
    val expenseRepository: ExpenseRepository
    val exportRepository: ExportRepository
    val pendingExpenseRepository: PendingExpenseRepository
    val settingsRepository: SettingsRepository
    val homeViewModel: HomeViewModel
    val budgetItemViewModel: BudgetItemViewModel
    val settingsViewModel: SettingsViewModel
    val snackbarViewModel: SnackbarViewModel
}
