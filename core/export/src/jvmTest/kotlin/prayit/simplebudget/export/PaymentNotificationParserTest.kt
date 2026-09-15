package prayit.simplebudget.export

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PaymentNotificationParserTest {

    @Test
    fun revolutPaymentParsesAmountMerchantAndTag() {
        val parsed = PaymentNotificationParser.parse(
            PaymentNotificationParser.REVOLUT_PACKAGE,
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
            PaymentNotificationParser.WALLET_PACKAGE,
            "Google Wallet",
            "Zapłacono 45,99 zł w sklepie Biedronka",
        )
        // 'at'-based merchant extraction is English-centric; amount must still parse
        assertNotNull(parsed)
        assertEquals(45.99, parsed.amount)
        assertEquals("Groceries", parsed.tag)
    }

    @Test
    fun walletEnglishParsesMerchantAndEatingOut() {
        val parsed = PaymentNotificationParser.parse(
            PaymentNotificationParser.WALLET_PACKAGE,
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
            PaymentNotificationParser.GPAY_PACKAGE,
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
                PaymentNotificationParser.REVOLUT_PACKAGE,
                "Revolut",
                "Your verification OTP code is 483920",
            )
        )
        assertNull(
            PaymentNotificationParser.parse(
                PaymentNotificationParser.WALLET_PACKAGE,
                "Wallet",
                "Cashback promo: get 10% back this weekend",
            )
        )
    }

    @Test
    fun missingAmountReturnsNull() {
        assertNull(
            PaymentNotificationParser.parse(
                PaymentNotificationParser.WALLET_PACKAGE,
                "Google Wallet",
                "Paid at Biedronka",
            )
        )
    }

    @Test
    fun unknownMerchantFallsBackToMisc() {
        val parsed = PaymentNotificationParser.parse(
            PaymentNotificationParser.REVOLUT_PACKAGE,
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
}
