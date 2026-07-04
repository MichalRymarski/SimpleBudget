package prayit.simplebudget.export

import prayit.simplebudget.core.domain.model.Expense
import prayit.simplebudget.core.utils.Month

object CsvGenerator {

    fun generateSingleMonth(
        expenses: List<Expense>,
        month: Month,
        year: Int,
    ): String {
        val filtered = expenses
            .filter { it.date.monthNumber == month.ordinal + 1 && it.date.year == year }
            .sortedBy { it.date }

        val total = filtered.sumOf { it.amount }
        val prevMonth = month.previous()
        val prevYear = if (month == Month.January) year - 1 else year
        val prevTotal = expenses
            .filter { it.date.monthNumber == prevMonth.ordinal + 1 && it.date.year == prevYear }
            .sumOf { it.amount }
        val difference = total - prevTotal

        return buildString {
            appendLine("Date,Title,Tag,Amount,Sum,Difference")
            filtered.forEachIndexed { index, expense ->
                if (index == 0) {
                    appendLine("${expense.date},${escape(expense.title)},${expense.tag},${expense.amount},${formatCurrency(total)},${formatSigned(difference)}")
                } else {
                    appendLine("${expense.date},${escape(expense.title)},${expense.tag},${expense.amount},,")
                }
            }
            if (filtered.isEmpty()) {
                appendLine(",,,,${formatCurrency(total)},${formatSigned(difference)}")
            }
        }
    }

    fun generateFullHistory(expenses: List<Expense>): String {
        return buildString {
            appendLine("Date,Title,Tag,Amount,Sum,Difference")

            var prevTotal = 0.0
            val sortedKeys = expenses
                .map { it.date.monthNumber to it.date.year }
                .distinct()
                .sortedBy { (m, y) -> y * 100 + m }

            sortedKeys.forEach { (monthNum, year) ->
                val monthExpenses = expenses
                    .filter { it.date.monthNumber == monthNum && it.date.year == year }
                    .sortedBy { it.date }
                val total = monthExpenses.sumOf { it.amount }
                val difference = total - prevTotal

                monthExpenses.forEachIndexed { index, expense ->
                    if (index == 0) {
                        appendLine("${expense.date},${escape(expense.title)},${expense.tag},${expense.amount},${formatCurrency(total)},${formatSigned(difference)}")
                    } else {
                        appendLine("${expense.date},${escape(expense.title)},${expense.tag},${expense.amount},,")
                    }
                }
                if (monthExpenses.isEmpty()) {
                    val monthName = Month.entries[monthNum - 1].stringName
                    appendLine("$monthName $year,,,,${formatCurrency(total)},${formatSigned(difference)}")
                }

                prevTotal = total
            }
        }
    }

    private fun escape(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    private fun formatCurrency(value: Double): String {
        val whole = value.toLong()
        val fraction = ((value - whole) * 100).toInt().let { if (it < 10) "0$it" else "$it" }
        return "$whole.$fraction"
    }

    private fun formatSigned(value: Double): String {
        val sign = if (value > 0) "+" else ""
        return "$sign${formatCurrency(value)}"
    }
}

private fun Month.previous(): Month {
    val values = Month.entries
    val index = values.indexOf(this)
    return if (index == 0) values.last() else values[index - 1]
}
