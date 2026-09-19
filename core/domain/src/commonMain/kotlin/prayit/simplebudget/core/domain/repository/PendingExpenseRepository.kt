package prayit.simplebudget.core.domain.repository

import prayit.simplebudget.core.domain.model.Expense
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

interface PendingExpenseRepository {
    suspend fun stageAndCommit(expense: Expense): Boolean

    @OptIn(ExperimentalTime::class)
    suspend fun purgeExpired(nowEpochMillis: Long = Clock.System.now().toEpochMilliseconds())
}
