package prayit.simplebudget.androidApp.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import prayit.simplebudget.androidApp.R
import prayit.simplebudget.core.domain.model.Expense
import prayit.simplebudget.core.domain.repository.PendingExpenseRepository
import prayit.simplebudget.di.Graph
import prayit.simplebudget.export.PaymentNotificationParser
import prayit.simplebudget.export.PaymentPackage

class ExpenseNotificationListener : NotificationListenerService() {

    internal var repositoryOverride: PendingExpenseRepository? = null

    private fun repository(): PendingExpenseRepository =
        repositoryOverride ?: Graph.app.pendingExpenseRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        try {
            if (sbn == null) return
            val extras = sbn.notification?.extras
            val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
            val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
            val bigText = extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
            val body = bigText.ifBlank { text }

            Log.d(TAG, "package=${sbn.packageName} title='$title' body='${body.take(100)}'")

            val pkg = PaymentPackage.fromPackage(sbn.packageName)
            if (pkg == null) {
                Log.d(TAG, "SKIP: unknown package '${sbn.packageName}'")
                return
            }
            if (title.isBlank() && body.isBlank()) {
                Log.d(TAG, "SKIP: empty title and body")
                return
            }
            val parsed = PaymentNotificationParser.parse(pkg, title, body)
            if (parsed == null) {
                Log.d(TAG, "SKIP: parser null title='$title' body='${body.take(80)}'")
                return
            }
            Log.d(
                TAG,
                "PARSED: title='${parsed.title}' amount=${parsed.amount} tag='${parsed.tag}'"
            )
            val expense = Expense(
                id = "notif_${System.currentTimeMillis()}",
                title = parsed.title,
                amount = parsed.amount,
                date = kotlin.time.Clock.System.todayIn(TimeZone.currentSystemDefault()),
                tag = parsed.tag,
            )
            scope.launch {
                try {
                    val inserted = repository().stageAndCommit(expense)
                    if (inserted) {
                        Log.d(TAG, "STAGED: ${expense.title} ${expense.amount}")
                        showExpenseNotification(expense)
                    } else {
                        Log.d(TAG, "DUP: ${expense.title} ${expense.amount}")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "stageAndCommit failed", e)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "onNotificationPosted failed", e)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Expense Captures",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Notifications when expenses are captured from payment apps"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun showExpenseNotification(expense: Expense) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this,
            expense.id.hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val amount = "%.2f".format(expense.amount)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_expense_captured)
            .setColor(0xFF6D5E0F.toInt())
            .setContentTitle(expense.title)
            .setContentText("$amount · ${expense.tag}")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        getSystemService(NotificationManager::class.java)
            .notify(expense.id.hashCode(), notification)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "ExpenseNotifListener"
        private const val CHANNEL_ID = "expense_captures"
    }
}
