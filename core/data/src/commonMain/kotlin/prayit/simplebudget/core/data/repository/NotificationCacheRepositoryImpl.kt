package prayit.simplebudget.core.data.repository

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import prayit.simplebudget.core.data.dao.ExpenseDao
import prayit.simplebudget.core.data.dao.NotificationCacheDao
import prayit.simplebudget.core.data.entity.ExpenseEntity
import prayit.simplebudget.core.data.entity.NotificationCacheEntity
import prayit.simplebudget.core.domain.model.Expense
import prayit.simplebudget.core.domain.repository.PendingExpenseRepository
import prayit.simplebudget.di.AppScope
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@ContributesBinding(AppScope::class)
@Inject
class NotificationCacheRepositoryImpl(
    private val cacheDao: NotificationCacheDao,
    private val expenseDao: ExpenseDao,
) : PendingExpenseRepository {

    private val dedupMutex = Mutex()

    @OptIn(ExperimentalTime::class)
    override suspend fun stageAndCommit(expense: Expense) = dedupMutex.withLock {
        purgeExpired()
        val dateEpochDays = expense.date.toEpochDays()
        if (cacheDao.existsDuplicate(expense.amount, dateEpochDays)) {
            return@withLock
        }
        cacheDao.insert(
            NotificationCacheEntity(
                title = expense.title,
                amount = expense.amount,
                date = dateEpochDays,
                tag = expense.tag,
                capturedAt = Clock.System.now().toEpochMilliseconds(),
            )
        )
        expenseDao.insert(expense.toEntity())
    }

    override suspend fun purgeExpired(nowEpochMillis: Long) {
        cacheDao.deleteOlderThan(nowEpochMillis - EXPIRY_MILLIS)
    }

    companion object {
        private const val EXPIRY_MILLIS = 5_000L
    }
}

private fun Expense.toEntity() = ExpenseEntity(
    id = id,
    title = title,
    amount = amount,
    date = date.toEpochDays(),
    tag = tag,
)
