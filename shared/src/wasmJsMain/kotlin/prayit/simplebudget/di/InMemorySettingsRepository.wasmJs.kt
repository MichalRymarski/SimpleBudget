package prayit.simplebudget.di

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import prayit.simplebudget.core.domain.model.AppSettings
import prayit.simplebudget.core.domain.repository.SettingsRepository

// DataStore has no wasmJs target, so settings live in memory for web.
// Email fields stay editable in UI, but email export is unsupported (see WasmExportRepository).
@ContributesBinding(AppScope::class, binding = binding<SettingsRepository>())
@SingleIn(AppScope::class)
@Inject
class InMemorySettingsRepository : SettingsRepository {
    private val _settings = MutableStateFlow(AppSettings())

    override fun getSettings(): Flow<AppSettings> = _settings.asStateFlow()

    override suspend fun updateSettings(settings: AppSettings) {
        _settings.value = settings
    }
}
