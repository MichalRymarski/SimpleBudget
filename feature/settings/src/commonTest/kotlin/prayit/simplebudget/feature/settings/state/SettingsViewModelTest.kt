package prayit.simplebudget.feature.settings.state

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import prayit.simplebudget.core.components.navigation.SnackbarType
import prayit.simplebudget.core.domain.model.AppSettings
import prayit.simplebudget.core.domain.model.Expense
import prayit.simplebudget.core.domain.repository.ExportRepository
import prayit.simplebudget.core.domain.repository.SettingsRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val settings = MutableStateFlow(AppSettings())

    private val fakeSettingsRepository = object : SettingsRepository {
        override fun getSettings(): Flow<AppSettings> = settings
        override suspend fun updateSettings(settings: AppSettings) {
            this@SettingsViewModelTest.settings.value = settings
        }
    }

    private val exportCalls = mutableListOf<String>()
    private val fakeExportRepository = object : ExportRepository {
        override suspend fun exportMonthCsv(expenses: List<Expense>, monthNumber: Int, year: Int) = Result.success(Unit)
        override suspend fun exportMonthXlsx(expenses: List<Expense>, monthNumber: Int, year: Int) = Result.success(Unit)
        override suspend fun exportHistoryXlsx(expenses: List<Expense>) = Result.success(Unit)
        override fun supportsAutoCapture() = false
        override fun isAutoCaptureEnabled() = false
        override fun openAutoCaptureSettings() {}
        override suspend fun sendExportEmail(expenses: List<Expense>, monthNumber: Int, year: Int, attachmentName: String) = Result.success(Unit)
        override suspend fun sendCsvEmail(expenses: List<Expense>, monthNumber: Int, year: Int) = Result.success(Unit)
        override suspend fun sendHistoryEmail(expenses: List<Expense>) = Result.success(Unit)
        override suspend fun sendTestEmail(): Result<Unit> {
            exportCalls.add("sendTestEmail")
            return Result.success(Unit)
        }
    }

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        exportCalls.clear()
        settings.value = AppSettings()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SettingsViewModel(
        settingsRepository = fakeSettingsRepository,
        exportRepository = fakeExportRepository,
        coroutineContext = testDispatcher,
    )

    @Test
    fun sendTestEmail_success_emitsSuccessSnackbar() = runTest {
        val vm = createViewModel()
        val messages = mutableListOf<Pair<String, SnackbarType>>()

        val job = backgroundScope.launch(testDispatcher) {
            vm.snackbarMessages.collect { messages.add(it.message to it.type) }
        }

        vm.onSendTestEmail()
        advanceUntilIdle()

        assertEquals(1, messages.size)
        assertEquals("Test email sent", messages[0].first)
        assertEquals(SnackbarType.SUCCESS, messages[0].second)

        job.cancel()
    }

    @Test
    fun sendTestEmail_failure_emitsErrorSnackbar() = runTest {
        val failingExport = object : ExportRepository by fakeExportRepository {
            override suspend fun sendTestEmail(): Result<Unit> =
                Result.failure(RuntimeException("SMTP error"))
        }
        val vm = SettingsViewModel(fakeSettingsRepository, failingExport, coroutineContext = testDispatcher)
        val messages = mutableListOf<Pair<String, SnackbarType>>()

        val job = backgroundScope.launch(testDispatcher) {
            vm.snackbarMessages.collect { messages.add(it.message to it.type) }
        }

        vm.onSendTestEmail()
        advanceUntilIdle()

        assertEquals(1, messages.size)
        assertEquals("SMTP error", messages[0].first)
        assertEquals(SnackbarType.ERROR, messages[0].second)

        job.cancel()
    }

    @Test
    fun onRecipientEmailChanged_updatesSettings() = runTest {
        val vm = createViewModel()
        val stateJob = backgroundScope.launch(testDispatcher) { vm.state.collect {} }

        vm.onRecipientEmailChanged("test@example.com")
        advanceUntilIdle()

        assertEquals("test@example.com", settings.value.recipientEmail)
        stateJob.cancel()
    }

    @Test
    fun onSenderEmailChanged_updatesSettings() = runTest {
        val vm = createViewModel()
        val stateJob = backgroundScope.launch(testDispatcher) { vm.state.collect {} }

        vm.onSenderEmailChanged("sender@example.com")
        advanceUntilIdle()

        assertEquals("sender@example.com", settings.value.senderEmail)
        stateJob.cancel()
    }

    @Test
    fun onAppPasswordChanged_updatesSettings() = runTest {
        val vm = createViewModel()
        val stateJob = backgroundScope.launch(testDispatcher) { vm.state.collect {} }

        vm.onAppPasswordChanged("secret123")
        advanceUntilIdle()

        assertEquals("secret123", settings.value.appPassword)
        stateJob.cancel()
    }

    @Test
    fun onAutoExportToggle_updatesSettings() = runTest {
        val vm = createViewModel()
        val stateJob = backgroundScope.launch(testDispatcher) { vm.state.collect {} }

        vm.onAutoExportToggle(true)
        advanceUntilIdle()

        assertTrue(settings.value.autoExportEnabled)
        stateJob.cancel()
    }

    @Test
    fun onAutoExportWithManualToggle_updatesSettings() = runTest {
        val vm = createViewModel()
        val stateJob = backgroundScope.launch(testDispatcher) { vm.state.collect {} }

        vm.onAutoExportWithManualToggle(true)
        advanceUntilIdle()

        assertTrue(settings.value.autoExportWithManual)
        stateJob.cancel()
    }

    @Test
    fun state_isLoadingInitially() = runTest {
        val vm = createViewModel()
        assertEquals(SettingsState.Loading, vm.state.value)
    }

    @Test
    fun state_becomesContentAfterLoading() = runTest {
        val vm = createViewModel()
        val stateJob = backgroundScope.launch(testDispatcher) { vm.state.collect {} }
        advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is SettingsState.Content)
        assertEquals(AppSettings(), state.settings)
        stateJob.cancel()
    }
}
