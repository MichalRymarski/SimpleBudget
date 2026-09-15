package prayit.simplebudget.androidApp.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import prayit.simplebudget.core.domain.model.Expense
import prayit.simplebudget.core.domain.repository.PendingExpenseRepository
import prayit.simplebudget.di.Graph
import prayit.simplebudget.export.PaymentNotificationParser

class ExpenseNotificationListener : NotificationListenerService() {

    internal var repositoryOverride: PendingExpenseRepository? = null

    private fun repository(): PendingExpenseRepository =
        repositoryOverride ?: Graph.app.pendingExpenseRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        try {
            if (sbn == null) return
            if (sbn.packageName !in PaymentNotificationParser.SUPPORTED_PACKAGES) return
            val extras = sbn.notification?.extras ?: return
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
            val body = bigText.ifBlank { text }
            if (title.isBlank() && body.isBlank()) return
            val parsed = PaymentNotificationParser.parse(sbn.packageName, title, body) ?: return
            val expense = Expense(
                id = "notif_${System.currentTimeMillis()}",
                title = parsed.title,
                amount = parsed.amount,
                date = kotlin.time.Clock.System.todayIn(TimeZone.currentSystemDefault()),
                tag = parsed.tag,
            )
            scope.launch {
                try {
                    repository().stageAndCommit(expense)
                } catch (e: Exception) {
                    Log.w(TAG, "stageAndCommit failed", e)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "onNotificationPosted failed", e)
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "ExpenseNotifListener"
    }
}
