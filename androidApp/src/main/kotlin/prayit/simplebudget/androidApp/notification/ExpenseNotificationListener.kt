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
import org.json.JSONObject
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

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        try {
            if (sbn == null) {
                Log.d(TAG, "onNotificationPosted: sbn is null")
                return
            }
            Log.d(
                TAG,
                "onNotificationPosted: package=${sbn.packageName} id=${sbn.id} tag=${sbn.tag}"
            )
            val extras = sbn.notification?.extras
            val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
            val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
            val bigText = extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
            val body = bigText.ifBlank { text }
            Log.d(
                TAG,
                "  title='$title' text='$text' bigText='${bigText.take(80)}' body='${body.take(80)}'"
            )
            logRaw(sbn, title, body)
            val pkg = PaymentPackage.fromPackage(sbn.packageName)
            if (pkg == null) {
                Log.d(TAG, "  SKIP: package '${sbn.packageName}' not in PaymentPackage enum")
                return
            }
            Log.d(TAG, "  MATCHED package: $pkg")
            if (title.isBlank() && body.isBlank()) {
                Log.d(TAG, "  SKIP: title and body are both blank")
                return
            }
            val parsed = PaymentNotificationParser.parse(pkg, title, body)
            if (parsed == null) {
                Log.d(
                    TAG,
                    "  SKIP: parser returned null for pkg=$pkg title='$title' body='${body.take(60)}'"
                )
                return
            }
            Log.d(
                TAG,
                "  PARSED: title='${parsed.title}' amount=${parsed.amount} tag='${parsed.tag}' merchant='${parsed.merchant}'"
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
                    repository().stageAndCommit(expense)
                    Log.d(
                        TAG,
                        "  STAGED expense: id=${expense.id} title='${expense.title}' amount=${expense.amount}"
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "stageAndCommit failed", e)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "onNotificationPosted failed", e)
        }
    }

    private fun logRaw(sbn: StatusBarNotification, title: String, body: String) {
        val json = JSONObject()
            .put("package", sbn.packageName)
            .put("time", sbn.postTime)
            .put("id", sbn.id)
            .put("tag", sbn.tag)
            .put("title", title)
            .put("body", body)
            .toString()
        scope.launch {
            try {
                repository().logRawNotification(json)
            } catch (e: Exception) {
                Log.w(TAG, "logRawNotification failed", e)
            }
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
