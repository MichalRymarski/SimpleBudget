package prayit.simplebudget.androidApp.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import prayit.simplebudget.core.utils.Month
import prayit.simplebudget.di.Graph
import prayit.simplebudget.export.generateSingleMonthXlsx
import prayit.simplebudget.export.sendEmailWithAttachment

class ExpenseExportWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val prevMonthNumber = if (today.month.number == 1) 12 else today.month.number - 1
            val prevYear = if (today.month.number == 1) today.year - 1 else today.year
            val month = Month.entries[prevMonthNumber - 1]

            val expenses = Graph.app.expenseRepository.getExpenses().first()
            val monthExpenses = expenses.filter {
                it.date.month.number == prevMonthNumber && it.date.year == prevYear
            }

            if (monthExpenses.isEmpty()) {
                Log.d(TAG, "No expenses for $month $prevYear, skipping")
                return Result.success()
            }

            val xlsx = generateSingleMonthXlsx(monthExpenses, month, prevYear).getOrThrow()

            val settings = Graph.app.settingsRepository.getSettings().first()
            if (!settings.autoExportEnabled) {
                Log.d(TAG, "Auto-export disabled, skipping email")
                return Result.success()
            }

            if (settings.recipientEmail.isBlank() || settings.senderEmail.isBlank() || settings.appPassword.isBlank()) {
                Log.w(TAG, "Email settings incomplete, skipping")
                return Result.failure()
            }

            val fileName = "Budget-${prevMonthNumber.toString().padStart(2, '0')}.$prevYear.xlsx"
            sendEmailWithAttachment(
                to = settings.recipientEmail,
                from = settings.senderEmail,
                password = settings.appPassword,
                subject = "SimpleBudget export - $month $prevYear",
                attachmentName = fileName,
                attachmentBytes = xlsx,
            ).getOrThrow()

            Log.d(TAG, "Email sent for $month $prevYear")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Export failed", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "ExpenseExportWorker"
        const val WORK_NAME = "monthly_expense_export"
    }
}
