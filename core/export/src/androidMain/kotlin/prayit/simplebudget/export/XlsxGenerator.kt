package prayit.simplebudget.export

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import prayit.simplebudget.core.domain.model.Expense
import prayit.simplebudget.core.utils.Month
import java.io.ByteArrayOutputStream

actual fun generateXlsx(expenses: List<Expense>): ByteArray {
    val workbook = XSSFWorkbook()

    val headerStyle = workbook.createCellStyle().apply {
        val font = workbook.createFont().apply { bold = true }
        setFont(font)
    }
    val currencyStyle = workbook.createCellStyle().apply {
        dataFormat = workbook.createDataFormat().getFormat("#,##0.00")
    }
    val boldCurrencyStyle = workbook.createCellStyle().apply {
        val font = workbook.createFont().apply { bold = true }
        setFont(font)
        dataFormat = workbook.createDataFormat().getFormat("#,##0.00")
    }

    val sortedKeys = expenses
        .map { it.date.monthNumber to it.date.year }
        .distinct()
        .sortedBy { (m, y) -> y * 100 + m }

    // Summary sheet
    val summarySheet = workbook.createSheet("Summary")
    val summaryHeader = summarySheet.createRow(0)
    listOf("Month", "Total Spent", "Difference").forEachIndexed { i, h ->
        summaryHeader.createCell(i).apply {
            setCellValue(h)
            cellStyle = headerStyle
        }
    }

    var prevTotal = 0.0
    sortedKeys.forEachIndexed { rowIndex, (monthNum, year) ->
        val monthExpenses = expenses
            .filter { it.date.monthNumber == monthNum && it.date.year == year }
        val total = monthExpenses.sumOf { it.amount }
        val difference = total - prevTotal

        val row = summarySheet.createRow(rowIndex + 1)
        row.createCell(0).setCellValue("${Month.entries[monthNum - 1].stringName} $year")
        row.createCell(1).apply {
            setCellValue(total)
            cellStyle = currencyStyle
        }
        row.createCell(2).apply {
            setCellValue(difference)
            cellStyle = currencyStyle
        }

        prevTotal = total
    }

    // Per-month sheets with tag summary at H2
    var runningPrevTotal = 0.0
    sortedKeys.forEach { (monthNum, year) ->
        val monthName = Month.entries[monthNum - 1].stringName
        val sheet = workbook.createSheet("$monthName $year")

        val header = sheet.createRow(0)
        listOf("Date", "Title", "Tag", "Amount", "Sum", "Difference").forEachIndexed { i, h ->
            header.createCell(i).apply {
                setCellValue(h)
                cellStyle = headerStyle
            }
        }

        val monthExpenses = expenses
            .filter { it.date.monthNumber == monthNum && it.date.year == year }
            .sortedBy { it.date }
        val total = monthExpenses.sumOf { it.amount }
        val difference = total - runningPrevTotal

        monthExpenses.forEachIndexed { index, expense ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(expense.date.toString())
            row.createCell(1).setCellValue(expense.title)
            row.createCell(2).setCellValue(expense.tag)
            row.createCell(3).apply {
                setCellValue(expense.amount)
                cellStyle = currencyStyle
            }
            if (index == 0) {
                row.createCell(4).apply {
                    setCellValue(total)
                    cellStyle = currencyStyle
                }
                row.createCell(5).apply {
                    setCellValue(difference)
                    cellStyle = currencyStyle
                }
            }
        }

        // Tag summary at H2 (column index 7, row index 1)
        val tagCol = 7
        val tagHeaderRow = sheet.getRow(1) ?: sheet.createRow(1)
        tagHeaderRow.createCell(tagCol).apply {
            setCellValue("Tag")
            cellStyle = headerStyle
        }
        tagHeaderRow.createCell(tagCol + 1).apply {
            setCellValue("SUM of Amount")
            cellStyle = headerStyle
        }

        val tagTotals = monthExpenses
            .groupBy { it.tag }
            .map { (tag, items) -> tag to items.sumOf { it.amount } }
            .sortedByDescending { it.second }

        tagTotals.forEachIndexed { index, (tag, tagTotal) ->
            val row = sheet.getRow(index + 2) ?: sheet.createRow(index + 2)
            row.createCell(tagCol).setCellValue(tag)
            row.createCell(tagCol + 1).apply {
                setCellValue(tagTotal)
                cellStyle = currencyStyle
            }
        }

        val grandRow = sheet.getRow(tagTotals.size + 2) ?: sheet.createRow(tagTotals.size + 2)
        grandRow.createCell(tagCol).apply {
            setCellValue("Grand Total")
            cellStyle = headerStyle
        }
        grandRow.createCell(tagCol + 1).apply {
            setCellValue(total)
            cellStyle = boldCurrencyStyle
        }

        runningPrevTotal = total
    }

    return ByteArrayOutputStream().also { workbook.write(it) }.toByteArray()
}
