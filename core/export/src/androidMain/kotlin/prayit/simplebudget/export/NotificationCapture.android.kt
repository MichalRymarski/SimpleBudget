package prayit.simplebudget.export

import prayit.simplebudget.core.utils.AppContext

private const val LISTENER_CLASS =
    "prayit.simplebudget.androidApp.notification.ExpenseNotificationListener"

actual fun supportsNotificationCapture(): Boolean = true

actual fun isNotificationCaptureEnabled(): Boolean {
    val context = AppContext.requireInstance()
    return NotificationAccess.isListenerEnabled(context, context.packageName, LISTENER_CLASS)
}

actual fun openNotificationCaptureSettings() {
    NotificationAccess.openSettings(AppContext.requireInstance())
}
