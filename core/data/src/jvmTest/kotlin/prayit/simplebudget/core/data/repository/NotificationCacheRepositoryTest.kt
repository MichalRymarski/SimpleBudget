package prayit.simplebudget.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import prayit.simplebudget.core.data.dao.ExpenseDao
import prayit.simplebudget.core.data.dao.NotificationCacheDao
import prayit.simplebudget.core.data.entity.ExpenseEntity
import prayit.simplebudget.core.data.entity.NotificationCacheEntity
import prayit.simplebudget.core.domain.model.Expense
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private class FakeExpenseDao : ExpenseDao {
    private val rows = MutableStateFlow<List<ExpenseEntity>>(emptyList())

    override fun getAll(): Flow<List<ExpenseEntity>> = rows.asStateFlow()

    override suspend fun getById(id: String): ExpenseEntity? =
        rows.value.firstOrNull { it.id == id }

    override suspend fun insert(expense: ExpenseEntity) {
        rows.update { list -> list.filterNot { it.id == expense.id } + expense }
    }

    override suspend fun deleteById(id: String) {
        rows.update { list -> list.filterNot { it.id == id } }
    }
}

private class FakeNotificationCacheDao : NotificationCacheDao {
    val rows = mutableListOf<NotificationCacheEntity>()
    private var nextId = 1L

    override suspend fun insert(entry: NotificationCacheEntity) {
        rows += entry.copy(id = nextId++)
    }

    override suspend fun existsDuplicate(
        title: String,
        amount: Double,
        date: Long,
        tag: String,
    ): Boolean =
        rows.any { it.title == title && it.amount == amount && it.date == date && it.tag == tag }

    override suspend fun deleteOlderThan(cutoff: Long) {
        rows.removeAll { it.capturedAt < cutoff }
    }

    override suspend fun purgeAll() {
        rows.clear()
    }

    fun ageAll(byMillis: Long) {
        rows.replaceAll { it.copy(capturedAt = it.capturedAt - byMillis) }
    }
}

class NotificationCacheRepositoryTest {

    @OptIn(ExperimentalTime::class)
    @Test
    fun stagedExpenseBecomesRealExpenseAndSurvivesCacheExpiry() = runTest {
        val expenses = FakeExpenseDao()
        val cache = FakeNotificationCacheDao()
        val repository = NotificationCacheRepositoryImpl(cache, expenses)
        val title = "Biedronka"
        val expense = Expense(
            id = "notif_1",
            title = title,
            amount = 45.99,
            date = LocalDate(2026, 7, 3),
            tag = "Groceries",
        )

        repository.stageAndCommit(expense)

        assertEquals(1, expenses.getAllList().size, "real expense must be committed")
        assertEquals(1, cache.rows.size, "stage must be cached")
        assertEquals(title, expenses.getAllList().first().title)

        // Simulate the end of the 5-second staged lifetime.
        cache.ageAll(byMillis = 6_000)
        repository.purgeExpired(Clock.System.now().toEpochMilliseconds())

        assertTrue(cache.rows.isEmpty(), "expired stage must be cleared")
        assertEquals(1, expenses.getAllList().size, "real expense must survive cache expiry")
        assertEquals(title, expenses.getAllList().first().title)
    }
}

private suspend fun FakeExpenseDao.getAllList(): List<ExpenseEntity> = getAll().first()