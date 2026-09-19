package prayit.simplebudget.export

actual suspend fun sendEmailWithAttachment(
    to: String,
    from: String,
    password: String,
    subject: String,
    attachmentName: String,
    attachmentBytes: ByteArray,
): Result<Unit> = Result.failure(UnsupportedOperationException("Email not supported on JVM"))
