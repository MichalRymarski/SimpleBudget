package prayit.simplebudget.core.utils

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import io.github.aakira.napier.Napier

object Log {

    fun init(debug: Boolean = true, antilog: Antilog = if (debug) DebugLogAntilog() else ReleaseLogAntilog()) {
        Napier.base(antilog)
    }

    fun d(tag: String = "", message: () -> String) {
        Napier.d(message(), tag = tag)
    }

    fun i(tag: String = "", message: () -> String) {
        Napier.i(message(), tag = tag)
    }

    fun w(tag: String = "", message: () -> String) {
        Napier.w(message(), tag = tag)
    }

    fun e(tag: String = "", throwable: Throwable? = null, message: () -> String) {
        Napier.e(message(), throwable, tag = tag)
    }
}

class DebugLogAntilog : Antilog() {

    override fun isEnable(priority: LogLevel, tag: String?): Boolean = priority >= LogLevel.DEBUG

    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?,
    ) {
        val logMessage = buildString {
            append("[${priority.name.first()}]")
            if (!tag.isNullOrBlank()) append("/$tag")
            append(" $message")
            throwable?.let { append("\n${it.stackTraceToString()}") }
        }
        println(logMessage)
    }
}

class ReleaseLogAntilog(
    private val crashReporter: CrashReporter? = null,
) : Antilog() {

    override fun isEnable(priority: LogLevel, tag: String?): Boolean = priority >= LogLevel.WARNING

    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?,
    ) {
        val logMessage = buildString {
            append("[${priority.name.first()}]")
            if (!tag.isNullOrBlank()) append("/$tag")
            append(" $message")
        }

        throwable?.let { crashReporter?.recordException(it) }

        if (priority >= LogLevel.WARNING) {
            println(logMessage)
        }
    }
}

fun interface CrashReporter {
    fun recordException(throwable: Throwable)
}
