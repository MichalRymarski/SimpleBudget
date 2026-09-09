package prayit.simplebudget.export

import prayit.simplebudget.core.domain.model.Expense
import prayit.simplebudget.core.utils.Month

actual fun generateXlsx(expenses: List<Expense>): ByteArray = buildFullHistoryXlsx(expenses)

actual fun generateSingleMonthXlsx(expenses: List<Expense>, month: Month, year: Int): ByteArray =
    buildSingleMonthXlsx(expenses, month, year)
