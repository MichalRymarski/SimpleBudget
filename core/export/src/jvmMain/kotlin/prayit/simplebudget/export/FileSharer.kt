package prayit.simplebudget.export

import java.awt.Desktop
import java.io.File

actual fun shareCsvFile(fileName: String, csvContent: String, subject: String): Result<Unit> =
    runCatching {
    val tempDir = System.getProperty("java.io.tmpdir") ?: "/tmp"
    val file = File(tempDir, fileName)
    file.writeText(csvContent)

    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().open(file)
    }
}

actual fun shareXlsxFile(fileName: String, byteArray: ByteArray, subject: String): Result<Unit> =
    runCatching {
    val tempDir = System.getProperty("java.io.tmpdir") ?: "/tmp"
    val file = File(tempDir, fileName)
    file.writeBytes(byteArray)

    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().open(file)
    }
}

actual fun shareTextFile(
    fileName: String,
    textContent: String,
    subject: String,
    mimeType: String,
): Result<Unit> =
    runCatching {
        val tempDir = System.getProperty("java.io.tmpdir") ?: "/tmp"
        val file = File(tempDir, fileName)
        file.writeText(textContent)

        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file)
        }
    }
