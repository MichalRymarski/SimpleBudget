package prayit.simplebudget.feature.home.state

import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import prayit.simplebudget.core.domain.model.Expense
import prayit.simplebudget.core.domain.repository.ExpenseRepository
import prayit.simplebudget.core.utils.Month
import prayit.simplebudget.export.CsvGenerator
import prayit.simplebudget.export.shareCsvFile
import prayit.simplebudget.export.generateXlsx
import prayit.simplebudget.export.shareXlsxFile
import kotlin.time.Clock

@Inject
class HomeViewModel(
    private val expenseRepository: ExpenseRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    private val todayMonth = Month.entries[today.monthNumber - 1]

    private val _monthYear = MutableStateFlow(MonthYear(todayMonth, today.year))
    private val _showAddSheet = MutableStateFlow(false)
    private val _formState = MutableStateFlow(FormState())

    val state: StateFlow<HomeState> = combine(
        expenseRepository.getExpenses(),
        _monthYear,
        _showAddSheet,
        _formState,
    ) { expenses, monthYear, showAddSheet, form ->
        val filtered = expenses
            .filter { it.date.monthNumber == monthYear.month.ordinal + 1 && it.date.year == monthYear.year }
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
        )
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
        _showAddSheet.update { !it }
        if (_showAddSheet.value) {
            _formState.value = FormState()
        }
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
        _showAddSheet.value = false
        _formState.value = FormState()
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
            val csv = CsvGenerator.generateSingleMonth(allExpenses, my.month, my.year)
            val monthNum = (my.month.ordinal + 1).toString().padStart(2, '0')
            val fileName = "Budget-$monthNum.${my.year}.csv"
            shareCsvFile(fileName, csv, "Budget-$monthNum.${my.year}")
        }
    }

    fun onExportHistory() {
        scope.launch {
            val allExpenses = expenseRepository.getExpenses().first()
            val xlsx = generateXlsx(allExpenses)
            shareXlsxFile("Budget-history.xlsx", xlsx, "Budget history")
        }
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
    tag = FinancialTag.entries.first { it.name == tag },
)

private fun List<Expense>.totalForMonth(month: Month, year: Int): Double =
    filter { it.date.monthNumber == month.ordinal + 1 && it.date.year == year }
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
