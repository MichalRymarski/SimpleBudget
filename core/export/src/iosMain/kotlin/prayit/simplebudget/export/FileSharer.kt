package prayit.simplebudget.export

private fun iosSharingUnsupported(): Result<Unit> =
    Result.failure(UnsupportedOperationException("File sharing is not supported on iOS yet"))

actual fun shareCsvFile(fileName: String, csvContent: String, subject: String): Result<Unit> =
    iosSharingUnsupported()

actual fun shareXlsxFile(fileName: String, byteArray: ByteArray, subject: String): Result<Unit> =
    iosSharingUnsupported()

actual fun shareTextFile(
    fileName: String,
    textContent: String,
    subject: String,
    mimeType: String,
): Result<Unit> = iosSharingUnsupported()
