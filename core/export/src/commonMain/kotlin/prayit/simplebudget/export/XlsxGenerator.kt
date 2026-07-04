package prayit.simplebudget.export

import prayit.simplebudget.core.domain.model.Expense

expect fun generateXlsx(expenses: List<Expense>): ByteArray
