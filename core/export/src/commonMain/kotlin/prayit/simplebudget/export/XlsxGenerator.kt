package prayit.simplebudget.export

import com.gyanoba.kexcel.Excel
import com.gyanoba.kexcel.number_format.NumFormat
import com.gyanoba.kexcel.sheet.CellIndex
import com.gyanoba.kexcel.sheet.CellStyle
import com.gyanoba.kexcel.sheet.DateCellValue
import com.gyanoba.kexcel.sheet.DoubleCellValue
import com.gyanoba.kexcel.sheet.TextCellValue
import prayit.simplebudget.core.domain.model.Expense
import prayit.simplebudget.core.utils.Month

fun generateXlsx(expenses: List<Expense>): ByteArray {
    val excel = Excel.createExcel()
    val headerStyle = CellStyle(bold = true)
    val currencyStyle = CellStyle(numberFormat = NumFormat.standard_4)
    val boldCurrencyStyle = CellStyle(bold = true, numberFormat = NumFormat.standard_4)
    val dateStyle = CellStyle(numberFormat = NumFormat.custom("dd-mm-yyyy"))

    val sortedKeys = expenses
        .map { it.date.monthNumber to it.date.year }
        .distinct()
        .sortedBy { (m, y) -> y * 100 + m }

    val summary = excel["Summary"]
    listOf("Month", "Total Spent", "Difference").forEachIndexed { col, h ->
        summary.updateCell(CellIndex.indexByColumnRow(col, 0), TextCellValue(h), headerStyle)
    }

    var prevTotal = 0.0
    sortedKeys.forEachIndexed { rowIndex, (monthNum, year) ->
        val monthExpenses = expenses
            .filter { it.date.monthNumber == monthNum && it.date.year == year }
        val total = monthExpenses.sumOf { it.amount }
        val difference = total - prevTotal
        val row = rowIndex + 1
        summary.updateCell(CellIndex.indexByColumnRow(0, row), TextCellValue("${Month.entries[monthNum - 1].stringName} $year"))
        summary.updateCell(CellIndex.indexByColumnRow(1, row), DoubleCellValue(total), currencyStyle)
        summary.updateCell(CellIndex.indexByColumnRow(2, row), DoubleCellValue(difference), currencyStyle)
        prevTotal = total
    }

    var runningPrevTotal = 0.0
    sortedKeys.forEach { (monthNum, year) ->
        val monthName = Month.entries[monthNum - 1].stringName
        val sheet = excel["$monthName $year"]
        listOf("Date", "Title", "Tag", "Amount", "Sum", "Difference").forEachIndexed { col, h ->
            sheet.updateCell(CellIndex.indexByColumnRow(col, 0), TextCellValue(h), headerStyle)
        }

        val monthExpenses = expenses
            .filter { it.date.monthNumber == monthNum && it.date.year == year }
            .sortedBy { it.date }
        val total = monthExpenses.sumOf { it.amount }
        val difference = total - runningPrevTotal

        monthExpenses.forEachIndexed { index, expense ->
            val row = index + 1
            sheet.updateCell(CellIndex.indexByColumnRow(0, row), DateCellValue.fromLocalDate(expense.date), dateStyle)
            sheet.updateCell(CellIndex.indexByColumnRow(1, row), TextCellValue(expense.title))
            sheet.updateCell(CellIndex.indexByColumnRow(2, row), TextCellValue(expense.tag))
            sheet.updateCell(CellIndex.indexByColumnRow(3, row), DoubleCellValue(expense.amount), currencyStyle)
            if (index == 0) {
                sheet.updateCell(CellIndex.indexByColumnRow(4, row), DoubleCellValue(total), currencyStyle)
                sheet.updateCell(CellIndex.indexByColumnRow(5, row), DoubleCellValue(difference), currencyStyle)
            }
        }

        val tagCol = 7
        sheet.updateCell(CellIndex.indexByColumnRow(tagCol, 1), TextCellValue("Tag"), headerStyle)
        sheet.updateCell(CellIndex.indexByColumnRow(tagCol + 1, 1), TextCellValue("Sum of Amount"), headerStyle)

        val tagTotals = monthExpenses
            .groupBy { it.tag }
            .map { (tag, items) -> tag to items.sumOf { it.amount } }
            .sortedByDescending { it.second }

        tagTotals.forEachIndexed { index, (tag, tagTotal) ->
            val row = index + 2
            sheet.updateCell(CellIndex.indexByColumnRow(tagCol, row), TextCellValue(tag))
            sheet.updateCell(CellIndex.indexByColumnRow(tagCol + 1, row), DoubleCellValue(tagTotal), currencyStyle)
        }

        val grandRow = tagTotals.size + 2
        sheet.updateCell(CellIndex.indexByColumnRow(tagCol, grandRow), TextCellValue("Grand Total"), headerStyle)
        sheet.updateCell(CellIndex.indexByColumnRow(tagCol + 1, grandRow), DoubleCellValue(total), boldCurrencyStyle)

        runningPrevTotal = total
    }

    excel.delete("Sheet1")
    excel.setDefaultSheet("Summary")
    return withAutoFilter(excel.encode() ?: error("Failed to encode workbook"))
}

fun generateSingleMonthXlsx(expenses: List<Expense>, month: Month, year: Int): ByteArray {
    val excel = Excel.createExcel()
    val headerStyle = CellStyle(bold = true)
    val currencyStyle = CellStyle(numberFormat = NumFormat.standard_4)
    val boldCurrencyStyle = CellStyle(bold = true, numberFormat = NumFormat.standard_4)
    val dateStyle = CellStyle(numberFormat = NumFormat.custom("dd-mm-yyyy"))

    val monthNum = month.ordinal + 1
    val monthExpenses = expenses
        .filter { it.date.monthNumber == monthNum && it.date.year == year }
        .sortedBy { it.date }
    val total = monthExpenses.sumOf { it.amount }
    val prevMonth = Month.entries[if (month.ordinal == 0) 11 else month.ordinal - 1]
    val prevYear = if (month == Month.January) year - 1 else year
    val prevTotal = expenses
        .filter { it.date.monthNumber == prevMonth.ordinal + 1 && it.date.year == prevYear }
        .sumOf { it.amount }
    val difference = total - prevTotal

    val sheet = excel["${month.stringName} $year"]
    listOf("Date", "Title", "Tag", "Amount", "Sum", "Difference").forEachIndexed { col, h ->
        sheet.updateCell(CellIndex.indexByColumnRow(col, 0), TextCellValue(h), headerStyle)
    }

    monthExpenses.forEachIndexed { index, expense ->
        val row = index + 1
        sheet.updateCell(CellIndex.indexByColumnRow(0, row), DateCellValue.fromLocalDate(expense.date), dateStyle)
        sheet.updateCell(CellIndex.indexByColumnRow(1, row), TextCellValue(expense.title))
        sheet.updateCell(CellIndex.indexByColumnRow(2, row), TextCellValue(expense.tag))
        sheet.updateCell(CellIndex.indexByColumnRow(3, row), DoubleCellValue(expense.amount), currencyStyle)
        if (index == 0) {
            sheet.updateCell(CellIndex.indexByColumnRow(4, row), DoubleCellValue(total), currencyStyle)
            sheet.updateCell(CellIndex.indexByColumnRow(5, row), DoubleCellValue(difference), currencyStyle)
        }
    }
    if (monthExpenses.isEmpty()) {
        sheet.updateCell(CellIndex.indexByColumnRow(4, 1), DoubleCellValue(total), currencyStyle)
        sheet.updateCell(CellIndex.indexByColumnRow(5, 1), DoubleCellValue(difference), currencyStyle)
    }

    val tagCol = 7
    sheet.updateCell(CellIndex.indexByColumnRow(tagCol, 1), TextCellValue("Tag"), headerStyle)
    sheet.updateCell(CellIndex.indexByColumnRow(tagCol + 1, 1), TextCellValue("Sum of Amount"), headerStyle)

    val tagTotals = monthExpenses
        .groupBy { it.tag }
        .map { (tag, items) -> tag to items.sumOf { it.amount } }
        .sortedByDescending { it.second }

    tagTotals.forEachIndexed { index, (tag, tagTotal) ->
        val row = index + 2
        sheet.updateCell(CellIndex.indexByColumnRow(tagCol, row), TextCellValue(tag))
        sheet.updateCell(CellIndex.indexByColumnRow(tagCol + 1, row), DoubleCellValue(tagTotal), currencyStyle)
    }

    val grandRow = tagTotals.size + 2
    sheet.updateCell(CellIndex.indexByColumnRow(tagCol, grandRow), TextCellValue("Grand Total"), headerStyle)
    sheet.updateCell(CellIndex.indexByColumnRow(tagCol + 1, grandRow), DoubleCellValue(total), boldCurrencyStyle)

    excel.delete("Sheet1")
    excel.setDefaultSheet("${month.stringName} $year")
    return withAutoFilter(excel.encode() ?: error("Failed to encode workbook"))
}
