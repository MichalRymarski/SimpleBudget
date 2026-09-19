package prayit.simplebudget.feature.settings.state

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import prayit.simplebudget.core.components.navigation.SnackbarMessage
import prayit.simplebudget.core.components.navigation.SnackbarType
import prayit.simplebudget.core.domain.model.AppSettings
import prayit.simplebudget.core.domain.repository.ExportRepository
import prayit.simplebudget.core.domain.repository.SettingsRepository
import prayit.simplebudget.core.utils.Log
import prayit.simplebudget.di.AppScope

@SingleIn(AppScope::class)
@Inject
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val exportRepository: ExportRepository,
    coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Default,
) {
    private val scope = CoroutineScope(coroutineContext)
    private val _isSaving = MutableStateFlow(false)
    private val _snackbarMessages = MutableSharedFlow<SnackbarMessage>()

    val snackbarMessages = _snackbarMessages.asSharedFlow()

    val state: StateFlow<SettingsState> = combine(
        settingsRepository.getSettings(),
        _isSaving,
    ) { settings, isSaving ->
        SettingsState.Content(
            settings = settings,
            isSaving = isSaving,
        )
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), SettingsState.Loading)

    fun onRecipientEmailChanged(email: String) {
        updateSetting { it.copy(recipientEmail = email) }
    }

    fun onSenderEmailChanged(email: String) {
        updateSetting { it.copy(senderEmail = email) }
    }

    fun onAppPasswordChanged(password: String) {
        updateSetting { it.copy(appPassword = password) }
    }

    fun onAutoExportToggle(enabled: Boolean) {
        updateSetting { it.copy(autoExportEnabled = enabled) }
    }

    fun onAutoExportWithManualToggle(enabled: Boolean) {
        updateSetting { it.copy(autoExportWithManual = enabled) }
    }

    fun onSendTestEmail() {
        Log.d("SettingsViewModel") { "onSendTestEmail called" }
        scope.launch {
            exportRepository.sendTestEmail()
                .onSuccess {
                    Log.d("SettingsViewModel") { "Test email sent successfully" }
                    _snackbarMessages.emit(SnackbarMessage("Test email sent", SnackbarType.SUCCESS))
                }
                .onFailure { error ->
                    Log.e("SettingsViewModel") { "Failed to send test email: ${error.message}" }
                    _snackbarMessages.emit(SnackbarMessage(error.message ?: "Failed to send test email", SnackbarType.ERROR))
                }
        }
    }

    private fun updateSetting(transform: (AppSettings) -> AppSettings) {
        val current = (state.value as? SettingsState.Content)?.settings ?: return
        val updated = transform(current)
        scope.launch {
            _isSaving.update { true }
            settingsRepository.updateSettings(updated)
            _isSaving.update { false }
        }
    }
}
