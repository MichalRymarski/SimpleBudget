package prayit.simplebudget.export

actual fun supportsNotificationCapture(): Boolean = false

actual fun isNotificationCaptureEnabled(): Boolean = false

actual fun openNotificationCaptureSettings() = Unit

actual fun postTestNotification() = Unit
