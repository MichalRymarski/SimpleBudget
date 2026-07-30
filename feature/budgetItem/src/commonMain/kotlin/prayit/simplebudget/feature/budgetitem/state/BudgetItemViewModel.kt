package prayit.simplebudget.feature.budgetitem.state

import dev.zacsweers.metro.Inject
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

@Inject
class BudgetItemViewModel(
    private val expenseRepository: ExpenseRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow<BudgetItemState>(BudgetItemState.Loading)
    val state: StateFlow<BudgetItemState> = _state.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    private val _isDeleted = MutableStateFlow(false)
    val isDeleted: StateFlow<Boolean> = _isDeleted.asStateFlow()

    fun load(id: String) {
        _state.value = BudgetItemState.Loading
        _showDeleteDialog.value = false
        _isDeleted.value = false

        scope.launch {
            val expense = expenseRepository.getById(id)
            if (expense != null) {
                _state.value = BudgetItemState.Content(
                    id = expense.id,
                    title = expense.title,
                    amount = expense.amount,
                    date = expense.date,
                    tag = expense.tag,
                    editTitle = expense.title,
                    editAmount = expense.amount.toString(),
                    hasChanges = false,
                )
            } else {
                _state.value = BudgetItemState.NotFound
            }
        }
    }

    fun onTitleChanged(value: String) {
        _state.update {
            (it as? BudgetItemState.Content)?.copy(
                editTitle = value,
                hasChanges = value != it.title || it.editAmount != it.amount.toString(),
            ) ?: it
        }
    }

    fun onAmountChanged(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _state.update {
                (it as? BudgetItemState.Content)?.copy(
                    editAmount = value,
                    hasChanges = it.editTitle != it.title || value != it.amount.toString(),
                ) ?: it
            }
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
                    tag = content.tag,
                )
            )
            _state.update {
                (it as? BudgetItemState.Content)?.copy(
                    title = content.editTitle.trim(),
                    amount = amount,
                    editTitle = content.editTitle.trim(),
                    editAmount = amount.toString(),
                    hasChanges = false,
                ) ?: it
            }
        }
    }

    fun onDeleteRequest() {
        _showDeleteDialog.value = true
    }

    fun onDeleteDismiss() {
        _showDeleteDialog.value = false
    }

    fun onDeleteConfirm() {
        val content = _state.value as? BudgetItemState.Content ?: return
        _showDeleteDialog.value = false
        scope.launch {
            expenseRepository.deleteExpense(content.id)
            _isDeleted.value = true
        }
    }
}
