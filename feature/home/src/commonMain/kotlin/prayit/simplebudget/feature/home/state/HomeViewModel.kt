package prayit.simplebudget.feature.home.state

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import prayit.simplebudget.core.domain.model.Expense
import prayit.simplebudget.core.domain.repository.ExpenseRepository
import prayit.simplebudget.core.domain.repository.ExportRepository
import prayit.simplebudget.core.utils.Month
import prayit.simplebudget.di.AppScope
import kotlin.time.Clock

@SingleIn(AppScope::class)
@Inject
class HomeViewModel(
    private val expenseRepository: ExpenseRepository,
    private val exportRepository: ExportRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    private val todayMonth = Month.entries[today.month.number - 1]

    private val _monthYear = MutableStateFlow(MonthYear(todayMonth, today.year))
    private val _showAddSheet = MutableStateFlow(false)
    private val _formState = MutableStateFlow(FormState())
    private val _exportError = MutableStateFlow<String?>(null)
    private val _notificationBanner = MutableStateFlow(false)

    val state: StateFlow<HomeState> = combine(
        expenseRepository.getExpenses(),
        _monthYear,
        _showAddSheet,
        _formState,
        _exportError,
    ) { expenses, monthYear, showAddSheet, form, exportError ->
        val filtered = expenses
            .filter { it.date.month.number == monthYear.month.ordinal + 1 && it.date.year == monthYear.year }
            .map { it.toItem() }

        val prevMonth = monthYear.month.previous()
        val prevYear = if (monthYear.month == Month.January) monthYear.year - 1 else monthYear.year

        HomeState.Content(
            currentMonth = monthYear.month,
            currentYear = monthYear.year,
            items = filtered,
            totalSpent = filtered.sumOf { it.amount },
            previousMonthTotal = expenses.totalForMonth(prevMonth, prevYear),
            showAddSheet = showAddSheet,
            title = form.title,
            amount = form.amount,
            selectedTag = form.selectedTag,
            selectedDate = form.selectedDate,
            exportError = exportError,
        )
    }.combine(_notificationBanner) { state, banner ->
        state.copy(showNotificationBanner = banner)
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), HomeState.Content())

    fun onPreviousMonth() {
        _monthYear.update {
            val prevMonth = it.month.previous()
            val prevYear = if (it.month == Month.January) it.year - 1 else it.year
            MonthYear(prevMonth, prevYear)
        }
    }

    fun onNextMonth() {
        _monthYear.update {
            val nextMonth = it.month.next()
            val nextYear = if (it.month == Month.December) it.year + 1 else it.year
            MonthYear(nextMonth, nextYear)
        }
    }

    fun onToggleAddSheet() {
        var opened = false
        _showAddSheet.update { opened = !it; !it }
        if (opened) _formState.update { FormState() }
    }

    fun onTitleChanged(value: String) {
        _formState.update { it.copy(title = value) }
    }

    fun onAmountChanged(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _formState.update { it.copy(amount = value) }
        }
    }

    fun onTagSelected(tag: FinancialTag) {
        _formState.update { it.copy(selectedTag = tag) }
    }

    fun onDateSelected(date: LocalDate) {
        _formState.update { it.copy(selectedDate = date) }
    }

    fun onConfirmAdd() {
        val form = _formState.value
        val amount = form.amount.toDoubleOrNull() ?: return
        if (form.title.isBlank() || amount <= 0.0) return

        scope.launch {
            expenseRepository.insertExpense(
                Expense(
                    id = "${Clock.System.now().toEpochMilliseconds()}",
                    title = form.title.trim(),
                    amount = amount,
                    date = form.selectedDate,
                    tag = form.selectedTag.name,
                )
            )
        }
        _showAddSheet.update { false }
        _formState.update { FormState() }
    }

    fun removeExpense(id: String) {
        scope.launch {
            expenseRepository.deleteExpense(id)
        }
    }

    fun onExportMonth() {
        scope.launch {
            val allExpenses = expenseRepository.getExpenses().first()
            val my = _monthYear.value
            exportRepository.exportMonthCsv(allExpenses, my.month.ordinal + 1, my.year)
                .onFailure(::showExportError)
        }
    }

    fun onExportMonthXlsx() {
        scope.launch {
            val allExpenses = expenseRepository.getExpenses().first()
            val my = _monthYear.value
            exportRepository.exportMonthXlsx(allExpenses, my.month.ordinal + 1, my.year)
                .onFailure(::showExportError)
        }
    }

    fun onExportHistory() {
        scope.launch {
            val allExpenses = expenseRepository.getExpenses().first()
            exportRepository.exportHistoryXlsx(allExpenses)
                .onFailure(::showExportError)
        }
    }

    fun onExportErrorDismiss() {
        _exportError.update { null }
    }

    fun refreshNotificationBanner() {
        _notificationBanner.update { exportRepository.supportsAutoCapture() && !exportRepository.isAutoCaptureEnabled() }
    }

    fun onNotificationBannerDismiss() {
        _notificationBanner.update { false }
    }

    fun openNotificationSettings() {
        exportRepository.openAutoCaptureSettings()
    }

    private fun showExportError(throwable: Throwable) {
        _exportError.update { throwable.message ?: "Export failed" }
    }
}

private data class MonthYear(val month: Month, val year: Int)

private data class FormState(
    val title: String = "",
    val amount: String = "",
    val selectedTag: FinancialTag = FinancialTag.Misc,
    val selectedDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
)

private fun Expense.toItem() = ExpenseItem(
    id = id,
    title = title,
    amount = amount,
    date = date,
    tag = FinancialTag.entries.firstOrNull { it.name == tag } ?: FinancialTag.Misc,
)

private fun List<Expense>.totalForMonth(month: Month, year: Int): Double =
    filter { it.date.month.number == month.ordinal + 1 && it.date.year == year }
        .sumOf { it.amount }

private fun Month.previous(): Month {
    val values = Month.entries
    val index = values.indexOf(this)
    return if (index == 0) values.last() else values[index - 1]
}

private fun Month.next(): Month {
    val values = Month.entries
    val index = values.indexOf(this)
    return if (index == values.lastIndex) values.first() else values[index + 1]
}
