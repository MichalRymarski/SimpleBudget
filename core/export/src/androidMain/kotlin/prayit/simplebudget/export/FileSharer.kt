package prayit.simplebudget.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

actual fun shareCsvFile(fileName: String, csvContent: String, subject: String) {
    val context: Context = prayit.simplebudget.core.data.dbSetup.AppContext.instance
    val cacheDir = File(context.cacheDir, "exports")
    cacheDir.mkdirs()
    val file = File(cacheDir, fileName)
    file.writeText(csvContent)

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share CSV").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}

actual fun shareXlsxFile(fileName: String, byteArray: ByteArray, subject: String) {
    val context: Context = prayit.simplebudget.core.data.dbSetup.AppContext.instance
    val cacheDir = File(context.cacheDir, "exports")
    cacheDir.mkdirs()
    val file = File(cacheDir, fileName)
    file.writeBytes(byteArray)

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share XLSX").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}
