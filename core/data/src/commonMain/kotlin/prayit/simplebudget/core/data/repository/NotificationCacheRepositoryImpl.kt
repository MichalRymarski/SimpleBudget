package prayit.simplebudget.core.data.repository

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import prayit.simplebudget.core.data.dao.ExpenseDao
import prayit.simplebudget.core.data.dao.NotificationCacheDao
import prayit.simplebudget.core.data.entity.ExpenseEntity
import prayit.simplebudget.core.data.entity.NotificationCacheEntity
import prayit.simplebudget.core.domain.model.Expense
import prayit.simplebudget.core.domain.repository.PendingExpenseRepository
import prayit.simplebudget.di.AppScope
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Deterministic cache id for an (amount, date) pair. Collisions across
 * different pairs are possible in theory (64-bit space) but negligible in
 * practice; a collision would only drop a same-day duplicate capture.
 */
internal fun dedupId(amount: Double, date: Long): Long = amount.toBits() xor date

@ContributesBinding(AppScope::class)
@Inject
class NotificationCacheRepositoryImpl(
    private val cacheDao: NotificationCacheDao,
    private val expenseDao: ExpenseDao,
) : PendingExpenseRepository {

    @OptIn(ExperimentalTime::class)
    override suspend fun stageAndCommit(expense: Expense): Boolean {
        purgeExpired()
        val rowId = cacheDao.insert(
            NotificationCacheEntity(
                id = dedupId(expense.amount, expense.date.toEpochDays()),
                title = expense.title,
                amount = expense.amount,
                date = expense.date.toEpochDays(),
                tag = expense.tag,
                capturedAt = Clock.System.now().toEpochMilliseconds(),
            )
        )
        if (rowId == -1L) return false
        expenseDao.insert(expense.toEntity())
        return true
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
