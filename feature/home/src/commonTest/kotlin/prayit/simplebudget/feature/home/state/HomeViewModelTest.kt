package prayit.simplebudget.feature.home.state

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import prayit.simplebudget.core.components.navigation.SnackbarType
import prayit.simplebudget.core.domain.model.AppSettings
import prayit.simplebudget.core.domain.model.Expense
import prayit.simplebudget.core.domain.repository.ExpenseRepository
import prayit.simplebudget.core.domain.repository.ExportRepository
import prayit.simplebudget.core.domain.repository.SettingsRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val expenses = MutableStateFlow<List<Expense>>(emptyList())
    private val settings = MutableStateFlow(AppSettings())

    private val fakeExpenseRepository = object : ExpenseRepository {
        override fun getExpenses(): Flow<List<Expense>> = expenses
        override suspend fun getById(id: String): Expense? = expenses.value.firstOrNull { it.id == id }
        override suspend fun insertExpense(expense: Expense) {
            expenses.value = expenses.value + expense
        }
        override suspend fun deleteExpense(id: String) {
            expenses.value = expenses.value.filterNot { it.id == id }
        }
    }

    private val exportCalls = mutableListOf<String>()
    private val fakeExportRepository = object : ExportRepository {
        override suspend fun exportMonthCsv(expenses: List<Expense>, monthNumber: Int, year: Int): Result<Unit> {
            exportCalls.add("exportMonthCsv:$monthNumber:$year")
            return Result.success(Unit)
        }
        override suspend fun exportMonthXlsx(expenses: List<Expense>, monthNumber: Int, year: Int): Result<Unit> {
            exportCalls.add("exportMonthXlsx:$monthNumber:$year")
            return Result.success(Unit)
        }
        override suspend fun exportHistoryXlsx(expenses: List<Expense>): Result<Unit> {
            exportCalls.add("exportHistoryXlsx")
            return Result.success(Unit)
        }
        override fun supportsAutoCapture(): Boolean = false
        override fun isAutoCaptureEnabled(): Boolean = false
        override fun openAutoCaptureSettings() {}
        override suspend fun sendExportEmail(expenses: List<Expense>, monthNumber: Int, year: Int, attachmentName: String): Result<Unit> {
            exportCalls.add("sendExportEmail:$monthNumber:$year:$attachmentName")
            return Result.success(Unit)
        }
        override suspend fun sendCsvEmail(expenses: List<Expense>, monthNumber: Int, year: Int): Result<Unit> {
            exportCalls.add("sendCsvEmail:$monthNumber:$year")
            return Result.success(Unit)
        }
        override suspend fun sendHistoryEmail(expenses: List<Expense>): Result<Unit> {
            exportCalls.add("sendHistoryEmail")
            return Result.success(Unit)
        }
        override suspend fun sendTestEmail(): Result<Unit> {
            exportCalls.add("sendTestEmail")
            return Result.success(Unit)
        }
    }

    private val fakeSettingsRepository = object : SettingsRepository {
        override fun getSettings(): Flow<AppSettings> = settings
        override suspend fun updateSettings(settings: AppSettings) {
            this@HomeViewModelTest.settings.value = settings
        }
    }

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        exportCalls.clear()
        expenses.value = emptyList()
        settings.value = AppSettings()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = HomeViewModel(
        expenseRepository = fakeExpenseRepository,
        exportRepository = fakeExportRepository,
        settingsRepository = fakeSettingsRepository,
        coroutineContext = testDispatcher,
    )

    @Test
    fun exportMonthCsv_noAutoExport_callsExportMonthCsv() = runTest {
        settings.value = AppSettings(autoExportWithManual = false)
        val vm = createViewModel()

        vm.onExportMonth()
        advanceUntilIdle()

        assertEquals(1, exportCalls.size)
        assertTrue(exportCalls[0].startsWith("exportMonthCsv:"))
    }

    @Test
    fun exportMonthCsv_withAutoExport_callsSendCsvEmail() = runTest {
        settings.value = AppSettings(autoExportWithManual = true)
        val vm = createViewModel()

        vm.onExportMonth()
        advanceUntilIdle()

        assertEquals(1, exportCalls.size)
        assertTrue(exportCalls[0].startsWith("sendCsvEmail:"))
    }

    @Test
    fun exportMonthXlsx_noAutoExport_callsExportMonthXlsx() = runTest {
        settings.value = AppSettings(autoExportWithManual = false)
        val vm = createViewModel()

        vm.onExportMonthXlsx()
        advanceUntilIdle()

        assertEquals(1, exportCalls.size)
        assertTrue(exportCalls[0].startsWith("exportMonthXlsx:"))
    }

    @Test
    fun exportMonthXlsx_withAutoExport_callsSendExportEmail() = runTest {
        settings.value = AppSettings(autoExportWithManual = true)
        val vm = createViewModel()

        vm.onExportMonthXlsx()
        advanceUntilIdle()

        assertEquals(1, exportCalls.size)
        assertTrue(exportCalls[0].startsWith("sendExportEmail:"))
    }

    @Test
    fun exportHistory_noAutoExport_callsExportHistoryXlsx() = runTest {
        settings.value = AppSettings(autoExportWithManual = false)
        val vm = createViewModel()

        vm.onExportHistory()
        advanceUntilIdle()

        assertEquals(1, exportCalls.size)
        assertEquals("exportHistoryXlsx", exportCalls[0])
    }

    @Test
    fun exportHistory_withAutoExport_callsSendHistoryEmail() = runTest {
        settings.value = AppSettings(autoExportWithManual = true)
        val vm = createViewModel()

        vm.onExportHistory()
        advanceUntilIdle()

        assertEquals(1, exportCalls.size)
        assertEquals("sendHistoryEmail", exportCalls[0])
    }

    @Test
    fun exportMonthCsv_success_emitsSuccessSnackbar() = runTest {
        val vm = createViewModel()
        val messages = mutableListOf<Pair<String, SnackbarType>>()

        val job = backgroundScope.launch {
            vm.snackbarMessages.collect { messages.add(it.message to it.type) }
        }

        vm.onExportMonth()
        advanceUntilIdle()

        assertEquals(1, messages.size)
        assertEquals("Month CSV exported", messages[0].first)
        assertEquals(SnackbarType.SUCCESS, messages[0].second)

        job.cancel()
    }

    @Test
    fun exportMonthCsv_failure_emitsErrorSnackbar() = runTest {
        val failingExport = object : ExportRepository by fakeExportRepository {
            override suspend fun exportMonthCsv(expenses: List<Expense>, monthNumber: Int, year: Int): Result<Unit> =
                Result.failure(RuntimeException("Network error"))
        }
        settings.value = AppSettings(autoExportWithManual = false)
        val vm = HomeViewModel(fakeExpenseRepository, failingExport, fakeSettingsRepository, coroutineContext = testDispatcher)
        val messages = mutableListOf<Pair<String, SnackbarType>>()

        val job = backgroundScope.launch {
            vm.snackbarMessages.collect { messages.add(it.message to it.type) }
        }

        vm.onExportMonth()
        advanceUntilIdle()

        assertEquals(1, messages.size)
        assertEquals("Network error", messages[0].first)
        assertEquals(SnackbarType.ERROR, messages[0].second)

        job.cancel()
    }

    @Test
    fun onExportMonthXlsx_withAutoExport_doesNotCallShareSheet() = runTest {
        settings.value = AppSettings(autoExportWithManual = true)
        val vm = createViewModel()

        vm.onExportMonthXlsx()
        advanceUntilIdle()

        assertTrue(exportCalls.none { it.startsWith("exportMonthXlsx") })
        assertTrue(exportCalls.any { it.startsWith("sendExportEmail") })
    }
}
