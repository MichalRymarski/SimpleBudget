package prayit.simplebudget.feature.home.ui

internal fun padZero(value: Int): String = if (value < 10) "0$value" else "$value"

internal fun formatCurrency(value: Double): String {
    val whole = value.toLong()
    val fraction = ((value - whole) * 100).toInt().let { padZero(it) }
    return "$whole.$fraction"
}

internal fun formatSigned(value: Double): String {
    val sign = if (value > 0) "+" else ""
    return "$sign${formatCurrency(value)}"
}

internal fun formatDate(day: Int, month: Int, year: Int): String =
    "${padZero(day)}.${padZero(month)}.$year"
