package prayit.simplebudget.export

object PaymentNotificationParser {

    const val WALLET_PACKAGE = "com.google.android.apps.walletnfcrel"
    const val REVOLUT_PACKAGE = "com.revolut.revolut"
    const val GPAY_PACKAGE = "com.google.android.apps.nbu.paisa.user"

    val SUPPORTED_PACKAGES: Set<String> = setOf(WALLET_PACKAGE, GPAY_PACKAGE, REVOLUT_PACKAGE)

    data class ParsedPayment(
        val title: String,
        val merchant: String?,
        val amount: Double,
        val tag: String,
    )

    private val DENYLIST = listOf(
        "otp",
        "verification",
        "promo",
        "cashback",
        "reward",
        "advertisement",
    )

    private val PAYMENT_VERB =
        Regex("""(?i)\b(paid|sent|payment|purchase|zapłac|wysłan|transakcj)""")

    private val AMOUNT = Regex(
        """(?i)(?:(zł|pln|€|eur|\$|usd)\s*(\d[\d\s.,]*)|(\d[\d\s.,]*)\s*(zł|pln|€|eur|\$|usd))""",
    )

    private val AT_MERCHANT = Regex("""(?i)\bat\s+([^\n]+)""")

    private val TRAILERS = listOf(
        Regex("""\s{2,}.*$"""),
        Regex("""\s[•·|—–-]\s.*$"""),
        Regex("""(?i)\s+spent\s+.*$"""),
        Regex("""(?i)\s+with\s+.*$"""),
        Regex("""(?i)\s+using\s+.*$"""),
    )

    fun parse(packageName: String, title: String, text: String): ParsedPayment? {
        if (packageName !in SUPPORTED_PACKAGES) return null
        val combined = "$title $text"
        if (combined.isBlank()) return null
        val lower = combined.lowercase()
        if (DENYLIST.any { it in lower }) return null
        if (!PAYMENT_VERB.containsMatchIn(combined)) return null
        val amount = extractAmount(combined) ?: return null
        val merchant = extractMerchant(combined)
        val displayTitle = (merchant ?: title.ifBlank { text }).trim()
        if (displayTitle.isBlank()) return null
        return ParsedPayment(
            title = displayTitle,
            merchant = merchant,
            amount = amount,
            tag = inferTag("$displayTitle $combined"),
        )
    }

    internal fun extractAmount(input: String): Double? {
        for (match in AMOUNT.findAll(input)) {
            val raw =
                if (match.groupValues[1].isNotEmpty()) match.groupValues[2] else match.groupValues[3]
            normalizeAmount(raw)?.let { return it }
        }
        return null
    }

    internal fun normalizeAmount(raw: String): Double? {
        var s = raw.replace("\u00a0", " ").replace(" ", "").trim().trimEnd('.', ',')
        if (s.isEmpty() || s.none { it.isDigit() }) return null
        val lastDot = s.lastIndexOf('.')
        val lastComma = s.lastIndexOf(',')
        s = when {
            lastDot >= 0 && lastComma >= 0 ->
                if (lastComma > lastDot) s.replace(".", "").replace(',', '.') else s.replace(
                    ",",
                    ""
                )

            lastComma >= 0 -> s.replace(',', '.')
            else -> s
        }
        return s.toDoubleOrNull()?.takeIf { it > 0 }
    }

    internal fun extractMerchant(input: String): String? {
        val raw = AT_MERCHANT.find(input)?.groupValues?.get(1)?.trim() ?: return null
        var merchant = raw
        for (trailer in TRAILERS) merchant = trailer.replace(merchant, "")
        merchant = merchant.trim().trimEnd('.', ',', '…', ' ', '"', '\'')
        merchant = merchant.split(Regex("""\s+""")).take(4).joinToString(" ").trim()
        if (merchant.isBlank() || merchant.length > 64) return null
        if (merchant.none { it.isLetterOrDigit() }) return null
        return merchant
    }

    internal fun inferTag(input: String): String {
        val lower = input.lowercase()
        fun has(vararg keys: String) = keys.any { it in lower }
        return when {
            has(
                "biedronka", "lidl", "auchan", "carrefour", "kaufland", "tesco",
                "żabka", "zabka", "leclerc", "grocery", "groceries", "supermarket",
                "spożyw", "delikates", "piekarni", "bakery", "warzyw",
            ) -> "Groceries"

            has(
                "restaurant", "restaurac", "mcdonald", "kfc", "burger", "pizza",
                "sushi", "kebab", "cafe", "coffee", "starbucks", "bistro",
                "pyszne", "wolt", "glovo", "eating", "dining",
            ) -> "EatingOut"

            has(
                "steam", "google play", "apple", "media markt", "media expert",
                "x-kom", "morele", "komputronik", "neonet", "playstation", "xbox",
            ) -> "Technology"

            has(
                "rossmann", "hebe", "sephora", "douglas", "cosmetic", "kosmety", "beauty",
            ) -> "Cosmetics"

            has(
                "pharmacy", "apteka", "doctor", "lekarz", "clinic", "klinik",
                "hospital", "szpital", "dentist", "dentyst", "health",
            ) -> "Health"

            has(
                "bill", "electric", "energy", "tauron", "pge", "enea", "orange",
                "t-mobile", "netflix", "spotify", "rent", "czynsz", "internet",
                "prąd", "ubezpiecz", "abonament", "subscrip",
            ) -> "Bills"

            else -> "Misc"
        }
    }
}
