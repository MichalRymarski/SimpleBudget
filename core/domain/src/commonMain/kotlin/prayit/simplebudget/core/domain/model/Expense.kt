package prayit.simplebudget.core.domain.model

import kotlinx.datetime.LocalDate

data class Expense(
    val id: String,
    val title: String,
    val amount: Double,
    val date: LocalDate,
    val tag: String,
)
