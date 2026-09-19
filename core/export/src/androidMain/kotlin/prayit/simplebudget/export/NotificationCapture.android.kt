package prayit.simplebudget.export

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.util.Log
import androidx.core.app.NotificationCompat
import prayit.simplebudget.core.utils.AppContext

private const val LISTENER_CLASS =
    "prayit.simplebudget.androidApp.notification.ExpenseNotificationListener"

private const val CHANNEL_ID = "test_notification"
private const val CHANNEL_NAME = "Test"
private const val NOTIFICATION_ID = 9999
private const val TAG = "PostTestNotif"

actual fun supportsNotificationCapture(): Boolean = true

actual fun isNotificationCaptureEnabled(): Boolean {
    val context = AppContext.requireInstance()
    return NotificationAccess.isListenerEnabled(context, context.packageName, LISTENER_CLASS)
}

actual fun openNotificationCaptureSettings() {
    NotificationAccess.openSettings(AppContext.requireInstance())
}

actual fun postTestNotification() {
    val context = AppContext.requireInstance()
    val nm = context.getSystemService(NotificationManager::class.java)

    val channel = NotificationChannel(
        CHANNEL_ID,
        CHANNEL_NAME,
        NotificationManager.IMPORTANCE_DEFAULT,
    )
    nm.createNotificationChannel(channel)

    val amount = (100..9900).random() / 100.0
    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    val pendingIntent = PendingIntent.getActivity(
        context, 0, launchIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    Log.d(TAG, "Posting 3 identical notifications: title='Test Shop' text='You spent PLN$amount'")
    for (i in 1..3) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Test Shop")
            .setContentText("You spent PLN$amount")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        nm.notify(9999 + i, notification)
    }
    Log.d(TAG, "3 notifications posted (ids 10000-10002)")
}
