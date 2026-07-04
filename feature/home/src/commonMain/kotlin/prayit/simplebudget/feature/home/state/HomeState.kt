package prayit.simplebudget.feature.home.state

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import prayit.simplebudget.core.utils.Month
import kotlin.time.Clock

private val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

sealed interface HomeState {
    data object Loading : HomeState
    data class Content(
        val currentMonth: Month = Month.entries[today.monthNumber - 1],
        val currentYear: Int = today.year,
        val items: List<ExpenseItem> = emptyList(),
        val totalSpent: Double = 0.0,
        val previousMonthTotal: Double = 0.0,
        val showAddSheet: Boolean = false,
        val title: String = "",
        val amount: String = "",
        val selectedTag: FinancialTag = FinancialTag.Misc,
        val selectedDate: LocalDate = today,
    ) : HomeState
    data class Error(val message: String) : HomeState
}
