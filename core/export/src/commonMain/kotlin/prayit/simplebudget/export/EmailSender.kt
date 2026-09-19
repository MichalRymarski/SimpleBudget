package prayit.simplebudget.export

expect suspend fun sendEmailWithAttachment(
    to: String,
    from: String,
    password: String,
    subject: String,
    attachmentName: String,
    attachmentBytes: ByteArray,
): Result<Unit>
