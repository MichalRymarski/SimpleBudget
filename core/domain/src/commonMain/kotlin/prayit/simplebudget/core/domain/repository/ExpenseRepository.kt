package prayit.simplebudget.core.domain.repository

import kotlinx.coroutines.flow.Flow
import prayit.simplebudget.core.domain.model.Expense

interface ExpenseRepository {
    fun getExpenses(): Flow<List<Expense>>
    suspend fun getById(id: String): Expense?
    suspend fun insertExpense(expense: Expense)
    suspend fun deleteExpense(id: String)
}
