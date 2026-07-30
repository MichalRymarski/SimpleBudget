package prayit.simplebudget.feature.budgetitem.state

import kotlinx.datetime.LocalDate

sealed interface BudgetItemState {
    data object Loading : BudgetItemState

    data class Content(
        val id: String = "",
        val title: String = "",
        val amount: Double = 0.0,
        val date: LocalDate = LocalDate(2026, 1, 1),
        val tag: String = "",
        val editTitle: String = title,
        val editAmount: String = "",
        val hasChanges: Boolean = false,
    ) : BudgetItemState

    data object NotFound : BudgetItemState
}
