package prayit.simplebudget.core.domain.repository

import prayit.simplebudget.core.domain.model.Expense

interface ExportRepository {
    suspend fun exportMonthCsv(expenses: List<Expense>, monthNumber: Int, year: Int): Result<Unit>

    suspend fun exportMonthXlsx(expenses: List<Expense>, monthNumber: Int, year: Int): Result<Unit>

    suspend fun exportHistoryXlsx(expenses: List<Expense>): Result<Unit>

    fun supportsAutoCapture(): Boolean

    fun isAutoCaptureEnabled(): Boolean

    fun openAutoCaptureSettings()

    suspend fun sendExportEmail(
        expenses: List<Expense>,
        monthNumber: Int,
        year: Int,
        attachmentName: String,
    ): Result<Unit>

    suspend fun sendCsvEmail(
        expenses: List<Expense>,
        monthNumber: Int,
        year: Int,
    ): Result<Unit>

    suspend fun sendHistoryEmail(expenses: List<Expense>): Result<Unit>

    suspend fun sendTestEmail(): Result<Unit>
}
