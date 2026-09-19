package prayit.simplebudget.core.utils

import android.util.Log as AndroidLog
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel

class AndroidAntilog : Antilog() {
    override fun isEnable(priority: LogLevel, tag: String?): Boolean = priority >= LogLevel.DEBUG

    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?,
    ) {
        val logTag = tag?.takeIf { it.isNotBlank() } ?: "SimpleBudget"
        when {
            priority >= LogLevel.ERROR -> AndroidLog.e(logTag, message ?: "", throwable)
            priority >= LogLevel.WARNING -> AndroidLog.w(logTag, message ?: "", throwable)
            priority >= LogLevel.INFO -> AndroidLog.i(logTag, message ?: "")
            priority >= LogLevel.DEBUG -> AndroidLog.d(logTag, message ?: "")
            else -> AndroidLog.v(logTag, message ?: "")
        }
    }
}

fun initLog() {
    Log.init(debug = true, antilog = AndroidAntilog())
}
