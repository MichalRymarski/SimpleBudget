package prayit.simplebudget.core.domain.repository

import kotlinx.coroutines.flow.Flow
import prayit.simplebudget.core.domain.model.AppSettings

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun updateSettings(settings: AppSettings)
}
