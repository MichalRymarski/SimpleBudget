package prayit.simplebudget.export

import no.synth.kmpzip.io.ByteArrayOutputStream
import no.synth.kmpzip.zip.ZipEntry
import no.synth.kmpzip.zip.ZipOutputStream
import prayit.simplebudget.core.domain.model.Expense
import prayit.simplebudget.core.utils.Month

private sealed interface WCell {
    data class Text(val value: String, val bold: Boolean = false) : WCell
    data class Num(val value: Double, val bold: Boolean = false) : WCell
    data class Date(val epochDays: Long) : WCell
}

private data class WSheet(val name: String, val rows: List<List<WCell?>>, val filterRef: String?)

private fun esc(s: String): String = buildString(s.length + 8) {
    for (c in s) when (c) {
        '&' -> append("&amp;")
        '<' -> append("&lt;")
        '>' -> append("&gt;")
        '"' -> append("&quot;")
        else -> if (c.code < 0x20 && c != '\t' && c != '\n') append(' ') else append(c)
    }
}

private fun colLetters(index1: Int): String {
    var n = index1
    var r = ""
    while (n > 0) {
        r = ('A' + (n - 1) % 26) + r
        n = (n - 1) / 26
    }
    return r
}

private fun numStr(v: Double): String = v.toString()

private fun sheetXml(sheet: WSheet): String = buildString {
    append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
    append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
    append("<sheetData>")
    sheet.rows.forEachIndexed { r, row ->
        append("""<row r="${r + 1}">""")
        row.forEachIndexed { c, cell ->
            if (cell == null) return@forEachIndexed
            val ref = colLetters(c + 1) + (r + 1)
            when (cell) {
                is WCell.Text -> {
                    val s = if (cell.bold) 1 else 0
                    append("""<c r="$ref" s="$s" t="inlineStr"><is><t xml:space="preserve">${esc(cell.value)}</t></is></c>""")
                }
                is WCell.Num -> {
                    val s = if (cell.bold) 4 else 2
                    append("""<c r="$ref" s="$s"><v>${numStr(cell.value)}</v></c>""")
                }
                is WCell.Date -> append("""<c r="$ref" s="3"><v>${cell.epochDays + 25569}</v></c>""")
            }
        }
        append("</row>")
    }
    append("</sheetData>")
    sheet.filterRef?.let { append("""<autoFilter ref="$it"/>""") }
    append("</worksheet>")
}

private const val STYLES = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""" +
    """<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""" +
    """<numFmts count="1"><numFmt numFmtId="164" formatCode="dd-mm-yyyy"/></numFmts>""" +
    """<fonts count="2"><font><sz val="11"/><name val="Calibri"/></font>""" +
    """<font><b/><sz val="11"/><name val="Calibri"/></font></fonts>""" +
    """<fills count="2"><fill><patternFill patternType="none"/></fill>""" +
    """<fill><patternFill patternType="gray125"/></fill></fills>""" +
    """<borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>""" +
    """<cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>""" +
    """<cellXfs count="5">""" +
    """<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>""" +
    """<xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/>""" +
    """<xf numFmtId="4" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/>""" +
    """<xf numFmtId="164" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/>""" +
    """<xf numFmtId="4" fontId="1" fillId="0" borderId="0" xfId="0" applyNumberFormat="1" applyFont="1"/>""" +
    """</cellXfs><cellStyles count="1"><cellStyle name="Normal" xfId="0" builtinId="0"/></cellStyles></styleSheet>"""

private fun contentTypes(count: Int): String = buildString {
    append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
    append("""<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">""")
    append("""<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>""")
    append("""<Default Extension="xml" ContentType="application/xml"/>""")
    append("""<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>""")
    append("""<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>""")
    repeat(count) { i ->
        append("""<Override PartName="/xl/worksheets/sheet${i + 1}.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>""")
    }
    append("</Types>")
}

private const val ROOT_RELS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""" +
    """<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""" +
    """<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>""" +
    """</Relationships>"""

private fun workbookXml(names: List<String>): String = buildString {
    append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
    append("""<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">""")
    append("<sheets>")
    names.forEachIndexed { i, n ->
        append("""<sheet name="${esc(n)}" sheetId="${i + 1}" r:id="rId${i + 1}"/>""")
    }
    append("</sheets></workbook>")
}

private fun workbookRels(count: Int): String = buildString {
    append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
    append("""<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""")
    repeat(count) { i ->
        append("""<Relationship Id="rId${i + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet${i + 1}.xml"/>""")
    }
    append("""<Relationship Id="rId${count + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>""")
    append("</Relationships>")
}

private fun pack(sheets: List<WSheet>): ByteArray {
    val out = ByteArrayOutputStream()
    ZipOutputStream(out).use { zos ->
        fun add(name: String, content: String) {
            zos.putNextEntry(ZipEntry(name))
            zos.write(content.encodeToByteArray())
            zos.closeEntry()
        }
        add("[Content_Types].xml", contentTypes(sheets.size))
        add("_rels/.rels", ROOT_RELS)
        add("xl/workbook.xml", workbookXml(sheets.map { it.name }))
        add("xl/_rels/workbook.xml.rels", workbookRels(sheets.size))
        add("xl/styles.xml", STYLES)
        sheets.forEachIndexed { i, s -> add("xl/worksheets/sheet${i + 1}.xml", sheetXml(s)) }
    }
    return out.toByteArray()
}

private fun summarySheet(expenses: List<Expense>, keys: List<Pair<Int, Int>>): WSheet {
    val rows = mutableListOf<List<WCell?>>(
        listOf(WCell.Text("Month", true), WCell.Text("Total Spent", true), WCell.Text("Difference", true))
    )
    var prev = 0.0
    keys.forEach { (m, y) ->
        val total = expenses.filter { it.date.monthNumber == m && it.date.year == y }.sumOf { it.amount }
        rows.add(listOf(WCell.Text("${Month.entries[m - 1].stringName} $y"), WCell.Num(total), WCell.Num(total - prev)))
        prev = total
    }
    return WSheet("Summary", rows, if (rows.size > 1) "A1:C${rows.size}" else null)
}

private fun monthSheet(expenses: List<Expense>, month: Month, year: Int, prevTotal: Double): WSheet {
    val monthExpenses = expenses
        .filter { it.date.monthNumber == month.ordinal + 1 && it.date.year == year }
        .sortedBy { it.date }
    val total = monthExpenses.sumOf { it.amount }
    val rows = mutableListOf<List<WCell?>>(
        listOf(
            WCell.Text("Date", true), WCell.Text("Title", true), WCell.Text("Tag", true),
            WCell.Text("Amount", true), WCell.Text("Sum", true), WCell.Text("Difference", true),
        )
    )
    monthExpenses.forEachIndexed { i, e ->
        rows.add(
            listOf(
                WCell.Date(e.date.toEpochDays().toLong()),
                WCell.Text(e.title),
                WCell.Text(e.tag),
                WCell.Num(e.amount),
                if (i == 0) WCell.Num(total) else null,
                if (i == 0) WCell.Num(total - prevTotal) else null,
            )
        )
    }
    if (monthExpenses.isEmpty()) {
        rows.add(listOf(null, null, null, null, WCell.Num(total), WCell.Num(total - prevTotal)))
    }
    val tags = monthExpenses.groupBy { it.tag }
        .map { (t, items) -> t to items.sumOf { it.amount } }
        .sortedByDescending { it.second }
    while (rows.size < tags.size + 3) rows.add(listOf(null, null, null, null, null, null))
    rows[1] = rows[1] + listOf(null, WCell.Text("Tag", true), WCell.Text("Sum of Amount", true))
    tags.forEachIndexed { i, (t, tt) ->
        rows[i + 2] = rows[i + 2] + listOf(null, WCell.Text(t), WCell.Num(tt))
    }
    rows[tags.size + 2] = rows[tags.size + 2] + listOf(null, WCell.Text("Grand Total", true), WCell.Num(total, true))
    val lastDataRow = (if (monthExpenses.isEmpty()) 2 else monthExpenses.size + 1).coerceAtLeast(2)
    return WSheet("${month.stringName} $year", rows, "A1:D$lastDataRow")
}

internal fun buildFullHistoryXlsx(expenses: List<Expense>): ByteArray {
    val keys = expenses.map { it.date.monthNumber to it.date.year }.distinct().sortedBy { (m, y) -> y * 100 + m }
    val sheets = mutableListOf(summarySheet(expenses, keys))
    var running = 0.0
    keys.forEach { (m, y) ->
        val month = Month.entries[m - 1]
        val total = expenses.filter { it.date.monthNumber == m && it.date.year == y }.sumOf { it.amount }
        sheets.add(monthSheet(expenses, month, y, running))
        running = total
    }
    return pack(sheets)
}

internal fun buildSingleMonthXlsx(expenses: List<Expense>, month: Month, year: Int): ByteArray {
    val prevMonth = Month.entries[if (month.ordinal == 0) 11 else month.ordinal - 1]
    val prevYear = if (month == Month.January) year - 1 else year
    val prevTotal = expenses
        .filter { it.date.monthNumber == prevMonth.ordinal + 1 && it.date.year == prevYear }
        .sumOf { it.amount }
    return pack(listOf(monthSheet(expenses, month, year, prevTotal)))
}
