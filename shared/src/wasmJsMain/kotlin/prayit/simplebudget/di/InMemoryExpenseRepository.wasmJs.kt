package prayit.simplebudget.di

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import prayit.simplebudget.core.domain.model.Expense
import prayit.simplebudget.core.domain.repository.ExpenseRepository
import prayit.simplebudget.core.domain.repository.PendingExpenseRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@ContributesBinding(AppScope::class, binding = binding<ExpenseRepository>())
@ContributesBinding(AppScope::class, binding = binding<PendingExpenseRepository>())
@SingleIn(AppScope::class)
@Inject
class InMemoryExpenseRepository :
    ExpenseRepository,
    PendingExpenseRepository {
    private data class Entry(val expense: Expense, val capturedAt: Long)

    private val _entries = MutableStateFlow<List<Entry>>(emptyList())

    override fun getExpenses(): Flow<List<Expense>> =
        _entries.map { list -> list.map { it.expense } }

    override suspend fun getById(id: String): Expense? =
        _entries.value.firstOrNull { it.expense.id == id }?.expense

    override suspend fun insertExpense(expense: Expense) {
        _entries.update { it + Entry(expense, now()) }
    }

    override suspend fun deleteExpense(id: String) {
        _entries.update { list -> list.filterNot { it.expense.id == id } }
    }

    override suspend fun stageAndCommit(expense: Expense) {
        purgeExpired()
        val dateEpochDays = expense.date.toEpochDays()
        val duplicate = _entries.value.any {
            it.expense.title == expense.title &&
                    it.expense.amount == expense.amount &&
                    it.expense.date.toEpochDays() == dateEpochDays &&
                    it.expense.tag == expense.tag
        }
        if (duplicate) return
        _entries.update { it + Entry(expense, now()) }
    }

    override suspend fun purgeExpired(nowEpochMillis: Long) {
        _entries.update { list -> list.filterNot { nowEpochMillis - it.capturedAt > EXPIRY_MILLIS } }
    }

    companion object {
        private const val EXPIRY_MILLIS = 5_000L
    }
}

@OptIn(ExperimentalTime::class)
private fun now(): Long = Clock.System.now().toEpochMilliseconds()
