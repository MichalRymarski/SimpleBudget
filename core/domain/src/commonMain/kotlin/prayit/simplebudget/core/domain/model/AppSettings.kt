package prayit.simplebudget.core.domain.model

data class AppSettings(
    val recipientEmail: String = "",
    val senderEmail: String = "",
    val appPassword: String = "",
    val autoExportEnabled: Boolean = false,
    val autoExportWithManual: Boolean = false,
)
