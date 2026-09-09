package prayit.simplebudget.di

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import prayit.simplebudget.core.domain.model.Expense
import prayit.simplebudget.core.domain.repository.ExpenseRepository

@ContributesBinding(AppScope::class)
@Inject
class InMemoryExpenseRepository : ExpenseRepository {
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    override fun getExpenses(): Flow<List<Expense>> = _expenses.asStateFlow()
    override suspend fun getById(id: String): Expense? = _expenses.value.firstOrNull { it.id == id }
    override suspend fun insertExpense(expense: Expense) {
        _expenses.update { it + expense }
    }
    override suspend fun deleteExpense(id: String) {
        _expenses.update { list -> list.filterNot { it.id == id } }
    }
}
