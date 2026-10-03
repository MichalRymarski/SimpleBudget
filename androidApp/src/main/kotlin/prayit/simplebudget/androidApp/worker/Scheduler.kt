package prayit.simplebudget.androidApp.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit


fun scheduleMonthlyExport(context: Context) {
    val workManager = WorkManager.getInstance(context)
    workManager.cancelUniqueWork(ExpenseExportWorker.LEGACY_WORK_NAME)
    workManager.enqueueUniqueWork(
        ExpenseExportWorker.WORK_NAME,
        ExistingWorkPolicy.KEEP,
        monthlyExportRequest(),
    )
}

internal fun rearmMonthlyExport(context: Context) {
    WorkManager.getInstance(context).enqueueUniqueWork(
        ExpenseExportWorker.WORK_NAME,
        ExistingWorkPolicy.REPLACE,
        monthlyExportRequest(),
    )
}

private fun monthlyExportRequest(): OneTimeWorkRequest =
    OneTimeWorkRequestBuilder<ExpenseExportWorker>()
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build(),
        )
        .setInitialDelay(millisUntilNextMonthlyExport(), TimeUnit.MILLISECONDS)
        .build()

internal fun millisUntilNextMonthlyExport(now: Calendar = Calendar.getInstance()): Long {
    val target = (now.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 8)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (!after(now)) {
            add(Calendar.MONTH, 1)
        }
    }
    return target.timeInMillis - now.timeInMillis
}
