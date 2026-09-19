package prayit.simplebudget.core.data.security

import prayit.simplebudget.core.data.dataDirPath
import java.io.File
import java.security.SecureRandom

// Software fallback: AES-256-GCM via whyoleg, key in a file under the app data dir.
// Better than plaintext; not hardware-backed like Android Keystore.
actual object PasswordCrypto {
    actual suspend fun encrypt(plain: String): String {
        if (plain.isEmpty()) return ""
        return aesGcmEncrypt(getOrCreateKeyBytes(), plain)
    }

    actual suspend fun decrypt(encoded: String): String {
        if (encoded.isEmpty()) return ""
        return aesGcmDecrypt(getOrCreateKeyBytes(), encoded)
    }

    private fun getOrCreateKeyBytes(): ByteArray {
        val keyFile = File(dataDirPath(), "crypto/aes_gcm_key.bin")
        val existing = runCatching { keyFile.takeIf { it.isFile }?.readBytes() }.getOrNull()
        if (existing != null && existing.size == 32) return existing
        val fresh = ByteArray(32).also { SecureRandom().nextBytes(it) }
        runCatching {
            keyFile.parentFile?.mkdirs()
            keyFile.writeBytes(fresh)
            keyFile.setReadable(false, false)
            keyFile.setReadable(true, true)
            keyFile.setWritable(false, false)
            keyFile.setWritable(true, true)
        }
        return fresh
    }
}
