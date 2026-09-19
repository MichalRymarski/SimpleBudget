package prayit.simplebudget.core.data.security

// TODO: Keychain-backed AES-GCM (e.g. whyoleg CryptoKit/OpenSSL provider + Keychain-stored key).
// Passthrough stub for now — password stays plaintext on iOS.
actual object PasswordCrypto {
    actual suspend fun encrypt(plain: String): String = plain
    actual suspend fun decrypt(encoded: String): String = encoded
}
