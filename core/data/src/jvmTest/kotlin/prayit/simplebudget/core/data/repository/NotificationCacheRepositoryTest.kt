package prayit.simplebudget.core.data.repository

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import prayit.simplebudget.core.data.dao.ExpenseDao
import prayit.simplebudget.core.data.dao.NotificationCacheDao
import prayit.simplebudget.core.data.dbSetup.AppDatabase
import prayit.simplebudget.core.data.dbSetup.AppDatabase_Impl
import prayit.simplebudget.core.data.entity.ExpenseEntity
import prayit.simplebudget.core.data.entity.NotificationCacheEntity
import prayit.simplebudget.core.domain.model.Expense
import java.io.File
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

    private val gate = Mutex()

    override suspend fun insert(entry: NotificationCacheEntity): Long = gate.withLock {
        if (rows.any { it.id == entry.id }) return -1L
        rows += entry
        entry.id
    }

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

@OptIn(ExperimentalTime::class)
private fun testExpense(id: String, amount: Double = 45.99) = Expense(
    id = id,
    title = "Biedronka",
    amount = amount,
    date = LocalDate(2026, 7, 3),
    tag = "Groceries",
)

class NotificationCacheRepositoryTest {

    @Test
    fun stagedExpenseBecomesRealExpenseAndSurvivesCacheExpiry() = runTest {
        val expenses = FakeExpenseDao()
        val cache = FakeNotificationCacheDao()
        val repository =
            NotificationCacheRepositoryImpl(cache, expenses)
        val title = "Biedronka"
        val expense = testExpense("notif_1")

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

    @Test
    fun sequentialDuplicateIsRejected() = runTest {
        val expenses = FakeExpenseDao()
        val cache = FakeNotificationCacheDao()
        val repository = NotificationCacheRepositoryImpl(cache, expenses)

        assertTrue(repository.stageAndCommit(testExpense("notif_1")))
        assertTrue(!repository.stageAndCommit(testExpense("notif_2")))

        assertEquals(1, expenses.getAllList().size, "duplicate must not commit twice")
    }

    @Test
    fun concurrentDuplicatesCommitOnlyOnceAgainstRealDb(): Unit = runBlocking {
        val dbFile = File(
            System.getProperty("java.io.tmpdir"),
            "dedup-test-${System.nanoTime()}.db",
        )
        val db: AppDatabase = Room.databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath,
            factory = ::AppDatabase_Impl,
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
        try {
            val repository = NotificationCacheRepositoryImpl(
                db.notificationCacheDao(),
                db.expenseDao(),
            )
            // 100 simultaneous captures of the same payment, distinct ids like
            // two notifications for one purchase arriving at once.
            (1..100).map { i ->
                async(Dispatchers.Default) {
                    repository.stageAndCommit(testExpense("notif_$i"))
                }
            }.awaitAll()

            val committed = db.expenseDao().getAll().first()
            assertEquals(1, committed.size, "exactly one expense must be committed")
        } finally {
            db.close()
            dbFile.delete()
        }
    }
}

private suspend fun FakeExpenseDao.getAllList(): List<ExpenseEntity> = getAll().first()
