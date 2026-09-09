package prayit.simplebudget.export

import no.synth.kmpzip.io.ByteArrayOutputStream
import no.synth.kmpzip.io.readBytes
import no.synth.kmpzip.zip.ZipEntry
import no.synth.kmpzip.zip.ZipInputStream
import no.synth.kmpzip.zip.ZipOutputStream

internal fun withAutoFilter(xlsx: ByteArray): ByteArray {
    val entries = mutableListOf<Pair<String, ByteArray>>()
    ZipInputStream(xlsx).use { zis ->
        while (true) {
            val entry = zis.nextEntry ?: break
            val bytes = zis.readBytes()
            entries.add(entry.name to bytes)
        }
    }
    val out = ByteArrayOutputStream()
    ZipOutputStream(out).use { zos ->
        for ((name, bytes) in entries) {
            val patched = if (name.startsWith("xl/worksheets/sheet") && name.endsWith(".xml")) {
                addAutoFilterToSheetXml(bytes.decodeToString())
            } else {
                bytes.decodeToString()
            }.encodeToByteArray()
            val content = if (name.startsWith("xl/worksheets/sheet") && name.endsWith(".xml")) {
                patched
            } else {
                bytes
            }
            zos.putNextEntry(ZipEntry(name))
            zos.write(content)
            zos.closeEntry()
        }
    }
    return out.toByteArray()
}

private fun addAutoFilterToSheetXml(xml: String): String {
    if ("<autoFilter" in xml) return xml
    val ref = computeRef(xml) ?: return xml
    val tag = "<autoFilter ref=\"$ref\"/>"
    val anchor = "</sheetData>"
    val idx = xml.indexOf(anchor)
    if (idx == -1) return xml
    val insertAt = idx + anchor.length
    return xml.substring(0, insertAt) + tag + xml.substring(insertAt)
}

private fun computeRef(xml: String): String? {
    var maxRow = 0
    var i = xml.indexOf("<c r=\"")
    while (i != -1) {
        val start = i + 6
        val end = xml.indexOf('"', start)
        if (end == -1) break
        val ref = xml.substring(start, end)
        val colLetters = ref.takeWhile { it.isLetter() }
        val rowDigits = ref.dropWhile { it.isLetter() }.takeWhile { it.isDigit() }
        val row = rowDigits.toIntOrNull() ?: 0
        val col = colLettersToIndex(colLetters)
        if (col in 1..4 && row > maxRow) maxRow = row
        i = xml.indexOf("<c r=\"", end)
    }
    if (maxRow < 2) return null
    return "A1:D$maxRow"
}

private fun colLettersToIndex(letters: String): Int {
    var result = 0
    for (ch in letters.uppercase()) {
        if (ch !in 'A'..'Z') return 0
        result = result * 26 + (ch - 'A' + 1)
    }
    return result
}

private fun colIndexToLetters(index: Int): String {
    var n = index
    var result = ""
    while (n > 0) {
        val rem = (n - 1) % 26
        result = ('A' + rem) + result
        n = (n - 1) / 26
    }
    return result
}
