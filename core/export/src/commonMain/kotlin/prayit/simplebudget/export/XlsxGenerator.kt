package prayit.simplebudget.export

import prayit.simplebudget.core.domain.model.Expense
import prayit.simplebudget.core.utils.Month

expect fun generateXlsx(expenses: List<Expense>): ByteArray

expect fun generateSingleMonthXlsx(expenses: List<Expense>, month: Month, year: Int): ByteArray
