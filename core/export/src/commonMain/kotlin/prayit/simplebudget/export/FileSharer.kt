package prayit.simplebudget.export

expect fun shareCsvFile(fileName: String, csvContent: String, subject: String): Result<Unit>

expect fun shareXlsxFile(fileName: String, byteArray: ByteArray, subject: String): Result<Unit>
