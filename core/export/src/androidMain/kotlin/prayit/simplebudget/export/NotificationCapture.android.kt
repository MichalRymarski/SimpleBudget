package prayit.simplebudget.export

import prayit.simplebudget.core.utils.AppContext

private const val LISTENER_PACKAGE = "prayit.simplebudget.androidApp"
private const val LISTENER_CLASS =
    "prayit.simplebudget.androidApp.notification.ExpenseNotificationListener"

actual fun supportsNotificationCapture(): Boolean = true

actual fun isNotificationCaptureEnabled(): Boolean =
    NotificationAccess.isListenerEnabled(
        AppContext.requireInstance(),
        LISTENER_PACKAGE,
        LISTENER_CLASS
    )

actual fun openNotificationCaptureSettings() {
    NotificationAccess.openSettings(AppContext.requireInstance())
}
