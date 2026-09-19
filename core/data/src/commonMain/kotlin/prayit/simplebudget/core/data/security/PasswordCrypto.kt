package prayit.simplebudget.core.data.security

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.AES
import kotlin.io.encoding.Base64

/**
 * Platform password encryption.
 *
 * - androidMain: real AES-256-GCM with a non-extractable key in Android Keystore.
 * - jvmMain: real AES-256-GCM via whyoleg, key in a file under the app data dir (software fallback).
 * - iosMain: stub (passthrough) until a Keychain-backed implementation lands.
 *
 * Empty input always maps to empty output (no ciphertext for blank passwords).
 */
expect object PasswordCrypto {
    suspend fun encrypt(plain: String): String
    suspend fun decrypt(encoded: String): String
}

/** Shared AES-GCM helpers for platforms that hold the raw key bytes (JVM, later iOS). */
internal suspend fun aesGcmEncrypt(keyBytes: ByteArray, plain: String): String {
    val aesGcm = CryptographyProvider.Default.get(AES.GCM)
    val key = aesGcm.keyDecoder().decodeFromByteArray(AES.Key.Format.RAW, keyBytes)
    val ciphertext = key.cipher().encrypt(plain.encodeToByteArray())
    return Base64.encode(ciphertext)
}

internal suspend fun aesGcmDecrypt(keyBytes: ByteArray, encoded: String): String {
    val aesGcm = CryptographyProvider.Default.get(AES.GCM)
    val key = aesGcm.keyDecoder().decodeFromByteArray(AES.Key.Format.RAW, keyBytes)
    return key.cipher().decrypt(Base64.decode(encoded)).decodeToString()
}
