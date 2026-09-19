package prayit.simplebudget.export

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PaymentNotificationParserTest {

    @Test
    fun revolutPaymentParsesAmountMerchantAndTag() {
        val parsed = PaymentNotificationParser.parse(
            PaymentPackage.REVOLUT,
            "Revolut",
            "🎬 Paid €9.99 at Steamgames.com 4259522 Spent today: €9.99",
        )
        assertNotNull(parsed)
        assertEquals(9.99, parsed.amount)
        assertEquals("Technology", parsed.tag)
    }

    @Test
    fun walletPlnParsesCommaDecimalAndGroceries() {
        val parsed = PaymentNotificationParser.parse(
            PaymentPackage.WALLET,
            "Google Wallet",
            "Zapłacono 45,99 zł w sklepie Biedronka",
        )
        assertNotNull(parsed)
        assertEquals(45.99, parsed.amount)
        assertEquals("Groceries", parsed.tag)
    }

    @Test
    fun walletEnglishParsesMerchantAndEatingOut() {
        val parsed = PaymentNotificationParser.parse(
            PaymentPackage.WALLET,
            "Google Wallet",
            "Purchase $12.50 at Starbucks Downtown",
        )
        assertNotNull(parsed)
        assertEquals(12.5, parsed.amount)
        assertEquals("EatingOut", parsed.tag)
    }

    @Test
    fun gpayPackageIsSupported() {
        val parsed = PaymentNotificationParser.parse(
            PaymentPackage.GPAY,
            "Google Pay",
            "Sent $15.00 to John",
        )
        assertNotNull(parsed)
        assertEquals(15.0, parsed.amount)
    }

    @Test
    fun thousandSeparatorWithSpaceAndComma() {
        assertEquals(1234.56, PaymentNotificationParser.normalizeAmount("1 234,56"))
        assertEquals(1234.56, PaymentNotificationParser.normalizeAmount("1,234.56"))
        assertEquals(9.99, PaymentNotificationParser.normalizeAmount("9.99"))
    }

    @Test
    fun unsupportedPackageReturnsNull() {
        assertNull(PaymentNotificationParser.parse("com.random.app", "Hi", "Paid $5.00 at Store"))
    }

    @Test
    fun otpAndPromoAreRejected() {
        assertNull(
            PaymentNotificationParser.parse(
                PaymentPackage.REVOLUT,
                "Revolut",
                "Your verification OTP code is 483920",
            )
        )
        assertNull(
            PaymentNotificationParser.parse(
                PaymentPackage.WALLET,
                "Wallet",
                "Cashback promo: get 10% back this weekend",
            )
        )
    }

    @Test
    fun missingAmountReturnsNull() {
        assertNull(
            PaymentNotificationParser.parse(
                PaymentPackage.WALLET,
                "Google Wallet",
                "Paid at Biedronka",
            )
        )
    }

    @Test
    fun unknownMerchantFallsBackToMisc() {
        val parsed = PaymentNotificationParser.parse(
            PaymentPackage.REVOLUT,
            "Revolut",
            "Sent $20.00 to John",
        )
        assertNotNull(parsed)
        assertEquals(20.0, parsed.amount)
        assertEquals("Misc", parsed.tag)
    }

    @Test
    fun merchantTrailerIsStripped() {
        assertEquals(
            "Steamgames.com 4259522",
            PaymentNotificationParser.extractMerchant("Paid €9.99 at Steamgames.com 4259522 Spent today: €9.99"),
        )
    }

    @Test
    fun revolutYouSpentFormatParsesCorrectly() {
        val parsed = PaymentNotificationParser.parse(
            PaymentPackage.REVOLUT,
            "Sonata Sp. Z O.o.",
            "\uD83E\uDEF6 You spent PLN74.99\nPLN balance: PLN474.84",
        )
        assertNotNull(parsed)
        assertEquals(74.99, parsed.amount)
    }

    @Test
    fun walletNfcTapParsesAmountWithoutVerb() {
        val parsed = PaymentNotificationParser.parse(
            PaymentPackage.WALLET,
            "SONATA Sp. z o.o.",
            "PLN74.99 with Revolut Mastercard \u2022\u20221912",
        )
        assertNotNull(parsed)
        assertEquals(74.99, parsed.amount)
    }

    @Test
    fun walletNfcWithNrInTitleDoesNotStealAmount() {
        val parsed = PaymentNotificationParser.parse(
            PaymentPackage.WALLET,
            "STOKROTKA NR 0652",
            "PLN69.45 with Revolut Mastercard \u2022\u20221912",
        )
        assertNotNull(parsed)
        assertEquals(69.45, parsed.amount)
    }

    @Test
    fun ingBlikNotificationParsesAmount() {
        val parsed = PaymentNotificationParser.parse(
            PaymentPackage.ING,
            "Moje ING. Twój Asystent",
            "5,00 PLN mniej na Twoim koncie - KONTO Mobi 18-26 - płatność BLIK",
        )
        assertNotNull(parsed)
        assertEquals(5.0, parsed.amount)
    }
}
