package prayit.simplebudget.di

import dev.zacsweers.metro.DependencyGraph
import io.ktor.client.HttpClient
import prayit.simplebudget.core.domain.repository.ExpenseRepository
import prayit.simplebudget.feature.budgetitem.state.BudgetItemViewModel
import prayit.simplebudget.feature.home.state.HomeViewModel

@DependencyGraph(AppScope::class)
interface AppGraph {
    val httpClient: HttpClient
    val expenseRepository: ExpenseRepository
    val homeViewModel: HomeViewModel
    val budgetItemViewModel: BudgetItemViewModel
}
