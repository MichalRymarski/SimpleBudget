package prayit.simplebudget.feature.home.ui

internal fun padZero(value: Int): String = if (value < 10) "0$value" else "$value"

internal fun formatCurrency(value: Double): String {
    val totalCents = kotlin.math.round(value * 100).toLong()
    val sign = if (totalCents < 0) "-" else ""
    val absCents = kotlin.math.abs(totalCents)
    return "$sign${absCents / 100}.${(absCents % 100).toString().padStart(2, '0')}"
}

internal fun formatSigned(value: Double): String {
    val sign = if (value > 0) "+" else ""
    return "$sign${formatCurrency(value)}"
}

internal fun formatDate(day: Int, month: Int, year: Int): String =
    "${padZero(day)}.${padZero(month)}.$year"
