package prayit.simplebudget.export

import prayit.simplebudget.core.domain.model.Expense
import prayit.simplebudget.core.utils.Month

actual fun generateXlsx(expenses: List<Expense>): Result<ByteArray> =
    runCatching { buildFullHistoryXlsx(expenses) }

actual fun generateSingleMonthXlsx(
    expenses: List<Expense>,
    month: Month,
    year: Int,
): Result<ByteArray> =
    runCatching { buildSingleMonthXlsx(expenses, month, year) }
