package prayit.simplebudget.core.data.security

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PasswordCryptoTest {

    @Test
    fun encryptDecrypt_roundTrip() = runTest {
        val plain = "abcd efgh ijkl mnop"
        val encrypted = PasswordCrypto.encrypt(plain)
        assertTrue(encrypted.isNotEmpty())
        assertNotEquals(plain, encrypted)
        assertEquals(plain, PasswordCrypto.decrypt(encrypted))
    }

    @Test
    fun encrypt_isRandomized() = runTest {
        val first = PasswordCrypto.encrypt("same-password")
        val second = PasswordCrypto.encrypt("same-password")
        assertNotEquals(first, second)
    }

    @Test
    fun empty_mapsToEmpty() = runTest {
        assertEquals("", PasswordCrypto.encrypt(""))
        assertEquals("", PasswordCrypto.decrypt(""))
    }
}
