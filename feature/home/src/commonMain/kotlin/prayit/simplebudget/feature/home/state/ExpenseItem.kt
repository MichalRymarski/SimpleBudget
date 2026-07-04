package prayit.simplebudget.feature.home.state

import kotlinx.datetime.LocalDate

data class ExpenseItem(
    val id: String,
    val title: String,
    val amount: Double,
    val date: LocalDate,
    val tag: FinancialTag,
)
