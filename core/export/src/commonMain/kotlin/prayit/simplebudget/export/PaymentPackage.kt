package prayit.simplebudget.export

enum class PaymentPackage(
    val packageName: String,
    val amountRegexFirst: Regex,
    val amountRegexLast: Regex,
    val merchantRegex: Regex? = null,
) {
    WALLET(
        packageName = "com.google.android.apps.walletnfcrel",
        amountRegexFirst = Regex("""(?i)(zł|pln|€|eur|\$|usd)\s*(\d[\d\s.,]*)"""),
        amountRegexLast = Regex("""(?i)(\d[\d\s.,]*)\s*(zł|pln|€|eur|\$|usd)"""),
    ),
    REVOLUT(
        packageName = "com.revolut.revolut",
        amountRegexFirst = Regex("""(?i)(zł|pln|€|eur|\$|usd)\s*(\d[\d\s.,]*)"""),
        amountRegexLast = Regex("""(?i)(\d[\d\s.,]*)\s*(zł|pln|€|eur|\$|usd)"""),
    ),
    GPAY(
        packageName = "com.google.android.apps.nbu.paisa.user",
        amountRegexFirst = Regex("""(?i)(zł|pln|€|eur|\$|usd)\s*(\d[\d\s.,]*)"""),
        amountRegexLast = Regex("""(?i)(\d[\d\s.,]*)\s*(zł|pln|€|eur|\$|usd)"""),
        merchantRegex = Regex("""(?i)\bto\s+(.+)"""),
    ),
    ING(
        packageName = "pl.ing.mojeing",
        amountRegexFirst = Regex("""(?i)(zł|pln|€|eur|\$|usd)\s*(\d[\d\s.,]*)"""),
        amountRegexLast = Regex("""(?i)(\d[\d\s.,]*)\s*(zł|pln|€|eur|\$|usd)"""),
    ),
    TEST(
        packageName = "prayit.simplebudget.androidApp.staging",
        amountRegexFirst = Regex("""(?i)(zł|pln|€|eur|\$|usd)\s*(\d[\d\s.,]*)"""),
        amountRegexLast = Regex("""(?i)(\d[\d\s.,]*)\s*(zł|pln|€|eur|\$|usd)"""),
    );

    companion object {
        private val BY_NAME = entries.associateBy { it.packageName }
        val SUPPORTED_PACKAGES: Set<String> = BY_NAME.keys

        fun fromPackage(packageName: String): PaymentPackage? = BY_NAME[packageName]
    }
}
