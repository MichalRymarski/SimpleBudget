package prayit.simplebudget.core.data.repository

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import prayit.simplebudget.core.data.dao.ExpenseDao
import prayit.simplebudget.core.data.entity.ExpenseEntity
import prayit.simplebudget.core.domain.model.Expense
import prayit.simplebudget.core.domain.repository.ExpenseRepository
import prayit.simplebudget.di.AppScope

@ContributesBinding(AppScope::class)
@Inject
class ExpenseRepositoryImpl(
    private val expenseDao: ExpenseDao,
) : ExpenseRepository {

    override fun getExpenses(): Flow<List<Expense>> {
        return expenseDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getById(id: String): Expense? {
        return expenseDao.getById(id)?.toDomain()
    }

    override suspend fun insertExpense(expense: Expense) {
        expenseDao.insert(expense.toEntity())
    }

    override suspend fun deleteExpense(id: String) {
        expenseDao.deleteById(id)
    }
}

private fun ExpenseEntity.toDomain() = Expense(
    id = id,
    title = title,
    amount = amount,
    date = kotlinx.datetime.LocalDate.fromEpochDays(date.toInt()),
    tag = tag,
)

private fun Expense.toEntity() = ExpenseEntity(
    id = id,
    title = title,
    amount = amount,
    date = date.toEpochDays().toLong(),
    tag = tag,
)
