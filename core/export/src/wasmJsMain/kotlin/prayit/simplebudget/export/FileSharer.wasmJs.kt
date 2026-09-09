package prayit.simplebudget.export

import kotlinx.browser.document
import org.w3c.dom.HTMLAnchorElement
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private fun download(fileName: String, mime: String, dataUrl: String) {
    val body = document.body ?: return
    val a = document.createElement("a") as HTMLAnchorElement
    a.href = dataUrl
    a.download = fileName
    a.type = mime
    body.appendChild(a)
    a.click()
    a.remove()
}

@OptIn(ExperimentalEncodingApi::class)
actual fun shareCsvFile(fileName: String, csvContent: String, subject: String) {
    val bytes = csvContent.encodeToByteArray()
    download(fileName, "text/csv", "data:text/csv;base64,${Base64.encode(bytes)}")
}

@OptIn(ExperimentalEncodingApi::class)
actual fun shareXlsxFile(fileName: String, byteArray: ByteArray, subject: String) {
    download(
        fileName,
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "data:application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;base64,${Base64.encode(byteArray)}",
    )
}
