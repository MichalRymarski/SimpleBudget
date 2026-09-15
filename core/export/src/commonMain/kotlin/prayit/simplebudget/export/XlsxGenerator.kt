package prayit.simplebudget.export

import prayit.simplebudget.core.domain.model.Expense
import prayit.simplebudget.core.utils.Month

expect fun generateXlsx(expenses: List<Expense>): Result<ByteArray>

expect fun generateSingleMonthXlsx(
    expenses: List<Expense>,
    month: Month,
    year: Int,
): Result<ByteArray>
