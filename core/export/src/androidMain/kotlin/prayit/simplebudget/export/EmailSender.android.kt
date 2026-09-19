package prayit.simplebudget.export

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.Message
import javax.mail.Multipart
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

actual suspend fun sendEmailWithAttachment(
    to: String,
    from: String,
    password: String,
    subject: String,
    attachmentName: String,
    attachmentBytes: ByteArray,
): Result<Unit> = withContext(Dispatchers.IO) {
    runCatching {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
        }

        val session = Session.getInstance(props, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication() =
                javax.mail.PasswordAuthentication(from, password)
        })

        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(from))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            this.subject = subject

            val attachmentPart = MimeBodyPart().apply {
                dataHandler = DataHandler(FileDataSource(
                    java.io.File.createTempFile("export", ".xlsx").apply {
                        writeBytes(attachmentBytes)
                    }
                ))
                fileName = attachmentName
            }

            val multipart: Multipart = MimeMultipart().apply {
                addBodyPart(attachmentPart)
            }

            setContent(multipart)
        }

        Transport.send(message)
    }
}
