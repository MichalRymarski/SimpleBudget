package prayit.simplebudget.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import prayit.simplebudget.core.data.security.PasswordCrypto
import prayit.simplebudget.core.domain.model.AppSettings
import prayit.simplebudget.core.domain.repository.SettingsRepository
import prayit.simplebudget.core.utils.Log
import prayit.simplebudget.di.AppScope

@ContributesBinding(AppScope::class)
@Inject
class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    private object Keys {
        val RECIPIENT_EMAIL = stringPreferencesKey("recipient_email")
        val SENDER_EMAIL = stringPreferencesKey("sender_email")
        val APP_PASSWORD = stringPreferencesKey("app_password")
        val AUTO_EXPORT_ENABLED = booleanPreferencesKey("auto_export_enabled")
        val AUTO_EXPORT_WITH_MANUAL = booleanPreferencesKey("auto_export_with_manual")
    }

    override fun getSettings(): Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            recipientEmail = prefs[Keys.RECIPIENT_EMAIL] ?: "",
            senderEmail = prefs[Keys.SENDER_EMAIL] ?: "",
            appPassword = decryptStored(prefs[Keys.APP_PASSWORD] ?: ""),
            autoExportEnabled = prefs[Keys.AUTO_EXPORT_ENABLED] ?: false,
            autoExportWithManual = prefs[Keys.AUTO_EXPORT_WITH_MANUAL] ?: false,
        )
    }

    override suspend fun updateSettings(settings: AppSettings) {
        val encryptedPassword = encryptForStorage(settings.appPassword)
        dataStore.edit { prefs ->
            prefs[Keys.RECIPIENT_EMAIL] = settings.recipientEmail
            prefs[Keys.SENDER_EMAIL] = settings.senderEmail
            prefs[Keys.APP_PASSWORD] = encryptedPassword
            prefs[Keys.AUTO_EXPORT_ENABLED] = settings.autoExportEnabled
            prefs[Keys.AUTO_EXPORT_WITH_MANUAL] = settings.autoExportWithManual
        }
    }

    companion object {
        /** Prefix marking a value encrypted with [PasswordCrypto]. Unprefixed = legacy plaintext. */
        const val ENCRYPTED_PREFIX = "v1:"
    }

    private suspend fun encryptForStorage(plain: String): String {
        if (plain.isEmpty()) return ""
        return ENCRYPTED_PREFIX + PasswordCrypto.encrypt(plain)
    }

    private suspend fun decryptStored(stored: String): String {
        if (stored.isEmpty() || !stored.startsWith(ENCRYPTED_PREFIX)) return stored
        return runCatching {
            PasswordCrypto.decrypt(stored.removePrefix(ENCRYPTED_PREFIX))
        }.getOrElse { error ->
            Log.e("SettingsRepository") { "Failed to decrypt app password: ${error.message}" }
            ""
        }
    }
}
