package prayit.simplebudget.feature.budgetitem.state

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import prayit.simplebudget.core.domain.model.Expense
import prayit.simplebudget.core.domain.repository.ExpenseRepository
import prayit.simplebudget.di.AppScope

@SingleIn(AppScope::class)
@Inject
class BudgetItemViewModel(
    private val expenseRepository: ExpenseRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow<BudgetItemState>(BudgetItemState.Loading)
    val state: StateFlow<BudgetItemState> = _state.asStateFlow()

    fun load(id: String) {
        _state.update { BudgetItemState.Loading }

        scope.launch {
            val expense = expenseRepository.getById(id)
            if (expense != null) {
                _state.update {
                    BudgetItemState.Content(
                    id = expense.id,
                    title = expense.title,
                    amount = expense.amount,
                    date = expense.date,
                    tag = expense.tag,
                    editTitle = expense.title,
                    editAmount = expense.amount.toString(),
                        editTag = ExpenseTag.entries.firstOrNull { it.name == expense.tag }
                            ?: ExpenseTag.Misc,
                    hasChanges = false,
                )
                }
            } else {
                _state.update { BudgetItemState.NotFound }
            }
        }
    }

    fun onTitleChanged(value: String) {
        _state.update {
            (it as? BudgetItemState.Content)?.copy(
                editTitle = value,
                hasChanges = value != it.title || it.editAmount != it.amount.toString() ||
                        it.editTag.name != it.tag,
            ) ?: it
        }
    }

    fun onAmountChanged(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _state.update {
                (it as? BudgetItemState.Content)?.copy(
                    editAmount = value,
                    hasChanges = it.editTitle != it.title || value != it.amount.toString() ||
                            it.editTag.name != it.tag,
                ) ?: it
            }
        }
    }

    fun onTagSelected(tag: ExpenseTag) {
        _state.update {
            (it as? BudgetItemState.Content)?.copy(
                editTag = tag,
                hasChanges = it.editTitle != it.title || it.editAmount != it.amount.toString() ||
                        tag.name != it.tag,
            ) ?: it
        }
    }

    fun onSaveChanges() {
        val content = _state.value as? BudgetItemState.Content ?: return
        val amount = content.editAmount.toDoubleOrNull() ?: return
        if (content.editTitle.isBlank() || amount <= 0.0) return

        scope.launch {
            expenseRepository.insertExpense(
                Expense(
                    id = content.id,
                    title = content.editTitle.trim(),
                    amount = amount,
                    date = content.date,
                    tag = content.editTag.name,
                )
            )
            _state.update {
                (it as? BudgetItemState.Content)?.copy(
                    title = content.editTitle.trim(),
                    amount = amount,
                    tag = content.editTag.name,
                    editTitle = content.editTitle.trim(),
                    editAmount = amount.toString(),
                    hasChanges = false,
                ) ?: it
            }
        }
    }

    fun onDeleteRequest() {
        _state.update { (it as? BudgetItemState.Content)?.copy(showDeleteDialog = true) ?: it }
    }

    fun onDeleteDismiss() {
        _state.update { (it as? BudgetItemState.Content)?.copy(showDeleteDialog = false) ?: it }
    }

    fun onDeleteConfirm() {
        val content = _state.value as? BudgetItemState.Content ?: return
        _state.update { (it as? BudgetItemState.Content)?.copy(showDeleteDialog = false) ?: it }
        scope.launch {
            expenseRepository.deleteExpense(content.id)
            _state.update { (it as? BudgetItemState.Content)?.copy(isDeleted = true) ?: it }
        }
    }
}
